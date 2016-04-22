package RotationFlow;

import java.util.ArrayList;

public class RotationEdge {
	private RotationGraph graph;
	private RotationNode fromNode;
	private RotationNode toNode;
	private boolean sail;
	private boolean feeder;
	private boolean omission;
	private int noInRotation;
	private int capacity;
	private int cost;
	private ArrayList<RotationRoute> servicedRoutes;
	private boolean active;

	public RotationEdge(RotationGraph graph, RotationNode fromNode, RotationNode toNode, int capacity, int cost, boolean sail, boolean feeder, boolean omission, int noInRotation) {
		super();
		this.graph = graph;
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.capacity = capacity;
		this.cost = cost;
		this.sail = sail;
		this.feeder = feeder;
		this.omission = omission;
		this.noInRotation = noInRotation;
		servicedRoutes = new ArrayList<RotationRoute>();
		fromNode.addOutgoingEdge(this);
		toNode.addIngoingEdge(this);
		active = true;
	}
	
	public void setActive(){
		active = true;
	}
	
	public void setInactive(){
		active = false;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public RotationNode getFromNode() {
		return fromNode;
	}

	public RotationNode getToNode() {
		return toNode;
	}

	public boolean isSail() {
		return sail;
	}

	public boolean isFeeder() {
		return feeder;
	}

	public boolean isOmission() {
		return omission;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getCost() {
		return cost;
	}

	public void addServicedRoute(RotationRoute r){
		servicedRoutes.add(r);
	}

	public void clearServicedRoutes(){
		servicedRoutes.clear();
	}

	public int getLoad(){
		int load = 0;
		for(RotationRoute r : servicedRoutes){
			load += r.getFFE();
		}
		return load;
	}

	public int getOverflow() {
		int overflow = Math.max(0, getLoad() - capacity);
		return overflow;
	}

	public int getNoInRotation(){
		return noInRotation;
	}

	public void removeLoad(RotationBellmanFord bf, int overflow) {
		int highestCost = Integer.MIN_VALUE;
		RotationRoute highestCostRoute = null;
		for(RotationRoute r : servicedRoutes){
			if(r.getFFE() > 0){
				int cost = r.getCost();
				if(cost > highestCost){
					highestCostRoute = r;
					highestCost = cost;
				}
			}
		}
		int removedFFE = Math.min(highestCostRoute.getFFE(), overflow);
		RotationDemand demand = highestCostRoute.getDemand();
		ArrayList<RotationEdge> repRoute = bf.getRouteRep(demand);
		demand.createRepRoute(removedFFE, repRoute);
		highestCostRoute.removeFFE(removedFFE);
	}

	public String getFromPortUNLo() {
		return fromNode.getUNLocode();
	}

	public String getToPortUNLo() {
		return toNode.getUNLocode();
	}

	public void removeServicedRoute(RotationRoute r) {
		servicedRoutes.remove(r);		
	}

	public void decrementNoInRotation(){
		if(!sail){
			throw new RuntimeException("Decrementing no in rotation for a non-sail edge.");
		}
		noInRotation--;
	}
	
	public void incrementNoInRotation(){
		if(!sail){
			throw new RuntimeException("Incrementing no in rotation for a non-sail edge.");
		}
		noInRotation++;
	}
	
	public void delete(){
		active = false;
		fromNode.removeIngoingEdge(this);
		toNode.removeOutgoingEdge(this);
		graph.removeRotationEdge(this);
		active = false;
	}

	public String printType() {
		if(sail)
			return "sail";
		if(omission)
			return "omission";
		if(feeder)
			return "feeder";
		return null;
	}
}
