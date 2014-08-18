package com.taozeyu.album;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.taozeyu.album.dao.AttributeDao;
import com.taozeyu.album.dao.ImageAttributeDao;
import com.taozeyu.album.dao.ImageTagDao;
import com.taozeyu.album.dao.TagDao;
import com.taozeyu.album.frame.AlbumFrame.ImageSource;
import com.taozeyu.album.frame.TagEditorFrame;

public class TagEditorLogic {

	private final TagEditorFrame frame = new TagEditorFrame();
	
	public void changeVisiable() {
		frame.setVisible(!frame.isVisible());
	}
	
	public void changeImageSource(ImageSource imageSource) {
		
		frame.clearitems();
		long imageID = imageSource.getImageID();
		try {
			List<ImageAttributeDao> imageAttrList = findAttributes(imageID);
			chooseAndSetAttributes(imageAttrList);
			
			changeVisiable();
			changeVisiable();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void onNotCareAttribute(AttributeDao attrBean) {
		
	}
	
	public void onSubmitTags(AttributeDao attrBean, List<Long> tagIDList) {
		
	}
	
	private List<ImageAttributeDao> findAttributes(long imageID) throws SQLException {
		
		List<ImageAttributeDao> list = ImageAttributeDao.manager.findAll(
				new LinkedList<ImageAttributeDao>(), "imageID = ?", imageID);

		ConfigLoader config = AlbumManager.instance().getConfigLoader();
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
		Set<Long> allVisiableAttrIDs = config.listAllAttributeVisiableIDS();
		for(ImageAttributeDao imageAttr:list) {
			allVisiableAttrIDs.remove(imageAttr.getAttributeID());
		}
		for(long attrID:allVisiableAttrIDs) {
			ImageAttributeDao imageAttr = ImageAttributeDao.manager.create();
			imageAttr.setAttributeID(attrID);
			imageAttr.setImageID(imageID);
			imageAttr.setState(0);
			imageAttr.save();
			list.add(imageAttr);
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
				int c = o1.getImportance() - o2.getImportance();
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
				new LinkedList<TagDao>(), "attributeID = ? AND hide = 0", attr.getId());
		frame.addItem(attr, tagsList);
	}
}