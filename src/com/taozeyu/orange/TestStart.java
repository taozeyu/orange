package com.taozeyu.orange;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.swing.JFrame;

import com.sun.glass.events.WindowEvent;
import com.taozeyu.orange.ImageWindow.ImageSource;

public class TestStart {

	private static ImageWindow<ImageWindow.ImageSource> getImageReadQueue() throws FileNotFoundException{
		
		LinkedList<ImageWindow.ImageSource> list = new LinkedList<ImageWindow.ImageSource>();
		
		File dir = new File("O:\\girls");
		
		for(final File file:dir.listFiles()) {
			
			list.add(new ImageWindow.ImageSource() {

				@Override
				public InputStream getInputStream() {
					try {
						return new BufferedInputStream(new FileInputStream(file));
					} catch (FileNotFoundException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public String toString() {
					return "" + this.hashCode() + " " + file.getPath();
				}
			});
		}
		ImageWindow<ImageSource> iw = new ImageWindow<ImageWindow.ImageSource>(list);
		
		return iw;
	}
	
	public static void main(String[] args) throws IOException {
		
		final JFrame jframe = new JFrame();
		jframe.setSize(new Dimension(900, 650));
		jframe.setLocation(120, 60);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ImageView<ImageWindow.ImageSource> imageView = new ImageView<ImageWindow.ImageSource>(getImageReadQueue());
		
		jframe.add(imageView);
		jframe.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(java.awt.event.WindowEvent arg0) {
				super.windowClosing(arg0);
				imageView.close();
			}
		});
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				jframe.setVisible(true);
			}
		});
	}
}
