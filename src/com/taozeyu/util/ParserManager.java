package com.taozeyu.util;

import java.util.concurrent.ConcurrentHashMap;

public abstract class ParserManager<P extends AbstractParser> {

	private final ConcurrentHashMap<Class<?>, P> parserMap = new ConcurrentHashMap<Class<?>, P>();
	
	protected abstract P newInstance(Class<?> clazz);
	
	private P getUniquenessParser(Class<?> clazz){
		P parser = parserMap.get(clazz);
		if(parser==null){
			parser = newInstance(clazz);
			parser.clazz = clazz;
			P oldValue = parserMap.putIfAbsent(clazz, parser);
			if(null !=oldValue) {
				parser = oldValue;
			}
		}
		return parser;
	}
	/**
	 * ����һ����clazz��Ӧ��Parser������֤���Ѿ��ɹ���ʼ����
	 * @param clazz ����
	 * @return
	 * @throws NullPointerException ����clazzΪ��
	 * @throws ParserInitException clazz��Ӧ��Parser��ʼ��ʧ���ˣ��˺�ÿ����ͼ��ȡ�����׳����쳣��
	 * @throws LoopException Parser�������γɻ�
	 */
	public final P getParser(Class<?> clazz) throws NullPointerException, ParserInitException, LoopException{
		
		if(null==clazz){
			throw new NullPointerException();
		}
		P parser = getUniquenessParser(clazz);
		parser.tryInit();
		return parser;
	}
}
