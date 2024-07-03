package simulator.view;

import java.awt.BorderLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.launcher.Main;

class ControlPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;
	private ChangeRegionsDialog _changeRegionsDialog;
	private JToolBar _toolaBar;
	private JFileChooser _fc;
	private JSpinner spinner;
	private JTextField _deltaTime;
	private boolean _stopped = true;

	private JButton _fileChooserButton;
	private JButton _mapViewButton;
	private JButton _regionsButton;
	private JButton _runButton;
	private JButton _stopButton;
	private JButton _quitButton;

	ControlPanel(Controller ctrl) {
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		setLayout(new BorderLayout());
		_toolaBar = new JToolBar();
		add(_toolaBar, BorderLayout.PAGE_START);

		this._fc = new JFileChooser();
		this._changeRegionsDialog=new ChangeRegionsDialog(_ctrl);

		// File Chooser Button
		this._fileChooserButton = new JButton();
		this._fileChooserButton.setToolTipText("Choose a file");
		this._fileChooserButton.setIcon(new ImageIcon("resources/icons/open.png"));
		this._fileChooserButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				_fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));
				if (_fc.showOpenDialog(ViewUtils.getWindow(_fileChooserButton)) == JFileChooser.APPROVE_OPTION) {
					JSONObject json;
					try {
						json = new JSONObject(new JSONTokener(new FileReader(_fc.getSelectedFile())));

						int cols = json.getInt("cols");
						int rows = json.getInt("rows");
						int width = json.getInt("width");
						int height = json.getInt("height");

						_ctrl.reset(cols, rows, width, height);
						_ctrl.load_data(json);

					} catch (Exception e1) {
						ViewUtils.showErrorMsg("Error de lectura de archivo "+e1.getMessage());
					}
					
					

				}
			}
		});
		this._toolaBar.add(_fileChooserButton);
		add_separations();

		// Map View Button
		this._mapViewButton = new JButton();
		this._mapViewButton.setToolTipText("Map View");
		this._mapViewButton.setIcon(new ImageIcon("resources/icons/viewer.png"));
		this._mapViewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MapWindow _mapWindow=new MapWindow(new JFrame(),_ctrl);
				
				_mapWindow.setVisible(true);
			}

		});
		this._toolaBar.add(_mapViewButton);

		// Regions Button
		this._regionsButton = new JButton();
		this._regionsButton.setToolTipText("Regions Dialogue");
		this._regionsButton.setIcon(new ImageIcon("resources/icons/regions.png"));
		this._regionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 _changeRegionsDialog.open(ViewUtils.getWindow(ControlPanel.this));
			}

		});
		this._toolaBar.add(_regionsButton);
		
		add_separations();

		// Run Button
		this._runButton = new JButton();
		this._runButton.setToolTipText("Run");
		this._runButton.setIcon(new ImageIcon("resources/icons/run.png"));
		this._runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_fileChooserButton.setEnabled(false);
				_mapViewButton.setEnabled(false);
				_regionsButton.setEnabled(false);
				_runButton.setEnabled(false);
				_stopButton.setEnabled(true);
				_quitButton.setEnabled(false);
				_stopped = false;
				
				run_sim((Double) spinner.getValue(), Double.parseDouble(_deltaTime.getText()));
				
				
			}
		});
		this._toolaBar.add(_runButton);

		// Stop Button
		this._stopButton = new JButton();
		this._stopButton.setToolTipText("Stop");
		this._stopButton.setIcon(new ImageIcon("resources/icons/stop.png"));
		this._stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_stopped = true;
			}
		});
		this._toolaBar.add(_stopButton);
		
		//adding the steps,delta time and spinner
		
		JLabel steps=new JLabel(" Steps: ");
		steps.setToolTipText("Simulation steps to run: 1-10000");
		
		this._toolaBar.add(steps);
		double defaulte=Main._time*100;
		this.spinner = new JSpinner(new SpinnerNumberModel(defaulte, 1, 10000, 1));
		this.spinner.setPreferredSize(new Dimension(100,30));
		this._toolaBar.add(spinner);
		
		
		JLabel dt=new JLabel(" Delta-Time: ");
		dt.setToolTipText("Real time (seconds) corresponding to a step");
		
		this._toolaBar.add(dt);
		this._deltaTime = new JTextField();
		this._deltaTime.setText(Main._delta_time.toString());
		this._deltaTime.setPreferredSize(new Dimension(100,30));
		this._toolaBar.add(_deltaTime);
		
		add_separations();

		// Quit Button
		_toolaBar.add(Box.createGlue()); // this aligns the button to the right
		_quitButton = new JButton();
		_quitButton.setToolTipText("Quit");
		_quitButton.setIcon(new ImageIcon("resources/icons/exit.png"));
		_quitButton.addActionListener((e) -> ViewUtils.quit(this));
		_toolaBar.add(_quitButton);
		this.add(_toolaBar);
		
		
		
	}
	
	private void add_separations() {
		//add separations between objects
		JSeparator sep=new JSeparator(JSeparator.VERTICAL);
		this._toolaBar.addSeparator();
		this._toolaBar.add(sep);
		this._toolaBar.addSeparator();
	}

	private void run_sim(double n, double dt) {
		if (n > 0 && !_stopped) {
			try {
				//for (int i = 0; i < n; i++) {
					_ctrl.advance(dt);
				//}
				SwingUtilities.invokeLater(() -> run_sim(n - 1, dt));
			} catch (Exception e) {
				ViewUtils.showErrorMsg("Error: " + e.getMessage());
				_fileChooserButton.setEnabled(true);
				_mapViewButton.setEnabled(true);
				_regionsButton.setEnabled(true);
				_runButton.setEnabled(true);
				_stopButton.setEnabled(true);
				_quitButton.setEnabled(true);
				_stopped = true;
			}
		} else {
			_fileChooserButton.setEnabled(true);
			_mapViewButton.setEnabled(true);
			_regionsButton.setEnabled(true);
			_runButton.setEnabled(true);
			_stopButton.setEnabled(true);
			_quitButton.setEnabled(true);
			_stopped = true;
		}
	}
}
