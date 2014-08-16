package com.taozeyu.util;

import java.util.LinkedList;

/** Parser�ĳ�ʼ����ɣ�����������һ��Parser��ʼ����ɡ�
 * ����������ϵ������ȥ��֮Ϊ������������������Ǹ����������κ�һ��Parser�Ʊ��޷���ɳ�ʼ�������˴˵ȴ���
 * ��������ǲ�����ģ�һ����⵽�����׳����쳣��*/
public class LoopException extends RuntimeException {

	private static final long serialVersionUID = -5801159782044519033L;

	private LinkedList<Class<?>> nodeRing = new LinkedList<Class<?>>();
	
	void push(Class<?> node) {
		nodeRing.addFirst(node);
	}

	@Override
	public String getMessage() {
		String s = "find depend loop";
        for(Class<?> node:nodeRing) {
        	s +=" -> ";
        	s += node.getName();
        }
        return s;
	}
}
