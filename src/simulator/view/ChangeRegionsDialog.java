package simulator.view;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class ChangeRegionsDialog extends JDialog implements EcoSysObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DefaultComboBoxModel<String> _regionsModel;
	private DefaultComboBoxModel<String> _fromRowModel;
	private DefaultComboBoxModel<String> _toRowModel;
	private DefaultComboBoxModel<String> _fromColModel;
	private DefaultComboBoxModel<String> _toColModel;
	private DefaultTableModel _dataTableModel;
	private Controller _ctrl;
	private List<JSONObject> _regionsInfo;
	private String[] _headers = { "Key", "Value", "Description" };

	ChangeRegionsDialog(Controller ctrl) {
		super((Frame) null, true);
		_ctrl = ctrl;
		initGUI();
		this._ctrl.addObserver(this);
	}

	private void initGUI() {
		setTitle("Change Regions");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);

		JPanel helpTextPanel, tablePanel, comboboxPanel, buttonsPanel;
		helpTextPanel = new JPanel();
		tablePanel = new JPanel();
		comboboxPanel = new JPanel();
		buttonsPanel = new JPanel();
		
		JLabel text_table = new JLabel("<html><h5>Select a region type, the rows/cols interval, and provide values for the parametres in the Value Column"
				+ " (default values are used<br>" + " for parametes with no value).</h5></html>");
		helpTextPanel.add(text_table);
		this.add(helpTextPanel, BorderLayout.PAGE_START);

		_regionsInfo = Main.region_factory.get_info();

		_dataTableModel = new DefaultTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 1;
			}
		};
		_dataTableModel.setColumnIdentifiers(_headers);

		JTable table_model = new JTable(_dataTableModel);
		tablePanel.add(table_model);
		JScrollPane scroll= new JScrollPane(table_model);
		this.add(scroll);

		// _regionsModel es un modelo de combobox que incluye los tipos de regiones
		_regionsModel = new DefaultComboBoxModel<>();
		for (JSONObject regions : this._regionsInfo) {
			if (regions.has("type")) {
				_regionsModel.addElement(regions.getString("type"));
			}
		}

		JComboBox<String> regionsComboBox = new JComboBox<>(_regionsModel);
		comboboxPanel.add(new JLabel("Region type "));
		comboboxPanel.add(regionsComboBox);

		regionsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JSONObject info = _regionsInfo.get(regionsComboBox.getSelectedIndex());
				JSONObject data = info.getJSONObject("data");
				for (String str : data.keySet()) {
					_dataTableModel.addRow(new String[] { str, "", data.getString(str) });
				}
			}
		});

		this._fromRowModel = new DefaultComboBoxModel<>();
		this._toRowModel = new DefaultComboBoxModel<>();
		this._fromColModel = new DefaultComboBoxModel<>();
		this._toColModel = new DefaultComboBoxModel<>();

		JComboBox<String> fromRowModel = new JComboBox<>(_fromRowModel);
		JComboBox<String> toRowModel = new JComboBox<>(_toRowModel);
		JComboBox<String> fromColModel = new JComboBox<>(_fromColModel);
		JComboBox<String> toColModel = new JComboBox<>(_toColModel);

		comboboxPanel.add(new JLabel("Row from/to: "));
		comboboxPanel.add(fromRowModel);
		comboboxPanel.add(toRowModel);
		comboboxPanel.add(new JLabel("Column from/to: "));
		comboboxPanel.add(fromColModel);
		comboboxPanel.add(toColModel);
		//mainPanel.add(comboboxPanel);
		this.add(comboboxPanel, BorderLayout.CENTER);

		JButton cancelButton = new JButton("Cancel");
		JButton okButton = new JButton("OK");

		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setVisible(false);
			}
		});
		buttonsPanel.add(cancelButton);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				//a.
				JSONObject region_data = new JSONObject();
				for (int i = 0; i < _dataTableModel.getRowCount(); i++) {
					
					String value=(String)_dataTableModel.getValueAt(i, 1);
					if(!value.isEmpty()) {
						region_data.put((String) _dataTableModel.getValueAt(i, 0),
							value);
					}
					
				}
				
				//b.
				String region_type=_regionsInfo.get(_regionsModel.getIndexOf(_regionsModel.getSelectedItem())).getString("type");
				
				//c.
				int row_from=Integer.parseInt((String)_fromRowModel.getSelectedItem());
				int row_to=Integer.parseInt((String)_toRowModel.getSelectedItem());
				int col_from=Integer.parseInt((String)_fromColModel.getSelectedItem());
				int col_to=Integer.parseInt((String)_toColModel.getSelectedItem());
				
				//d.
				JSONObject region=new JSONObject();
				region.put("row", new JSONArray(Arrays.asList(row_from,row_to)));
				region.put("col", new JSONArray(Arrays.asList(col_from,col_to)));
				
				JSONObject spec=new JSONObject();
				spec.put("type", region_type);
				spec.put("data", region_data);
				
				region.put("spec", spec);
				
				JSONArray regions=new JSONArray();
				regions.put(region);
				
				JSONObject O=new JSONObject();
				O.put("regions", regions);
				
				_ctrl.set_regions(O);
				//_status=1;
				setVisible(false);
				
			}
		});

		buttonsPanel.add(okButton);
		this.add(buttonsPanel, BorderLayout.PAGE_END);
		setPreferredSize(new Dimension(700, 400));
		pack();
		setResizable(false);
		setVisible(false);
	}

	public void open(Frame parent) {
		setLocation(//
				parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, //
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		this._fromRowModel.removeAllElements();
		this._toRowModel.removeAllElements();
		for (int i = 0; i < map.get_rows(); i++) {
			this._fromRowModel.addElement(Integer.toString(i));
			this._toRowModel.addElement(Integer.toString(i));
		}

		this._fromColModel.removeAllElements();
		this._toColModel.removeAllElements();
		for (int i = 0; i < map.get_cols(); i++) {
			this._fromColModel.addElement(Integer.toString(i));
			this._toColModel.addElement(Integer.toString(i));
		}

	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this._fromRowModel.removeAllElements();
		this._toRowModel.removeAllElements();
		for (int i = 0; i < map.get_rows(); i++) {
			this._fromRowModel.addElement(Integer.toString(i));
			this._toRowModel.addElement(Integer.toString(i));
		}

		this._fromColModel.removeAllElements();
		this._toColModel.removeAllElements();
		for (int i = 0; i < map.get_cols(); i++) {
			this._fromColModel.addElement(Integer.toString(i));
			this._toColModel.addElement(Integer.toString(i));
		}

	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

	}
}