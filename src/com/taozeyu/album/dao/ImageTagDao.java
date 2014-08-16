package com.taozeyu.album.dao;

public class ImageTagDao extends BaseDao<ImageTagDao> {

	public static ImageTagDao manager = null;
	
	private static final String TableName = "imageTags";
	private static final String[] ColumnNames = new String[] {
		"imageID", "tagID",
	};
	private static final String CreateSQL = 
			"create table imageTags ("
			+ "id bigint not null,"
			+ "imageID bigint not null,"
			+ "tagID bigint not null,"
			+ "primary key (id));";
	
	public ImageTagDao(DatabaseManager dbmanager) { super(dbmanager); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }
	
	private long imageID;
	private long tagID;

	public long getImageID() {
		return imageID;
	}

	public void setImageID(long imageID) {
		this.imageID = imageID;
	}

	public long getTagID() {
		return tagID;
	}

	public void setTagID(long tagID) {
		this.tagID = tagID;
	}
}
