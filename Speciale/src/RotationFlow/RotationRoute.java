package RotationFlow;

import java.util.ArrayList;

public class RotationRoute {
	private ArrayList<RotationEdge> route;
	private RotationDemand demand;
	private int FFE;
	private int cost;
	
	public RotationRoute(RotationDemand demand){
		this.route = new ArrayList<RotationEdge>();
		this.demand = demand;
		this.cost = 0;
		for(RotationEdge e : route){
			cost += e.getCost();
		}
	}
	
	public void setRoute(ArrayList<RotationEdge> route){
		this.route = route;
		for(RotationEdge e : route){
			e.addServicedRoute(this);
		}
	}

	public int getTotalCost() {
		return cost * FFE;
	}
	
	public int getCost(){
		return cost;
	}

	public void setFFE(int FFE) {
		this.FFE = FFE;
	}
	
	public int getFFE(){
		return FFE;
	}
	
	public RotationDemand getDemand(){
		return demand;
	}

	public void removeFFE(int removeFFE) {
		this.FFE -= removeFFE;
		if(this.FFE == 0){
			destroy();
		}
	}
	
	public void destroy(){
		this.demand.removeRoute(this);
		for(RotationEdge e : route){
			e.removeServicedRoute(this);
		}
	}


	public ArrayList<RotationEdge> getRoute() {
		return route;
	}
}
