package com.taozeyu.orange;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageWindow<S extends ImageWindow.ImageSource> {

	private final static long DefaultBufferedLimitSize = 1024 * 1024 * 128; //128MB
	private final static int DefaultWindowLength = 9;
	private final static int DefaultThreadNum = 1;
	
	public interface ImageSource {
		InputStream getInputStream() throws IOException;
	}
	
	public interface ImageGetter<S> {
		void onGetImage(Image image, S imageSource);
	}
	
	private static class Task {
		private Image image = null;
		private int targetIndex;
	}
	private volatile boolean threadContinue = true;
	
	private boolean lastTryGetNext = false;
	
	private final List<S> sourceList;
	private final ImageBufferedList imageBufferedList;
	private final int windowLength;
	
	private int currIndex = -1;
	
	private final Object getterLock = new Object();
	
	private volatile int waitForIndex = 0;
	private ImageGetter<S> waitForGetter = null;
	
	private final Object windowLock = new Object();
	
	private final ArrayList<Task> windowTaskList;
	private int windowStartIndex = -1;
	
	private LockQueue<Task> taskQueue = new LockQueue<Task>();
	
	public ImageWindow(List<S> sourceList) {
		this(sourceList, DefaultWindowLength, DefaultThreadNum);
	}
	
	public ImageWindow(List<S> sourceList, int windowLength, int threadNum) {
		this(sourceList, windowLength, threadNum, DefaultBufferedLimitSize);
	}
	
	public ImageWindow(List<S> sourceList, int windowLength, int threadNum, long bufferedLimitSize) {
		this.sourceList = sourceList;
		this.windowLength = windowLength;
		this.windowTaskList = new ArrayList<Task>(windowLength);
		
		this.imageBufferedList = new ImageBufferedList(sourceList.size(), bufferedLimitSize);
		
		for(int i=0; i<windowLength; ++i) {
			this.windowTaskList.add(null);
		}
		this.initTreads(threadNum);
		this.moveWindow(0);
	}
	
	public boolean getNextImage(ImageGetter<S> getter) {
		if(getTargetImage(getter, this.currIndex + 1)) {
			lastTryGetNext = true;
			currIndex++;
			return true;
		}
		return false;
	}
	
	public boolean getPrevImage(ImageGetter<S> getter) {
		if(getTargetImage(getter, this.currIndex - 1)) {
			lastTryGetNext = false;
			currIndex--;
			return true;
		}
		return false;
	}
	
	private boolean getTargetImage(ImageGetter<S> getter, int targetIndex) {
		
		if(isIndexOutOfSourceList(targetIndex)) {
			return false;
		}
		
		if(targetIndex != this.currIndex) {
			int toWindowStartIndex = targetIndex - windowLength / 2;
			if(toWindowStartIndex < 0) {
				toWindowStartIndex = 0;
			}else if(toWindowStartIndex + windowLength >= sourceList.size()) {
				toWindowStartIndex = sourceList.size() - windowLength;
			}
			moveWindow(targetIndex - windowLength / 2);
		}
		int windowIndex = toWindowIndex(targetIndex);
		Image image = imageBufferedList.getImage(targetIndex);
		
		if(image == null) {
			image = getImageFromWindow(windowIndex);
			if(image != null) {
				imageBufferedList.setImage(targetIndex, image);
			}
		}
		
		if(image != null) {
			getter.onGetImage(image, sourceList.get(targetIndex));
			return true;
		}
		
		synchronized (getterLock) {
			waitForGetter = getter;
			waitForIndex = targetIndex;
		}
		
		return true;
	}
	
	private Image getImageFromWindow(int index) {
		synchronized (windowLock) {
			Task task = windowTaskList.get(index);
			if(task == null) {
				return null;
			}
			return task.image;
		}
	}
	
	private void loadedImage(Image image, int targetIndex) {
		
		synchronized (getterLock) {
			
			if(waitForGetter == null || waitForIndex != targetIndex) {
				return;
			}
			imageBufferedList.setImage(targetIndex, image);
			waitForGetter.onGetImage(image, sourceList.get(targetIndex));
			targetIndex = 0;
			waitForGetter = null;
		}
	}
	
	private void moveWindow(int index) {
		
		if(isIndexOutOfSourceList(index)) {
			return;
		}
		
		if (index == windowStartIndex) {
			return;
		}

		synchronized (windowLock) {
			
			if (index < windowStartIndex) {

				int clearLength = windowLength;
				if (index + windowLength > windowStartIndex) {
					
					int len = index + windowLength - windowStartIndex;
					Task[] tasks = new Task[len];
					for (int i = 0; i < len; ++i) {
						tasks[i] = windowTaskList.get(i);
					}
					for (int i = 0; i < len; ++i) {
						windowTaskList.set(windowLength - len + i, tasks[i]);
					}
					clearLength -= len;
				}
				for (int i = 0; i < clearLength; ++i) {
					windowTaskList.set(i, null);
				}
			} else if (index > windowStartIndex) {

				int startIndex = 0;
				if (windowStartIndex + windowLength > index) {
					
					int len = windowStartIndex + windowLength - index;
					Task[] tasks = new Task[len];
					for (int i = 0; i < len; ++i) {
						tasks[i] = windowTaskList.get(windowLength - len + i);
					}
					for (int i = 0; i < len; ++i) {
						windowTaskList.set(i, tasks[i]);
					}
					startIndex = len;
				}
				for (int i = startIndex; i < windowLength; ++i) {
					windowTaskList.set(i, null);
				}
			}
			windowStartIndex = index;
			
			fillTask();
		}
	}
	
	private void fillTask() {

		int startIndex = toWindowIndex(currIndex);
		int len = Math.max(startIndex + 1, windowLength - startIndex);
		
		for(int i=0; i<len; ++i) {
			
			if(lastTryGetNext) {
				tryCreateNewTaskByWindowIndex(startIndex + i, currIndex + i);
				tryCreateNewTaskByWindowIndex(startIndex - i, currIndex - i);
			} else {
				tryCreateNewTaskByWindowIndex(startIndex - i, currIndex - i);
				tryCreateNewTaskByWindowIndex(startIndex + i, currIndex + i);
			}
		}
	}
	
	private void tryCreateNewTaskByWindowIndex(int index, int targetIndex) {
		
		if(isIndexOutOfWindow(index)) {
			return;
		}
		if(windowTaskList.get(index) == null) {
			Task task = createNewTask(toSourceIndex(index));
			task.targetIndex = targetIndex;
			windowTaskList.set(index, task);
		}
	}
	
	private Task createNewTask(int targetIndex) {
		
		Image bufferedImage = imageBufferedList.getImage(targetIndex);
		
		Task task = new Task();
		task.targetIndex = targetIndex;
		task.image = bufferedImage;
		
		if(bufferedImage == null) {
			taskQueue.add(task);
		}
		return task;
	}
	
	private boolean isIndexHitWindow(int index) {
		synchronized (windowLock) {
			int widx = toWindowIndex(index);
			return !isIndexOutOfWindow(widx);
		}
	}
	
	private class InterruptInputStream extends InputStream{
		
		private final InputStream inputStream;
		private final int targetIndex;
		
		private int count = 0;
		private static final int CheckEach = 1024 * 256; // 0.25KB
		
		private InterruptInputStream(InputStream inputStream, int targetIndex) {
			this.inputStream = inputStream;
			this.targetIndex = targetIndex;
		}

		@Override
		public int read() throws IOException {
			if(count++ >= CheckEach) {
				count = 0;
				if(!isIndexHitWindow(targetIndex)) {
					throw new InterruptedIOException();
				}
			}
			return inputStream.read();
		}

		@Override
		public void close() throws IOException {
			inputStream.close();
		}
	}
	
	private void backThreadRunLoop() throws IOException {
		
		Task task = taskQueue.get();
		if(isIndexOutOfSourceList(task.targetIndex)) {
			return;
		}
		S source = sourceList.get(task.targetIndex);
		
		InputStream is = new InterruptInputStream(source.getInputStream(), task.targetIndex);
		BufferedInputStream bis = new BufferedInputStream(is);
		
		try{
			
			Image image = ImageIO.read(bis);
			
			synchronized (windowLock) {
				insertImageToWindow(image, toWindowIndex(task.targetIndex));
			}
			loadedImage(image, task.targetIndex);
			
		} catch(InterruptedIOException e) {
			
			// do nothing!
			
		} finally {
			bis.close();
		}
	}
	
	private void insertImageToWindow(Image image, int index) {
		
		if(isIndexOutOfWindow(index)) {
			return;
		}
		windowTaskList.get(index).image = image;
	}
	
	private void initTreads(int threadNum) {
		for(int i=0; i<threadNum; ++i) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(threadContinue) {
						try {
							backThreadRunLoop();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}
	
	private boolean isIndexOutOfSourceList(int index) {
		return index < 0 || index >= sourceList.size();
	}
	
	private boolean isIndexOutOfWindow(int index) {
		return index <0 || index >= windowLength;
	}
	
	private int toSourceIndex(int index) {
		return this.windowStartIndex + index;
	}
	
	private int toWindowIndex(int index) {
		return index - this.windowStartIndex;
	}
}
