package com.taozeyu.album;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.taozeyu.album.dao.DatabaseManager;

public class Test {

	public static void main(String[] args) throws Exception {
		DatabaseManager manager = new DatabaseManager("test.db");
		InputStream is = new FileInputStream(new File(
				System.getProperty("user.dir"), "tag_config.json"
		));
		ConfigLoader config = new ConfigLoader();
		config.synchronize(is);
		manager.close();
	}
}
