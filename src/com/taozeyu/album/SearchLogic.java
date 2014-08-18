package com.taozeyu.album;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

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
	
	private final SearchFrame frame;

	private final ComboBoxModel<String> searchItemList;
	
	private boolean hasInitFrame = false;
	private final List<SearchAttributeView> viewList = new LinkedList<SearchAttributeView>();
	
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
		viewList.clear();
		
		JPanel panel = frame.getSearchViewPanel();
		panel.removeAll();
		
		int width = 490; //Swing是SB不想解释。。。
		
		panel.setLayout(null);
		HashMap<Long, SearchAttributeView> idmap = new HashMap<Long, SearchAttributeView>();
		
		int startY = 0;
		
		for(AttributeDao bean:list) {
			SearchAttributeView view = translateFrom(bean, idmap);
			if(view != null) {
				int height = countRealHeight(view, width);
				view.setLocation(0, startY);
				view.setSize(width, height);
				panel.add(view);
				viewList.add(view);
				panel.setPreferredSize(new Dimension(0, 0));
				idmap.put(bean.getId(), view);
				startY += height;
			}
		}
		panel.setPreferredSize(new Dimension(width, startY));
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
			String condition = "id IN (SELECT imageID FROM imageTags WHERE tagID NOT IN (?))";
			
			if(!mustSet.isEmpty()) {
				condition += " AND id NOT IN (SELECT imageID FROM imageTags WHERE tagID IN (?))";
			}
			List<ImageDao> imageList;
			
			if(mustSet.isEmpty()) {
				imageList = ImageDao.manager.findAll(new ArrayList<ImageDao>(), condition, excludeSet);
			} else {
				imageList = ImageDao.manager.findAll(new ArrayList<ImageDao>(), condition, excludeSet, mustSet);
			}
			
			if(imageList.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "找不到任何符合条件的结果！", "信息", JOptionPane.PLAIN_MESSAGE);
			}else {
				showImages(imageList);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, e.getMessage(), "搜索出错啦！", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void showImages(List<ImageDao> imageList) {
		
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
		frame.setVisible(false);
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
