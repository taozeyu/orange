package com.taozeyu.util;

/** Parser��ʼ��ʧ��ʱ�׳����쳣���˺�ÿ����ͼ��ȡ��ʼ��ʧ�ܵ�Parser�����׳���*/
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
