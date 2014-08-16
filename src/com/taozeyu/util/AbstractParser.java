package com.taozeyu.util;

public abstract class AbstractParser {

	private ThreadLocal<Boolean> threadHasInto;
	
	protected AbstractParser() {
		threadHasInto = new ThreadLocal<Boolean>();
		threadHasInto.set(false);
	}
	
	private volatile boolean hasInit = false;
	private volatile Throwable initExp = null;
	
	private final Object initLock = new Object();
	
	Class<?> clazz;
	
	protected abstract void init(Class<?> clazz) throws Exception;
	
	protected final Class<?> getType(){
		return clazz;
	}
	
	private void doInitJob(){
		
		if(threadHasInto.get()){
			//如果该线程在初始化阶段已经访问过，说明形成了依赖环。
			LoopException exp = new LoopException();
			exp.push(clazz);
			throw exp;
		}
		threadHasInto.set(true);
		try{
			init(clazz);
			
		}catch(LoopException e){
			
			initExp = e;
			e.push(clazz);
			throw e;
			
		}catch(Throwable t){
			initExp = t;
			throw new ParserInitException(t);
		}
		threadHasInto = null;
	}
	
	void tryInit() {
		if(!hasInit){
			synchronized (initLock) {
				if(null != initExp){
					throw new ParserInitException(initExp);
				}
				if(!hasInit){
					doInitJob();
					hasInit = true;
				}
			}
		}
	}
}
