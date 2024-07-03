package simulator.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CountCarnivoresObserver implements EcoSysObserver{

    private static final int CARNIVORES = 3;

    private HashMap<MapInfo.RegionData, Double> regionMap;

    public CountCarnivoresObserver(){
        regionMap = new HashMap<>();
    }


    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {

    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {

    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {

    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {

        for (MapInfo.RegionData region : map) {

            int carnivores = countCarnivores(region.r().getAnimalsInfo());

            if (carnivores > CARNIVORES && !regionMap.containsKey(region)){
                regionMap.put(region, time);
            }

        }
    }

    private int countCarnivores(List<AnimalInfo> animals) {
        int carnivores = 0;
        for (AnimalInfo animalInfo : animals) {
            if (animalInfo.get_diet().equals(Diet.CARNIVORE))
                carnivores++;
        }
        return carnivores;
    }

    public void printResults() {
        for (Map.Entry<MapInfo.RegionData, Double> entry : regionMap.entrySet()) {
            System.out.println(entry.getKey().row() + " - " + entry.getKey().col() + ": " + entry.getValue());
        }


    }

}
