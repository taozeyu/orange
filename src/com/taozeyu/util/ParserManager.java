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
	 * 返回一个与clazz对应的Parser，并保证它已经成功初始化。
	 * @param clazz 类型
	 * @return
	 * @throws NullPointerException 参数clazz为空
	 * @throws ParserInitException clazz对应的Parser初始化失败了，此后每次试图获取都会抛出该异常。
	 * @throws LoopException Parser依赖链形成环
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
