package com.taozeyu.orange;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JPanel;

public class ImageView<S extends ImageWindow.ImageSource> extends JPanel {

	private static final long serialVersionUID = -6846369311741815022L;
	
	private static Color BackgroundColor = Color.BLACK;
	
	private final ImageWindow<S> imageWindows;
	
	private Image image = null;
	public static final int LEFT = 0;
	public static final int BUTTON = 1;
	public static final int RIGHT = 2;
	public static final int TOP = 3;
	
	private HashMap<Component, Integer> childrenLocation = new HashMap<Component, Integer>();
	
	public ImageView(ImageWindow<S> imageWindows) {
		this.imageWindows = imageWindows;
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		this.addMouseWheelListener(mouseAdapter);
		this.addComponentListener(componentAdapter);
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	public void close() {
		this.imageWindows.close();
	}
	
	//相对于图片的中点，在不放缩图片的情况下，相机正中央对着的坐标。
	private int viewX, viewY;
	//相机放系数
	private float scale = 1.0f;
	
	public void setImage(Image image) {
		this.image = image;
	}
	
	public void resetCamera() {
		if(image != null) {
			viewX = image.getWidth(null) / 2;
			viewY = image.getHeight(null) / 2;
			scale = 1.0f;
		}
	}
	
	public void resetCameraButKeepScale() {
		if(image != null) {
			viewX = image.getWidth(null) / 2;
			viewY = image.getHeight(null) / 2;
		}
	}
	
	public void showAllImage() {
		if(image != null) {
			viewX = image.getWidth(null) / 2;
			viewY = image.getHeight(null) / 2;
			
			float scaleX = (float)getWidth() / (float) image.getWidth(null);
			float scaleY = (float)getHeight() / (float) image.getHeight(null);
			
			setScale(scaleX > scaleY ? scaleY : scaleX);
		}
	}
	
	public void addChildView(Component child, int location) {
		childrenLocation.put(child, location);
	}
	
	@Override
	public void paint(Graphics g) {
		setBackgroundColor(g);
		if (image != null) {
			drawImage(g);
		}
		super.paintChildren(g);
	}
	
	private void setBackgroundColor(Graphics g) {
		g.setColor(BackgroundColor);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
	}
	
	private void setScale(float value) {
		if(value >= 10.0f || value <= 0.1f) {
			return;
		}
		scale = value;
	}
	
	private long loadImageMark = 0L;
	
	private boolean isWaitingNextImage = false;
	private boolean isWaitinPrevImage = false;
	
	private void releaseWaitingLock() {
		isWaitingNextImage = false;
		isWaitinPrevImage = false;
	}
	
	private void nextImage() {

		if(isWaitingNextImage) {
			return;
		}
		loadImageMark++;
		
		final long mark = loadImageMark;
		
		isWaitingNextImage = true;
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		ImageWindow.ImageGetter<S> getter = new ImageWindow.ImageGetter<S>() {
			@Override
			public void onGetImage(final Image image, final S imageSource) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if(loadImageMark != mark) {
							return;
						}
						releaseWaitingLock();
						setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						setImage(image);
						resetCameraButKeepScale();
						repaint();
					}
				});
			}
		};
		
		boolean hasNextImage = imageWindows.getNextImage(getter);
		
		if(!hasNextImage) {
			releaseWaitingLock();
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		}
	}
	
	private void prevImage() {

		if(isWaitinPrevImage) {
			return;
		}
		loadImageMark++;
		
		final long mark = loadImageMark;
		
		isWaitinPrevImage = true;
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		
		ImageWindow.ImageGetter<S> getter = new ImageWindow.ImageGetter<S>() {
			@Override
			public void onGetImage(final Image image, final S imageSource) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if(mark != loadImageMark) {
							return;
						}
						releaseWaitingLock();
						setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						setImage(image);
						resetCameraButKeepScale();
						repaint();
					}
				});
			}
		};
		
		boolean hasPrevImage = imageWindows.getPrevImage(getter);
		
		if(!hasPrevImage) {
			releaseWaitingLock();
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		}
	}
	
	private Point getLocationOnScreen(Component child) {
		
		int width = child.getWidth();
		int height = child.getHeight();
		
		int x=0, y=0;
		
		if(width > getWidth() || height > getHeight()) {
			return null; //有超出屏幕的子原件，一律不显示：
		}
		
		switch(childrenLocation.get(child)) {
		case LEFT:
			x = 0;
			y = (getHeight() - height) / 2;
			break;
			
		case RIGHT:
			x = getWidth() - width;
			y = (getHeight() - height) / 2;
			break;
			
		case TOP:
			x = (getWidth() - width) / 2;
			y = 0;
			break;
			
		case BUTTON:
			x = (getWidth() - width) / 2;
			y = getHeight() - height;
			break;
			
		default:
			return null;
		}
		
		return new Point(x, y);
	}
	
	private final ComponentAdapter componentAdapter = new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e) {
			
			for(Component child:childrenLocation.keySet()) {
				Point location = getLocationOnScreen(child);
				child.setLocation(location.x, location.y);
			}
		}
	};
	
	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		
		int startButton;
		
		int startViewX, startViewY;
		int startMouseX, startMouseY;
		float startScale;
		
		@Override
		public void mouseDragged(MouseEvent e) {
			
			int mouseX = e.getX();
			int mouseY = e.getY();
			
			if(startButton == MouseEvent.BUTTON1) {
				viewX = startViewX + (int)((float)(startMouseX - mouseX) / scale);
				viewY = startViewY + (int)((float)(startMouseY - mouseY) / scale);
				
			} else if(startButton == MouseEvent.BUTTON3) {
				
				float increase = 0.0f;
				increase += (float)(- mouseY + startMouseY)/(float)getWidth();
				increase += (float)(- mouseX + startMouseX)/(float)getWidth();
				increase = increase * 0.7f + 1.0f;
				
				setScale(startScale * increase);
				
			} else {
				return;
			}
			repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
			startButton = e.getButton();
			
			startViewX = viewX;
			startViewY = viewY;
			
			startMouseX = e.getX();
			startMouseY = e.getY();
			
			startScale = scale;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			
			if(e.getClickCount() != 2) {
				return;
			}
			if(e.getButton() == MouseEvent.BUTTON1) {
				resetCamera();
			} else if(e.getButton() == MouseEvent.BUTTON3) {
				showAllImage();
			}
			repaint();
		}
		
		private HashSet<Component> showChildren = new HashSet<Component>();
		
		@Override
		public void mouseMoved(MouseEvent e) {
			
			for(Component child:childrenLocation.keySet()) {
				Point location = getLocationOnScreen(child);
				Rectangle rect = new Rectangle(
						location.x, location.y, child.getWidth(), child.getHeight()
				);
				if(rect.contains(e.getX(), e.getY())) {
					if(!showChildren.contains(child)) {
						showChildren.add(child);
						child.setLocation(location.x, location.y);
						ImageView.this.add(child);
						repaint();
					}
				} else {
					if(showChildren.contains(child)) {
						showChildren.remove(child);
						ImageView.this.remove(child);
						repaint();
					}
				}
			}
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(e.getWheelRotation() > 0) {
				nextImage();
			} else {
				prevImage();
			}
		}
	};
	
	private void drawImage(Graphics g) {
		
		//相机投射到图片上的尺寸（尺度以图片为准）
		int width = (int) (((float) this.getWidth()) / this.scale);
		int height = (int) (((float) this.getHeight()) / this.scale);
		
		//裁剪区域的左上角，相对于图片左上角的坐标（尺度以图片为准）
		int imageX0 = this.viewX - width / 2;
		int imageY0 = this.viewY - height / 2;
		
		if(imageX0 >= image.getWidth(null) || imageY0 >= image.getHeight(null)) {
			return;
		}
		
		//裁剪区域的右下角，相对于图片左上角的坐标（尺度以图片为准）
		int imageX1 = imageX0 + width;
		int imageY1 = imageY0 + height;
		
		if (imageX1 < 0 || imageY1 < 0) {
			return;
		}
		
		//绘制原点（尺度以JPanel为准）
		int drawX = 0, drawY = 0;
		
		if(imageX0 < 0) {
			drawX = (int) (((float) -imageX0) * this.scale);
			imageX0 = 0;
		}
		
		if(imageY0 < 0) {
			drawY = (int) (((float) -imageY0) * this.scale);
			imageY0 = 0;
		}

		//绘制尺度（尺度以JPanel为准）
		int drawWidth = this.getWidth() - drawX;
		int drawHeight = this.getHeight() - drawY;
		
		if (imageX1 > image.getWidth(null)) {
			drawWidth -= (int)(((float)(imageX1 - image.getWidth(null))) * this.scale);
			imageX1 = image.getWidth(null);
		}
		
		if (imageY1 > image.getHeight(null)) {
			drawHeight -= (int)(((float)(imageY1 - image.getHeight(null))) * this.scale);
			imageY1 = image.getHeight(null);
		}
		
		g.drawImage(
				image,
				drawX, drawY, drawX + drawWidth, drawY + drawHeight,
				imageX0, imageY0, imageX1, imageY1,
				null
		);
	}
	
}
