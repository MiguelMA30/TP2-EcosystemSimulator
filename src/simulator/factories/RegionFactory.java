package simulator.factories;

import java.util.List;

import org.json.JSONObject;

import simulator.model.Region;

public class RegionFactory implements Factory<Region> {

	@Override
	public Region create_instance(JSONObject info) {

		DefaultRegionBuilder default_region_b = new DefaultRegionBuilder();
		DynamicSupplyRegionBuilder dynamic_supply_region_b = new DynamicSupplyRegionBuilder();

		if (info.getString("type").equals("dynamic")) {

			return dynamic_supply_region_b.create_instance(info);
		} else if (info.getString("type").equals("default")) {
			return default_region_b.create_instance(info);
		}
		throw new IllegalArgumentException("Tipo de regi�n desconocida: " + info.toString());
	}

	@Override
	public List<JSONObject> get_info() {
		return this.get_info();
	}

}
