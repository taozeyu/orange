package com.taozeyu.album;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.taozeyu.album.dao.AttributeDao;
import com.taozeyu.album.dao.TagDao;
import com.taozeyu.album.dao.TagGroupDao;

class ConfigLoader {

	private final InputStream inputStream;
	
	ConfigLoader(InputStream inputStream) {
		this.inputStream = new BufferedInputStream(inputStream);
	}
	
	void synchronize() throws IOException, SQLException {
		String configContent = readFromSourceStream();
		JSONObject json = new JSONObject(configContent);
		
		for(AttributeDao bean:AttributeDao.manager.findAll(new LinkedList<AttributeDao>())) {
			bean.setHide(1);
			bean.save();
		}
		
		for(Object attrName: json.keySet()) {
			try{
				//解析是以Attribute为单位，某个Attribute的错误不会中断解析。（除非错误影响都之后的解析）
				JSONObject attrJson = json.getJSONObject(attrName.toString());
				synAttribute(attrName.toString(), attrJson);
				
			} catch(ConfigParseException e) {
				e.printStackTrace();
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void synAttribute(String attrName, JSONObject attrJson) throws ConfigParseException, SQLException{
		AttributeDao attriBean = AttributeDao.manager.find("name = ?", attrName);
		if(attriBean == null) {
			attriBean = AttributeDao.manager.create();
			attriBean.setName(attrName);
		}
		if(attrJson.has("depend")) {
			attriBean.setDependTagID(getDependTagID(attrJson.getJSONArray("depend")));
		}
		attriBean.setInfo(attrJson.getString("info"));
		attriBean.setImportance(attrJson.getInt("importance"));
		attriBean.setHide(0);
		attriBean.save();
		
		for(TagDao tagBean:TagDao.manager.findAll(
				new LinkedList<TagDao>(), "attributeID = ?", attriBean.getId())
		) {
			tagBean.setHide(1);
			tagBean.save();
		}
		
		JSONObject tagsJson = attrJson.getJSONObject("tags");
		for(Object tagName: tagsJson.keySet()) {
			JSONObject tagJson = tagsJson.getJSONObject(tagName.toString());
			synTag(attriBean.getId(), tagName.toString(), tagJson);
		}
		
		for(TagGroupDao groupBean:TagGroupDao.manager.findAll(
				new LinkedList<TagGroupDao>(), "attributeID = ?", attriBean.getId())
		) {
			groupBean.setHide(1);
			groupBean.save();
		}
		
		if(attrJson.has("groups")) {
			JSONObject groupsJson = attrJson.getJSONObject("groups");
			for(Object groupName: groupsJson.keySet()) {
				JSONObject groupJson = groupsJson.getJSONObject(groupName.toString());
				synGroup(attriBean.getId(), groupName.toString(), groupJson);
			}
		}
	}
	
	private void synTag(long attriID, String tagName, JSONObject tagJson) throws SQLException {
		TagDao tagBean = TagDao.manager.find("attributeID = ?, name = ?", attriID, tagName);
		if(tagBean == null) {
			tagBean = TagDao.manager.create();
			tagBean.setAttributeID(attriID);
			tagBean.setName(tagName);
		}
		tagBean.setInfo(tagJson.getString("info"));
		tagBean.setHide(0);
		tagBean.save();
	}
	
	private void synGroup(long attriID, String groupName, JSONObject groupJson) throws SQLException, ConfigParseException {
		TagGroupDao groupBean = TagGroupDao.manager.find("attributeID = ?, name = ?", attriID, groupName);
		if(groupBean != null) {
			groupBean = TagGroupDao.manager.create();
			groupBean.setAttributeID(attriID);
			groupBean.setName(groupName);
		}
		groupBean.setHide(0);
		groupBean.save();

		JSONArray arr = groupJson.getJSONArray("content");
		for(int i=0; i<arr.length(); ++i) {
			String tagName = arr.getString(i);
			TagDao tagBean = TagDao.manager.find("name = ?, attributeID = ?", tagName, attriID);
			if(tagBean == null) {
				throw new ConfigParseException("不存在名为"+tagName+"的标签");
			}
			tagBean.setTagGroupID(groupBean.getId());
			tagBean.save();
		}
	}
	
	private Long getDependTagID(JSONArray arr) throws SQLException, ConfigParseException {
		String attrName = arr.getString(0);
		AttributeDao attriBean = AttributeDao.manager.find("name = ?", attrName);
		if(attriBean == null || attriBean.getHide() != 0) {
			throw new ConfigParseException("不存在名为"+attrName+"的属性");
		}
		String tagName = arr.getString(1);
		TagDao tagBean = TagDao.manager.find("attributeID = ?, name = ?", attriBean.getId(), tagName);
		if(tagBean == null || tagBean.getHide() != 0) {
			throw new ConfigParseException("不存在名为"+tagName+"的标签");
		}
		return tagBean.getId();
	}
	
	private String readFromSourceStream() throws IOException {
		StringBuilder sb = new StringBuilder();
		Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
		char[] buff = new char[1024];
		try {
			int length;
			do{
				length = reader.read(buff);
				sb.append(buff, 0, length);
				
			} while(length != -1);
			return sb.toString();
		} finally {
			reader.close();
		}
	}
}
