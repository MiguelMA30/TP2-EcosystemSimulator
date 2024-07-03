package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

	public Animal select(Animal a, List<Animal> as) {
		Animal an = null;
		if (!as.isEmpty()) {
			for (Animal animal : as) {
				if (animal.get_age() < as.get(0).get_age()) {
					an = animal;
				}
			}
		}

		return an;
	}

}
