package simulator.model;

import java.util.List;

public class SelectFirst implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		Animal an = null;
		if (!as.isEmpty()) {
			an = as.get(0);
		}

		return an;
	}
}
