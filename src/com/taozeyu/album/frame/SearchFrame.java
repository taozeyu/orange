package com.taozeyu.album.frame;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;

import org.dyno.visual.swing.layouts.Constraints;
import org.dyno.visual.swing.layouts.GroupLayout;
import org.dyno.visual.swing.layouts.Leading;

//VS4E -- DO NOT REMOVE THIS LINE!
public class SearchFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JButton btnSaveCondition;
	private JButton btnLoadFromDir;
	private JButton btnLoadFromFile;
	private JButton btnSearch;
	private JComboBox<String> cbSearchCondition;
	
	private final ComboBoxModel<String> searchItemList;
	private final ListCellRenderer<SearchAttributeView> listCellRenderer;
	private JScrollPane jScrollPane0;
	private JPanel searchViewPanel;
	private JLabel jLabel0;
	private JTextField jtfFilePathFilter;
	private JCheckBox jcbFilter;
	public SearchFrame(
			ComboBoxModel<String> searchItemList,
			ListCellRenderer<SearchAttributeView> listCellRenderer
	) {
		this.searchItemList = searchItemList;
		this.listCellRenderer = listCellRenderer;
		initComponents();
	}
	
	public SearchFrame() {
		this(new DefaultComboBoxModel<String>(), null);
	}

	private void initComponents() {
		setTitle("橘子相册 - 搜索");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setLayout(new GroupLayout());
		add(getBtnSaveCondition(), new Constraints(new Leading(401, 12, 12), new Leading(12, 24, 12, 12)));
		add(getCbSearchCondition(), new Constraints(new Leading(13, 376, 12, 12), new Leading(9, 12, 12)));
		add(getBtnLoadFromFile(), new Constraints(new Leading(150, 10, 10), new Leading(469, 10, 10)));
		add(getBtnLoadFromDir(), new Constraints(new Leading(16, 122, 12, 12), new Leading(469, 12, 12)));
		add(getBtnSearch(), new Constraints(new Leading(430, 83, 12, 12), new Leading(471, 10, 10)));
		add(getJLabel0(), new Constraints(new Leading(13, 12, 12), new Leading(42, 12, 12)));
		add(getJScrollPane0(), new Constraints(new Leading(13, 500, 12, 12), new Leading(91, 372, 12, 12)));
		add(getJcbFilter(), new Constraints(new Leading(460, 53, 10, 10), new Leading(61, 12, 12)));
		add(getJtfFilePathFilter(), new Constraints(new Leading(13, 441, 12, 12), new Leading(63, 12, 12)));
		setSize(532, 539);
	}

	public JCheckBox getJcbFilter() {
		if (jcbFilter == null) {
			jcbFilter = new JCheckBox();
			jcbFilter.setText("排除");
		}
		return jcbFilter;
	}

	public JTextField getJtfFilePathFilter() {
		if (jtfFilePathFilter == null) {
			jtfFilePathFilter = new JTextField();
			jtfFilePathFilter.setText("");
		}
		return jtfFilePathFilter;
	}

	private JLabel getJLabel0() {
		if (jLabel0 == null) {
			jLabel0 = new JLabel();
			jLabel0.setText("文件地址过滤（正则表达式）");
		}
		return jLabel0;
	}

	public JPanel getSearchViewPanel() {
		if (searchViewPanel == null) {
			searchViewPanel = new JPanel();
		}
		return searchViewPanel;
	}

	public JScrollPane getJScrollPane0() {
		if (jScrollPane0 == null) {
			jScrollPane0 = new JScrollPane();
			jScrollPane0.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPane0.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane0.setViewportView(getSearchViewPanel());
			
			jScrollPane0.getVerticalScrollBar().setUnitIncrement(27);
		}
		return jScrollPane0;
	}

	public JButton getBtnSaveCondition() {
		if (btnSaveCondition == null) {
			btnSaveCondition = new JButton();
			btnSaveCondition.setText("保存搜索条件");
		}
		return btnSaveCondition;
	}

	public JComboBox<String> getCbSearchCondition() {
		if (cbSearchCondition == null) {
			cbSearchCondition = new JComboBox<String>();
			cbSearchCondition.setModel(this.searchItemList);
			cbSearchCondition.setDoubleBuffered(false);
			cbSearchCondition.setBorder(null);
		}
		return cbSearchCondition;
	}

	public JButton getBtnSearch() {
		if (btnSearch == null) {
			btnSearch = new JButton();
			btnSearch.setText("搜索");
		}
		return btnSearch;
	}

	public JButton getBtnLoadFromFile() {
		if (btnLoadFromFile == null) {
			btnLoadFromFile = new JButton();
			btnLoadFromFile.setText("图片文件导入");
		}
		return btnLoadFromFile;
	}

	public JButton getBtnLoadFromDir() {
		if (btnLoadFromDir == null) {
			btnLoadFromDir = new JButton();
			btnLoadFromDir.setText("图片目录导入");
		}
		return btnLoadFromDir;
	}

}
