package com.taozeyu.album.dao;

public class ImageAttributeDao extends BaseDao<ImageAttributeDao> {

	public static ImageAttributeDao manager = null;
	
	private static final String TableName = "attributes";
	private static final String[] ColumnNames = new String[] {
		"name", "info"
	};
	private static final String CreateSQL = 
			"create table attributes ("
			+ "id bigint not null,"
			+ "name string,"
			+ "info string,"
			+ "primary key (id));";
	
	public ImageAttributeDao(DatabaseManager dbmanager) { super(dbmanager); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }


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
}
