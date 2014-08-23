package com.taozeyu.album;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.JSONArray;
import org.json.JSONObject;

import com.taozeyu.album.dao.AttributeDao;
import com.taozeyu.album.dao.ImageDao;
import com.taozeyu.album.dao.TagDao;
import com.taozeyu.album.frame.AlbumFrame;
import com.taozeyu.album.frame.SearchAttributeView;
import com.taozeyu.album.frame.SearchAttributeView.DepandNode;
import com.taozeyu.album.frame.SearchAttributeView.TagNode;
import com.taozeyu.album.frame.SearchAttributeView.TagState;
import com.taozeyu.album.frame.SearchFrame;

public class SearchLogic {
	
	private static final String DefaultName = "(未命名搜索条件)";
	
	private final SearchFrame frame;

	private final ComboBoxModel<String> searchItemList;
	private int currSearchIndex = -1;
	private boolean currSearchChanged = false;
	
	private boolean hasInitFrame = false;
	private final List<SearchAttributeView> viewList = new LinkedList<SearchAttributeView>();
	
	private final SearchParameterSaver searchParameterSaver;
	
	public SearchLogic() throws SQLException, IOException {
		this.searchParameterSaver = new SearchParameterSaver();
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
	
	private void initParams() throws IOException {
		searchParameterSaver.initFromFile();
		JComboBox<String> cb = frame.getCbSearchCondition();
		List<String> namesList = searchParameterSaver.getNamesList(DefaultName);
		
		namesList.add(0, DefaultName);
		searchParameterSaver.put(DefaultName, searchParameterSaver.getEmpty());
		
		cb.setModel(new DefaultComboBoxModel<String>(new Vector<String>(namesList)));
		cb.setSelectedIndex(0);
		searchParameterComboBoxChange(0);
	}
	
	private void searchParameterComboBoxChange(int index) {
		
		if(currSearchIndex == index) {
			return;
		}
		
		if(currSearchChanged) {
			searchParameterSaver.put(getName(currSearchIndex), readObjFromCurrSearchPanel());
		}
		currSearchChanged = false;
		currSearchIndex = index;
		
		JSONObject obj;
		if(index == 0) {
			obj = searchParameterSaver.get(DefaultName);
		}else {
			String name = frame.getCbSearchCondition().getItemAt(index);
			obj = searchParameterSaver.get(name);
		}
		setCurrSearchPanelWithObj(obj);
	}
	
	private void currConditionChanged() {
		currSearchChanged = true;
	}
	
	private void clickSaveButton() {

		try {
			if (currSearchIndex == 0) {
				
				String name;
				
				while(true) {
					
					name = JOptionPane.showInputDialog(frame, "请输入保存的名字");
					if(name == null) {
						return;
					}
					if(searchParameterSaver.get(name) == null) {
						break;
					}
					int code = JOptionPane.showConfirmDialog(
							frame, "\"" + name + "\"已存在，是否覆盖？",
							"保存", JOptionPane.YES_NO_CANCEL_OPTION);
					
					if(code == JOptionPane.YES_OPTION) {
						break;
					} else if(code == JOptionPane.NO_OPTION) {
						continue;
					} else if(code == JOptionPane.CANCEL_OPTION) {
						return;
					}
				}
				searchParameterSaver.put(name, readObjFromCurrSearchPanel());
				searchParameterSaver.save(name);
				
				List<String> namesList = searchParameterSaver.getNamesList(DefaultName);
				namesList.add(0, DefaultName);
				
				frame.getCbSearchCondition().setModel(
						new DefaultComboBoxModel<String>(new Vector<String>(namesList))
				);
				for(int i=0; i<namesList.size(); ++i) {
					if(namesList.get(i).equals(name)) {
						frame.getCbSearchCondition().setSelectedIndex(i);
						searchParameterComboBoxChange(i);
						break;
					}
				}
				
			} else {

				String name = getName(currSearchIndex);
				if (currSearchChanged) {
					searchParameterSaver
							.put(name, readObjFromCurrSearchPanel());
				}
				searchParameterSaver.save(name);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "保存时出错 "+ e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
		JOptionPane.showMessageDialog(frame, "保存成功！");
	}

	private JSONObject readObjFromCurrSearchPanel() {
		
		JSONObject obj = new JSONObject();
		obj.put("filter", frame.getJcbFilter().isSelected());
		obj.put("filter_regx", frame.getJtfFilePathFilter().getText());
		
		LinkedList<Long> mustList = new LinkedList<Long>();
		LinkedList<Long> excludeList = new LinkedList<Long>();
		
		for(SearchAttributeView view:viewList) {
			for(TagNode node:view.getTags()) {
				TagState state = node.getState();
				if(state == TagState.Exclude) {
					excludeList.add(node.getId());
				} else if (state == TagState.Must) {
					mustList.add(node.getId());
				}
			}
		}
		obj.put("must", mustList);
		obj.put("exclude", excludeList);
		
		return obj;
	}
	
	private void setCurrSearchPanelWithObj(JSONObject obj) {
		frame.getJcbFilter().setSelected(obj.getBoolean("filter"));
		frame.getJtfFilePathFilter().setText(obj.getString("filter_regx"));
		
		LinkedList<Long> mustList = toList(obj.getJSONArray("must"));
		LinkedList<Long> excludeList = toList(obj.getJSONArray("exclude"));
		
		for(SearchAttributeView view:viewList) {
			view.setTagState(excludeList, mustList);
		}
	}
	
	private LinkedList<Long> toList(JSONArray arr) {
		LinkedList<Long> list = new LinkedList<Long>();
		for(int i=0; i<arr.length(); ++i) {
			list.add(arr.getLong(i));
		}
		return list;
	}
	
	private String getName(int index) {
		if(index == 0) {
			return DefaultName;
		}
		return searchParameterSaver.getNamesList(DefaultName).get(index - 1);
	}
	
	private void initPanel() throws SQLException {
		
		List<AttributeDao> list = AttributeDao.manager.findAll(
				new LinkedList<AttributeDao>(), "hide = 0", "id"
		);
		viewList.clear();
		
		JPanel panel = frame.getSearchViewPanel();
		panel.removeAll();
		
		int width = 490; //Swing是SB不想解释。。。
		
		panel.setLayout(null);
		HashMap<Long, SearchAttributeView> idmap = new HashMap<Long, SearchAttributeView>();
		
		int startY = 0;
		
		TreeSet<SearchAttributeView> sortSet = new TreeSet<SearchAttributeView>(new Comparator<SearchAttributeView>() {

			@Override
			public int compare(SearchAttributeView sav1, SearchAttributeView sav2) {
				int depth1 = getAttributeViewDepth(sav1);
				int depth2 = getAttributeViewDepth(sav2);
				if(depth1 != depth2) {
					return depth1 - depth2;
				}
				int importance1 = sav1.getImportance();
				int importance2 = sav2.getImportance();
				if(importance1 != importance2) {
					return importance1 - importance2;
				}
				int comp = sav1.getAttributeName().compareTo(sav2.getAttributeName());
				if(comp != 0) {
					return comp;
				}
				return sav1.hashCode() - sav2.hashCode();
			}
		});
		
		for(AttributeDao bean:list) {
			SearchAttributeView view = translateFrom(bean, idmap);
			if(view != null) {
				int height = countRealHeight(view, width);
				view.setLocation(0, startY);
				view.setSize(width, height);
				sortSet.add(view);
				panel.setPreferredSize(new Dimension(0, 0));
				idmap.put(bean.getId(), view);
				startY += height;
				
				view.onStateChanged = new Runnable() {
					@Override
					public void run() {
						currConditionChanged();
					}
				};
			}
		}
		for(SearchAttributeView view:sortSet) {
			panel.add(view);
			viewList.add(view);
		}
		panel.setPreferredSize(new Dimension(width, startY));

		final JComboBox<String> cb = frame.getCbSearchCondition();
		cb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				searchParameterComboBoxChange(cb.getSelectedIndex());
			}
		});
		frame.getBtnSaveCondition().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				clickSaveButton();
			}
		});
		frame.getJcbFilter().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				currConditionChanged();
			}
		});
		frame.getJtfFilePathFilter().addInputMethodListener(new InputMethodListener() {
			
			@Override
			public void inputMethodTextChanged(InputMethodEvent event) {
				currConditionChanged();
			}
			
			@Override
			public void caretPositionChanged(InputMethodEvent event) { }
		});
	}
	
	private int getAttributeViewDepth(SearchAttributeView view) {
		int depth = 0;
		DepandNode depend;
		while((depend = view.getDepend()) != null) {
			view = depend.getView();
			depth++;
		}
		return depth;
	}
	
	private int countRealHeight(SearchAttributeView view, int width) {
		Dimension size = view.getMinimumSize();
		int rows = size.width / width;
		if(size.width % width != 0) {
			rows += 1;
		}
		return rows * size.height;
	}
	
	private SearchAttributeView translateFrom(AttributeDao bean, Map<Long, SearchAttributeView> idmap) throws SQLException {
		
		List<TagDao> tagsList = TagDao.manager
				.findAll(new LinkedList<TagDao>(), "attributeID = ? AND hide = 0", bean.getId());
		
		TreeSet<TagDao> sortSet = new TreeSet<TagDao>(new Comparator<TagDao>() {

			@Override
			public int compare(TagDao tag1, TagDao tag2) {
				int comp = tag1.getName().compareTo(tag2.getName());
				if(comp == 0) {
					comp = (int) (tag1.getId() - tag2.getId());
				}
				return comp;
			}
		});
		sortSet.addAll(tagsList);
		
		Iterator<TagDao> tagsIterator = sortSet.iterator();
		TagNode[] tags = new TagNode[sortSet.size()];
		
		for(int i=0; i<tags.length; ++i) {
			TagDao tag = tagsIterator.next();
			String name = tag.getName().replaceAll("^\\(\\d+\\)", "");//去掉(13)这样的序列
			tags[i] = new TagNode(tag.getId(), name, tag.getInfo());
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
	
	public void setVisiable(boolean value) throws SQLException, IOException {
		if (value) {
			if(!hasInitFrame) {
				initFrame();
				initPanel();
				initParams();
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
	
	public boolean isVisable(){
		return frame.isVisible();
	}
	
	private void onClickLoadFromDir() {
		File root = getFileFromUser();
		if(root == null) {
			return;
		}
		if(!root.isDirectory()) {
			JOptionPane.showMessageDialog(frame, "必须输入文件夹地址", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int fileNum = searchFile(root);
		try {
			AlbumManager.instance().getDbManager().commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(frame, "一共载入"+fileNum+"个文件", "信息", JOptionPane.PLAIN_MESSAGE);
	}
	
	private void onClickLoadFromFile() {
		File file = getFileFromUser();
		if(file == null) {
			return;
		}
		if(file.isDirectory()) {
			JOptionPane.showMessageDialog(frame, "必须输入文件", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(!file.exists()) {
			JOptionPane.showMessageDialog(frame, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(!isImageFile(file)) {
			JOptionPane.showMessageDialog(frame, "请输入图片地址", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	private void onClickSaveCondition() {
		
	}
	
	private File getFileFromUser() {
		String path = JOptionPane.showInputDialog("请输入路径地址");
		if(path == null) {
			return null;
		}
		File file = new File(path);
		if(!file.exists()) {
			JOptionPane.showMessageDialog(frame, "地址对应的文件夹不存在", "错误", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return file;
	}
	
	private int searchFile(File dir) {
		int count = 0;
		for(File file:dir.listFiles()) {
			if(file.isDirectory()) {
				count += searchFile(file);
			} else if(isImageFile(file)){
				count += loadImageFile(file)? 1:0;
			}
		}
		return count;
	}
	
	private static final Pattern ImageFileNameRegx = Pattern.compile(
			".+\\.(png|jpg|jpeg)$"
	);
	
	private boolean isImageFile(File file) {
		return ImageFileNameRegx.matcher(file.getName()).matches();
	}
	
	private boolean loadImageFile(File file) {
		
		try {
			if(ImageDao.manager.find("filePath = ?", file.getPath()) != null) {
				System.out.println("图片已存在，忽略 " + file.getPath());
				return false;
			}
			ImageDao imageDao = ImageDao.manager.create();
			imageDao.setFilePath(file.getPath());
			imageDao.setWatchCount(0);
			imageDao.setWatchTime(0);
			imageDao.save();
			return true;
			
		} catch (SQLException e) {
			System.err.println(e);
			return false;
		}
	}
	
	private void onClickSearch() {
		
		HashSet<Long> excludeSet = new HashSet<Long>();
		HashSet<Long> mustSet = new HashSet<Long>();
		
		for(SearchAttributeView view: viewList) {
			if(!view.isEnabled()) {
				continue;
			}
			for(TagNode tag:view.getTags()) {
				TagState state = tag.getState();
				
				if(state == TagState.Exclude) {
					excludeSet.add(tag.getId());
				} else if (state == TagState.Must) {
					mustSet.add(tag.getId());
				}
			}
		}
		try {
			List<ImageDao> imageList;
			
			if(mustSet.isEmpty() && excludeSet.isEmpty()) {
				imageList = ImageDao.manager.findAll(new ArrayList<ImageDao>());
				
			} else {
				
				LinkedList<Object> argsList = new LinkedList<Object>();
				LinkedList<String> conditionList = new LinkedList<String>();
				
				if(!excludeSet.isEmpty()) {
					argsList.add(excludeSet);
					conditionList.add("id IN (SELECT imageID FROM imageTags WHERE tagID NOT IN (?))");
				}
				if(!mustSet.isEmpty()) {
					argsList.add(mustSet);
					conditionList.add("id IN (SELECT imageID FROM imageTags WHERE tagID IN (?))");
				}
				
				String condition;
				Object[] args = (Object[]) argsList.toArray(new Object[argsList
						.size()]);
				
				if(conditionList.size() > 1) {
					condition = conditionList.get(0) + " AND " + conditionList.get(1);
				} else {
					condition = conditionList.get(0);
				}
				imageList = ImageDao.manager.findAll(new ArrayList<ImageDao>(), condition, args);
			}
			
			if(imageList.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "找不到任何符合条件的结果！", "信息", JOptionPane.PLAIN_MESSAGE);
			}else {
				String filter = frame.getJtfFilePathFilter().getText();
				if(!"".equals(filter)) {
					try {
						Pattern pattern = Pattern.compile(filter);
						Iterator<ImageDao> it = imageList.iterator();
						
						boolean willFilter = frame.getJcbFilter().isSelected();
						
						while(it.hasNext()) {
							ImageDao image = it.next();
							Matcher matcher = pattern.matcher(image.getFilePath());
							if(matcher.find()) {
								if(willFilter) {
									it.remove();
								}
							} else {
								if(!willFilter) {
									it.remove();
								}
							}
						}
						
					} catch (PatternSyntaxException e) {
						JOptionPane.showMessageDialog(
								frame, "文件地址过滤条件不是正则表达式，或语法错误。该过滤条件将不起作用。",
								"错误", JOptionPane.ERROR_MESSAGE
						);
					}
				}
				showImages(imageList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.getMessage(), "搜索出错啦！", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void showImages(List<ImageDao> imageList) throws SQLException, IOException {
		
		//扑克牌算法打乱顺序
		Random rand = new Random();
		int length = imageList.size();
		for(int i=0; i<length; ++i) {
			int targetIdx = i + (int) (((float)(length - i)) * rand.nextFloat());
			ImageDao temp = imageList.get(targetIdx);
			imageList.set(targetIdx, imageList.get(i));
			imageList.set(i, temp);
		}
		AlbumFrame albumFrame = AlbumManager.instance().getAlbumFrame();
		albumFrame.resetImageList(imageList);
		albumFrame.setVisible(true);
		this.setVisiable(false);
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
				AlbumManager albumManager = AlbumManager.instance();
				if(albumManager.getAlbumFrame().isVisible()) {
					try {
						SearchLogic.this.setVisiable(false);
					} catch (Exception e1) {
						throw new RuntimeException(e1);
					}
				} else {
					albumManager.confirmExit(frame);
				}
			}
			@Override
			public void windowIconified(WindowEvent evt) {
				try {
					setVisiable(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
