package com.taozeyu.album.dao;

public class Test {

	public static void main(String[] args) throws Exception {

		DatabaseManager dbmanager = new DatabaseManager("test.db");
		
		ImageAttributeDao bean = ImageAttributeDao.manager.create();
		
		bean.setName("������");
		bean.setInfo("����һ������");
		
		bean.save();
	}
}
