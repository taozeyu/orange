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
	
	public E get() {
		synchronized (queue) {
			if(queue.isEmpty()) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					return null;
				}
			}
			return queue.removeLast();
		}
	}
}
