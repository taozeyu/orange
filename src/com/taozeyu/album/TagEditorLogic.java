package com.taozeyu.album;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import com.taozeyu.album.dao.AttributeDao;
import com.taozeyu.album.dao.ImageAttributeDao;
import com.taozeyu.album.dao.ImageTagDao;
import com.taozeyu.album.dao.TagDao;
import com.taozeyu.album.frame.AlbumFrame.ImageSource;
import com.taozeyu.album.frame.TagEditorFrame;

public class TagEditorLogic {

	private TagEditorFrame frame = new TagEditorFrame();
	
	public void changeVisiable() {
		frame.setVisible(!frame.isVisible());
	}
	
	public void setVisiable(boolean value) {
		frame.setVisible(value);
	}
	
	private long currImageID;
	
	public void changeImageSource(ImageSource imageSource) {
		
		boolean lastFrameIsVisable = frame.isVisible();
		Point location = frame.getLocation();
		frame.close();
		frame = new TagEditorFrame();
		frame.setLocation(location);
		frame.setVisible(lastFrameIsVisable);
		
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				setVisiable(false);
			}
		});
		
		currImageID = imageSource.getImageID();
		
		try {
			List<ImageAttributeDao> imageAttrList = findAttributes(currImageID);
			chooseAndSetAttributes(imageAttrList);
			
			AlbumManager.instance().getDbManager().commit();
			
		} catch (SQLException e) {

			JOptionPane.showMessageDialog(frame, "提交失败，抛出异常：" + e.getMessage(),
					"橘子相册出错啦！", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void onNotCareAttribute(AttributeDao attrBean) {
		try {
			ImageAttributeDao imageAttr = ImageAttributeDao.manager.find(
					"imageID = ? AND attributeID = ?", currImageID, attrBean.getId());
			imageAttr.setState(2);
			imageAttr.save();
			AlbumManager.instance().getDbManager().commit();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "提交失败，抛出异常：" + e.getMessage(),
					"橘子相册出错啦！", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void onSubmitTags(AttributeDao attrBean, List<Long> tagIDList) {
		try {
			ImageAttributeDao imageAttr = ImageAttributeDao.manager.find(
					"imageID = ? AND attributeID = ?", currImageID, attrBean.getId());
			imageAttr.setState(1);
			imageAttr.save();
			
			List<ImageTagDao> imageTagList = ImageTagDao.manager.findAll(
					new LinkedList<ImageTagDao>(),
					"tagID in (SELECT id FROM tags WHERE attributeID = ? AND imageID = ?)",
					imageAttr.getAttributeID(), currImageID
			);
			for(ImageTagDao tagBean:imageTagList) {
				tagBean.remove();
			}
			for(long tagID: tagIDList) {
				ImageTagDao imageTag = ImageTagDao.manager.create();
				imageTag.setImageID(currImageID);
				imageTag.setTagID(tagID);
				imageTag.save();
			}
			AlbumManager.instance().getDbManager().commit();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "提交失败，抛出异常：" + e.getMessage(),
					"橘子相册出错啦！", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private List<ImageAttributeDao> findAttributes(long imageID) throws SQLException {
		
		List<ImageAttributeDao> list = ImageAttributeDao.manager.findAll(
				new LinkedList<ImageAttributeDao>(), "imageID = ?", imageID);

		ConfigLoader config = AlbumManager.instance().getConfigLoader();
		
		Set<Long> allVisiableAttrIDs = config.listAllAttributeVisiableIDS();
		
		for(ImageAttributeDao imageAttr:list) {
			allVisiableAttrIDs.remove(imageAttr.getAttributeID());
		}
		for(long attrID:allVisiableAttrIDs) {
			ImageAttributeDao imageAttr = ImageAttributeDao.manager.create();
			imageAttr.setAttributeID(attrID);
			imageAttr.setImageID(imageID);
			imageAttr.setState(0);
			list.add(imageAttr);
			imageAttr.save();
		}
		
		Iterator<ImageAttributeDao> it = list.iterator();
		
		while(it.hasNext()) {
			ImageAttributeDao imageAttr = it.next();
			
			if(!config.isAttributeVisiable(imageAttr.getAttributeID())) {
				it.remove();
				
			} else if(imageAttr.getState() != 0) {
				it.remove();
				
			} else {
				
				AttributeDao attr = AttributeDao.manager.findById(imageAttr.getAttributeID());
				Long dependTagID = attr.getDependTagID();
				
				if(dependTagID != null) {
					if(null == ImageTagDao.manager.find("tagID = ? AND imageID = ?", dependTagID, imageID)) {
						it.remove();
					}
				}
			}
		}
		return list;
	}
	
	private void chooseAndSetAttributes(List<ImageAttributeDao> list) throws SQLException {
		
		LinkedList<Long> attrIDsList = new LinkedList<Long>();
		
		for(ImageAttributeDao imageAttr:list) {
			attrIDsList.add(imageAttr.getAttributeID());
		}
		
		List<AttributeDao> attrList = AttributeDao.manager.findAll(
				new LinkedList<AttributeDao>(), "id IN (?)", attrIDsList);
		
		TreeSet<AttributeDao> sortSet = new TreeSet<AttributeDao>(new Comparator<AttributeDao>() {
			@Override
			public int compare(AttributeDao o1, AttributeDao o2) {
				int c = o2.getImportance() - o1.getImportance();
				if(c == 0) {
					return (int) (o1.getId() - o2.getId());
				}
				return c;
			}
		});
		sortSet.addAll(attrList);
		
		Iterator<AttributeDao> sortIt = sortSet.iterator();
		
		for(int i=0; i<AlbumManager.TagEditorShowItemsNum; ++i) {
			if(!sortIt.hasNext()) {
				break;
			}
			AttributeDao attr = sortIt.next();
			chooseImageAttribute(attr);
		}
	}
	
	private void chooseImageAttribute(AttributeDao attr) throws SQLException {
		
		List<TagDao> tagsList = TagDao.manager.findAll(
				new LinkedList<TagDao>(), "attributeID = ? AND hide = 0", "name, id", attr.getId());
		
		for(TagDao tag:tagsList) {
			String name = tag.getName();
			name = name.replaceAll("^\\(\\d+\\)", ""); //删除诸如(12)这样的序号
		}
		frame.addItem(attr, tagsList);
	}
}
