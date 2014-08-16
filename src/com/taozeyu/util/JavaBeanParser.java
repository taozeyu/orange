package com.taozeyu.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Ϊ�˼�����ǰ������ࡣ
 * @see com.taozeyu.util.CustomJavaBeanParser*/
public class JavaBeanParser extends CustomJavaBeanParser<CustomJavaBeanParser.FieldNode>{

	/** ���ּȲ���setter��Ҳ����getter�ķ��� */
	protected void onFoundOtherMethod(Method method) {}
	
	/** ȷ��һ���ֶ� */
	protected void onFoundField(String fieldName, Class<?> type, Method setterMethod, Method getterMethod, Field field) {}
	
	/** ����һ��ȱ��getter���ֶΣ����ȷ�ϣ�������Ϊһ���ֶΣ�������ӡ�������д��*/
	protected boolean verifyAbsentGetterField(String fieldName, Class<?> type, Method setterMethod, Field field){
		return false;
	}

	/** ����һ��ȱ��setter���ֶΣ����ȷ�ϣ�������Ϊһ���ֶΣ�������ӡ�������д��*/
	protected boolean verifyAbsentSetterField(String fieldName, Class<?> type, Method getterMethod, Field field){
		return false;
	}

	@Override
	protected final CustomJavaBeanParser.FieldNode buildFieldNode() {
		return new CustomJavaBeanParser.FieldNode();
	}

	@Override
	protected final void onFoundField(CustomJavaBeanParser.FieldNode fieldNode) {
		onFoundField(fieldNode.getFieldName(), fieldNode.getFieldType(), fieldNode.getSetter(), fieldNode.getGetter(), fieldNode.getField());
	}

	@Override
	protected final boolean verifyAbsentGetterField(CustomJavaBeanParser.FieldNode fieldNode) {
		return verifyAbsentGetterField(fieldNode.getFieldName(), fieldNode.getFieldType(), fieldNode.getSetter(), fieldNode.getField());
	}

	@Override
	protected final boolean verifyAbsentSetterField(CustomJavaBeanParser.FieldNode fieldNode) {
		return verifyAbsentSetterField(fieldNode.getFieldName(), fieldNode.getFieldType(), fieldNode.getGetter(), fieldNode.getField());
	}

	@Override
	protected void beforeInit(Class<?> clazz) { }

	@Override
	protected void afterInit(Class<?> clazz) { }
}
