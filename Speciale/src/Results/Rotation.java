package Results;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import Data.Port;
import Data.VesselClass;
import Graph.*;
import RotationFlow.RotationEdge;
import RotationFlow.RotationGraph;

public class Rotation {
	private int id;
	private VesselClass vesselClass;
	private ArrayList<Node> rotationNodes;
	private ArrayList<Edge> rotationEdges;
	private double speed;
	private int noOfVessels;
	private int distance;
	private static AtomicInteger idCounter = new AtomicInteger();
	private boolean active;
	private Graph mainGraph;
	private Graph rotationGraph;

	public Rotation(){
	}

	public Rotation(VesselClass vesselClass, Graph mainGraph) {
		super();
		this.id = idCounter.getAndIncrement();
		this.vesselClass = vesselClass;
		this.rotationNodes = new ArrayList<Node>();
		this.rotationEdges = new ArrayList<Edge>();
		this.active = true;
		this.speed = 0;
		this.noOfVessels = 0;
		this.distance = 0;
		this.mainGraph = mainGraph;
		//		calculateSpeed();
	}
	
	public void createRotationGraph(){
		this.rotationGraph = new Graph(this);
	}
	
//	public void findRotationFlow(){
//		System.out.println("Checking rotation no. " + id);
//		rotationGraph.findFlow();
//		rotationGraph.testRemovePort();
////		rotationGraph.testAddPort();
////		rotationGraph.removeWorstPort();
////		rotationGraph.insertBestPort();
//		rotationGraph.findFlow();
//	}
	
	public void removeWorstPort() throws InterruptedException{
//		for(RotationEdge e : rotationEdges){
////			if(e.isFeeder()){
////				System.out.println(e.getFromPortUNLo() + "-" + e.getToPortUNLo() + " cost: " + e.getCost());
////			}
////		}
//		boolean madeChange = false;
//		int maxIndex = -1;
//		for(RotationEdge e : rotationEdges){
//			if(e.isSail() && e.isActive()){
//				maxIndex = Math.max(maxIndex, e.getNoInRotation());
//			}
//		}
//		int flowCost = rotationGraph.getResult().getObjective();
//		int rotationCost = getRotationCost();
		rotationGraph.runMcf();
		int bestObj = rotationGraph.getResult().getObjective();
		System.out.println("Org obj: " + bestObj);
//		RotationEdge worstInto = rotationEdges.get(maxIndex);
//		RotationEdge worstOut = rotationEdges.get(0);
//		ArrayList<RotationEdge> handledEdges = tryRemovePort(worstInto, worstOut);
//		int rotationObj = getFlowCost() + getRotationCost();
//		if(rotationObj < bestRotationObj){
//			bestRotationObj = rotationObj;
//			madeChange = true;
//		}
//		undoTryRemovePort(handledEdges);
		for(int i=rotationGraph.getEdges().size()-1; i>=0; i--){
			Edge e = rotationGraph.getEdges().get(i);
			if(e.isDwell()){
				Edge newSail = rotationGraph.removePort(e);
				rotationGraph.runMcf();
				int obj = rotationGraph.getResult().getObjective();
				if(obj > bestObj){
					
				}
			}
		}
			
//			RotationEdge into = rotationEdges.get(i);
//			RotationEdge out = rotationEdges.get(i+1);
//			handledEdges = tryRemovePort(into, out);
//			flowCost = getFlowCost();
//			rotationCost = getRotationCost();
//			rotationObj = flowCost + rotationCost;
//			System.out.println("Removing port " + into.getToPortUNLo() + " with flowCost " + flowCost + " & rotationCost " + rotationCost);
//			if(rotationObj < bestRotationObj){
//				worstInto = into;
//				worstOut = out;
//				bestRotationObj = rotationObj;
//				madeChange = true;
//			}
//			undoTryRemovePort(handledEdges);
//		}
//		if(madeChange){
//			System.out.println("Best obj: " + bestRotationObj + " by removing port " + worstInto.getToPortUNLo() + " noInRotation from " + worstInto.getNoInRotation());
//			implementRemovePort(worstInto, worstOut);
//		}
////		for(RotationEdge e : rotationEdges){
////			if(e.isFeeder()){
////				System.out.println(e.getFromPortUNLo() + "-" + e.getToPortUNLo() + " cost: " + e.getCost());
////			}
////		}
//		
	}

	public void calcOptimalSpeed(){
		int lowestCost = Integer.MAX_VALUE;
		int lbNoVessels = calculateMinNoVessels();
		int ubNoVessels = calculateMaxNoVessels();
		if(lbNoVessels > ubNoVessels){
			this.speed = vesselClass.getMinSpeed();
			setNoOfVessels(lbNoVessels);
			setSailTimes();
			setDwellTimes();

		} else {
			for(int i = lbNoVessels; i <= ubNoVessels; i++){
				double speed = calculateSpeed(i);
				int bunkerCost = calcSailingBunkerCost(speed, i);
				int TCRate = i * vesselClass.getTCRate();
				int cost = bunkerCost + TCRate;
				if(cost < lowestCost){
					lowestCost = cost;
					this.speed = speed;
					setNoOfVessels(i);
				}
			}
			setSailTimes();
			setDwellTimes();
		}
	}

	private void setNoOfVessels(int newNoOfVessels){
		vesselClass.removeNoUsed(noOfVessels);
		noOfVessels = newNoOfVessels;
		vesselClass.addNoUsed(newNoOfVessels);
	}

	private void setSailTimes() {
		for(Edge e : rotationEdges){
			if(e.isSail()){
				e.setTravelTime(e.getDistance().getDistance()/this.speed);	
			}
		}
	}

	private void setDwellTimes() {
		double travelTime = 0;
		int numDwells = 0;
		for(Edge e : rotationEdges){
			if(e.isDwell()){
				e.setTravelTime(24.0);
				numDwells++;
			}
			travelTime += e.getTravelTime();
		}
		double diffFromWeek = 168.0 * noOfVessels - travelTime;
		if(diffFromWeek < 0 - Graph.DOUBLE_TOLERANCE){
			throw new RuntimeException("invalid dwell times");
		}
		double extraDwellTime = diffFromWeek / numDwells;
		for(Edge e : rotationEdges){
			if(e.isDwell()){
				e.setTravelTime(e.getTravelTime()+extraDwellTime);
			}
		}
	}

	public double calculateSpeed(int noOfVessels){
		double availableTime = 168 * noOfVessels - 24 * getNoOfPortStays();
		return distance / availableTime;
	}

	public int calculateMinNoVessels(){
		double rotationTime = (24 * getNoOfPortStays() + (distance / vesselClass.getMaxSpeed())) / 168.0;
		int noVessels = (int) Math.ceil(rotationTime);
		return noVessels;
	}

	public int calculateMaxNoVessels(){
		double rotationTime = (24 * getNoOfPortStays() + (distance / vesselClass.getMinSpeed())) / 168.0;
		int noVessels = (int) Math.floor(rotationTime);
		return noVessels;
	}

	public int getDistance(){
		return distance;
	}

	public VesselClass getVesselClass() {
		return vesselClass;
	}

	public void addRotationNode(Node node){
		rotationNodes.add(node);
	}

	public void addRotationEdge(Edge edge){
		if(edge.isSail()){
			int index = edge.getNoInRotation();
			rotationEdges.add(index, edge);
			distance += edge.getDistance().getDistance();
		} else if(edge.isDwell()) {
			rotationEdges.add(edge);
			Port port = edge.getFromNode().getPort();
			port.addDwellEdge(edge);
		}
	}

	public ArrayList<Node> getRotationNodes() {
		return rotationNodes;
	}

	public ArrayList<Edge> getRotationEdges() {
		return rotationEdges;
	}

	public int calcCost(){
		int obj = 0;
		VesselClass v = this.getVesselClass();
		ArrayList<Edge> rotationEdges = this.getRotationEdges();
		double sailingTime = 0;
		double idleTime = 0;
		int portCost = 0;
		int suezCost = 0;
		int panamaCost = 0;
		for (Edge e : rotationEdges){
			if(e.isSail()){
				sailingTime += e.getTravelTime();
				Port p = e.getToNode().getPort();
				portCost += p.getFixedCallCost() + p.getVarCallCost() * v.getCapacity();
				if(e.isSuez()){
					suezCost += v.getSuezFee();
				}
				if(e.isPanama()){
					panamaCost += v.getPanamaFee();
				}
			}
			if(e.isDwell()){
				idleTime += e.getTravelTime();
			}
		}
		//TODO USD per metric tons fuel = 600
		int sailingBunkerCost = calcSailingBunkerCost(speed, noOfVessels);
		double idleBunkerCost = (int) Math.ceil(idleTime/24.0) * v.getFuelConsumptionIdle() * 600;

		int rotationDays = (int) Math.ceil((sailingTime+idleTime)/24.0);
		int TCCost = rotationDays * v.getTCRate();

//		System.out.println("Rotation number "+ this.id);
//		System.out.println("Voyage duration in nautical miles " + distance);
//		System.out.println(this.noOfVessels + " ships needed sailing with speed " + speed);
//		System.out.println("Port call cost " + portCost);
//		System.out.println("Bunker idle burn in Ton " + idleBunkerCost/600.0);
//		System.out.println("Bunker fuel burn in Ton " + sailingBunkerCost/600.0);
//		System.out.println("Total TC cost " + TCCost);
//		System.out.println();
		obj += sailingBunkerCost + idleBunkerCost + portCost + suezCost + panamaCost + TCCost;

		return obj;
	}

	public int calcPortCost(){
		int portCost = 0;
		for(Edge e : rotationEdges){
			if(e.isSail()){
				Port p = e.getToNode().getPort();
				portCost += p.getFixedCallCost() + vesselClass.getCapacity() * p.getVarCallCost();
			}
		}

		return portCost;
	}

	public int calcIdleFuelCost(){
		int idleTime = 0;
		for(Edge e : rotationEdges){
			if(e.isDwell()){
				idleTime += e.getTravelTime();
			}
		}
		//TODO hardCode fuelprice = 600
		int idleCost = (int) (Math.ceil(idleTime/24.0) * vesselClass.getFuelConsumptionIdle() * 600);

		return idleCost;
	}

	//TODO: Hardcoded 24 & 600.
	public int calcSailingBunkerCost(double speed, int noOfVessels){
		double fuelConsumption = vesselClass.getFuelConsumption(speed);
		double sailTimeDays = (distance / speed) / 24.0;
		double bunkerConsumption = sailTimeDays * fuelConsumption;
		return (int) (bunkerConsumption * 600.0);
	}

	public int getNoOfVessels() {
		return noOfVessels;
	}

	public int getId(){
		return id;
	}

	public double getSailTime(){
		double sailTime = 0;
		for(Edge e : rotationEdges){
			if(e.isSail()){
				sailTime += e.getTravelTime();
			}
		}

		return sailTime;
	}

	public ArrayList<Port> getPorts(){
		ArrayList<Port> ports = new ArrayList<Port>();
		for(Edge e : rotationEdges){
			if(e.getToNode().isArrival()){
				ports.add(e.getToNode().getPort());
			}
		}
		return ports;
	}

	public int getNoOfPortStays(){
		int counter = 0;
		for(Edge e : rotationEdges){
			if(e.isDwell()){
				counter++;
			}
		}
		return counter;
	}

	/**
	 * @return active or not
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * Set rotation to active
	 */
	public void setActive() {
		this.active = true;
	}


	/**
	 * Set rotation to inactive
	 */
	public void setInactive(){
		this.active = false;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		String print = "Rotation [vesselClass=" + vesselClass.getName() + ", noOfVessels=" + noOfVessels + "]\n";
		int counter = 0;
		for(Node i : rotationNodes){
			if(i.isDeparture()){
				print += "Port no. " + counter + ": " + i.getPort() + "\n";
				counter++;
			}
		}
		return print;
	}

	public void incrementNoInRotation(int fromNo) {
		for(Edge e : rotationEdges){
			if(e.getNoInRotation() > fromNo){
				e.incrementNoInRotation();
			}
		}
	}

	public void decrementNoInRotation(int fromNo) {
		for(Edge e : rotationEdges){
			if(e.getNoInRotation() > fromNo){
				e.decrementNoInRotation();
			}
		}
	}

	public void subtractDistance(int subtractDistance) {
		distance -= subtractDistance;
	}
	
	public void removePort(int noInRotationIn, int noInRotationOut){
		if(noInRotationIn != noInRotationOut - 1 && noInRotationOut != 0){
			throw new RuntimeException("Input mismatch");
		}
		Edge ingoingEdge = rotationEdges.get(noInRotationIn);
		Edge dwell = ingoingEdge.getNextEdge();
		mainGraph.removePort(dwell);
	}
	
	public void insertPort(int noInRotation, Port p){
		Edge edge = rotationEdges.get(noInRotation);
		if(!edge.isSail()){
			throw new RuntimeException("Wrong input");
		}
		mainGraph.insertPort(this, edge, p);
	}

	public void delete(){
		if(!rotationNodes.isEmpty() || !rotationEdges.isEmpty()){
			throw new RuntimeException("Nodes and edges must be deleted first via Graph class.");
		}
		setNoOfVessels(0);
		distance = 0;
		setInactive();
	}
}
