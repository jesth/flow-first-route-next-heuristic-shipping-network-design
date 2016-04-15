package RotationFlow;

import java.util.ArrayList;

import Data.Demand;
import Data.Port;

public class RotationDemand {
	private Demand orgDemand;
	private RotationNode origin;
	private RotationNode destination;
	private int demand;
	private ArrayList<RotationRoute> routes;

	public RotationDemand(Demand orgDemand, int demand){
		this.orgDemand = orgDemand;
		this.demand = demand;
		this.routes = new ArrayList<RotationRoute>();
	}

	public void createRepRoute(int FFE, ArrayList<RotationEdge> route){
		RotationRoute r = new RotationRoute(this);
		r.setRoute(route);
		r.setFFE(FFE);
		routes.add(r);
	}

	public Demand getOrgDemand() {
		return orgDemand;
	}

	public int getDemand() {
		return demand;
	}

	public void addDemand(int addDemand){
		demand += addDemand;
	}

	public void setOrigin(RotationNode origin) {
		this.origin = origin;
	}

	public void setDestination(RotationNode destination) {
		this.destination = destination;
	}

	public RotationNode getOrigin() {
		return origin;
	}

	public RotationNode getDestination() {
		return destination;
	}

	//	public int getTransported() {
	//		return transported;
	//	}
	//
	//	public int getOmission() {
	//		return omission;
	//	}

	public ArrayList<RotationRoute> getRoutes() {
		return routes;
	}

	/*
	public void setRoute(ArrayList<RotationEdge> route){
		this.route = route;
		setTransported(demand);
		for(RotationEdge e : route){
			e.addServicedDemand(this);
		}
	}
	 */

	//	public void setTransported(int transported){
	//		this.transported = transported;
	//		this.omission = this.demand - this.transported;
	//		if(omission < 0){
	//			throw new RuntimeException("Negative number of omission containers.");
	//		}
	//	}
	//
	//	public void addOmission(int removedFFE) {
	//		omission += removedFFE;
	//		transported -= removedFFE;
	//		if(omission + transported != demand || transported < 0){
	//			throw new RuntimeException("Something is completely wrong with the number of transported/number of omission containers.");
	//		}
	//	}
	//
	//	public void resetTransportedDemand(){
	//		this.transported = 0;
	//		this.omission = 0;
	//	}

	public int getTotalCost(){
		int cost = 0;
		for(RotationRoute r : routes){
			cost += r.getTotalCost();
		}
		//		cost += (omission * 1000);
		return cost;
	}

	public void clearRoutes(){
		this.routes.clear();
	}

	public void createRoute(ArrayList<RotationEdge> route) {
		RotationRoute r = new RotationRoute(this);
		r.setRoute(route);
		r.setFFE(demand);
		routes.add(r);	
	}

	public void removeRoute(RotationRoute r) {
		routes.remove(r);		
	}
}
