package simulator.model;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager implements AnimalMapView {

	private int height_map;
	private int width_map;
	private int rows;
	private int cols;
	private int height_region;
	private int width_region;

	private Region[][] _regions;
	private Map<Animal, Region> _animal_region;

	public RegionManager(int cols, int rows, int width, int height) throws IllegalArgumentException {
		if (width % cols != 0 || height % rows != 0) {
			throw new IllegalArgumentException("Error");
		}
		this.rows = rows;
		this.cols = cols;
		this.height_map = height;
		this.width_map = width;
		this.height_region = height / rows;
		this.width_region = width / cols;

		this._regions = new Region[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this._regions[i][j] = new DefaultRegion();
			}
		}

		this._animal_region = new HashMap<Animal, Region>();
	}

	void set_region(int row, int col, Region r) {

		if (row >= 0 && row < rows && col >= 0 && col < cols) {
			_regions[row][col] = r;

			for (Animal a : _regions[row][col].getAnimals()) {
				r.add_animal(a);
				_animal_region.put(a, r);

			}

		}
	}

	void register_animal(Animal a) {

		a.init(this);
		int col = (int) a.get_position().getX() / width_region;
		int row = (int) a.get_position().getY() / height_region;

		if (row >= 0 && row < rows && col >= 0 && col < cols) {

			_regions[row][col].add_animal(a);
			_animal_region.put(a, _regions[row][col]);

		}

	}

	void unregister_animal(Animal a) {
		_animal_region.get(a).remove_animal(a);
		_animal_region.remove(a);
	}

	void update_animal_region(Animal a) {
		int col = (int) a.get_position().getX() / width_region;
		int row = (int) a.get_position().getY() / height_region;

		if (row >= 0 && row < rows && col >= 0 && col < cols) {
			if (_regions[row][col] != _animal_region.get(a)) {
				_animal_region.get(a).remove_animal(a);
				_regions[row][col].add_animal(a);
				_animal_region.put(a, _regions[row][col]);
			}
		}
	}

	public double get_food(Animal a, double dt) {
		return _animal_region.get(a).get_food(a, dt);
	}

	void update_all_regions(double dt) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				this._regions[i][j].update(dt);
			}
		}
	}

	public List<Animal> get_animals_in_range(Animal a, Predicate<Animal> filter) {
		List<Animal> listAnimals = new ArrayList<Animal>();

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (Animal animal : this._regions[i][j].getAnimals()) {
					if (a.get_position().distanceTo(animal.get_position()) <= a.get_sight_range()
							&& filter.test(animal)) {
						listAnimals.add(animal);
					}
				}
			}
		}
		return listAnimals;
	}

	public JSONObject as_JSON() {
		JSONArray j_array = new JSONArray();
		JSONObject json = new JSONObject();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {

				json.put("row", i);
				json.put("col", j);
				json.put("data", _regions[i][j].as_JSON());
				j_array.put(json);
			}
		}
		JSONObject O = new JSONObject();
		O.put("regions", j_array);
		return O;
	}

	@Override
	public int get_cols() {
		return cols;
	}

	@Override
	public int get_rows() {
		return rows;
	}

	@Override
	public int get_width() {
		return this.width_map;
	}

	@Override
	public int get_height() {
		return this.height_map;
	}

	@Override
	public int get_region_width() {
		return this.width_region;
	}

	@Override
	public int get_region_height() {
		return this.height_region;
	}

	@Override
	public Iterator<RegionData> iterator() {
		List<RegionData> regions = new ArrayList<>();

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				regions.add(new RegionData(i, j, this._regions[i][j]));
			}
		}
	
		return regions.iterator();
	
	}
	
}
