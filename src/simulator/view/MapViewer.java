package simulator.view;




import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulator.model.AnimalInfo;
import simulator.model.MapInfo;
import simulator.model.State;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

	// Anchura/altura/ de la simulación -- se supone que siempre van a ser iguales
	// al tamaño del componente
	private int _width;
	private int _height;

	// Número de filas/columnas de la simulación
	private int _rows;
	private int _cols;

	// Anchura/altura de una región
	int _rwidth;
	int _rheight;

	// Mostramos sólo animales con este estado. Los posibles valores de _currState
	// son null, y los valores deAnimal.State.values(). Si es null mostramos todo.
	State _currState;

	// En estos atributos guardamos la lista de animales y el tiempo que hemos
	// recibido la última vez para dibujarlos.
	volatile private Collection<AnimalInfo> _objs;
	volatile private Double _time;

	// Una clase auxilar para almacenar información sobre una especie
	private static class SpeciesInfo {
		private Integer _count;
		private Color _color;

		SpeciesInfo(Color color) {
			_count = 0;
			_color = color;
		}
	}

	// Un mapa para la información sobre las especies
	Map<String, SpeciesInfo> _kindsInfo = new HashMap<>();

	// El font que usamos para dibujar texto
	private Font _font = new Font("Arial", Font.BOLD, 12);

	// Indica si mostramos el texto la ayuda o no
	private boolean _showHelp;
	
	//Variable que indica en el estado en el que nos encontramos
	private int index;

	public MapViewer() {
		initGUI();
	}

	private void initGUI() {

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
					case 'h':
						_showHelp = !_showHelp;
						repaint();
						break;
					case 's':
						if (_currState == null) {
							_currState = State.values()[index];
							
						}
						else {
							_currState = State.values()[index++];
						}
						
						if(index>=State.values().length) {
							index=0;
						}
						
						repaint();
						break;
					default:
				}
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); // Esto es necesario para capturar las teclas cuando el ratón está sobre este
								// componente.
			}
		});

		// Por defecto mostramos todos los animales
		_currState = null;

		// Por defecto mostramos el texto de ayuda
		_showHelp = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Cambiar el font para dibujar texto
		g.setFont(_font);

		// Dibujar fondo blanco
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, _width, _height);

		// Dibujar los animales, el tiempo, etc.
		if (_objs != null) {
			drawObjects(gr, _objs, _time);
		}

		if (_showHelp) {
			gr.setColor(Color.RED);
			gr.drawString("h: toggle help", 5, 15);
			gr.drawString("s: show animals of a specific state", 5, 30);
			
		}

	}

	private boolean visible(AnimalInfo a) {
		if (_currState == null || a.get_state().equals(_currState)) {
			return true;
		}
		return false;
	}

	private void drawObjects(Graphics2D g, Collection<AnimalInfo> animals, Double time) {

		g.setColor(Color.GRAY);

		int w = this._width / this._cols;
		int h = this._height / this._rows;

		for (int i = 0; i < this._cols; i++) {
			g.drawLine(i * w, 0, i * w, _height);
		}

		for (int i = 0; i < this._rows; i++) {
			g.drawLine(0, i * h, _width, i * h);
		}

		// Dibujar los animales
		for (AnimalInfo a : animals) {

			// Si no es visible saltamos la iteración
			if (!visible(a))
				continue;
			

			// La información sobre la especie de 'a'
			SpeciesInfo esp_info = _kindsInfo.get(a.get_genetic_code());

			if (esp_info == null) {
				Color color = ViewUtils.get_color(a.get_genetic_code());
	            esp_info = new SpeciesInfo(color);
				this._kindsInfo.put(a.get_genetic_code(), esp_info);
				
			}

			esp_info._count++;
			
			g.setColor(esp_info._color);
			g.fillRect((int) (a.get_position().getX()*this.getWidth()/this._width),
					(int) (a.get_position().getY()*this.getHeight()/this._height), (int) (a.get_age()),
					(int) (a.get_age()));

		}

		int x_align=10; //alineaci�n en el eje X
		int y_align=500; //alineaci�n en el eje Y
		
		for (Map.Entry<String, SpeciesInfo> e : _kindsInfo.entrySet()) {
				g.setColor(ViewUtils.get_color(e.getKey()));
				drawStringWithRect(g, x_align, y_align, e.getKey() + ": " + e.getValue()._count);
				y_align+=20;
				e.getValue()._count = 0;
		}
		
		g.setColor(Color.MAGENTA);
		drawStringWithRect(g, x_align, y_align, "Time: " + String.format("%.3f", time));
		
		if (this._currState != null) {
			g.setColor(Color.BLUE);
			drawStringWithRect(g, x_align, y_align+20,"State: "+_currState.name());
		}

		
	}

	// Un método que dibujar un texto con un rectángulo
	void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
	}

	@Override
	public void update(List<AnimalInfo> objs, Double time) {
		
		this._objs = objs;
		this._time = time;
		this.index=0;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<AnimalInfo> animals) {
		this._height = map.get_height();
		this._width = map.get_width();
		this._rows = map.get_rows();
		this._cols = map.get_cols();
		this._rheight = map.get_region_height();
		this._rwidth = map.get_region_width();

		// Esto cambia el tamaño del componente, y así cambia el tamaño de la ventana
		// porque en MapWindow llamamos a pack() después de llamar a reset
		setPreferredSize(new Dimension(map.get_width(), map.get_height()));

		// Dibuja el estado
		update(animals, time);
	}

}
