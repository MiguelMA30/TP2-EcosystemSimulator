package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Animal> {

	private Factory<SelectionStrategy> _strategyFactory;

	public WolfBuilder(Factory<SelectionStrategy> strategyFactory) {
		super("wolf", "Carnivore");
		if (strategyFactory == null) {
			throw new IllegalArgumentException("strategy factory it cannot be null");
		}
		this._strategyFactory = strategyFactory;
	}

	@Override
	protected Wolf create_instance(JSONObject data) {
		SelectionStrategy mate_strategy;
		SelectionStrategy hunting_strategy;
		Vector2D pos = null;

		// si existe en data una clave "mate_strategy" construimos la clase con ese
		// valor, sino usamos la estrategia SelectFirst.
		if (data.has("mate_strategy")) {
			mate_strategy = this._strategyFactory.create_instance(data.getJSONObject("mate_strategy"));
		} else {
			mate_strategy = new SelectFirst();
		}

		// si existe en data una clave "hunt_strategy" construimos la clase con ese
		// valor, sino usamos la estrategia SelectFirst.
		if (data.has("hunt_strategy")) {
			hunting_strategy = this._strategyFactory.create_instance(data.getJSONObject("hunt_strategy"));
		} else {
			hunting_strategy = new SelectFirst();
		}

		// si existe en data una clave "pos" construimos la clase con ese valor, sino
		// usamos null.
		if (data.has("pos")) {
			// JSONObject json=data.getJSONObject("data").getJSONObject("pos");
			JSONObject json = data.getJSONObject("pos");
			double x1 = json.getJSONArray("x_range").getDouble(0); // dentro del JSONArray, dentro de pos, cogemos el
																	// valor de la primera posición de x_range
			double x2 = json.getJSONArray("x_range").getDouble(1); // dentro del JSONArray, dentro de pos, cogemos el
																	// valor de la segunda posición de x_range
			double y1 = json.getJSONArray("y_range").getDouble(0); // dentro del JSONArray, dentro de pos, cogemos el
																	// valor de la primera posición de y_range
			double y2 = json.getJSONArray("y_range").getDouble(1); // dentro del JSONArray, dentro de pos, cogemos el
																	// valor de la segunda posición de y_range

			double X = x1 + Utils._rand.nextDouble(x2 - x1 + 1); // cogemos un valor aleatorio dentro de x1 y x2
			double Y = y1 + Utils._rand.nextDouble(y2 - y1 + 1); // cogemos un valor aleatorio dentro de y1 y y2

			pos = new Vector2D(X, Y);
		}

		return new Wolf(mate_strategy, hunting_strategy, pos);
	}

}
