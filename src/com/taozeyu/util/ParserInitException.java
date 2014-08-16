package com.taozeyu.util;

/** Parser初始化失败时抛出该异常，此后，每次试图获取初始化失败的Parser都会抛出。*/
public class ParserInitException extends RuntimeException{

	private static final long serialVersionUID = 10147706179558305L;

	private Throwable throwed;
	
	public ParserInitException(Throwable e){
		super("Parser throwed a Exception when initialized.", e);
		throwed = e;
	}
	
	public Throwable getTargetThrowable(){
		return throwed;
	}

	public ParserInitException copy() {
		return new ParserInitException(throwed);
	}
}
