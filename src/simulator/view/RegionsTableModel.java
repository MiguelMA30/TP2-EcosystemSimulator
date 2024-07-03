package simulator.view;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.AnimalInfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import simulator.model.MapInfo.RegionData;

class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Controller _ctrl;
	private List<RegionData> listRegions;
	private List<String>regions;
	private Map<String,Map<Diet,Integer>>regionsNumAnimals;

	RegionsTableModel(Controller ctrl) {
		this.listRegions = new ArrayList<>();
		this.regions=new ArrayList<String>();
		this.regionsNumAnimals=new HashMap<>();
		this._ctrl = ctrl;
		this._ctrl.addObserver(this);
		this.regions.add("Row");
		this.regions.add("Col");
		this.regions.add("Desc.");
		
		for(Diet diet:Diet.values()) {
			this.regions.add(diet.name());
		}
	}

	@Override
	public int getRowCount() {
		return this.listRegions.size();
	}

	@Override
	public int getColumnCount() {
		return this.regions.size();
	}
	
	@Override
    public String getColumnName(int column) {
        return regions.get(column);
    }

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object ob = null;
		RegionData info = this.listRegions.get(rowIndex);
		
		
		switch (columnIndex) {
		case 0:
			ob=info.row();
			break;
		case 1:
			ob=info.col();
			break;
		case 2:
			ob=info.r().toString();
			break;
		default:
			Diet diet = Diet.values()[columnIndex - 3];
			
			Map<Diet, Integer> dietCounts = regionsNumAnimals.get("col" + info.col() + "row" + info.row());
			if (dietCounts != null && dietCounts.containsKey(diet)) {
	           ob=dietCounts.get(diet);
	        } 
			else {
				ob=0;
	        }
			break;
		}
		return ob;
	}
	
	private void updateData(MapInfo map,List<AnimalInfo> animals) {
		this.listRegions.clear();
		this.regionsNumAnimals.clear();
		
		for(MapInfo.RegionData data:map) {
			this.listRegions.add(data);
			Map<Diet, Integer> cont_diets = new HashMap<>();
			
			for (Diet diet : Diet.values()) {
				cont_diets.put(diet, 0);
            }
            regionsNumAnimals.put("col" + data.col() + "row" + data.row(), cont_diets);
		}
		
		for (AnimalInfo a : animals) {
            Map<Diet, Integer> dietCounts = regionsNumAnimals.get(calculateRegion(a, map));
            
            if(dietCounts.containsKey(a.get_diet())) {
            	int cont=dietCounts.get(a.get_diet());
            	dietCounts.put(a.get_diet(), cont+1);
            }
            else {
            	dietCounts.put(a.get_diet(), 1);
            }           
        }
		
		fireTableDataChanged();
	}
	
	private String calculateRegion(AnimalInfo animal,MapInfo _mapInfo) {
		
		Iterator<MapInfo.RegionData> iterator = _mapInfo.iterator();
		double regionWidth = _mapInfo.get_width() / _mapInfo.get_cols();
	    double regionHeight = _mapInfo.get_height() / _mapInfo.get_rows();
		
		while(iterator.hasNext()) {
			
			MapInfo.RegionData region=iterator.next();
			double xMin = region.col() * regionWidth;
	        double yMin = region.row() * regionHeight;
	        double xMax = xMin + regionWidth;
	        double yMax = yMin + regionHeight;
			
			if(animal.get_position().getX()>=xMin&&animal.get_position().getX()<xMax&&
					animal.get_position().getY()>=yMin&&animal.get_position().getY()<yMax) {
				
				
				return "col"+(int)(animal.get_position().getX()/regionWidth)+"row"+(int)(animal.get_position().getY()/regionHeight);
			}
			
		}
		return null;
	}
	
	@Override
	public void onRegister(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(map,animals);
	}

	@Override
	public void onReset(double time, MapInfo map, List<AnimalInfo> animals) {
		updateData(map,animals);
	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<AnimalInfo> animals, AnimalInfo a) {
		updateData(map,animals);
		
	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		//updateData(map);
		fireTableDataChanged();
	}

	@Override
	public void onAvanced(double time, MapInfo map, List<AnimalInfo> animals, double dt) {
		
	}

}
