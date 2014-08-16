package com.taozeyu.orange;

import java.awt.Image;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

class ImageBufferedList {

	private static final long DefaultColorDepth = 4; //RGBA
	
	private final long limitSize;
	
	private final ArrayList<WeakReference<Image>> weakImageList;
	
	private SoftReference<SoftReferenceBuffered> buffered = new SoftReference<SoftReferenceBuffered>(null);
	
	private static class SoftReferenceBuffered {
		long currSize = 0L;
		LinkedList<BufferedNode> nodeList = new LinkedList<BufferedNode>();
		HashSet<Integer> savedNodeSet = new HashSet<Integer>();
	}
	
	private static class BufferedNode {
		int index;
		Image image;
	}
	
	ImageBufferedList(int arrayLength, long limitSize) {
		this.limitSize = limitSize;
		this.weakImageList = new ArrayList<WeakReference<Image>>(arrayLength);
		for(int i=0; i<arrayLength; ++i) {
			weakImageList.add(new WeakReference<Image>(null));
		}
	}
	
	synchronized void setImage(int index, Image image) {
		if(index < 0 || index >= weakImageList.size()) {
			return;
		}
		handleImage(index, image);
		weakImageList.set(index, new WeakReference<Image>(image));
	}
	
	synchronized Image getImage(int index) {
		if(index < 0 || index >= weakImageList.size()) {
			return null;
		}
		Image image = weakImageList.get(index).get();
		if(image != null) {
			handleImage(index, image);
		}
		return image;
	}
	
	private void handleImage(int index, Image image) {
		
		SoftReferenceBuffered buffered = this.buffered.get();
		
		if(buffered == null) {
			buffered = new SoftReferenceBuffered();
			this.buffered = new SoftReference<SoftReferenceBuffered>(buffered);
		}
		
		if(!buffered.savedNodeSet.add(index)) {
			return;
		}
		
		long imageSize = getImageSize(image);
		
		if(buffered.currSize + imageSize > limitSize) {
			releaseSize(imageSize, buffered);
		}
		buffered.currSize += imageSize;
		
		BufferedNode node = new BufferedNode();
		node.image = image;
		node.index = index;
		
		buffered.nodeList.addFirst(node);
	}
	
	private void releaseSize(long size, SoftReferenceBuffered buffered) {
		long hasReleaseSize = 0L;
		LinkedList<BufferedNode> nodeList = buffered.nodeList;
		
		while(hasReleaseSize < size && nodeList.isEmpty()) {
			BufferedNode node = nodeList.removeLast();
			hasReleaseSize += getImageSize(node.image);
			buffered.savedNodeSet.remove(node.index);
		}
		buffered.currSize -= hasReleaseSize;
	}
	
	private long getImageSize(Image image) {
		
		if(image == ImageWindow.FailImage) {
			return 0L;
		}
		long width = image.getWidth(null);
		long height = image.getHeight(null);
		return width * height * DefaultColorDepth;
	}
}
