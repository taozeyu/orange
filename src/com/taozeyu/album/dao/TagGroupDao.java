package com.taozeyu.album.dao;

import java.util.HashMap;

public class TagGroupDao extends BaseDao<TagDao> {

	public static TagGroupDao manager = null;
	
	private static final String TableName = "tageGroups";
	private static final String[] ColumnNames = new String[] {
		"attributeID", "name", "info", "relactionLevel"
	};
	private static final String CreateSQL = 
			"create table tageGroups ("
			+ "id bigint not null,"
			+ "attributeID bigint not null"
			+ "name string not null,"
			+ "info string not null,"
			+ "relactionLevel int not null,"
			+ "hide int not null,"
			+ "primary key (id));";
	
	private static final HashMap<Class<?>, String> BelongsColumnMap = new HashMap<>();
 	
	static {
		BelongsColumnMap.put(TagDao.class, "tagGroupID");
	}
	
	public TagGroupDao(DatabaseManager dbmanager) { super(dbmanager, BelongsColumnMap); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }

	private int hide;
	private long attributeID;
	private String name;
	private String info;
	private int relactionLevel;

	public long getAttributeID() {
		return attributeID;
	}

	public void setAttributeID(long attributeID) {
		this.attributeID = attributeID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getRelactionLevel() {
		return relactionLevel;
	}

	public void setRelactionLevel(int relactionLevel) {
		this.relactionLevel = relactionLevel;
	}

	public int getHide() {
		return hide;
	}

	public void setHide(int hide) {
		this.hide = hide;
	}
}
