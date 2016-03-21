package Graph;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import Data.DistanceElement;
import Methods.BellmanFord;
import Results.Rotation;
import Results.Route;

public class Edge {
	private int id;
	private Node fromNode;
	private Node toNode;
	private int cost; 
	private int realCost;
	private int lagrange;
	private int lagrangeStep;
	private int lagrangeUp;
	private int lagrangeDown;
	private int capacity;
	private double travelTime;
	private boolean omission;
	private boolean sail;
	private boolean dwell;
	private boolean transshipment;
	private boolean loadUnload;
	private Rotation rotation;
	private int noInRotation;
	private ArrayList<Route> routes;
	private static AtomicInteger idCounter = new AtomicInteger();
	private DistanceElement distance;
	private int[] lagrangeValues = new int[1200];
	private int[] loadValues = new int[1200];

	public Edge(){
	}

	/** Constructor for rotation, load/unload and transshipment edges, i.e. not omission edges.
	 * @param fromNode
	 * @param toNode
	 * @param cost - the real cost associated with shipping one FFE on this edge.
	 * @param capacity - the capacity of the edge, measured in FFE per week (???????????????),
	 * @param rotationEdge - boolean, true if sail or dwell, false if transshipment or load/unload.
	 * @param rotation - the rotation represented by the edge. Null if transshipment or load/unload.
	 * @param noInRotation - the number in the rotation for sail edges only. -1 if dwell, transshipment or load/unload.
	 * @param distance - the DistanceElement associated with the edge. Null if dwell, transshipment or load/unload.
	 */
	public Edge(Node fromNode, Node toNode, int cost, int capacity, boolean rotationEdge, Rotation rotation, int noInRotation, DistanceElement distance){
		super();
		this.distance = distance;
		this.id = idCounter.getAndIncrement();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.realCost = cost;
		this.cost = cost;
		this.lagrange = 0;
		//TODO: Hardcoded lagrangeStep.
		this.lagrangeStep = 100;
		this.capacity = capacity;
		this.omission = false;
		this.sail = false;	
		this.dwell = false;
		this.transshipment = false;
		this.loadUnload = false;
		this.rotation = rotation;
		this.noInRotation = noInRotation;
		if(fromNode.isDeparture() && toNode.isArrival() && rotationEdge){
			this.sail = true;
			this.lagrange = 1;
			this.travelTime = this.distance.getDistance()/rotation.getVesselClass().getDesignSpeed();
		} else if(fromNode.isArrival() && toNode.isDeparture() && rotationEdge){
			this.dwell = true;
			//TODO hard-code
			this.travelTime = 24;
		} else if(fromNode.isArrival() && toNode.isDeparture() && !rotationEdge) {
			this.transshipment = true;
			// TODO hard-code
			this.travelTime = 24;
		} else if(fromNode.isArrival() && toNode.isToCentroid() || fromNode.isFromCentroid() && toNode.isDeparture()){
			this.loadUnload = true;
			// TODO hard-code
			this.travelTime = 0;
		} else {
			throw new RuntimeException("Tried to construct an edge that does not fit "
					+ "with either sail, dwell, transshipment or load/unload definitions.");
		}
		toNode.addIngoingEdge(this);
		fromNode.addOutgoingEdge(this);
		routes = new ArrayList<Route>();
	}

	/** Constructor for omission edges.
	 *  Cost is set to the rate of the OD-pair + 1000$.
	 *  Capacity is set default to Integer.MAX_VALUE.
	 * @param fromNode
	 * @param toNode
	 * @param rate - the rate for transporting one FFE from origin to destination for the OD-pair represented by this edge.
	 */
	public Edge(Node fromNode, Node toNode, int rate){
		super();
		this.id = idCounter.getAndIncrement();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.cost = 1000 + rate;
		this.realCost = 1000 + rate;
		this.lagrange = 0;
		this.lagrangeStep = 0;
		this.capacity = Integer.MAX_VALUE;
		this.travelTime = 0;
		this.omission = true;
		this.sail = false;
		this.dwell = false;
		this.transshipment = false;
		this.loadUnload = false;
		this.rotation = null;
		this.noInRotation = -1;
		toNode.addIngoingEdge(this);
		fromNode.addOutgoingEdge(this);
		routes = new ArrayList<Route>();
		this.distance = null;
	}

	/**
	 * @return The id.
	 */
	public int getId(){
		return id;
	}

	/**
	 * @return The fromNode.
	 */
	public Node getFromNode() {
		return fromNode;
	}

	/**
	 * @return The toNode.
	 */
	public Node getToNode() {
		return toNode;
	}

	/**
	 * @return The total cost, i.e. the real cost plus the Lagrange cost.
	 */
	public int getCost() {
		return cost;
	}

	/*
	public void addLagrange(int lagrangeInput){
		if(!this.dwell){ //Dwell edges can never be restricting.
			this.lagrangeStart = lagrangeInput;
		}
	}
	 */

	/*
	public void resetLagrange(){
		int lowestProfit = Integer.MAX_VALUE;
		for(Route r : getRoutes()){
			if(r.getLagrangeProfit() < lowestProfit){
				lowestProfit = r.getLagrangeProfit();
			}
		}
		if(lowestProfit == Integer.MAX_VALUE){
			lowestProfit = -1001;
			throw new RuntimeException("lowestProfit == Integer.MAX_VALUE");
		}

		addLagrange(lowestProfit + 1000);
	}
	 */

	public void lagrangeAdjustment(int iteration){
		if(capacity < getLoad()){
			// if we initially didn't use the edge we set the lagrangeStart to -1,
			// but since edge is now used in a shortest route for a OD-pair we have to update the lagrange.
			/*if(lagrangeStart < 0 && !routes.isEmpty()){
				resetLagrange();
			} 
			 */
			adjustLagrange(iteration, true);
			BellmanFord.relaxEdge(this);
			//					System.out.println("Cost changed from " + wasCost + " to " + e.getCost());
			//					System.out.println();
		} else if(capacity > getLoad()){
			adjustLagrange(iteration, false);
			//			System.out.println(this.simplePrint());
			BellmanFord.relaxEdge(this);
			//					System.out.println("Cost changed from " + wasCost + " to " + e.getCost());
			//					System.out.println();
		} 
	}

	public void adjustLagrange(int iteration, boolean overflow){
		//		int adjust = (int) Math.max( (double)this.lagrangeStart / (double) iteration, 1);
		//		System.out.println("LagrangeStart " + lagrangeStart + " for " + simplePrint());
		if(this.sail){
			if(overflow){
				this.lagrange = Math.max(this.lagrange + lagrangeStep, 1);
				/*
				if(getLagrangeUp() > 0){
					doubleLagrangeStep();
				} else {
					halveLagrangeStep();
				}
				this.lagrange = Math.max(Math.min(this.lagrange + this.lagrangeStep, 1000000),1);
				incrementLagrangeUp();
				resetLagrangeDown();
				*/
			} else {
				this.lagrange = Math.max(this.lagrange - lagrangeStep / 5, 1);
				/*
				if(getLagrangeDown() >= 3 && this.lagrange > 1){
					doubleLagrangeStep();
				} else if(getLagrangeDown() == 0){
					halveLagrangeStep();
				}
				if(getLagrangeDown() >= 3){
					this.lagrange = Math.max(this.lagrange - this.lagrangeStep, 1);
				}
				incrementLagrangeDown();
				resetLagrangeUp();
				*/
			}
			this.cost = this.realCost+this.lagrange;
		}
		lagrangeValues[iteration] = this.lagrange;
		loadValues[iteration] = this.getLoad();
	}

	/**
	 * @return The Lagrange cost.
	 */
	public int getLagrange(){
		return lagrange;
	}

	/** Sets the Lagrange cost to the specified input without conversion.
	 * @param lagrange - the Lagrange cost to be used.
	 */
	public void setLagrange(int lagrange){
		this.lagrange = lagrange;
		this.cost = this.realCost+this.lagrange;
	}

	/**
	 * @return The realCost, i.e. the total cost without the Lagrange cost.
	 */
	public int getRealCost() {
		return realCost;
	}

	/**
	 * @return The capacity.
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @return Whether this is an omission edge.
	 */
	public boolean isOmission() {
		return omission;
	}

	/**
	 * @return Whether this is a sail edge.
	 */
	public boolean isSail() {
		return sail;
	}

	/**
	 * @return Whether this is a dwell edge.
	 */
	public boolean isDwell() {
		return dwell;
	}

	/**
	 * @return Whether this is a transshipment edge.
	 */
	public boolean isTransshipment() {
		return transshipment;
	}

	/**
	 * @return Whether this is a load/unload edge.
	 */
	public boolean isLoadUnload() {
		return loadUnload;
	}

	/**
	 * @return Whether this edge passes the Suez canal.
	 */
	public boolean isSuez() {
		return distance.isSuez();
	}

	/**
	 * @return Whether this edge passes the Panama canal.
	 */
	public boolean isPanama() {
		return distance.isPanama();
	}

	/**
	 * @return the distance
	 */
	public DistanceElement getDistance() {
		return distance;
	}

	/**
	 * @return The travel time.
	 */
	public double getTravelTime() {
		return travelTime;
	}

	/**
	 * @return The UNLo-code of the from port.
	 */
	public String getFromPortUNLo(){
		return fromNode.getPort().getUNLocode();
	}

	/**
	 * @return The UNLo-code of the to port.
	 */
	public String getToPortUNLo(){
		return toNode.getPort().getUNLocode();
	}

	/**
	 * @return The rotation that is represented by the edge.
	 */
	public Rotation getRotation(){
		return this.rotation;
	}

	public int[] getLagrangeValues(){
		return lagrangeValues;
	}

	public int[] getLoadValues(){
		return loadValues;
	}

	/**
	 * @return What number in the rotation the edge is. I.e. 1 if the edge is first in the rotation.
	 */
	public int getNoInRotation(){
		return this.noInRotation;
	}

	/**
	 * @param travelTime the travelTime to set
	 */
	public void setTravelTime(double travelTime) {
		this.travelTime = travelTime;
	}

	/** Removes all routes from the routes array.
	 * 
	 */
	public void clearRoutes(){
		routes.clear();
	}

	/**
	 * @param removeRoute - the route to be removed from the routes array.
	 */
	public void removeRoute(Route removeRoute){
		routes.remove(removeRoute);
	}

	/** Adds the input to the list of routes.
	 * @param newRoute - the route to be added.
	 */
	public void addRoute(Route newRoute){
		routes.add(newRoute);
	}

	/**
	 * @return The list of routes that has the shortest path through this edge.
	 */
	public ArrayList<Route> getRoutes(){
		return routes;
	}

	public void incrementNoInRotation(){
		noInRotation++;
	}

	public void decrementNoInRotation(){
		noInRotation--;
	}

	/**
	 * @return The repaired load, i.e. the sum of all FFErep for the routes of this edge.
	 */
	public int getRepLoad(){
		int repLoad = 0;
		for(Route r : routes){
			repLoad += r.getFFErep();
		}
		return repLoad;
	}

	/**
	 * @return The load, i.e. the sum of all FFE for the routes of this edge.
	 */
	public int getLoad() {
		int load = 0;
		for(Route r : routes){
			load += r.getFFE();
		}
		return load;
	}

	public Edge getPrevEdge(){
		return fromNode.getPrevEdge();
	}

	public Edge getNextEdge(){
		return toNode.getNextEdge();
	}

	public int getSpareCapacity(){
		return capacity - getLoad();
	}

	public int getLagrangeDown(){
		return lagrangeDown;
	}

	public int getLagrangeUp(){
		return lagrangeUp;
	}

	public void resetLagrangeDown(){
		lagrangeDown = 0;
	}

	public void resetLagrangeUp(){
		lagrangeUp = 0;
	}

	public void incrementLagrangeUp(){
		lagrangeUp++;
	}

	public void incrementLagrangeDown(){
		lagrangeDown++;
	}

	public void doubleLagrangeStep(){
		lagrangeStep = lagrangeStep * 2;
	}

	public void halveLagrangeStep(){
		int decrement = lagrangeStep / 4;
		lagrangeStep = Math.max(lagrangeStep - decrement, 1);
//		lagrangeStep = Math.max(lagrangeStep / 2, 1);
	}

	public void delete(){
		fromNode.removeOutgoingEdge(this);
		toNode.removeIngoingEdge(this);
		if(this.isDwell() || this.isSail()){
			rotation.getRotationEdges().remove(this);
			if(this.isSail()){
				rotation.subtractDistance(this.distance.getDistance());
			}
			if(this.isDwell()){
				fromNode.getPort().removeDwellEdge(this);
			}
		}
	}

	/**
	 * @return A simple print of the edge.
	 */
	public String simplePrint(){
		String str = "Edge: ";
		if(sail || omission){
			str+= fromNode.getPort().getUNLocode() + "-" + toNode.getPort().getUNLocode();
			if(omission){
				str+= " omission";
			}
		} else if(dwell){
			str+= "Dwell in " + fromNode.getPort().getUNLocode();
		} else if(loadUnload){
			str+= "Load/unload in " + fromNode.getPort().getUNLocode();
		} else {
			str+="??????";
		}
		//		str +=  " with load: " + load + " and capacity: " + capacity;
		return str;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Edge [fromNode=" + fromNode + ", toNode=" + toNode + ", cost=" + cost + ", capacity=" + capacity
				+ ", omission=" + omission + "]";
	}

}
