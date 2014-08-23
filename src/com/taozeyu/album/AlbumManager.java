package com.taozeyu.album;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.taozeyu.album.dao.DatabaseManager;
import com.taozeyu.album.frame.AlbumFrame;

public class AlbumManager {

	public static final int TagEditorShowItemsNum = 4;
	
	private static AlbumManager singleInstance = null;
	
	public static AlbumManager instance() {
		return singleInstance;
	}
	
	private final DatabaseManager dbManager;
	private final SearchLogic searchLogic;
	private final ConfigLoader configLoader;
	private final AlbumFrame albumFrame;
	private final TagEditorLogic tagEditorLogic;
	
	private String rootPath = "E:\\githubprojects\\orange\\test";
	
	private AlbumManager() throws ClassNotFoundException, SQLException, IOException {
		this.dbManager = new DatabaseManager(rootPath + ".db");
		this.searchLogic = new SearchLogic();
		this.configLoader = new ConfigLoader();
		this.albumFrame = new AlbumFrame();
		this.tagEditorLogic = new TagEditorLogic();
		
		dbManager.setEnablePrintSql(false);
	}
	
	public void confirmExit(Component parentWindow) {
		int rsCode = JOptionPane.showConfirmDialog(parentWindow, "确定要退出橘子相册吗？", "退出确认", JOptionPane.OK_CANCEL_OPTION);
		if(rsCode == JOptionPane.OK_OPTION) {
			AlbumManager.instance().dispose();
		}
	}
	
	public void dispose() {
		try {
			dbManager.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		albumFrame.close();
		
		System.exit(0);
	}
	
	public SearchLogic getSearchLogic() {
		return searchLogic;
	}

	public ConfigLoader getConfigLoader() {
		return configLoader;
	}

	public AlbumFrame getAlbumFrame() {
		return albumFrame;
	}

	public DatabaseManager getDbManager() {
		return dbManager;
	}

	public TagEditorLogic getTagEditorLogic() {
		return tagEditorLogic;
	}

	public void synchronize() throws IOException, SQLException {
		InputStream inputStream = new FileInputStream(new File(
				rootPath +".json"
		));
		inputStream = new BufferedInputStream(inputStream);
		configLoader.synchronize(inputStream);
	}
	
	public String getRootPath() {
		return rootPath;
	}

	public static void main(String[] args) {
		
		try{
			AlbumManager.singleInstance = new AlbumManager();
			AlbumManager.singleInstance.rootPath = initRootPath();
			AlbumManager.singleInstance.configLoader.load();
			//debug
			AlbumManager.singleInstance.synchronize();
			//end debug
			AlbumManager.singleInstance.searchLogic.setVisiable(true);
			
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "橘子相册启动出错啦！", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	
	private static String initRootPath() {
		final Pattern fileNameFilter = Pattern.compile("\\.(db|json|params|params)$");
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				return fileNameFilter.matcher(f.getName()).find() || f.isDirectory();
			}

			public String getDescription() {
				return "图片数据库相关文件 .db .json .params";
			}
		});

		int r = chooser.showDialog(null, "打开图片数据库");
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			String path = f.getPath();
			path = fileNameFilter.matcher(path).replaceAll("");
			return path;
			
		} else {
			JOptionPane.showMessageDialog(null, "必须选择一个文件，否则无法启动。", "橘子相册启动出错啦！", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		return null;
	}
}
