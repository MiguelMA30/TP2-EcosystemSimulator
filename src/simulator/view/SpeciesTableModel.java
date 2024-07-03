package simulator.view;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;
	private Set<String>_genetic_codes;
	private List<String> states;
	private Map<String,Map<State,Integer>>_speciesNumAnimals;

	SpeciesTableModel(Controller ctrl) {
		this._genetic_codes = new HashSet<>();
		this.states= new ArrayList<>();
		this._speciesNumAnimals= new HashMap<>();
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
		this.states.add("Species");
		
		for(State state:State.values()) {
			this.states.add(state.name());
		}
		
	}

	@Override
	public int getRowCount() {
		return this._genetic_codes.size();
	}

	@Override
	public int getColumnCount() {
		return this.states.size();
	}
	
	@Override
    public String getColumnName(int column) {
        return states.get(column);
    }

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object ob = null;
		List<String>_genetic_codes_list=new ArrayList<>(_genetic_codes);
		
		if(columnIndex==0) {
			ob=_genetic_codes_list.get(rowIndex);
		}
		else {
			
			if(this._speciesNumAnimals.containsKey(_genetic_codes_list.get(rowIndex))) {
				Map<State,Integer> state_animals=this._speciesNumAnimals.get(_genetic_codes_list.get(rowIndex));
				if(state_animals.containsKey(State.valueOf(states.get(columnIndex)))) {
					ob=state_animals.get(State.valueOf(states.get(columnIndex)));
				}
				else {
					ob=0;
				}
			}
			else{
				ob=0;
			}
		}
		
		return ob;
	}
	
	private void updateData(List<AnimalInfo>animals) {
		this._genetic_codes.clear();
		this._speciesNumAnimals.clear();
		
		for(AnimalInfo a:animals) {
			this._genetic_codes.add(a.get_genetic_code());
			Map<State, Integer> state_map=this._speciesNumAnimals.get(a.get_genetic_code());
			
			if(state_map==null) {
				state_map=new HashMap<>();
				_speciesNumAnimals.put(a.get_genetic_code(), state_map);
			}
			if(state_map.containsKey(a.get_state())) {
				int cont=state_map.get(a.get_state());
				state_map.put(a.get_state(), cont+1);
			}
			else {
				state_map.put(a.get_state(), 1);
			}
		}
		
		
		
		fireTableDataChanged();
	}

	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(animals);
		
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateData(animals);
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		updateData(animals);
	}
}
