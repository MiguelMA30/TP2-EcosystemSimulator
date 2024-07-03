package simulator.model;

public class DefaultRegion extends Region {

	private static final double INIT_PARAM = 60.0;
	private static final double SEC_PARAM = 5.0;
	private static final double THRD_PARAM = 2.0;

	public DefaultRegion() {
		super();
	}

	public double get_food(Animal a, double dt) {
		double food;
		if (a._diet.equals(Diet.CARNIVORE)) {
			food = 0.0;
		} else {
			/*
			 * a._region_mngr.get_animals_in_range(a,
			 * animal->animal.get_diet().equals(Diet.HERBIVORE)).size() --> el número de
			 * animales hervíboros de la región
			 */
			food = INIT_PARAM * Math.exp(-Math.max(0,
					this.animaList.stream().filter(animal -> animal.get_diet().equals(Diet.HERBIVORE)).count()
							- SEC_PARAM)
					* THRD_PARAM) * dt;

		}
		return food;
	}

	@Override
	public void update(double dt) {

	}

	public String toString() {
		return "Default region";
	}

}
