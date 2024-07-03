package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal an = null;
		double min = Double.MAX_VALUE;
		if (!as.isEmpty()) {
			for (Animal animal : as) {
				if (a.get_position().distanceTo(animal.get_position()) < min) {
					min = a.get_position().distanceTo(animal.get_position());
					an = animal;
				}
			}
		}

		return an;
	}

}
