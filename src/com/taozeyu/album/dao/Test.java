package com.taozeyu.album.dao;

public class Test {

	public static void main(String[] args) throws Exception {

		DatabaseManager dbmanager = new DatabaseManager("test.db");
		
		ImageAttributeDao bean = ImageAttributeDao.manager.create();
		
		bean = ImageAttributeDao.manager.findById(1);
		bean.setInfo("\"好人\" \'真好\'");
		bean.save();
	}
}
