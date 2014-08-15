package com.taozeyu.orange;

import java.util.LinkedList;

class LockQueue<E> {

	private final LinkedList<E> queue = new LinkedList<E>();
	
	public void add(E element) {
		synchronized (queue) {
			queue.addFirst(element);
			queue.notify();
		}
	}
	
	public E get() throws InterruptedException {
		synchronized (queue) {
			if(queue.isEmpty()) {
				queue.wait();
			}
			return queue.removeLast();
		}
	}
}
