package com.taozeyu.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.taozeyu.util.CustomJavaBeanParser.FieldNode;

public abstract class CustomJavaBeanParser<FN extends FieldNode> extends AbstractParser implements Iterable<String>{

	private static final HashMap<Class<?>, Class<?>> specialType = new HashMap<Class<?>, Class<?>>();
	
	static{
		specialType.put(byte.class, Byte.class);
		specialType.put(short.class, Short.class);
		specialType.put(int.class, Integer.class);
		specialType.put(long.class, Long.class);
		specialType.put(float.class, Float.class);
		specialType.put(double.class, Double.class);
		specialType.put(boolean.class, Boolean.class);
		specialType.put(char.class, Character.class);
		specialType.put(int.class, Integer.class);
		specialType.put(Byte.class, byte.class);
		specialType.put(Short.class, short.class);
		specialType.put(Integer.class, int.class);
		specialType.put(Long.class, long.class);
		specialType.put(Float.class, float.class);
		specialType.put(Double.class, double.class);
		specialType.put(Boolean.class, boolean.class);
		specialType.put(Character.class, char.class);
	}
	private static String[] getterPrevs = new String[]{"get", "is"};
	private static String[] setterPrevs = new String[]{"set"};
	
	private static Object[] emptyParams = new Object[]{};
	
	private HashMap<String, FN> fieldMap;
	private Constructor<?> constructor;
	
	protected abstract FN buildFieldNode();
	
	private FN newFieldNode(){
		FN rs = buildFieldNode();
		if(rs==null){
			throw new JavaBeanParserException("Method buildFieldNode can not return null.");
		}
		return rs;
	}
	/** 鍙戠幇鏃笉鏄痵etter锛屼篃涓嶆槸getter鐨勬柟娉�? */
	protected void onFoundOtherMethod(Method method) {}

	/** 纭涓�涓瓧娈� */
	protected void onFoundField(FN fieldNode) {}

	/** 鍙戠幇涓�涓己涔廹etter鐨勫瓧娈碉紝濡傛灉纭锛屽垯灏嗗叾瑙嗕负涓�涓瓧娈碉紝鍚�?垯蹇借銆傦紙渚涢噸鍐欙�?*/
	protected boolean verifyAbsentGetterField(FN fieldNode){
		return false;
	}

	/** 鍙戠幇涓�涓己涔弒etter鐨勫瓧娈碉紝濡傛灉纭锛屽垯灏嗗叾瑙嗕负涓�涓瓧娈碉紝鍚�?垯蹇借銆傦紙渚涢噸鍐欙�?*/
	protected boolean verifyAbsentSetterField(FN fieldNode){
		return false;
	}
	
	public static class FieldNode {
		
		String fieldName;
		Class<?> fieldType;
		
		Field field;
		Method getter;
		Method setter;
		
		@Override
		public final int hashCode() {
			return fieldName.hashCode() ^ fieldType.hashCode();
		}
		@Override
		public final boolean equals(Object obj) {
			FieldNode other = (FieldNode) obj;
			return this.fieldName.equals(other.fieldName) && this.fieldType.equals(other.fieldType);
		}
		public final String getFieldName() {
			return fieldName;
		}
		public final Class<?> getFieldType() {
			return fieldType;
		}
		public final Field getField() {
			return field;
		}
		public final Method getGetter() {
			return getter;
		}
		public final Method getSetter() {
			return setter;
		}
	}
	
	public final boolean containsField(String fieldName) {
		return fieldMap.containsKey(fieldName);
	}
	
	public final Class<?> getFieldType(String fieldName) {
		FieldNode node = fieldMap.get(fieldName);
		return node==null?null:node.fieldType;
	}
	
	public final Method getGetter(String fieldName) {
		FieldNode node = fieldMap.get(fieldName);
		return node==null?null:node.getter;
	}
	
	public final Method getSetter(String fieldName) {
		FieldNode node = fieldMap.get(fieldName);
		return node==null?null:node.setter;
	}
	
	public final Field getField(String fieldName){
		FieldNode node = fieldMap.get(fieldName);
		return node==null?null:node.field;
	}
	
	public final FN getFiledNode(String fieldName) {
		return fieldMap.get(fieldName);
	}
	
	public final Map<String, Object> putAll(Map<String, Object> map, Object obj){
		for(String fieldName:this){
			map.put(fieldName, getValue(obj, fieldName));
		}
		return map;
	}
	
	@Override
	public final Iterator<String> iterator() {
		final Iterator<String> proxy = fieldMap.keySet().iterator();
		return new Iterator<String>(){

			@Override
			public boolean hasNext() {
				return proxy.hasNext();
			}
			@Override
			public String next() {
				return proxy.next();
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	public final Object newInstance() {
		try {
			try {
				return constructor.newInstance(emptyParams);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} catch (Throwable e) {
			throw (Error) e;
		}
	}
	
	private boolean isInstanceOf(Class<?> type, Object instance) {
		if(instance==null){
			return true;
		}
		if(type.isInstance(instance)){
			return true;
		}
		if(specialType.get(type).equals(instance.getClass())){
			return true;
		}
		return false;
	}
	
	public final void setValue(Object obj, String fieldName, Object value) {
		FieldNode node = fieldMap.get(fieldName);
		if(node == null){
			throw new RuntimeException("field "+fieldName+" was not found.");
		}
		if(obj==null){
			throw new NullPointerException();
		}
		if(!clazz.isInstance(obj)){
			throw new RuntimeException("obj must be "+clazz.getName());
		}
		if(!isInstanceOf(node.fieldType, value)){
			throw new ClassCastException("need "+node.fieldType.getName()+" but supplied "+(null==value?"null":value.getClass()));
		}
		try {
			try {
				Object[] params = new Object[]{value};
				node.setter.invoke(obj, params);
				
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} catch (Throwable e) {
			throw (Error) e;
		}
	}
	
	public final Object getValue(Object obj, String fieldName) {
		FieldNode node = fieldMap.get(fieldName);
		if(node == null){
			throw new RuntimeException("field "+fieldName+" was not found.");
		}
		if(obj==null){
			throw new NullPointerException();
		}
		if(!clazz.isInstance(obj)){
			throw new RuntimeException("obj must be "+clazz.getName()+" but it is "+obj.getClass());
		}
		try {
			try {
				return node.getter.invoke(obj, emptyParams);
				
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} catch (Throwable e) {
			throw (Error) e;
		}
	}
	
	private String parseFieldName(String fieldName) {
		
		char firstChar = fieldName.charAt(0);
		String first = String.valueOf(firstChar);
		
		first = first.toLowerCase();
		
		if(fieldName.length() > 1){
			return first + fieldName.substring(1);
		}else{
			return first;
		}
	}
	
	private String tryGetFiledName(String[] prevArray, String methodName) {
		
		for(String prev:prevArray){
			if(methodName.startsWith(prev)){
				return parseFieldName(methodName.substring(prev.length()));
			}
		}
		return null;
	}
	
	private FN tryGetGetterNode(Method method) {
		
		String fieldName = tryGetFiledName(getterPrevs, method.getName());
		if(fieldName == null ){
			return null;
		}
		if(method.getReturnType().equals(void.class)){
			return null;
		}
		if(method.getParameterTypes().length!=0){
			return null;
		}
		FN node = newFieldNode();
		
		node.fieldName = fieldName;
		node.fieldType = method.getReturnType();
		node.getter = method;
		
		return node;
	}
	
	
	private FN tryGetSetterNode(Method method){
		
		String fieldName = tryGetFiledName(setterPrevs, method.getName());
		if(fieldName == null ){
			return null;
		}
		if(!method.getReturnType().equals(void.class)){
			return null;
		}
		Class<?>[] params = method.getParameterTypes();
		if(params.length != 1){
			return null;
		}
		Class<?> type = params[0];
		if(type.equals(void.class)){
			return null;
		}
		FN node = newFieldNode();
		
		node.fieldName = fieldName;
		node.fieldType = type;
		node.setter = method;
		
		return node;
	}
	
	private HashSet<String> subSet(HashMap<String, FN> m1, HashMap<String, FN> m2) {
		
		HashSet<String> set = new HashSet<String>();
		for(String fn:m1.keySet()) {
			set.add(fn);
		}
		for(String fn:m2.keySet()) {
			set.remove(fn);
		}
		return set;
	}
	
	private Field findField(Class<?> clazz, String fieldName){
		Field field;
		try {
			field = clazz.getDeclaredField(fieldName);
		} catch (Exception e){
			return null;
		}
		return field;
	}
	
	protected void beforeInit(Class<?> clazz) {}
	
	protected void afterInit(Class<?> clazz) {}
	
	@Override
	protected final void init(Class<?> clazz) throws Exception {
		
		beforeInit(clazz);
		
		Constructor<?> constructor = clazz.getConstructor(new Class<?>[]{});
		
		if((clazz.getModifiers() & Modifier.PUBLIC)==0){
			throw new JavaBeanParserException(clazz.getName()+" must be public.");
		}
		if((clazz.getModifiers() & Modifier.ABSTRACT)!=0){
			throw new JavaBeanParserException(clazz.getName()+" can't be abstract.");
		}
		if(clazz.isInterface()){
			throw new JavaBeanParserException(clazz.getName()+" must be a class.");
		}
		if((constructor.getModifiers() & Modifier.PUBLIC)==0){
			throw new JavaBeanParserException(clazz.getName()+" needs a public no params constructor.");
		}
		HashMap<String, FN> fieldMap = new HashMap<String, FN>();
		
		HashMap<String, FN> getterMap = new HashMap<String, FN>();
		HashMap<String, FN> setterMap = new HashMap<String, FN>();
		
		for(Method method:clazz.getMethods()){
			
			if(method.getDeclaringClass().equals(Object.class)){
				continue;
			}
			FN node = tryGetGetterNode(method);
			if(node != null){
				getterMap.put(node.fieldName, node);
				continue;
			}
			node = tryGetSetterNode(method);
			if(node != null){
				setterMap.put(node.fieldName, node);
				continue;
			}
			try{
				onFoundOtherMethod(method);
			}catch(RuntimeException e){
				e.printStackTrace();
			}
		}
		HashSet<String> onlyGetterFieldSet = subSet(getterMap, setterMap);
		HashSet<String> onlySetterFieldSet = subSet(setterMap, getterMap);
		
		for(String fn:onlyGetterFieldSet){
			FN node = getterMap.get(fn);
			try{
				node.field = findField(clazz, fn);
				if(verifyAbsentGetterField(node)){
					fieldMap.put(fn, node);
				}
			}catch(RuntimeException e){
				e.printStackTrace();
			}
		}
		for(String fn:onlySetterFieldSet){
			FN node = setterMap.get(fn);
			try{
				node.field = findField(clazz, fn);
				if(verifyAbsentSetterField(node)){
					fieldMap.put(fn, node);
				}
			}catch(RuntimeException e){
				e.printStackTrace();
			}
		}
		//閿熸枻鎷烽敓楗烘枻鎷穏etter閿熸枻鎷烽敓鏂ゆ嫹map閿熷彨锝忔嫹鐒堕敓鏂ゆ嫹閿熸枻鎷烽敓绲猠tter閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷穝etter閿熸枻鎷烽敓鏂ゆ嫹閿熸彮浼欐嫹閿熸枻鎷疯�嶉敓鏂ゆ嫹鍋烽敓鏂ゆ嫹娌℃皭閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓锟�
		for(Entry<String, FN> e:getterMap.entrySet()){
			String fieldName = e.getKey();
			FN node = e.getValue();

			if(!onlyGetterFieldSet.contains(fieldName)) {
				fieldMap.put(fieldName, node);
			}
		}
		for(Entry<String, FN> e:setterMap.entrySet()){
			String fieldName = e.getKey();
			FN node = fieldMap.get(fieldName);
			if(null != node){
				node.setter = e.getValue().setter;
				node.field = findField(clazz, fieldName);
				try{
					onFoundField(node);
				}catch(RuntimeException exp){
					exp.printStackTrace();
				}
			}
		}
		this.fieldMap = fieldMap;
		this.constructor = constructor;
		
		afterInit(clazz);
	}
}
