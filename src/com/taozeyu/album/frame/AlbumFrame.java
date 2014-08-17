package com.taozeyu.album.frame;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.taozeyu.album.AlbumManager;
import com.taozeyu.album.dao.ImageDao;
import com.taozeyu.orange.ImageView;
import com.taozeyu.orange.ImageView.ImageChangeListeners;
import com.taozeyu.orange.ImageWindow;

public class AlbumFrame extends JFrame {

	private static final long serialVersionUID = 8647737239009706776L;
	
	private ImageView<ImageSource> imageView = null;
	private int imagesNum;
	
	public AlbumFrame() {
		this.setTitle("橘子相册 - 图片预览");
		this.setBackground(Color.BLACK);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int rsCode = JOptionPane.showConfirmDialog(AlbumFrame.this, "确定要退出橘子相册吗？", "退出确认", JOptionPane.OK_CANCEL_OPTION);
				if(rsCode == JOptionPane.OK_OPTION) {
					AlbumManager.instance().dispose();
				}
			}
		});
	}
	
	public void resetImageList(List<ImageDao> imageList) {
		
		if(imageView != null) {
			remove(imageView);
			imageView.close();
		}
		ArrayList<ImageSource> sourceList = new ArrayList<ImageSource>(imageList.size());
		imagesNum = imageList.size();
		for(int i=0; i<imageList.size(); ++i) {
			sourceList.add(new ImageSource(imageList.get(i), i));
		}
		ImageWindow<ImageSource> imageWindows = new ImageWindow<>(sourceList);
		imageView = new ImageView<ImageSource>(imageWindows);
		initImageView();
		add(imageView);
	}
	
	private void initImageView() {
		imageView.setImageChangeListeners(new ImageChangeListeners<ImageSource>() {
			@Override
			public void onImageChange(Image image, ImageSource imageSource) {
				setTitle("橘子相册 - (" + imageSource.index + "/" + imagesNum + ") " + imageSource.filePath );
			}
		});
	}
	
	public void close() {
		if(imageView != null) {
			imageView.close();
		}
	}
	
	public static class ImageSource implements ImageWindow.ImageSource {
		
		private final String filePath;
		private final int index;
		
		private ImageSource(ImageDao imageDao, int index) {
			this.filePath = imageDao.getFilePath();
			this.index = index;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			try{
				return new BufferedInputStream(new FileInputStream(new File(filePath)));
			}catch(FileNotFoundException e) {
				System.err.println(e);
				return null;
			}
		}
	}
}
