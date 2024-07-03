package simulator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

public class SimHunDangTableModel extends AbstractTableModel implements EcoSysObserver {
    private static final long serialVersionUID = 1L;
    private Controller _ctrl;
    private List<Integer> simulator_steps;
    private Map<Integer, Integer> herbNumAnimals;
    private Map<Integer, Integer> carnNumAnimals;
    private int steps;
    private int herbivore_danger;
    private int carnivore_hunger;

    SimHunDangTableModel(Controller ctrl) {
        this.simulator_steps = new ArrayList<>();
        this.herbNumAnimals = new HashMap<>();
        this.carnNumAnimals = new HashMap<>();
        this.steps = 0;
        this._ctrl = ctrl;
        this._ctrl.addObserver(this);
        this.herbivore_danger = 0;
        this.carnivore_hunger = 0;
        this.simulator_steps.add(steps);
    }

    @Override
    public int getRowCount() {
        return simulator_steps.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0: return "Simulation Step";
            case 1: return "Herbivores (Danger)";
            case 2: return "Carnivores (Hunger)";
            default: return "";
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int step = simulator_steps.get(rowIndex);
        switch (columnIndex) {
            case 0: return step;
            case 1: return herbNumAnimals.get(step);
            case 2: return carnNumAnimals.get(step);
            default: return null;
        }
    }

    private void updateData(List<AnimalInfo> animals) {
        herbivore_danger = 0;
        carnivore_hunger = 0;

        for (AnimalInfo a : animals) {
            if (a.get_diet() == Diet.HERBIVORE && a.get_state() == State.DANGER) {
                herbivore_danger++;
            }
            if (a.get_diet() == Diet.CARNIVORE && a.get_state() == State.HUNGER) {
                carnivore_hunger++;
            }
        }

        herbNumAnimals.put(steps, herbivore_danger);
        carnNumAnimals.put(steps, carnivore_hunger);
        fireTableDataChanged();
    }

    @Override
    public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
        updateData(animals);
    }

    @Override
    public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
        steps = 0;
        simulator_steps.clear();
        herbNumAnimals.clear();
        carnNumAnimals.clear();
        simulator_steps.add(steps);
        updateData(animals);
    }

    @Override
    public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
        updateData(animals);
    }

    @Override
    public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
        // No action required
    }

    @Override
    public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
        steps++;
        simulator_steps.add(steps);
        updateData(animals);
    }
}
