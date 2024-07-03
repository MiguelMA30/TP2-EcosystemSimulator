package simulator.model;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

	private Factory<Animal> _animals_factory;
	private Factory<Region> _regions_factory;
	private RegionManager _region_manager;
	private List<Animal> listAnimals;
	private double time;

	private List<EcoSysObserver> observerList;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory,
			Factory<Region> regions_factory) {
		this._animals_factory = animals_factory;
		this._regions_factory = regions_factory;
		this._region_manager = new RegionManager(cols, rows, width, height);
		this.listAnimals = new ArrayList<>();
		this.time = 0.0;
		this.observerList = new ArrayList<>();
	}

	private void set_region(int row, int col, Region r) {
		_region_manager.set_region(row, col, r);
		for (EcoSysObserver eco : this.observerList) {
			eco.onRegionSet(row, col, _region_manager, r);
		}
	}

	public void set__region(int row, int col, JSONObject r_json) {
		set_region(row, col, this._regions_factory.create_instance(r_json));
	}

	private void add_animal(Animal a) {
		listAnimals.add(a);
		_region_manager.register_animal(a);
		for (EcoSysObserver eco : this.observerList) {
			eco.onAnimalAdded(time, _region_manager, Collections.unmodifiableList(this.listAnimals), a);
		}
	}

	public void add_animal(JSONObject a_json) {
		add_animal(_animals_factory.create_instance(a_json));

	}

	public MapInfo get_map_info() {
		return _region_manager;
	}

	public List<? extends AnimalInfo> get_animals() {
		return Collections.unmodifiableList(listAnimals);
	}

	public double get_time() {
		return time;
	}

	public void advance(double dt) {
		
		// Incrementar el tiempo por dt
		time += dt;

		// Quitar todos los animales con estado DEAD de la lista de animales y
		// eliminarlos del gestor de regiones
		List<Animal> dead = new ArrayList<>();

		for (Animal a : this.listAnimals) {
			if (a.get_state().equals(State.DEAD)) {
				dead.add(a);
			} else {
				a.update(dt);
				this._region_manager.update_animal_region(a);
			}

		}

		// Para cada animal: llama a su update(dt) y pide al gestor de regiones que
		// actualice su región
		for (Animal a : dead) {
			this.listAnimals.remove(a);
			this._region_manager.unregister_animal(a);
		}

		// Pedir al gestor de regiones actualizar todas las regiones

		this._region_manager.update_all_regions(dt);
		/*
		 * Para cada animal: si is_pregnant() devuelve true, obtenemos el bebé usando
		 * su método deliver_baby() y lo añadimos a la simulación usando add_animal
		 */
		List<Animal> babies = new ArrayList<>();
		for (Animal a : this.listAnimals) {
			if (a.is_pregnant()) {

				babies.add(a.deliver_baby());
			}
		}

		for (Animal a : babies) {
			add_animal(a);
		}

		for (EcoSysObserver eco : this.observerList) {
			eco.onAvanced(time, _region_manager, Collections.unmodifiableList(this.listAnimals), dt);
		}

	}

	public JSONObject as_JSON() {
		JSONObject json = new JSONObject();
		json.put("time", time);
		json.put("state", _region_manager.as_JSON());
		return json;
	}

	public void reset(int cols, int rows, int width, int height) {
		this.listAnimals.clear();

		this._region_manager = new RegionManager(cols, rows, width, height);

		this.time = 0.0;

		for (EcoSysObserver eco : this.observerList) {
			eco.onReset(time, _region_manager, Collections.unmodifiableList(this.listAnimals));
		}
	}

	@Override
	public void addObserver(EcoSysObserver o) {
		this.observerList.add(o);
		o.onRegister(time, _region_manager, Collections.unmodifiableList(this.listAnimals));
	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		this.observerList.remove(o);
	}

}
