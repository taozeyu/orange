package com.taozeyu.orange;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLongArray;

import javax.imageio.ImageIO;

public class ImageWindow<S extends ImageWindow.ImageSource> {

	final static Image FailImage;
	
	static {
		InputStream inputStream = ImageWindow.class.getResourceAsStream("load_error.png");
		inputStream = new BufferedInputStream(inputStream); 
		try {
			FailImage = ImageIO.read(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final static long DefaultBufferedLimitSize = 1024 * 1024 * 128; //128MB
	private final static long DefaultImageLoadTimeOut = 0L; // ban time out
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
	private final long imageLoadTimeOut;
	
	private int currIndex = -1;
	
	private final Object getterLock = new Object();
	
	private volatile int waitForIndex = 0;
	private ImageGetter<S> waitForGetter = null;
	
	private final Object windowLock = new Object();
	
	private final ArrayList<Task> windowTaskList;
	private int windowStartIndex = -1;
	
	private LockQueue<Task> taskQueue = new LockQueue<Task>();
	
	private static final ThreadLocal<Integer> threadIndex = new ThreadLocal<>();
	private AtomicLongArray threadsTaskStartTime;
	private Thread[] threads;
	
	public ImageWindow(List<S> sourceList) {
		this(sourceList, DefaultWindowLength, DefaultThreadNum);
	}
	
	public ImageWindow(List<S> sourceList, int windowLength, int threadNum) {
		this(sourceList, windowLength, threadNum, DefaultBufferedLimitSize, DefaultImageLoadTimeOut);
	}
	
	public ImageWindow(List<S> sourceList, int windowLength, int threadNum, long bufferedLimitSize, long imageLoadTimeOut) {
		this.sourceList = sourceList;
		this.windowLength = windowLength;
		this.imageLoadTimeOut = imageLoadTimeOut;
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
			waitForGetter = null;
		}
	}
	
	private void loadedImageFail(int targetIndex) {
		try{
			throw new Exception();
		} catch(Exception e) {
			e.printStackTrace();
		}
		loadedImage(FailImage, targetIndex);
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
					throw new InterruptedIOException("unnecessary image");
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
		
		Task task;
		try {
			task = taskQueue.get();
		} catch (InterruptedException e1) {
			return;
		}
		if(isIndexOutOfSourceList(task.targetIndex)) {
			return;
		}
		S source = sourceList.get(task.targetIndex);
		InputStream is = null;

		Image image = null;
		
		try{
			is = source.getInputStream();
			
			if(is != null) {
				is = new InterruptInputStream(is, task.targetIndex);
				setStartTask();
				image = ImageIO.read(is);
			}
			
		} catch(RuntimeException e) {
			
			loadedImageFail(task.targetIndex);
			throw e;
			
		} catch(IOException e) {
			
			loadedImageFail(task.targetIndex);
			throw e;
			
		} finally {
			
			try{
				if(image == null) {
					image = FailImage;
				}
				synchronized (windowLock) {
					insertImageToWindow(image, toWindowIndex(task.targetIndex));
				}
				loadedImage(image, task.targetIndex);
				
			} finally {
				setWaitingTask();
				if(is != null) {
					is.close();
				}
			}
		}
	}
	
	private void insertImageToWindow(Image image, int index) {
		
		if(isIndexOutOfWindow(index)) {
			return;
		}
		windowTaskList.get(index).image = image;
	}
	
	private void initTreads(int threadNum) {
		
		threads = new Thread[threadNum + (imageLoadTimeOut == 0L?0:1)];
		threadsTaskStartTime = new AtomicLongArray(threadNum);
		
		for(int i=0; i<threadNum; ++i) {
			(threads[i] = createNewThread(i)).start();
		}
		if(imageLoadTimeOut != 0L) {
			threads[threadNum] = initGuardThread();
			threads[threadNum].start();
		}
	}
	
	private Thread createNewThread(final int index) {
		return new Thread(new Runnable() {
			@Override
			public void run() {
				threadIndex.set(index);
				while(threadContinue) {
					try {
						backThreadRunLoop();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void setWaitingTask() {
		int index = threadIndex.get();
		threadsTaskStartTime.set(index, 0L);
	}
	
	private void setStartTask() {
		int index = threadIndex.get();
		threadsTaskStartTime.set(index, System.currentTimeMillis());
	}
	
	private Thread initGuardThread() {
		return new Thread(new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				while(threadContinue) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) { }
					
					long curr = System.currentTimeMillis();
					
					for(int i=0; i<threadsTaskStartTime.length(); ++i) {
						long taskTime = threadsTaskStartTime.get(i);
						if(taskTime != 0L && taskTime - curr >= imageLoadTimeOut) {
							threadsTaskStartTime.set(i, 0L);
							threads[i].stop();
							threads[i] = createNewThread(i);
						}
					}
				}
			}
		});
	}
	
	public void close() {
		threadContinue = false;
		for(Thread thread:threads) {
			thread.interrupt();
		}
		for(Thread thread:threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// continue next;
			}
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
