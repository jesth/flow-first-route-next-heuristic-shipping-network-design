package AuxFlow;

import Data.DistanceElement;
import Data.VesselClass;

public class AuxEdge {
	private AuxGraph graph;
	private AuxNode fromNode;
	private AuxNode toNode;
	private int cost;
	private int load;
	private double sumLoad;
	private DistanceElement distance;
	private boolean rotation;
	private int capacity;
	private double a;
	private double b;

	public AuxEdge(AuxGraph graph, AuxNode fromNode, AuxNode toNode, DistanceElement distance){
		this.graph = graph;
		graph.addEdge(this);
		this.fromNode = fromNode;
		this.toNode = toNode;
		fromNode.addOutgoingEdge(this);
		toNode.addIngoingEdge(this);
		this.load = 0;
		this.sumLoad = 0;
		this.distance = distance;
		this.rotation = false;
		this.capacity = Integer.MAX_VALUE;
		calcCostFunction(10, 0.001);
		calcCost();
	}
	
	public AuxEdge(AuxEdge copyEdge, int capacity){
		this.graph = copyEdge.graph;
		graph.addEdge(this);
		this.fromNode = copyEdge.fromNode;
		this.toNode = copyEdge.toNode;
		fromNode.addOutgoingEdge(this);
		toNode.addIngoingEdge(this);
		this.load = 0;
		this.sumLoad = 0;
		this.distance = copyEdge.distance;
		this.rotation = true;
		this.capacity = capacity;
		this.cost = 0;
	}

	public void addFFE(){
		load++;
		calcCost();
	}

	public int getCost(){
		return cost;
	}

	private void calcCost(){
		if(!rotation){
			cost = (int) (a * Math.pow(b, load));
		}
	}

	private void calcCostFunction(double startMultiplier, double endMultiplier){
		VesselClass vessel = graph.getLargestVessel();
		//TODO: Add canal cost.
		if(distance.isPanama()){
		}
		if(distance.isSuez()){
		}
		double sailTimeDays = (distance.getDistance() / vessel.getDesignSpeed()) / 24.0;
		double fuelConsumptionSail = sailTimeDays * vessel.getFuelConsumptionDesign();
		double fuelConsumptionPort = vessel.getFuelConsumptionIdle();
		int fuelCost = (int) (600 * (fuelConsumptionSail + fuelConsumptionPort));
		int portCostFrom = (int) 0.5 * (fromNode.getPort().getFixedCallCost() + fromNode.getPort().getVarCallCost() * vessel.getCapacity());
		int portCostTo = (int) 0.5 * (toNode.getPort().getFixedCallCost() + toNode.getPort().getVarCallCost() * vessel.getCapacity());
		int TCCost = (int) (vessel.getTCRate() * sailTimeDays);
		int totalCost = fuelCost + portCostFrom + portCostTo + TCCost;
		int avgCost = totalCost / vessel.getCapacity();

		double startCost = avgCost * startMultiplier;
		a = startCost;
		double endCost = avgCost * endMultiplier;
		b = Math.pow((endCost / startCost), (1.0/ (double) vessel.getCapacity()));
	}

	public AuxNode getFromNode() {
		return fromNode;
	}

	public AuxNode getToNode() {
		return toNode;
	}
	
	public void convertLoad(double iterations){
		sumLoad += ((double) load / iterations);
		load = 0;
		calcCost();
	}

	public int getLoad(){
		return load;
	}
	
	public boolean isFull(){
		if(load >= capacity){
			return true;
		}
		return false;
	}
	
	public boolean isRotation(){
		return rotation;
	}

	public double getSumLoad() {
		return sumLoad;
	}
}
