package com.taozeyu.album.dao;


public class TagDao extends BaseDao<TagDao> {

	public static TagDao manager = null;
	
	private static final String TableName = "tags";
	private static final String[] ColumnNames = new String[] {
		"name", "info", "hide", "attributeID", "tagGroupID",
	};
	private static final String CreateSQL = 
			"create table tags ("
			+ "id bigint not null,"
			+ "attributeID bigint not null,"
			+ "tagGroupID bigint,"
			+ "name string not null,"
			+ "info string,"
			+ "hide int not null,"
			+ "primary key (id));";
	
	public TagDao(DatabaseManager dbmanager) { super(dbmanager); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }

	private int hide;
	private long attributeID;
	private Long tagGroupID;
	private String name;
	private String info;

	public long getAttributeID() {
		return attributeID;
	}

	public void setAttributeID(long attributeID) {
		this.attributeID = attributeID;
	}

	public Long getTagGroupID() {
		return tagGroupID;
	}

	public void setTagGroupID(Long tagGroupID) {
		this.tagGroupID = tagGroupID;
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

	public int getHide() {
		return hide;
	}

	public void setHide(int hide) {
		this.hide = hide;
	}
}
