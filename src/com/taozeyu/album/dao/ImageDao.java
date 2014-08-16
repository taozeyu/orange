package com.taozeyu.album.dao;

import java.util.HashMap;


public class ImageDao extends BaseDao<ImageDao> {

	public static ImageDao manager = null;
	
	private static final String TableName = "images";
	private static final String[] ColumnNames = new String[] {
		"filePath", "watchCount", "watchTime",
	};
	
	private static final String CreateSQL = 
			"create table imageTags ("
			+ "id bigint not null,"
			+ "filePath string not null,"
			+ "watchCount int not null,"
			+ "watchTime int not null,"
			+ "primary key (id));";
	
	private static final HashMap<Class<?>, String> BelongsColumnMap = new HashMap<>();
 	
	static {
		BelongsColumnMap.put(ImageTagDao.class, "imageID");
		BelongsColumnMap.put(ImageAttributeDao.class, "imageID");
	}
	
	public ImageDao(DatabaseManager dbmanager) { super(dbmanager, BelongsColumnMap); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }
	
	private String filePath;
	private int watchCount;
	private long watchTime;
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getWatchCount() {
		return watchCount;
	}

	public void setWatchCount(int watchCount) {
		this.watchCount = watchCount;
	}

	public long getWatchTime() {
		return watchTime;
	}

	public void setWatchTime(long watchTime) {
		this.watchTime = watchTime;
	}

}
