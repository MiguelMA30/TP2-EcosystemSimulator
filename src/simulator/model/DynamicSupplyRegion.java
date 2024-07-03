package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region {

	private static final double INIT_PARAM = 60.0;
	private static final double SEC_PARAM = 5.0;
	private static final double THRD_PARAM = 2.0;

	private double _food;
	private double _factor;

	public DynamicSupplyRegion(double food, double factor) {
		super();
		this._food = food;
		this._factor = factor;
	}

	public double get_food(Animal a, double dt) {
		double food = 0.0;
		if (a._diet.equals(Diet.CARNIVORE)) {
			food = 0.0;
		} else {
			/*
			 * a._region_mngr.get_animals_in_range(a,
			 * animal->animal.get_diet().equals(Diet.HERBIVORE)).size() --> el número de
			 * animales hervíboros de la región
			 */
			food -= Math.min(_food,
					INIT_PARAM * Math.exp(-Math.max(0,
							this.animaList.stream().filter(animal -> animal.get_diet().equals(Diet.HERBIVORE)).count()
									- SEC_PARAM)
							* THRD_PARAM) * dt);

		}
		return food;
	}

	@Override
	public void update(double dt) {
		if (Utils._rand.nextDouble() < 0.5) {
			_food += dt * _factor;
		}
	}

	public String toString() {
		return "Dynamic region";
	}
}
