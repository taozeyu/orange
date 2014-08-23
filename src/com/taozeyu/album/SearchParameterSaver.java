package com.taozeyu.album;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

class SearchParameterSaver {

	private JSONObject storeParams = new JSONObject();
	private JSONObject params = new JSONObject();
	
	private File ParamsFile;
	
	private static final Charset FileCharset = Charset.forName("utf-8");
	
	void initFromFile() throws IOException {
		ParamsFile = new File(AlbumManager.instance().getRootPath() + ".params");
		if(ParamsFile.exists()) {
			readFromFile();
		}
	}
	
	private void readFromFile() throws IOException {

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(ParamsFile), FileCharset)
		);
		
		try {
			StringBuilder sb = new StringBuilder();
			String line;

			while (null != (line = reader.readLine())) {
				sb.append(line);
			}
			params = new JSONObject(sb.toString());
			storeParams = new JSONObject(sb.toString());
			
		} finally {
			reader.close();
		}
	}
	
	private void save() throws IOException {
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(ParamsFile), FileCharset)
		);
		try{
			writer.write(storeParams.toString());
			writer.flush();
		} finally {
			writer.close();
		}
	}
	
	List<String> getNamesList(String filterName) {
		ArrayList<String> list = new ArrayList<>(params.length());
		for(Object name:params.keySet()) {
			if(!name.equals(filterName)) {
				list.add((String)name);
			}
		}
		return list;
	}
	
	void put(String name, JSONObject obj) {
		params.put(name, obj);
	}
	
	void save(String name) throws IOException {
		storeParams.put(name, params.getJSONObject(name));
		save();
	}
	
	JSONObject get(String name) {
		if(params.has(name)) {
			return params.getJSONObject(name);
		} else {
			return null;
		}
	}
	
	JSONObject getEmpty() {
		JSONObject obj = new JSONObject();
		obj.put("filter", false);
		obj.put("filter_regx", "");
		obj.put("must", new JSONArray());
		obj.put("exclude", new JSONArray());
		return obj;
	}
}
