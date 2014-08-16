package com.taozeyu.util;

public class JavaBeanParserException extends RuntimeException{

	private static final long serialVersionUID = 3985183157575260496L;

	public JavaBeanParserException(String msg){
		super(msg);
	}
	
	public JavaBeanParserException(Throwable e){
		super(e);
	}
}
