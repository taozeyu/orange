package com.taozeyu.album.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

	private static DatabaseManager instance;
	
	public static DatabaseManager getInstance() {
		return instance;
	}

	private static final Class<?>[] DaoClassList = new Class<?>[] {
		AttributeDao.class,
		ImageAttributeDao.class,
		ImageDao.class,
		ImageTagDao.class,
		TagDao.class,
		TagGroupDao.class,
		TagBelongsGroupDao.class,
	};
	
	final Connection conn;
	
	public DatabaseManager(String dbPath) throws ClassNotFoundException, SQLException {
		this(dbPath, false);
	}
	
	public DatabaseManager(String dbPath, boolean clearOldDateBase) throws ClassNotFoundException, SQLException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
		conn.setAutoCommit(false);
		
		initDaoManagerNodes();
		initDataBase(clearOldDateBase);
		
		instance = this;
	}
	
	public void commit() throws SQLException {
		conn.commit();
	}
	
	public void rollback() throws SQLException {
		conn.rollback();
	}
	
	public void close() throws SQLException {
		conn.commit();
		conn.close();
	}
	
	private void initDaoManagerNodes() {
		try {
			for (Class<?> daoClass : DaoClassList) {
				
				Constructor<?> constructor = daoClass
						.getConstructor(new Class<?>[] { DatabaseManager.class });
				
				Object daoNode = constructor.newInstance(this);
				
				Field field = daoClass.getField("manager");
				
				if(field==null) {
					throw new RuntimeException();
				}
				field.setAccessible(true);
				
				int modifiers = field.getModifiers();
				
				if(!Modifier.isStatic(modifiers)) {
					throw new RuntimeException();
				}
				if(!field.getType().equals(daoClass)) {
					throw new RuntimeException();
				}
				field.set(null, daoNode);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initDataBase(boolean clearOldDateBase) throws SQLException {
		
		Statement s = null;
		
		try {
			s  = conn.createStatement();
			for (Class<?> daoClass : DaoClassList) {
				
				BaseDao<?> dao = (BaseDao<?>) daoClass.getField("manager").get(null);
				
				String sql = String.format(
						"SELECT COUNT(*) as CNT FROM sqlite_master where type='table' and name='%s';",
						dao.getTableName()
				);
				ResultSet rs = s.executeQuery(sql);
				rs.next();
				
				int tableCount = rs.getInt("CNT");
				
				if( tableCount > 0 && clearOldDateBase) {
					
					s.executeUpdate("drop table if exists "+dao.getTableName()+";");
					tableCount = 0;
				}
				
				if(tableCount == 0) {
					String sqlCreateTable = dao.createTableSql();
					String sqlCreateIndex = String.format(
							"CREATE UNIQUE INDEX %s ON %s (id)",
							dao.getTableName() + "_index",
							dao.getTableName()
					);
					System.out.println(sqlCreateTable);
					s.execute(sqlCreateTable);
					System.out.println(sqlCreateIndex);
					s.execute(sqlCreateIndex);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			s.close();
		}
	}
}
