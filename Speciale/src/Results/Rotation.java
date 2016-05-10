package Results;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.omg.CORBA.SystemException;

import Data.Data;
import Data.Demand;
import Data.DistanceElement;
import Data.Port;
import Data.VesselClass;
import Graph.*;
import Methods.ComputeRotations;
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
	private Rotation subRotation;

	public Rotation(){
	}

	public Rotation(VesselClass vesselClass, Graph mainGraph, int id) {
		super();
		if(id == -1){
			this.id = idCounter.getAndIncrement();
		} else {
			this.id = id;
		}
		this.vesselClass = vesselClass;
		this.rotationNodes = new ArrayList<Node>();
		this.rotationEdges = new ArrayList<Edge>();
		this.active = true;
		this.speed = 0;
		this.noOfVessels = 0;
		this.distance = 0;
		this.mainGraph = mainGraph;
		this.rotationGraph = null;
		this.subRotation = null;
		mainGraph.getResult().addRotation(this);
		//		calculateSpeed();
	}

	public void createRotationGraph(){
		this.rotationGraph = new Graph(this);
	}

	public void setSubRotation(Rotation r){
		this.subRotation = r;
	}

	public void findRotationFlow() throws InterruptedException{
		System.out.println("Checking rotation no. " + id);
		rotationGraph.runMcf();
		//		removeWorstPort();
		//		rotationGraph.testAddPort();
		//		rotationGraph.removeWorstPort();
		insertBestPort();
		//		rotationGraph.findFlow();
		//		mainGraph.runMcf();
		//		mainGraph.getMcf().saveODSol("ODSol.csv", mainGraph.getDemands());
	}

	public boolean insertBestPort() throws InterruptedException{
		boolean madeChange = false;
		int noVesselsAvailable = noOfVessels + mainGraph.getNoVesselsAvailable(vesselClass.getId())-mainGraph.getNoVesselsUsed(vesselClass.getId());
		rotationGraph.runMcf();
		rotationGraph.getMcf().saveODSol("ODSol_before.csv", rotationGraph.getDemands());
		rotationGraph.getMcf().saveRotationSol("RotationSol_before.csv", rotationGraph.getResult().getRotations());
		//		rotationGraph.getMcf().saveRotSol("ODSol_before.csv", rotationGraph.getDemands());
		int bestObj = rotationGraph.getResult().getObjective();
		//		int bestObj = -Integer.MAX_VALUE;
		System.out.println("Obj before insrting port: " + rotationGraph.getResult().getObjective());
		System.out.println("\n\nOrg obj: " + bestObj);
		Port bestOrgPort = null;
		Port bestFeederPort = null;
		Node bestOrgDepNode = null;
		Node bestOrgNextPortArrNode = null;
		Edge worstNextSail = null;
		for(int i=rotationGraph.getEdges().size()-1; i >= 0; i--){
			Edge e = rotationGraph.getEdges().get(i);
			Edge nextSail = null;
			Port feederPort = null;
//			if(e.isFeeder()){
//				String fromPort = "EGPSD";
//				String feederPort = "NLRTM";
//				if(e.getToPortUNLo().equals(fromPort) && e.getFromPortUNLo().equals(feederPort)){
//					int edgeCost = e.getCost();
//					throw new RuntimeException(feederPort + " -> " +fromPort+ " costs : " + edgeCost);
//				}
//			}
			if(e.isFeeder()){
				if(e.getFromNode().isFromCentroid()){
					nextSail = e.getToNode().getNextEdge();
					feederPort = e.getFromNode().getPort();	
				}
				if(e.getToNode().isToCentroid()){
					nextSail = e.getFromNode().getNextEdge().getNextEdge();
					feederPort = e.getToNode().getPort();
				}
				if(feederPort.getDraft() < vesselClass.getDraft()){
					continue;
				}
				ArrayList<Port> portArray = getInsertionPortArray(nextSail, feederPort);
				int neededVessels = ComputeRotations.calcNumberOfVessels(portArray, vesselClass);
				if(noVesselsAvailable < neededVessels){
					continue;
				}
				
				int obj = insertPortDetour(nextSail, feederPort, noVesselsAvailable);
				System.out.println("Feeder from port: " + e.getFromPortUNLo() + " to rotationPort: " + e.getToPortUNLo() +" yielding Try insert obj: " + obj);
				if(obj > bestObj){
					bestObj = obj;
					System.out.println("IMPROVEMENT");
//					bestOrgPort = nextSail.getFromNode().getPort();
					bestFeederPort = feederPort;
//					bestOrgDepNode = nextSail.getFromNode();
//					bestOrgNextPortArrNode = nextSail.getToNode();
					madeChange = true;
					worstNextSail = nextSail;
				}
			}
		}

		if(madeChange){
			int noInRot = worstNextSail.getNoInRotation();
			Edge mainGraphNextSail = null;
			for(Edge e : rotationEdges){
				if(e.getNoInRotation() == noInRot){
					mainGraphNextSail = e;
				}
			}			
			incrementNoInRotation(noInRot);
			incrementNoInRotation(noInRot);
			//			System.out.println("bestROTATIONOrgPort: " + bestOrgPort.getUNLocode() + " bestROTATIONFeederPort: " + bestFeederPort.getUNLocode());
			//			ArrayList<Node> newRotNodes = implementInsertPortNodes(rotationGraph, bestOrgPort, bestFeederPort);
			//			implementInsertPortEdges(rotationGraph, newRotNodes, bestOrgDepNode, bestOrgNextPortArrNode, prevNoInRot);

			bestOrgPort = mainGraph.getPort(worstNextSail.getFromNode().getPort().getPortId());
			bestFeederPort = mainGraph.getPort(bestFeederPort.getPortId());
			System.out.println("bestMAINOrgPort: " + bestOrgPort.getUNLocode() + " bestMAINFeederPort: " + bestFeederPort.getUNLocode());
			bestOrgDepNode = mainGraphNextSail.getFromNode();
			bestOrgNextPortArrNode = mainGraphNextSail.getToNode();
			ArrayList<Node> newRotNodes = implementInsertPortNodes(mainGraph, bestOrgPort, bestFeederPort, worstNextSail);
			implementInsertPortEdges(mainGraph, newRotNodes, bestOrgDepNode, bestOrgNextPortArrNode, noInRot);

			//			rotationGraph.deleteEdge(worstFromFeeder);
			//			if(worstToFeeder != null){
			//				rotationGraph.deleteEdge(worstToFeeder);	
			//			}
			//			rotationGraph.deleteEdge(worstNextSail);

			//			for(Node n : rotationNodes){
			//				if(n.isArrival()){
			//					System.out.println(n.getPort().getUNLocode());
			//				}
			//			}
			System.out.println("MADE CHANGE");
			this.calcOptimalSpeed();
		}
//		mainGraph.runMcf();
		mainGraph.getMcf().saveODSol("ODSol_after.csv", mainGraph.getDemands());
		mainGraph.getMcf().saveRotationSol("RotationSol_after.csv", mainGraph.getResult().getRotations());
		return madeChange;
	}
	
	private ArrayList<Port> getInsertionPortArray(Edge nextSail, Port feederPort) {
		ArrayList<Port> portArray = new ArrayList<Port>();
		Edge firstEdge = null;
		for(Edge anySail : rotationGraph.getEdges()){
			if(anySail.isSail()){
				firstEdge = anySail;
				break;
			}
		}
		portArray.add(firstEdge.getToNode().getPort());
		Edge currentEdge = firstEdge.getNextEdge();
		while(currentEdge != firstEdge){
			if(currentEdge.isSail()){
				portArray.add(currentEdge.getToNode().getPort());
			}
			if(currentEdge.getToNode().isEqualTo(nextSail.getFromNode())){
				portArray.add(feederPort);
				portArray.add(nextSail.getFromNode().getPort());
			}
			currentEdge = currentEdge.getNextEdge();
		}
		
		return portArray;
	}

	public int insertPortDetour(Edge sailEdge, Port insertPort, int orgNoVessels) throws InterruptedException{
		Port orgPort = sailEdge.getFromNode().getPort();
		if(insertPort.getDraft() < vesselClass.getDraft()){
			throw new RuntimeException("Draft at port trying to insert is too low!");
		}
		Node orgDepNode = sailEdge.getFromNode();
		Node orgNextPortArrNode = sailEdge.getToNode();
		
		ArrayList<Node> insertNodes = rotationGraph.tryInsertMakeNodes(subRotation, orgPort, insertPort, sailEdge);
		ArrayList<Edge> insertEdges = rotationGraph.tryInsertMakeEdges(subRotation, insertNodes, orgDepNode, orgNextPortArrNode);
		
		if(subRotation.enoughVessels(orgNoVessels)){
			subRotation.calcOptimalSpeed();
		} else {
			rotationGraph.undoTryInsertMakeNodes(insertNodes, sailEdge);
//			return -Integer.MAX_VALUE;
			throw new RuntimeException("Not enough ships to insert port!");
		}
		rotationGraph.runMcf();
		int obj = rotationGraph.getResult().getObjective();
		
		rotationGraph.undoTryInsertMakeNodes(insertNodes, sailEdge);
		subRotation.calcOptimalSpeed();
		
		return obj;
	}

//	private Edge getToFeeder(Node orgDepNode, Port feederPort) {
//		Edge toFeeder = null;
//		for(Edge outEdge : orgDepNode.getPrevEdge().getFromNode().getOutgoingEdges()){
//			if(outEdge.isFeeder() && outEdge.getToNode().getPort().getUNLocode().equals(feederPort.getUNLocode())){
//				toFeeder = outEdge;
//				break;
//			}
////			if(outEdge.isFeeder()){
////				System.out.println("feeder edge outEdge going from: " + outEdge.getFromPortUNLo() + " to feeder port: " + outEdge.getToPortUNLo() );
////			}	
//		}
//
//		return toFeeder;
//	}

	private void implementInsertPortEdges(Graph graph, ArrayList<Node> newNodes, Node bestOrgDepNode, Node bestOrgNextPortArrNode, int noInRot) {
		Node newFeederArrNode = newNodes.get(0);
		Node newFeederDepNode = newNodes.get(1);
		Node newOrgArrNode = newNodes.get(2);
		Node newOrgDepNode = newNodes.get(3);
		DistanceElement newToFeederPortDist = Data.getBestDistanceElement(bestOrgDepNode.getPort(), newFeederArrNode.getPort(), this.getVesselClass());
		DistanceElement newFromFeederPortDist = Data.getBestDistanceElement(newFeederDepNode.getPort(), newOrgArrNode.getPort(), this.getVesselClass());
		DistanceElement newOrgSailDist = Data.getBestDistanceElement(newOrgDepNode.getPort(), bestOrgNextPortArrNode.getPort(), this.getVesselClass());
		graph.createRotationEdge(this, bestOrgDepNode, newFeederArrNode, 0, this.getVesselClass().getCapacity(), noInRot, newToFeederPortDist);
		graph.createRotationEdge(this, newFeederDepNode, newOrgArrNode, 0, this.getVesselClass().getCapacity(), noInRot+1, newFromFeederPortDist);
		graph.createRotationEdge(this, newOrgDepNode, bestOrgNextPortArrNode, 0, this.getVesselClass().getCapacity(), noInRot+2, newOrgSailDist);

		Edge newRotFeederDwell = graph.createRotationEdge(this, newFeederArrNode, newFeederDepNode, 0, this.getVesselClass().getCapacity(), -1, null);
		graph.createTransshipmentEdges(newRotFeederDwell);
		graph.createLoadUnloadEdges(newRotFeederDwell);

		Edge newRotOrgDwell = graph.createRotationEdge(this, newOrgArrNode, newOrgDepNode, 0, this.getVesselClass().getCapacity(), -1, null);
		graph.createTransshipmentEdges(newRotOrgDwell);
		graph.createLoadUnloadEdges(newRotOrgDwell);

	}

	private ArrayList<Node> implementInsertPortNodes(Graph graph, Port bestOrgPort, Port bestFeederPort, Edge nextSail) {
		ArrayList<Node> newNodes = graph.tryInsertMakeNodes(this, bestOrgPort, bestFeederPort, nextSail);
		graph.deleteEdge(nextSail);
		return newNodes;
	}

	public boolean removeWorstPort() throws InterruptedException{
		boolean madeChange = false;
		rotationGraph.runMcf();
		int bestObj = rotationGraph.getResult().getObjective();
		System.out.println("Org obj: " + bestObj);

		Edge worstDwellEdge = null;
		for(int i=rotationGraph.getEdges().size()-1; i>=0; i--){
			Edge e = rotationGraph.getEdges().get(i);
			if(e.isDwell()){
				ArrayList<Edge> handledEdges = rotationGraph.tryRemovePort(e, this);
				rotationGraph.runMcf();
				int obj = rotationGraph.getResult().getObjective();
				System.out.println("Try obj: " + obj + " by removing " + e.getFromPortUNLo());
				if(obj > bestObj){
					bestObj = obj;
					worstDwellEdge = e;
					madeChange = true;
				}
				rotationGraph.undoTryRemovePort(handledEdges, this);
			}
		}
		if(madeChange){
			implementRemoveWorstPort(worstDwellEdge);
			rotationGraph.runMcf();
		}
		return madeChange;
	}

	public void implementRemoveWorstPort(Edge bestDwellEdge){
		int prevNoInRot = bestDwellEdge.getPrevEdge().getNoInRotation();
		Edge bestRealDwell = null;
		for(Edge e : rotationEdges){
			if(e.getNoInRotation()== prevNoInRot){
				bestRealDwell = e.getNextEdge();
				if(!bestRealDwell.isDwell()){
					throw new RuntimeException("Input mismatch. Edge found was not dwell");
				}
				break;
			}
		}
		rotationGraph.removePort(bestDwellEdge);
		mainGraph.removePort(bestRealDwell);
	}

	private boolean enoughVessels(int noVessels) {
		int lbNoVessels = calculateMinNoVessels();
		System.out.println("lb: " + lbNoVessels + " available: " + noVessels);
		if(lbNoVessels <= noVessels){
			return true;
		}
		return false;
	}

	public void calcOptimalSpeed(){
		int lowestCost = Integer.MAX_VALUE;
		int lbNoVessels = calculateMinNoVessels();
		int ubNoVessels = calculateMaxNoVessels();
		//		System.out.println("noAvailable: " + mainGraph.getNoVesselsAvailable(vesselClass.getId()) + " lb: " + lbNoVessels + " ub: " + ubNoVessels);
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
				if(cost < lowestCost && i <= mainGraph.getNoVesselsAvailable(vesselClass.getId())){
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
		mainGraph.removeNoUsed(vesselClass, noOfVessels);
		noOfVessels = newNoOfVessels;
		mainGraph.addNoUsed(vesselClass, newNoOfVessels);
	}

	private void setSailTimes() {
		for(Edge e : rotationEdges){
			if(e.isSail() && e.isActive()){
				e.setTravelTime(e.getDistance().getDistance()/this.speed);	
			}
		}
	}

	private void setDwellTimes() {
		double travelTime = 0;
		int numDwells = 0;
		for(Edge e : rotationEdges){
			if(e.isDwell() && e.isActive()){
				e.setTravelTime(Data.getPortStay());
				numDwells++;
			}
			if(e.isActive()){
				travelTime += e.getTravelTime();
			}
		}
		double diffFromWeek = 168.0 * noOfVessels - travelTime;
		if(diffFromWeek < 0 - Graph.DOUBLE_TOLERANCE){
			throw new RuntimeException("invalid dwell times. DiffFromWeek: " + diffFromWeek);
		}
		double extraDwellTime = diffFromWeek / numDwells;
		for(Edge e : rotationEdges){
			if(e.isDwell() && e.isActive()){
				e.setTravelTime(e.getTravelTime()+extraDwellTime);
			}
		}
	}

	public double calculateSpeed(int noOfVessels){
		double availableTime = 168 * noOfVessels - Data.getPortStay() * getNoOfPortStays();
		return distance / availableTime;
	}

	public int calculateMinNoVessels(){
		double rotationTime = (Data.getPortStay() * getNoOfPortStays() + (distance / vesselClass.getMaxSpeed())) / 168.0;
		int noVessels = (int) Math.ceil(rotationTime);
		return noVessels;
	}

	public int calculateMaxNoVessels(){
		double rotationTime = (Data.getPortStay() * getNoOfPortStays() + (distance / vesselClass.getMinSpeed())) / 168.0;
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
			if(e.isSail() && e.isActive()){
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
			if(e.isDwell() && e.isActive()){
				idleTime += e.getTravelTime();
			}
		}
		int sailingBunkerCost = calcSailingBunkerCost(speed, noOfVessels);
		double idleBunkerCost = (int) Math.ceil(idleTime/24.0) * v.getFuelConsumptionIdle() * Data.getFuelPrice();

		int rotationDays = (int) Math.ceil((sailingTime+idleTime)/24.0);
		int TCCost = rotationDays * v.getTCRate();
		//		System.out.println("Rotation number "+ this.id);
		//		System.out.println("Voyage duration in nautical miles " + distance);
		//		System.out.println(this.noOfVessels + " ships needed sailing with speed " + speed);
		//		System.out.println("Port call cost " + portCost);
		//		System.out.println("Bunker idle burn in Ton " + idleBunkerCost/(double)Data.getFuelPrice());
		//		System.out.println("Bunker fuel burn in Ton " + sailingBunkerCost/(double)Data.getFuelPrice());
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
		int idleCost = (int) (Math.ceil(idleTime/24.0) * vesselClass.getFuelConsumptionIdle() * Data.getFuelPrice());

		return idleCost;
	}

	public int calcSailingBunkerCost(double speed, int noOfVessels){
		double fuelConsumption = vesselClass.getFuelConsumption(speed);
		double sailTimeDays = (distance / speed) / 24.0;
		double bunkerConsumption = sailTimeDays * fuelConsumption;
		return (int) (bunkerConsumption * Data.getFuelPrice());
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
			if(e.isDwell() && e.isActive()){
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

	public void addDistance(int addDistance) {
		distance += addDistance;
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

	public boolean calls(int portId) {
		for(Node n : rotationNodes){
			if(n.getPortId() == portId){
				return true;
			}
		}
		return false;
	}
}
