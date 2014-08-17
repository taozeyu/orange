package com.taozeyu.album.frame;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import com.taozeyu.album.ConditionBean;

public class SearchConditionView extends AbstractListModel<String> {

	private static final long serialVersionUID = -785967460549599153L;
	
	private List<ConditionBean> list = new ArrayList<ConditionBean>();
	
	public void resetContent(List<ConditionBean> list) {
		int oldLen = this.list.size();
		int newLen = list.size();
		this.list = list;
		this.fireContentsChanged(null, 0, Math.min(oldLen, newLen) - 1);
		if(oldLen > newLen) {
			this.fireIntervalRemoved(null, newLen, oldLen);
		} else if(oldLen < newLen) {
			this.fireIntervalAdded(null, oldLen, newLen);
		}
	}
	@Override
	public String getElementAt(int index) {
		return list.get(index).getName();
	}

	@Override
	public int getSize() {
		return list.size();
	}

}
