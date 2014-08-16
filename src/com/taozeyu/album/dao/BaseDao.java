package com.taozeyu.album.dao;

abstract class BaseDao {

	private final DatabaseManager dbmanager;
	
	protected BaseDao(DatabaseManager dbmanager) {
		this.dbmanager = dbmanager;
	}
	
	public void save() {
		
	}
}
