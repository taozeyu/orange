package com.taozeyu.album.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


abstract class BaseDao<C extends BaseDao<C>> {

	private Long id;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private final static String[] GetterPrevNames = new String[]{
		"is", "get",
	};
	private final static String[] SetterPrevNames = new String[]{
		"set",
	};
	
	private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Method>> setterContainer = new ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Method>>();
	private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Method>> getterContainer = new ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Method>>();
	
	private final HashMap<Class<?>, String> belongsColumnMap;
	
	private final DatabaseManager dbmanager;
	
	protected BaseDao(DatabaseManager dbmanager) {
		this(dbmanager, new HashMap<Class<?>, String>());
	}
	
	protected BaseDao(DatabaseManager dbmanager, HashMap<Class<?>, String> belongsColumnMap) {
		this.dbmanager = dbmanager;
		this.belongsColumnMap = belongsColumnMap;
	}
	
	@SuppressWarnings("unchecked")
	public C create() {
		try {
			Constructor<?> constructor = this.getClass().getConstructor(
					new Class<?>[] { DatabaseManager.class });
			return (C) constructor.newInstance(dbmanager);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public <CT> CT findChild(Class<CT> type) {
		List<CT> list = findChildren(type);
		if(list == null) {
			return null;
		}
		if(list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	
	public <CT> List<CT> findChildren(Class<CT> type) {
		return findChildren(new LinkedList<CT>(), type, null);
	}
	
	public <CT> List<CT> findChildren(Class<CT> type, String orderby) {
		return findChildren(new LinkedList<CT>(), type, orderby);
	}
	
	public <CT> List<CT> findChildren(List<CT> containerList, Class<CT> type) {
		return findChildren(containerList, type, null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <CT> List<CT> findChildren(List<CT> containerList, Class<CT> type, String orderby) {
		
		String columnName = belongsColumnMap.get(type);
		
		if(columnName == null) {
			return null;
		}
		try {
			BaseDao dao = (BaseDao) type.getField("manager").get(null);
			return dao.findAll(containerList, columnName + " = ?", orderby, this.id);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public C findById(long id) throws SQLException {
		return find("id = ?", id);
	}
	
	public C find(String condition, Object...args) throws SQLException {
		List<C> rslist = findAll(new LinkedList<C>(), condition, "", args);
		return rslist.isEmpty()? null: rslist.get(0);
	}
	
	public List<C> findAll(List<C> containerList) throws SQLException {
		return findAll(containerList, "", "", null, null, new Object[] {});
	}
	
	public List<C> findAll(List<C> containerList, String condition, Object...args) throws SQLException {
		return findAll(containerList, condition, "", null, null, args);
	}
	
	public List<C> findAll(List<C> containerList, String condition, Integer limit, Object...args) throws SQLException {
		return findAll(containerList, condition, "", 0, limit, args);
	}
	
	public List<C> findAll(List<C> containerList, String condition, Integer startRow, Integer limitRow, Object...args) throws SQLException {
		return findAll(containerList, condition, "", startRow, limitRow, args);
	}
	
	public List<C> findAll(List<C> containerList, String condition, String orderby, Object...args) throws SQLException {
		return findAll(containerList, condition, orderby, null, null, args);
	}
	
	@SuppressWarnings("unchecked")
	public List<C> findAll(
			List<C> containerList,
			String condition, String orderby,
			Integer startRow, Integer limitRow,
			Object...args) throws SQLException {

		Statement s = dbmanager.conn.createStatement();
		ResultSet rs = null;
		
		if(args == null) {
			args = new Object[] {};
		}
		
		try{
			StringBuilder sql = new StringBuilder();
			
			for(int i=0; i<args.length; ++i) {
				args[i] = decorator(args[i]);
			}
			String[] columns = getColumnNames();
			sql.append("SELECT ");

			sql.append("id");
			for(int i=0; i<columns.length; ++i) {
				sql.append(", ");
				sql.append(columns[i]);
			}
			
			sql.append(" FROM ");
			sql.append(getTableName());
			sql.append(" ");
			if(condition != null && !condition.equals("")) {
				condition = condition.trim().replace("?", "%s");
				sql.append("WHERE ");
				sql.append(String.format(condition, args));
				sql.append(" ");
			}
			if(orderby != null && !orderby.equals("")) {
				sql.append("ORDER BY ");
				sql.append(orderby.trim());
				sql.append(" ");
			}
			if(startRow != null || limitRow != null) {
				startRow = (startRow == null) ? 0 : startRow;
				limitRow = (limitRow == null) ? -1 : limitRow;
				sql.append("LIMIT ");
				sql.append(startRow);
				sql.append(", ");
				sql.append(limitRow);
				sql.append(" ");
			}
			sql.append(";");
			DatabaseManager.printSql(sql);
			
			rs = s.executeQuery(sql.toString());
			Constructor<C> constructor;
			
			try {
				constructor = (Constructor<C>) this.getClass()
						.getConstructor(new Class<?>[]{ DatabaseManager.class });
				
				while(rs.next()) {
					C bean = constructor.newInstance(dbmanager);
					for(String column:columns) {
						Object value = rs.getObject(column);
						bean.setValueByName(column, value);
					}
					bean.setValueByName("id", rs.getLong("id"));
					containerList.add(bean);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			return containerList;
			
		} finally {
			if(rs != null) {
				rs.close();
			}
			s.close();
		}
	}
	
	public void remove() throws SQLException {
		
		if(id == null) {
			return;
		}
		Statement s = dbmanager.conn.createStatement();
		try{
			StringBuilder sql = new StringBuilder();
			sql.append("DELETE FROM ");
			sql.append(getTableName());
			sql.append(" WHERE id = ");
			sql.append(id);
			sql.append(";");
			
			DatabaseManager.printSql(sql);
			s.executeUpdate(sql.toString());
			
		} finally {
			s.close();
		}
	}
	
	public void save() throws SQLException {
		Statement s = dbmanager.conn.createStatement();
		try{
			StringBuilder sql = new StringBuilder();
			String[] columns = getColumnNames();
			if(id == null) {
				
				long nextID = 1L;
				ResultSet rs = s.executeQuery("SELECT MAX(id) AS TOP_ID FROM " + getTableName() + ";");
				try {
					if(rs.next()) {
						nextID = rs.getInt("TOP_ID") + 1;
					}
					
				} finally {
					rs.close();
				}
				sql.append("INSERT INTO ");
				sql.append(getTableName());
				sql.append(" (");
				
				for(int i=0; i<columns.length; ++i) {
					sql.append(columns[i]);
					sql.append(", ");
				}
				sql.append("id");
				sql.append(") VALUES (");
				
				for(int i=0; i<columns.length; ++i) {
					Object value = getValueByName(columns[i]);
					sql.append(decorator(value));
					sql.append(", ");
				}
				sql.append(nextID);
				sql.append(");");

				DatabaseManager.printSql(sql);
				s.executeUpdate(sql.toString());
				
				id = nextID;
				
			} else {
				
				sql.append("UPDATE ");
				sql.append(getTableName());
				sql.append(" SET ");
				
				for(int i=0; i<columns.length; ++i) {
					Object value = getValueByName(columns[i]);
					sql.append(columns[i]);
					sql.append(" = ");
					sql.append(decorator(value));
					if(i < columns.length - 1) {
						sql.append(", ");
					}
				}
				sql.append(" WHERE id = ");
				sql.append(id);
				sql.append(";");
				
				DatabaseManager.printSql(sql);
				s.executeUpdate(sql.toString());
			}
		} finally {
			s.close();
		}
	}
	
	protected abstract String getTableName();
	
	protected abstract String[] getColumnNames();
	
	public abstract String createTableSql();
	
	protected Object getValueByName(String name) {
		Method method = getMethod(name, GetterPrevNames, getterContainer);
		try{
			return method.invoke(this);
		}	catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void setValueByName(String name, Object value) {
		Method method = getMethod(name, SetterPrevNames, setterContainer);
		try{
			if(value != null) {
				Class<?> type = method.getParameters()[0].getType();
				if(value instanceof Integer && type.equals(Long.class)) {
					value = Long.valueOf((Integer)value);
				} else if(value instanceof Float && type.equals(Double.class)) {
					value = Double.valueOf((Float)value);
				}
			}
			method.invoke(this, value);
		}	catch(Exception e) {
			System.err.println("invoke err from " + method.getDeclaringClass().getName());
			System.err.println("set \"" + name + "\" " + value + " ("+(value == null?"void":value.getClass().getName())+")");
			System.err.println(method);
			throw new RuntimeException(e);
		}
	}
	
	private String decorator(Object value) {
		if(value == null) {
			return "NULL";
		} else if(value instanceof Iterable<?>) {
			
			StringBuilder buff = new StringBuilder();
			Iterator<?> it = ((Iterable<?>)value).iterator();
			
			while(it.hasNext()) {
				buff.append(decorator(it.next()));
				if(it.hasNext()) {
					buff.append(", ");
				}
			}
			return buff.toString();
			
		} else if(value instanceof String){
			String keyWord = (String) value;
			keyWord = keyWord.replace("/", "//");  
		    keyWord = keyWord.replace("'", "''");  
		    keyWord = keyWord.replace("[", "/[");  
		    keyWord = keyWord.replace("]", "/]");  
		    keyWord = keyWord.replace("%", "/%");  
		    keyWord = keyWord.replace("&","/&");  
		    keyWord = keyWord.replace("_", "/_");  
		    keyWord = keyWord.replace("(", "/(");  
		    keyWord = keyWord.replace(")", "/)");  
			return "'" + keyWord + "'";
		} else {
			return value.toString();
		}
	}
	
	private Method getMethod(
			String name, String[] prevName,
			ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Method>> container
	) {
		name = toUpperFirstChar(name.trim());
		ConcurrentHashMap<String, Method> map = container.get(this.getClass());
		if(map == null) {
			map = new ConcurrentHashMap<String, Method>();
			ConcurrentHashMap<String, Method> pia = container.putIfAbsent(getClass(), map);
			if(pia != null) {
				map = pia;
			}
		}
		Method method = map.get(name);
		if(method == null) {
			method = findMethod(name, prevName);
			Method pia = map.putIfAbsent(name, method);
			if(pia != null) {
				method = pia;
			}
		}
		return method;
	}
	
	private Method findMethod(String name, String[] prevName) {
		for(Method method:this.getClass().getMethods()) {
			boolean nameEquals = false;
			for(String prev:prevName) {
				if((prev + name).equals(method.getName())) {
					nameEquals = true;
					break;
				}
			}
			if(!nameEquals) {
				continue;
			}
			int modifier = method.getModifiers();
			if(Modifier.isAbstract(modifier)) {
				continue;
			}
			if(!Modifier.isPublic(modifier)) {
				continue;
			}
			if(Modifier.isStatic(modifier)) {
				continue;
			}
			return method;
		}
		throw new RuntimeException("method no find '" + name + "'");
	}
	
	private String toUpperFirstChar(String name) {
		char[] chararray = name.toCharArray();
		chararray[0] = String.valueOf(chararray[0]).toUpperCase().charAt(0);
		return String.valueOf(chararray);
	}
}
