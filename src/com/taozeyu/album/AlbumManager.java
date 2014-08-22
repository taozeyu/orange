package com.taozeyu.album;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

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
	
	private AlbumManager(String dbpath) throws ClassNotFoundException, SQLException {
		this.dbManager = new DatabaseManager(dbpath);
		this.searchLogic = new SearchLogic();
		this.configLoader = new ConfigLoader();
		this.albumFrame = new AlbumFrame();
		this.tagEditorLogic = new TagEditorLogic();
		
		dbManager.setEnablePrintSql(true);
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
				System.getProperty("user.dir"), "tag_config.json"
		));
		inputStream = new BufferedInputStream(inputStream);
		configLoader.synchronize(inputStream);
	}
	
	public static void main(String[] args) {
		
		try{
			AlbumManager.singleInstance = new AlbumManager("test.db");
			AlbumManager.singleInstance.configLoader.load();
			//debug
			AlbumManager.singleInstance.synchronize();
			//end debug
			AlbumManager.singleInstance.searchLogic.setVisiable(true);
			
		}catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "橘子相册启动出错啦！", JOptionPane.ERROR_MESSAGE);
			System.out.println(1);
		}
	}
}
