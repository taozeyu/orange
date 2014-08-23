package com.taozeyu.album.frame;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SearchAttributeView extends JPanel {

	private static final long serialVersionUID = 7891831078386960931L;

	public Runnable onStateChanged = null;
	
	private final long id;
	private final int importance;
	private final String name;
	private final String info;
	
	private final DepandNode depend;
	private final TagNode[] tags;
	
	private final HashMap<Long, JComboBox<?>> comboBoxMap = new HashMap<Long, JComboBox<?>>();
	
	public enum TagState {
		
		NotChoose(""),
		Must("必要"),
		Exclude("排除");
		
		private static final LinkedList<TagState> ShowList = new LinkedList<TagState>();
		static {
			ShowList.add(NotChoose);
			ShowList.add(Must);
			ShowList.add(Exclude);
		}
		
		private final String info;
		
		private TagState(String info) {
			this.info = info;
		}

		@Override
		public String toString() {
			return info;
		}
	}
	
	public static class DepandNode {
		private final SearchAttributeView view;
		private final TagNode tag;
		
		public DepandNode(SearchAttributeView view, TagNode tag) {
			this.view = view;
			this.tag = tag;
		}

		public SearchAttributeView getView() {
			return view;
		}

		public TagNode getTag() {
			return tag;
		}
	}
	
	public static class TagNode {
		
		private final LinkedList<SearchAttributeView> dependOnList = new LinkedList<SearchAttributeView>();
		
		private final long id;
		private final String name;
		private final String info;
		
		private TagState state;
		
		public TagNode(long id, String name, String info) {
			this.id = id;
			this.name = name;
			this.info = info;
		}

		public long getId() {
			return id;
		}

		public TagState getState() {
			return state;
		}
	}
	
	public SearchAttributeView(long id, String name, String info, int importance, TagNode[] tags) {
		this(id, name, info, importance, tags, null);
	}
	
	public SearchAttributeView(long id, String name, String info, int importance, TagNode[] tags, DepandNode depend) {
		this.id = id;
		this.name = name;
		this.info = info;
		this.importance = importance;
		this.depend = depend;
		this.tags = tags;
		
		initComponent();
	}
	
	public void setTagState(Collection<Long> excludeTagsID, Collection<Long> mustTagsID) {
		
		for(JComboBox<?> cb:comboBoxMap.values()) {
			cb.setSelectedItem(TagState.NotChoose);
		}
		for(long id:excludeTagsID) {
			JComboBox<?> cb = comboBoxMap.get(id);
			if(cb != null ) {
				cb.setSelectedItem(TagState.Exclude);
			}
		}
		for(long id:mustTagsID) {
			JComboBox<?> cb = comboBoxMap.get(id);
			if(cb != null ) {
				cb.setSelectedItem(TagState.Must);
			}
		}
	}
	
	private void currConditionChanged() {
		if(onStateChanged != null) {
			onStateChanged.run();
		}
	}
	
	private void initComponent() {
		
		if(depend != null) {
			depend.tag.dependOnList.add(this);
		}
		setToolTipText(info);
		setBorder(BorderFactory.createTitledBorder(this.name));
		setLayout(new FlowLayout());
		
		for(int i=0; i<this.tags.length; ++i) {
			final TagNode tag = this.tags[i];
			JPanel childPanel = new JPanel();
			final JComboBox<TagState> cb = new JComboBox<>(getDefaultVecotr());
			comboBoxMap.put(tag.getId(), cb);
			childPanel.add(cb);
			childPanel.add(new JLabel(" "+tag.name+"	"));
			childPanel.setToolTipText(tag.info);
			
			cb.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent evt) {
					TagState state = (TagState) cb.getSelectedItem();
					tag.state = state;
					
					boolean dependEnabled = (state != TagState.Exclude) && SearchAttributeView.this.isEnabled();
					
					for(SearchAttributeView view : tag.dependOnList) {
						view.setAllEnabled(dependEnabled);
					}
					currConditionChanged();
				}
			});
			add(childPanel);
		}
	}

	@Override
	public void setEnabled(boolean value) {
		super.setEnabled(value);
		
		if(value) {
			setToolTipText(info);
		} else {
			if(depend != null) {
				setToolTipText("需要\""+depend.getView().name + "\"包含\""+depend.getTag().name+"\"才能使用");
			}
		}
	}
	
	private void setAllEnabled(boolean value) {
		this.setEnabled(value);
		
		for(int i=0; i<this.getComponentCount(); ++i) {
			JPanel panel = (JPanel) this.getComponent(i);
			for(int j=0; j<panel.getComponentCount(); ++j) {
				panel.getComponent(j).setEnabled(value);
			}
		}
	}
	
	private Vector<TagState> getDefaultVecotr() {
		return new Vector<TagState>(TagState.ShowList);
	}
	
	public TagNode[] getTags() {
		return tags;
	}

	public long getId() {
		return id;
	}
	
	public String getAttributeName() {
		return name;
	}

	public int getImportance() {
		return importance;
	}

	public DepandNode getDepend() {
		return depend;
	}
}
