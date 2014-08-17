package com.taozeyu.album.frame;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;

//VS4E -- DO NOT REMOVE THIS LINE!
public class SearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton btnSaveCondition;
	private JList lstConditionContent;
	private JScrollPane jScrollPane0;
	private JButton jButton1;
	private JButton jButton2;
	private JButton jButton3;
	private JComboBox cbSearchCondition;
	public SearchFrame() {
		initComponents();
	}

	private void initComponents() {
		setTitle("橘子相册 - 搜索");
		setResizable(false);
		setAlwaysOnTop(true);
		setLayout(new GroupLayout());
		add(getBtnSaveCondition(), new Constraints(new Leading(401, 12, 12), new Leading(12, 24, 12, 12)));
		add(getJScrollPane0(), new Constraints(new Leading(13, 500, 12, 12), new Leading(43, 358, 10, 10)));
		add(getJButton1(), new Constraints(new Leading(13, 122, 10, 10), new Leading(409, 12, 12)));
		add(getjButton2(), new Constraints(new Leading(149, 10, 10), new Leading(409, 12, 12)));
		add(getJButton3(), new Constraints(new Leading(430, 83, 12, 12), new Leading(409, 12, 12)));
		add(getJComboBox0(), new Constraints(new Leading(13, 376, 12, 12), new Leading(9, 12, 12)));
		setSize(526, 447);
	}

	private JComboBox getJComboBox0() {
		if (cbSearchCondition == null) {
			cbSearchCondition = new JComboBox();
			cbSearchCondition.setModel(new DefaultComboBoxModel(new Object[] { "item0" }));
			cbSearchCondition.setDoubleBuffered(false);
			cbSearchCondition.setBorder(null);
		}
		return cbSearchCondition;
	}

	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("搜索");
		}
		return jButton3;
	}

	private JButton getjButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setText("图片文件导入");
		}
		return jButton2;
	}

	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("图片目录导入");
		}
		return jButton1;
	}

	private JScrollPane getJScrollPane0() {
		if (jScrollPane0 == null) {
			jScrollPane0 = new JScrollPane();
			jScrollPane0.setViewportView(getJList0());
		}
		return jScrollPane0;
	}

	private JList getJList0() {
		if (lstConditionContent == null) {
			lstConditionContent = new JList();
			DefaultListModel listModel = new DefaultListModel();
			listModel.addElement("item0");
			lstConditionContent.setModel(listModel);
		}
		return lstConditionContent;
	}

	private JButton getBtnSaveCondition() {
		if (btnSaveCondition == null) {
			btnSaveCondition = new JButton();
			btnSaveCondition.setText("保存搜索条件");
		}
		return btnSaveCondition;
	}

}
