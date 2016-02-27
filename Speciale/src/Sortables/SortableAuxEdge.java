package Sortables;

import AuxFlow.AuxEdge;

public class SortableAuxEdge implements Comparable<SortableAuxEdge>{
	private int load;
	private AuxEdge auxEdge;
	
	public SortableAuxEdge(int load, AuxEdge auxEdge){
		this.load = load;
		this.auxEdge = auxEdge;
	}
	
	public AuxEdge getAuxEdge(){
		return auxEdge;
	}
	
	public double getLoad(){
		return auxEdge.getAvgLoad();
	}
	
	@Override
	public int compareTo(SortableAuxEdge o) {
		return o.load-load;
	}
}
