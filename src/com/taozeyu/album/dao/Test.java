package com.taozeyu.album.dao;

public class Test {

	public static void main(String[] args) throws Exception {

		DatabaseManager dbmanager = new DatabaseManager("test.db");
		
		AttributeDao bean = AttributeDao.manager.create();
		
		bean = AttributeDao.manager.findById(1);
		bean.setInfo("\"����\" \'���\'");
		bean.save();
	}
}
