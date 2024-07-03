package simulator.factories;

import org.json.JSONObject;

import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic food supply");
	}

	@Override
	protected Region create_instance(JSONObject data) {
		DynamicSupplyRegion dynamicSupplyRegion;
		double factor = 0.0;
		double food = 0.0;

		if (data.has("factor")) {

			factor = data.getDouble("factor");

		} else {
			factor = 2.0;
		}

		if (data.has("food")) {
			food = data.getDouble("food");
		} else {
			food = 1000.0;
		}

		dynamicSupplyRegion = new DynamicSupplyRegion(food, factor);

		return dynamicSupplyRegion;
	}

	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("factor", "food increase factor (optional, default 2.0)");
		o.put("food", "initial amount of food (optional, default 100.0)");
	}

}
