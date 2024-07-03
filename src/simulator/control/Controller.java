package simulator.control;

import java.io.OutputStream;



import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

	private Simulator _sim;

	public Controller(Simulator sim) {
		this._sim = sim;
	}

	public void load_data(JSONObject data) {

		set_regions(data);

		JSONArray j_array = data.getJSONArray("animals");

		for (int i = 0; i < j_array.length(); i++) {
			JSONObject animal = j_array.getJSONObject(i);
			int N = animal.getInt("amount");
			JSONObject O = animal.getJSONObject("spec");

			for (int j = 0; j < N; j++) {
				_sim.add_animal(O);
			}
		}

	}

	public void run(double t, double dt, boolean sv, OutputStream out) {
		/* crear la instancia de la clase SimpleObjectViewer */
		SimpleObjectViewer view = null;
		if (sv) {
			MapInfo m = _sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]",

					m.get_width(), m.get_height(), m.get_cols(), m.get_rows());

			view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt);
		}

		JSONObject json = new JSONObject();
		JSONObject init_state = _sim.as_JSON();
		json.put("in", init_state);

		while (_sim.get_time() <= t) {
			_sim.advance(dt);
			if (sv)
				view.update(to_animals_info(_sim.get_animals()), _sim.get_time(), dt); // dibujar el estado del
																				// simulador en cada iteración
		}

		JSONObject final_state = _sim.as_JSON();
		json.put("out", final_state);

		PrintStream p = new PrintStream(out);
		p.println(json.toString()); // escribir en "out" la estructura JSON

		if (sv)
			view.close(); // cerrar la ventana del visor

	}

	// método para convertir una lista de tipo List<? extends Animalnfo> a
	// List<ObjInfo>
	private List<ObjInfo> to_animals_info(List<? extends AnimalInfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (AnimalInfo a : animals) {

			int x = (int) a.get_position().getX();
			int y = (int) a.get_position().getY();

			ol.add(new ObjInfo(a.get_genetic_code(),

					x, y, (int) Math.round(a.get_age()) + 2));
		}

		return ol;
	}

	public void reset(int cols, int rows, int width, int height) {
		this._sim.reset(cols, rows, width, height);
	}

	public void set_regions(JSONObject rs) {
		if (rs.has("regions")) {
			JSONArray j_array = rs.getJSONArray("regions");

			for (int i = 0; i < j_array.length(); i++) {
				JSONObject region = j_array.getJSONObject(i);
				JSONArray row = region.getJSONArray("row"); // cogemos el array [rf,rt]
				JSONArray col = region.getJSONArray("col"); // cogemos el array [cf,ct]
				JSONObject O = region.getJSONObject("spec");

				int rf = row.getInt(0); // cogemos del array [rf,rt] el primer atributo que es rf
				int rt = row.getInt(1); // cogemos del array [rf,rt] el segundo atributo que es rt
				int cf = col.getInt(0); // cogemos del array [cf,ct] el primer atributo que es cf
				int ct = col.getInt(1); // cogemos del array [cf,ct] el segundo atributo que es ct

				for (int R = rf; R <= rt; R++) {
					for (int C = cf; C <= ct; C++) {
						_sim.set__region(R, C, O);
					}
				}
			}
		}

	}

	public void advance(double dt) {
		this._sim.advance(dt);
	}

	public void addObserver(EcoSysObserver o) {
		this._sim.addObserver(o);
	}

	public void removeObserver(EcoSysObserver o) {
		this._sim.removeObserver(o);
	}

}
