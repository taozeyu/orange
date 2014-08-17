package com.taozeyu.album;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.taozeyu.album.dao.AttributeDao;
import com.taozeyu.album.dao.TagDao;
import com.taozeyu.album.frame.SearchAttributeView;
import com.taozeyu.album.frame.SearchAttributeView.DepandNode;
import com.taozeyu.album.frame.SearchAttributeView.TagNode;
import com.taozeyu.album.frame.SearchFrame;

public class SearchLogic {
	
	private final SearchFrame frame;

	private final ComboBoxModel<String> searchItemList;
	
	private boolean hasInitFrame = false;
	
	public SearchLogic() throws SQLException {
		this.searchItemList = new DefaultComboBoxModel<String>();
		this.frame = new SearchFrame(
				searchItemList,
				new ListCellRenderer<SearchAttributeView>() {

					@Override
					public Component getListCellRendererComponent(
							JList<? extends SearchAttributeView> list,
							SearchAttributeView value, int index,
							boolean isSelected, boolean cellHasFocus) {
						
						return value;
					}
				}
		);
	}
	
	private void initPanel() throws SQLException {
		
		List<AttributeDao> list = AttributeDao.manager.findAll(
				new LinkedList<AttributeDao>(), "hide = 0", "id"
		);
		JPanel panel = frame.getSearchViewPanel();
		panel.removeAll();
		
		HashMap<Long, SearchAttributeView> idmap = new HashMap<Long, SearchAttributeView>();
		
		for(AttributeDao bean:list) {
			SearchAttributeView view = translateFrom(bean, idmap);
			if(view != null) {
				panel.add(view);
				idmap.put(bean.getId(), view);
			}
		}
		panel.repaint();
	}
	
	private SearchAttributeView translateFrom(AttributeDao bean, Map<Long, SearchAttributeView> idmap) throws SQLException {
		
		List<TagDao> tagsList = TagDao.manager
				.findAll(new LinkedList<TagDao>(), "attributeID = ? AND hide = 0", bean.getId());
		Iterator<TagDao> tagsIterator = tagsList.iterator();
		TagNode[] tags = new TagNode[tagsList.size()];
		
		for(int i=0; i<tags.length; ++i) {
			TagDao tag = tagsIterator.next();
			tags[i] = new TagNode(tag.getId(), tag.getName(), tag.getInfo());
		}
		DepandNode dependNode = null;
		
		if(bean.getDependTagID() != null) {
			ConfigLoader config = AlbumManager.instance().getConfigLoader();
			long depnedID = bean.getDependTagID();
			TagDao tagBean = TagDao.manager.findById(depnedID);
			if(!config.isTagVisiable(tagBean.getId()) || !config.isAttributeVisiable(tagBean.getAttributeID())) {
				return null;
			}
			SearchAttributeView view = idmap.get(tagBean.getAttributeID());
			TagNode tag = null;
			for(TagNode t:view.getTags()) {
				if(t.getId() == tagBean.getId()) {
					tag = t;
					break;
				}
			}
			if(tag == null) {
				throw new NullPointerException("tag");
			}
			dependNode = new DepandNode(view, tag);
		}
		return new SearchAttributeView(
				bean.getId(), bean.getName(), bean.getInfo(), bean.getImportance(), tags, dependNode
		);
	}
	
	public void configReload() throws SQLException {
		initPanel();
	}
	
	public void setVisiable(boolean value) throws SQLException {
		if (value) {
			if(!hasInitFrame) {
				initFrame();
				initPanel();
				hasInitFrame = true;
			}
			Toolkit kit = frame.getToolkit();
			Dimension winSize = kit.getScreenSize();
			frame.setLocation(
					(winSize.width - frame.getWidth()) / 2,
					(winSize.height - frame.getHeight()) / 2
			);
		}
		frame.setVisible(value);
	}
	
	private void onClickLoadFromDir() {
		
	}
	
	private void onClickLoadFromFile() {
		
	}
	
	private void onClickSaveCondition() {
		
	}
	
	private void onClickSearch() {
		
	}
	
	private void initFrame() {
		
		frame.getBtnLoadFromDir().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClickLoadFromDir();
			}
		});
		frame.getBtnLoadFromFile().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClickLoadFromFile();
			}
		});
		frame.getBtnSaveCondition().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClickSaveCondition();
			}
		});
		frame.getBtnSearch().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClickSearch();
			}
		});
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int rsCode = JOptionPane.showConfirmDialog(frame, "确定要退出橘子相册吗？", "退出确认", JOptionPane.OK_CANCEL_OPTION);
				if(rsCode == JOptionPane.OK_OPTION) {
					AlbumManager.instance().dispose();
				}
			}
			@Override
			public void windowIconified(WindowEvent evt) {
				try {
					setVisiable(false);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
