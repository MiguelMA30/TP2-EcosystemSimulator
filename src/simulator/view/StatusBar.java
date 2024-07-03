package simulator.view;

import java.awt.Dimension;

import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

class StatusBar extends JPanel implements EcoSysObserver {

	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	private JLabel label_time;
	private JLabel label_num_animals;
	private JLabel label_dim_animals;
	private Controller _ctrl;

	StatusBar(Controller ctrl) {
		this._ctrl = ctrl;
		initGUI();
		this._ctrl.addObserver(this);
	}

	private void initGUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createBevelBorder(20));
		
		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));

		this.label_time = new JLabel("Time: 0.000");
		this.add(label_time);

		this.add(s);

		this.label_num_animals = new JLabel("Total Animals: 0");
		this.add(label_num_animals);

		

		this.label_dim_animals = new JLabel("Dimension: 0x0 0x0");
		this.add(label_dim_animals);
		
		

	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		this.label_time.setText("Time: " + time);
		this.label_num_animals.setText("Total Animals: " + animals.size());
		this.label_dim_animals.setText("Dimension: " + map.get_width() + "x" + map.get_height() + " " + map.get_cols() + "x" + map.get_rows());
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		this.label_time.setText("Time: " + time);
		this.label_num_animals.setText("Total Animals: " + animals.size());
		this.label_dim_animals.setText("Dimension: " + map.get_width() + "x" + map.get_height() + " " + map.get_cols() + "x" + map.get_rows());
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		this.label_time.setText("Time: " + time);
		this.label_num_animals.setText("Total Animals: " + animals.size());
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		this.label_time.setText("Time: " + time);
		this.label_num_animals.setText("Total Animals: " + animals.size());
	}
}
