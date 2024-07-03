package simulator.factories;

import java.util.List;

import org.json.JSONObject;

import simulator.model.Animal;
import simulator.model.SelectionStrategy;

public class AnimalFactory implements Factory<Animal> {

	private BuilderBasedFactory<SelectionStrategy> _strategyFactory;

	@Override
	public Animal create_instance(JSONObject info) {
		SheepBuilder sheep_b = new SheepBuilder(_strategyFactory);
		WolfBuilder wolf_b = new WolfBuilder(_strategyFactory);

		if (info.getString("type").equals("sheep")) {
			return sheep_b.create_instance(info);
		} else if (info.getString("type").equals("wolf")) {
			return wolf_b.create_instance(info);
		}

		throw new IllegalArgumentException("Tipo de animal desconocido: " + info.toString());
	}

	@Override
	public List<JSONObject> get_info() {
		return this.get_info();
	}

}
