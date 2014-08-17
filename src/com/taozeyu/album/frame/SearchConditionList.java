package com.taozeyu.album.frame;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

public class SearchConditionList extends AbstractListModel<SearchAttributeView> {

	private static final long serialVersionUID = -8445687221326496853L;

	private List<SearchAttributeView> list = new ArrayList<SearchAttributeView>();
	
	public void resetContent(List<SearchAttributeView> list) {
		int oldLen = this.list.size();
		int newLen = list.size();
		this.list = list;
		this.fireContentsChanged(this, 0, Math.min(oldLen, newLen) - 1);
		if(oldLen > newLen) {
			this.fireIntervalRemoved(this, newLen, oldLen);
		} else if(oldLen < newLen) {
			this.fireIntervalAdded(this, oldLen, newLen);
		}
	}
	
	@Override
	public SearchAttributeView getElementAt(int index) {
		return list.get(index);
	}

	@Override
	public int getSize() {
		return list.size();
	}

}
