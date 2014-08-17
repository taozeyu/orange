package com.taozeyu.album;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import com.taozeyu.album.dao.DatabaseManager;

public class AlbumManager {

	private static AlbumManager singleInstance = null;
	
	public static AlbumManager instance() {
		return singleInstance;
	}
	
	private final DatabaseManager dbManager;
	private final SearchLogic searchLogic;
	private final ConfigLoader configLoader;
	
	private AlbumManager(String dbpath) throws ClassNotFoundException, SQLException {
		this.dbManager = new DatabaseManager(dbpath);
		this.searchLogic = new SearchLogic();
		this.configLoader = new ConfigLoader();
		
		dbManager.setEnablePrintSql(false);
	}
	
	public void dispose() {
		try {
			dbManager.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	public SearchLogic getSearchLogic() {
		return searchLogic;
	}

	public ConfigLoader getConfigLoader() {
		return configLoader;
	}

	private void synchronize() throws IOException, SQLException {
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
