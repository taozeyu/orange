package com.taozeyu.util;

import java.util.LinkedList;

/** Parser的初始化完成，必须依赖另一个Parser初始化完成。
 * 这种依赖关系延续下去称之为依赖链。若这个链是是个环，则环上任何一个Parser势必无法完成初始化，而彼此等待。
 * 这种情况是不允许的，一旦检测到，则抛出该异常。*/
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
