package com.taozeyu.album.dao;

public class Test {

	public static void main(String[] args) throws Exception {

		DatabaseManager dbmanager = new DatabaseManager("test.db");
		
		ImageAttributeDao bean = ImageAttributeDao.manager.create();
		
		bean.setName("法克鱿");
		bean.setInfo("就是一种鱿鱼");
		
		bean.save();
	}
}
