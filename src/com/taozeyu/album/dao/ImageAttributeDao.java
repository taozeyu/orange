package com.taozeyu.album.dao;

public class ImageAttributeDao extends BaseDao<ImageAttributeDao> {

	public static ImageAttributeDao manager = null;
	
	private static final String TableName = "imageAttributes";
	private static final String[] ColumnNames = new String[] {
		"imageID", "attributeID", "state",
	};
	
	private static final String CreateSQL = 
			"create table imageAttributes ("
			+ "id bigint not null,"
			+ "imageID bigint not null,"
			+ "attributeID bitint not null,"
			+ "state int not null,"
			+ "primary key (id));";
	
	public ImageAttributeDao(DatabaseManager dbmanager) { super(dbmanager); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }
	
	private long imageID;
	private long attributeID;
	private int state;

	public long getImageID() {
		return imageID;
	}

	public void setImageID(long imageID) {
		this.imageID = imageID;
	}

	public long getAttributeID() {
		return attributeID;
	}

	public void setAttributeID(long attributeID) {
		this.attributeID = attributeID;
	}
	
	/** 0 未定	1 确定	2 忽略*/
	public int getState() {
		return state;
	}

	/** 0 未定	1 确定	2 忽略*/
	public void setState(int state) {
		this.state = state;
	}
}
