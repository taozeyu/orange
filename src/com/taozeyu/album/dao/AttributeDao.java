package com.taozeyu.album.dao;

import java.util.HashMap;

public class AttributeDao extends BaseDao<AttributeDao> {

	public static AttributeDao manager = null;
	
	private static final String TableName = "attributes";
	private static final String[] ColumnNames = new String[] {
		"name", "info", "hide", "importance", "dependTagID"
	};
	
	private static final HashMap<Class<?>, String> BelongsColumnMap = new HashMap<>();
 	
	static {
		BelongsColumnMap.put(TagGroupDao.class, "attributeID");
		BelongsColumnMap.put(TagDao.class, "attributeID");
	}
	
	private static final String CreateSQL = 
			"create table attributes ("
			+ "id bigint not null,"
			+ "name string not null,"
			+ "info string,"
			+ "hide int not null,"
			+ "dependTagID bigint,"
			+ "importance int not null,"
			+ "primary key (id));";
	
	public AttributeDao(DatabaseManager dbmanager) { super(dbmanager, BelongsColumnMap); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }

	private int hide;
	private int importance;
	private Long dependTagID;
	private String name;
	private String info;

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

	public int getHide() {
		return hide;
	}

	public void setHide(int hide) {
		this.hide = hide;
	}

	public Long getDependTagID() {
		return dependTagID;
	}

	public void setDependTagID(Long dependTagID) {
		this.dependTagID = dependTagID;
	}

	public int getImportance() {
		return importance;
	}

	public void setImportance(int importance) {
		this.importance = importance;
	}
}
