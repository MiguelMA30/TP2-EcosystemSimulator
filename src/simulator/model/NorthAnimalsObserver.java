package simulator.model;

import simulator.misc.Vector2D;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NorthAnimalsObserver implements EcoSysObserver{

    private HashMap<AnimalInfo, Vector2D> prevPositionMap;
    private HashMap<Double, Integer> counterMap;

    public NorthAnimalsObserver() {
        prevPositionMap = new HashMap<>();
        counterMap = new HashMap<>();
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {

    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {

    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        if (!prevPositionMap.containsKey(a)) {
            prevPositionMap.put(a, a.get_position());
        }
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

        int contador = 0;
        for (AnimalInfo animal : animals) {
            if (animal.get_position().getY() > prevPositionMap.get(animal).getY()) {
                contador++;
            }
            prevPositionMap.put(animal, animal.get_position());
        }

        counterMap.put(time, contador);

    }



    public void printResults() {
        for (double key : counterMap.keySet()) {
            int value = counterMap.get(key);
            System.out.println("Iteration :" + key + " - " + value);
        }



    }


}
