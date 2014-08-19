package com.taozeyu.album;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.taozeyu.album.dao.AttributeDao;
import com.taozeyu.album.dao.DatabaseManager;
import com.taozeyu.album.dao.TagBelongsGroupDao;
import com.taozeyu.album.dao.TagDao;
import com.taozeyu.album.dao.TagGroupDao;

class ConfigLoader {

	private HashSet<Long> visiableAttributeIDs;
	private HashSet<Long> visiableTagIDs;
	private HashSet<Long> visiableGroupIDs;
	
	void synchronize(InputStream inputStream) throws IOException, SQLException {
		String configContent = readFromSourceStream(inputStream);
		JSONArray json = new JSONArray(configContent);
		
		boolean hasComplete = false;
		
		try{
			synFileRootArray(json);
			searchVisiable();
			hasComplete = true;
			
		} finally {
			if(hasComplete) {
				DatabaseManager.getInstance().commit();
			} else {
				DatabaseManager.getInstance().rollback();
			}
		}
	}
	
	void load() throws SQLException {
		boolean hasComplete = false;
		try{
			searchVisiable();
			hasComplete = true;
			
		} finally {
			if(hasComplete) {
				DatabaseManager.getInstance().commit();
			} else {
				DatabaseManager.getInstance().rollback();
			}
		}
	}
	
	private void searchVisiable() throws SQLException {
		
		visiableAttributeIDs = new HashSet<Long>();
		visiableTagIDs = new HashSet<Long>();
		visiableGroupIDs = new HashSet<Long>();
		
		LinkedList<Long> ids = new LinkedList<Long>();
		
		for(AttributeDao bean:AttributeDao.manager.findAll(
				new LinkedList<AttributeDao>(), "hide = 0")
		) {
			ids.add(bean.getId());
			visiableAttributeIDs.add(bean.getId());
		}
		
		for(TagDao bean:TagDao.manager.findAll(
				new LinkedList<TagDao>(), "hide = 0 AND attributeID in (?)", ids)
		) {
			visiableTagIDs.add(bean.getId());
		}
		
		for(TagGroupDao bean:TagGroupDao.manager.findAll(
				new LinkedList<TagGroupDao>(), "hide = 0 AND attributeID in (?)", ids)
		) {
			visiableGroupIDs.add(bean.getId());
		}
	}
	
	boolean isAttributeVisiable(long id) {
		return visiableAttributeIDs.contains(id);
	}
	
	boolean isTagVisiable(long id) {
		return visiableTagIDs.contains(id);
	}
	
	boolean isGroupVisiable(long id) {
		return visiableGroupIDs.contains(id);
	}
	
	Set<Long> listAllAttributeVisiableIDS() {
		HashSet<Long> set = new HashSet<Long>();
		set.addAll(visiableAttributeIDs);
		return set;
	}
	
	private void synFileRootArray(JSONArray json) throws SQLException {
		
		for(AttributeDao bean:AttributeDao.manager.findAll(new LinkedList<AttributeDao>())) {
			bean.setHide(1);
			bean.save();
		}
		
		for(int i=0; i<json.length(); ++i) {
			
			String attrName = "unkown";
			try{
				//解析是以Attribute为单位，某个Attribute的错误不会中断解析。（除非错误影响都之后的解析）
				JSONObject attrJson = json.getJSONObject(i);
				attrName = attrJson.getString("name");
				synAttribute(attrName, attrJson);
				
			} catch(ConfigParseException e) {
				e.printStackTrace();
			} catch(JSONException e) {
				System.err.println("来自\""+attrName+"\"的JSON解析错误："+e.getMessage());
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
		TagDao tagBean = TagDao.manager.find("attributeID = ? AND name = ?", attriID, tagName);
		if(tagBean == null) {
			tagBean = TagDao.manager.create();
			tagBean.setAttributeID(attriID);
			tagBean.setName(tagName);
		}
		if(tagJson.has("info")) {
			tagBean.setInfo(tagJson.getString("info"));
		}
		tagBean.setHide(0);
		tagBean.save();
	}
	
	private void synGroup(long attriID, String groupName, JSONObject groupJson) throws SQLException, ConfigParseException {
		TagGroupDao groupBean = TagGroupDao.manager.find("attributeID = ? AND name = ?", attriID, groupName);
		if(groupBean == null) {
			groupBean = TagGroupDao.manager.create();
			groupBean.setAttributeID(attriID);
			groupBean.setName(groupName);
		}
		groupBean.setHide(0);
		groupBean.save();

		JSONArray arr = groupJson.getJSONArray("content");
		for(int i=0; i<arr.length(); ++i) {
			String tagName = arr.getString(i);
			TagDao tagBean = TagDao.manager.find("name = ? AND attributeID = ?", tagName, attriID);
			if(tagBean == null) {
				throw new ConfigParseException("不存在名为'"+tagName+"'的标签");
			}
			tagBean.setHide(0);
			tagBean.save();
			
			TagBelongsGroupDao link = TagBelongsGroupDao.manager.create();
			link.setTagGroupID(groupBean.getId());
			link.setTagID(tagBean.getId());
			link.save();
		}
	}
	
	private Long getDependTagID(JSONArray arr) throws SQLException, ConfigParseException {
		String attrName = arr.getString(0);
		AttributeDao attriBean = AttributeDao.manager.find("name = ?", attrName);
		if(attriBean == null || attriBean.getHide() != 0) {
			throw new ConfigParseException("不存在名为'"+attrName+"'的属性");
		}
		String tagName = arr.getString(1);
		TagDao tagBean = TagDao.manager.find("attributeID = ? AND name = ?", attriBean.getId(), tagName);
		if(tagBean == null || tagBean.getHide() != 0) {
			throw new ConfigParseException("不存在名为'"+tagName+"'的标签");
		}
		return tagBean.getId();
	}
	
	private String readFromSourceStream(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		Reader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
		char[] buff = new char[1024];
		try {
			int length = reader.read(buff);
			while(length != -1) {
				sb.append(buff, 0, length);
				length = reader.read(buff);
			}
			return sb.toString();
		} finally {
			reader.close();
		}
	}
}
