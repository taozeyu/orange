package com.taozeyu.album.dao;

public class TagBelongsGroupDao extends BaseDao<TagBelongsGroupDao> {

	public static TagBelongsGroupDao manager = null;
	
	private static final String TableName = "tageBelongsGroups";
	private static final String[] ColumnNames = new String[] {
		"tagID", "tagGroupID",
	};
	private static final String CreateSQL = 
			"create table tageBelongsGroups ("
			+ "id bigint not null,"
			+ "tagID bigint not null,"
			+ "tagGroupID bigint not null,"
			+ "primary key (id));";
	
	public TagBelongsGroupDao(DatabaseManager dbmanager) { super(dbmanager); }
	
	@Override
	protected String getTableName() { return TableName; }

	@Override
	protected String[] getColumnNames() { return ColumnNames; }

	@Override
	public String createTableSql() { return CreateSQL; }

	private long tagID, tagGroupID;

	public long getTagID() {
		return tagID;
	}

	public void setTagID(long tagID) {
		this.tagID = tagID;
	}

	public long getTagGroupID() {
		return tagGroupID;
	}

	public void setTagGroupID(long tagGroupID) {
		this.tagGroupID = tagGroupID;
	}
}
