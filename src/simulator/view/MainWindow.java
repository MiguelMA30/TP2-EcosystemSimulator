package simulator.view;

import java.awt.BorderLayout;


import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import simulator.control.Controller;

public class MainWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;

	public MainWindow(Controller ctrl) {
		super("[ECOSYSTEM SIMULATOR]");
		_ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		
		ControlPanel ctrlPanel=new ControlPanel(_ctrl);
		mainPanel.add(ctrlPanel, BorderLayout.PAGE_START);

		StatusBar statusbar = new StatusBar(_ctrl);
		mainPanel.add(statusbar, BorderLayout.PAGE_END);
		
		// Definici�n del panel de tablas (usa un BoxLayout vertical)
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		
		InfoTable species=new InfoTable("Species", new SpeciesTableModel(_ctrl));
		setPreferredSize(new Dimension(500, 250));
		contentPanel.add(species);
		
		InfoTable dietState=new InfoTable("Steps", new SimHunDangTableModel(_ctrl));
		setPreferredSize(new Dimension(500, 250));
		contentPanel.add(dietState);
		
		InfoTable regions=new InfoTable("Regions", new RegionsTableModel(_ctrl));
		setPreferredSize(new Dimension(500, 250));
		contentPanel.add(regions);
		
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosing(WindowEvent e) {
				ViewUtils.quit(MainWindow.this);

			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

		});
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);
	}
}