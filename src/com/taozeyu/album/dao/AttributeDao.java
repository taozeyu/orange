package com.taozeyu.album.dao;

import java.util.HashMap;

public class AttributeDao extends BaseDao<AttributeDao> {

	public static AttributeDao manager = null;
	
	private static final String TableName = "attributes";
	private static final String[] ColumnNames = new String[] {
		"name", "info", "hide"
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
			+ "info string not null,"
			+ "hide int not null,"
			+ "dependTagID bigint,"
			+ "primary key (id));";
	
	public AttributeDao(DatabaseManager dbmanager) { super(dbmanager, BelongsColumnMap); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }


	private int hide;
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
}
