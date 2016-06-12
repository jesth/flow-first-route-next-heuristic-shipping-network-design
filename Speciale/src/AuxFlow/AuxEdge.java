package AuxFlow;

import java.io.Serializable;

import Data.Data;
import Data.DistanceElement;
import Data.VesselClass;
import Graph.Graph;

public class AuxEdge implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private transient AuxGraph graph;
	private AuxNode fromNode;
	private AuxNode toNode;
	private int cost;
	private int load;
	private double avgLoad;
	private transient DistanceElement distance;
	private transient boolean rotation;
	private int capacity;
	private boolean isUsedInRotation;
	private transient double a;
	private transient double b;

	public AuxEdge(AuxGraph graph, AuxNode fromNode, AuxNode toNode, DistanceElement distance){
		this.graph = graph;
		graph.addEdge(this);
		this.fromNode = fromNode;
		this.toNode = toNode;
		fromNode.addOutgoingEdge(this);
		toNode.addIngoingEdge(this);
		this.load = 0;
		this.avgLoad = 0;
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
		this.avgLoad = 0;
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
	
	private VesselClass getMaxSize(){
		double maxDraftFrom = fromNode.getPort().getDraft();
		double maxDraftTo = toNode.getPort().getDraft();
		VesselClass maxSizeVessel = null;
		int maxSize = 0;
		for(VesselClass v : Data.getVesselClasses()){
			if(v.getNoAvailable() > 0 && v.getDraft() <= maxDraftFrom && v.getDraft() <= maxDraftTo && 
					v.getCapacity() > maxSize && v.getDraft() <= distance.getDraft()){
				maxSizeVessel = v;
				maxSize = v.getCapacity();
			}
		}
		return maxSizeVessel;
	}

	private void calcCostFunction(double startMultiplier, double endMultiplier){
		VesselClass vessel = Data.getVesselClassId(2);
		int panamaCost = 0;
		if(distance.isPanama()){
			panamaCost = vessel.getPanamaFee();
		}
		int suezCost = 0;
		if(distance.isSuez()){
			suezCost = vessel.getSuezFee();
		}
		double sailTimeDays = (distance.getDistance() / vessel.getDesignSpeed()) / 24.0;
		double fuelConsumptionSail = sailTimeDays * vessel.getFuelConsumptionDesign();
		double fuelConsumptionPort = vessel.getFuelConsumptionIdle();
		int fuelCost = (int) (Data.getFuelPrice() * (fuelConsumptionSail + fuelConsumptionPort));
		int portCostFrom = (int) 0.5 * (fromNode.getPort().getFixedCallCost() + fromNode.getPort().getVarCallCost() * vessel.getCapacity());
		int portCostTo = (int) 0.5 * (toNode.getPort().getFixedCallCost() + toNode.getPort().getVarCallCost() * vessel.getCapacity());
		//sailTimeDays + 1 to pay for ½ * 2 port stays.
		int TCCost = (int) (vessel.getTCRate() * (sailTimeDays + 1));
		int totalCost = panamaCost + suezCost + fuelCost + portCostFrom + portCostTo + TCCost;
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
	
	public boolean isLegal(VesselClass v, Graph mainGraph){
		if(v.getCapacity() > 800){
			return true;
		}
		if(v.getCapacity() <= 800){
			if(Data.getPort(fromNode.getPortId()).getDraft() < (v.getDraft() + 0.1) || Data.getPort(toNode.getPortId()).getDraft() < (v.getDraft() + 0.1)){
				return true;
				
			} else if(mainGraph.getPort(fromNode.getPortId()).getTotalDemand() <= v.getCapacity()*2 || mainGraph.getPort(toNode.getPortId()).getTotalDemand() <= v.getCapacity()*2){
				return true;
			}
		}
		return false;
	}
	
	public void convertLoad(double iterations){
		avgLoad += ((double) load / iterations);
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
	
	public boolean isUsedInRotation(){
		return isUsedInRotation;
	}
	
	public void setUsedInRotation(){
		isUsedInRotation = true;
	}
	
	public void setUnusedInRotation(){
		isUsedInRotation = false;
	}

	public double getAvgLoad() {
		return avgLoad;
	}
}
