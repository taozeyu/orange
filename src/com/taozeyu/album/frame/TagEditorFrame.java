package com.taozeyu.album.frame;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.taozeyu.album.AlbumManager;
import com.taozeyu.album.dao.AttributeDao;
import com.taozeyu.album.dao.TagDao;

public class TagEditorFrame extends JFrame {

	private static final int Width = 210, Height = 420;
	private static final int ButtonWidth = 50, ButtonHeight = 45;
	
	private static final long serialVersionUID = 6647796932230894447L;

	private final JPanel container;
	private int startYLocation = 0;
	
	public TagEditorFrame() {
		setTitle("图片标签顺手填 - 按 F5 开启/隐藏本窗口");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(true);
		setResizable(false);
		setSize(Width + 10, Height + 10);
		setLocation(0, 0);
		
		JScrollPane scrollPane = new JScrollPane();
		container = new JPanel();
		container.setLayout(null);
		
		scrollPane.getVerticalScrollBar().setUnitIncrement(27);
		
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(container);
		
		add(scrollPane);
	}
	
	public void clearitems() {
		startYLocation = 0;
		container.removeAll();
	}
	
	public void addItem(AttributeDao attrBean, List<TagDao> tagsList) {
		JPanel panel = getItemPanel(attrBean, tagsList);
		panel.setLocation(0, startYLocation);
		startYLocation += panel.getHeight() + 15;
		container.add(panel);
		container.setSize(new Dimension(Width, startYLocation));
		container.setPreferredSize(new Dimension(Width, startYLocation));
	}
	
	private JPanel getItemPanel(final AttributeDao attrBean, List<TagDao> tagsList) {
		JPanel itemPanel = new JPanel();
		itemPanel.setLayout(new GridLayout(tagsList.size() + 2, 1));

		itemPanel.setToolTipText(attrBean.getInfo());
		itemPanel.setBorder(BorderFactory.createTitledBorder(attrBean.getName()));
		
		final TagDao[] tagsArray = (TagDao[]) tagsList.toArray(new TagDao[tagsList.size()]);
		final JCheckBox[] jbcArray = new JCheckBox[tagsArray.length];
		
		for(int i=0; i<tagsArray.length; ++i) {
			TagDao tagBean = tagsArray[i];
			JPanel childPanel = new JPanel();
			JCheckBox jcb = new JCheckBox(" " + tagBean.getName() + "  ");
			jcb.setToolTipText(tagBean.getInfo());
			childPanel.add(jcb);
			itemPanel.add(childPanel);
			jbcArray[i] = jcb;
		}
		JButton btnNotCare = new JButton("忽略");
		JButton btnSubmit = new JButton("提交");
		
		btnNotCare.setSize(ButtonWidth, ButtonHeight);
		btnSubmit.setSize(ButtonWidth, ButtonHeight);
		
		itemPanel.add(btnNotCare);
		itemPanel.add(btnSubmit);
		
		final Runnable closePanel = new Runnable() {
			@Override
			public void run() {
				btnNotCare.setEnabled(false);
				btnSubmit.setEnabled(false);
				
				for(JCheckBox jcb : jbcArray) {
					jcb.setEnabled(false);
				}
			}
		};
		btnNotCare.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				AlbumManager.instance().getTagEditorLogic().onNotCareAttribute(attrBean);
				closePanel.run();
			}
		});
		btnSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				LinkedList<Long> tagIDList = new LinkedList<Long>();
				for(int i=0; i<tagsArray.length; ++i) {
					JCheckBox jcb = jbcArray[i];
					TagDao tagBean = tagsArray[i];
					if(jcb.isSelected()) {
						tagIDList.add(tagBean.getId());
					}
				}
				AlbumManager.instance().getTagEditorLogic().onSubmitTags(attrBean, tagIDList);
				closePanel.run();
			}
		});
		int height = (tagsList.size() + 2) * ButtonHeight;
		
		itemPanel.setSize(new Dimension(Width, height));
		itemPanel.setPreferredSize(new Dimension(Width, height));
		
		return itemPanel;
	}
}
