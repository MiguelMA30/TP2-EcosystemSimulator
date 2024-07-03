package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {

	protected List<Animal> animaList;

	public Region() {
		this.animaList = new ArrayList<Animal>();
	}

	final void add_animal(Animal a) {
		this.animaList.add(a);
	}

	final void remove_animal(Animal a) {
		this.animaList.remove(a);
	}

	final List<Animal> getAnimals() {
		return Collections.unmodifiableList(animaList);
	}

	public List<AnimalInfo> getAnimalsInfo() {
		return Collections.unmodifiableList(animaList);
	}

	public JSONObject as_JSON() {
		JSONArray j_array = new JSONArray();
		for (Animal a : animaList) {
			j_array.put(a.as_JSON());
		}

		JSONObject json = new JSONObject();
		json.put("animals", j_array);

		return json;
	}

}
