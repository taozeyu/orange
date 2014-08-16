package com.taozeyu.album.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

	final Connection conn;
	
	public DatabaseManager(String dbPath) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
	}
	
	public void close() throws SQLException {
		conn.close();
	}
}
