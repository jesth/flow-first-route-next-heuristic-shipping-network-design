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
		if(rotationGraph == null){
			this.rotationGraph = new Graph(this);
		}
	}

	public void setSubRotation(Rotation r){
		this.subRotation = r;
	}

	/*
	public void findRotationFlow() throws InterruptedException{
		System.out.println("Checking rotation no. " + id);
		rotationGraph.runMcf();
		//		insertBestPort(1.1);
		//		removeWorstPort();
		//		rotationGraph.testAddPort();
		//		rotationGraph.removeWorstPort();
		//		rotationGraph.findFlow();
		//		mainGraph.runMcf();
		rotationGraph.getMcf().saveODSol("ODSolRotation.csv", rotationGraph.getDemands());
		rotationGraph.getMcf().saveRotationSol("RotationSolRotation.csv", rotationGraph.getResult().getRotations());
		rotationGraph.getMcf().saveAllEdgesSol("AllEdgesRotation.csv");
	}
	 */

	public boolean insertBestPort(double flowBonus, double percentOfCapToAccept) throws InterruptedException{
		this.createRotationGraph();
		boolean madeChange = false;
		rotationGraph.runMcf();
		//		rotationGraph.getMcf().saveRotSol("ODSol_before.csv", rotationGraph.getDemands());
		int bestObj = rotationGraph.getResult().getObjective();
		//		int bestObj = -Integer.MAX_VALUE;
		//		System.out.println("\n\nObj before inserting port: " + rotationGraph.getResult().getObjective());
		//		System.out.println("First bestObj: " + bestObj);
		Port bestFeederPort = null;
		Edge worstNextSail = null;
		for(int i=rotationGraph.getEdges().size()-1; i >= 0; i--){
			Edge e = rotationGraph.getEdges().get(i);
			Edge nextSail = null;
			Port feederPort = null;
			if(e.isFeeder() && e.getLoad() >= vesselClass.getCapacity()*percentOfCapToAccept){
				//				System.out.println("percentOfCapToAccept * vesselCap = "+ vesselClass.getCapacity()*percentOfCapToAccept);
				if(e.getFromNode().isFromCentroid()){
					nextSail = e.getToNode().getNextEdge();
					feederPort = e.getFromNode().getPort();	
				} else if(e.getToNode().isToCentroid()){
					nextSail = e.getFromNode().getNextEdge().getNextEdge();
					feederPort = e.getToNode().getPort();
				} else {
					continue;
				}
				if(checkInsertPort(nextSail, feederPort)){
					int obj = (int) (insertPortObjective(nextSail, feederPort, flowBonus));
					//					System.out.println("Feeder from port: " + e.getFromPortUNLo() + " to rotationPort: " + e.getToPortUNLo() +" yielding Try insert obj: " + obj);
					if(obj > bestObj){
						bestObj = obj;
						bestFeederPort = feederPort;
						madeChange = true;
						worstNextSail = nextSail;
					}
				}
			}
		}

		if(madeChange){
			subRotation.implementInsertPort(rotationGraph, bestFeederPort, worstNextSail);
			int noInRot = worstNextSail.getNoInRotation();
			Edge mainGraphWorstNextSail = null;
			for(Edge e : rotationEdges){
				if(e.getNoInRotation() == noInRot){
					mainGraphWorstNextSail = e;
				}
			}
			Port mainGraphBestFeederPort = mainGraph.getPort(bestFeederPort.getPortId());
			this.implementInsertPort(mainGraph, mainGraphBestFeederPort, mainGraphWorstNextSail);
			System.out.println("Improvement by INSERTING. Rotation: " + mainGraphWorstNextSail.getRotation().getId() + " Port: " +mainGraphBestFeederPort.getUNLocode() + " noInRot: " + noInRot);
		}
		return madeChange;
	}

	private ArrayList<Port> getInsertionPortArray(Edge nextSail, Port feederPort) {
		ArrayList<Port> portArray = new ArrayList<Port>();
		for(Node n : rotationGraph.getNodes()){
			if(n.isDeparture() && n.isActive()){
				portArray.add(n.getPort());
				if(n.equals(nextSail.getFromNode())){
					portArray.add(feederPort);
					portArray.add(n.getPort());
				}
			}
		}
		return portArray;
	}

	public int insertPortObjective(Edge nextSailEdge, Port insertPort, double flowBonus) throws InterruptedException{
		Port orgPort = nextSailEdge.getFromNode().getPort();
		if(insertPort.getDraft() < vesselClass.getDraft()){
			throw new RuntimeException("Draft at port trying to insert is too low!");
		}
		Node orgDepNode = nextSailEdge.getFromNode();
		Node orgNextPortArrNode = nextSailEdge.getToNode();

		ArrayList<Node> insertNodes = rotationGraph.tryInsertMakeNodes(subRotation, orgPort, insertPort, nextSailEdge);
		ArrayList<Edge> insertEdges = rotationGraph.tryInsertMakeEdges(subRotation, insertNodes, orgDepNode, orgNextPortArrNode);

		//		if(subRotation.enoughVessels(orgNoVessels)){
		subRotation.calcOptimalSpeed();
		//		} else {
		//			rotationGraph.undoTryInsertMakeNodes(insertNodes, sailEdge);
		////			return -Integer.MAX_VALUE;
		//			throw new RuntimeException("Not enough ships to insert port!");
		//		}
		rotationGraph.runMcf();
		int flowProfit = (int) (rotationGraph.getResult().getFlowProfit(false) * flowBonus);
		int rotationCost = subRotation.calcCost();
		//		System.out.println("flowProfit = " + flowProfit + ". rotationCost = " + rotationCost);
		rotationGraph.undoTryInsertMakeNodes(insertNodes, nextSailEdge);

		subRotation.calcOptimalSpeed();

		return flowProfit-rotationCost;
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

	public void implementInsertPort(Graph graph, Port port, Edge nextSailEdge){
		Port prevPort = nextSailEdge.getFromNode().getPort();
		graph.insertDoublePort(this, nextSailEdge, port, prevPort);
		/*
		int noInRot = nextSailEdge.getNoInRotation();

		incrementNoInRotation(noInRot);
		incrementNoInRotation(noInRot);

		//			System.out.println("bestROTATIONOrgPort: " + bestOrgPort.getUNLocode() + " bestROTATIONFeederPort: " + bestFeederPort.getUNLocode());
		ArrayList<Node> newRotNodes = implementInsertPortNodes(graph, port, nextSailEdge);
		implementInsertPortEdges(graph, newRotNodes, nextSailEdge, noInRot);
		calcOptimalSpeed();
		 */
	}

	private void implementInsertPortEdges(Graph graph, ArrayList<Node> newNodes, Edge worstNextSail, int noInRot) {
		Node bestOrgDepNode = worstNextSail.getFromNode();
		Node bestOrgNextPortArrNode = worstNextSail.getToNode();

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

	private ArrayList<Node> implementInsertPortNodes(Graph graph, Port bestFeederPort, Edge worstNextSail) {
		Port bestOrgPort = worstNextSail.getFromNode().getPort();
		ArrayList<Node> newNodes = graph.tryInsertMakeNodes(this, bestOrgPort, bestFeederPort, worstNextSail);
		graph.deleteEdge(worstNextSail);
		return newNodes;
	}

	public boolean removeWorstPort(double bonus) throws InterruptedException{
		this.createRotationGraph();
		boolean madeChange = false;
		rotationGraph.runMcf();
		int bestObj = rotationGraph.getResult().getObjective();
		//		System.out.println("Org obj: " + bestObj);

		Edge worstDwellEdge = null;
		for(int i=rotationGraph.getEdges().size()-1; i>=0; i--){
			Edge e = rotationGraph.getEdges().get(i);
			if(e.isDwell() && isRelevantToRemove(e)){
				ArrayList<Edge> handledEdges = rotationGraph.tryRemovePort(e, subRotation);
				rotationGraph.runMcf();
				int flowProfit = rotationGraph.getResult().getFlowProfit(false);
				int rotationCost = (int) (subRotation.calcCost()*bonus);
				int obj = flowProfit - rotationCost;
				//				System.out.println("flowProfit: " + flowProfit + " rotationCost: " + rotationCost + " obj: " + obj);

				//				System.out.println("Try obj: " + obj + " by removing " + e.getFromPortUNLo());
				if(obj > bestObj){
					bestObj = obj;
					worstDwellEdge = e;
					madeChange = true;
				}
				rotationGraph.undoTryRemovePort(handledEdges, subRotation);
			}
		}
		if(madeChange){
			implementRemoveWorstPort(worstDwellEdge);
		}
		return madeChange;
	}

	private boolean isRelevantToRemove(Edge eIn) {
		if(!eIn.isDwell()){
			throw new RuntimeException("Input mismatch");
		}
		Port pIn = eIn.getFromNode().getPort();
		for(Edge e : rotationEdges){
			if(e.isDwell()){
				if(e.getFromNode().getPort().equals(pIn) && !e.equals(eIn)){
					return true;
				}
			}
		}
		int unload = eIn.getFromNode().getUnloadedFFE() + eIn.getFromNode().getTransshippedFromFFE();
		int load = eIn.getToNode().getLoadedFFE() + eIn.getToNode().getTransshippedToFFE();
		int totalLoad = unload + load;
		//TODO: Hardcoded parameter 30 %.
		if(totalLoad < 0.3 * vesselClass.getCapacity()){
			return true;
		}
		return false;
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
		System.out.println("Improvement by REMOVING. Rotation: " + bestRealDwell.getRotation().getId() + " Port: " + bestRealDwell.getFromPortUNLo() + " noInRot: " + prevNoInRot);
	}

	public int serviceOmissionDemand(ArrayList<Demand> oldDemands, int portId) throws InterruptedException{
		Port insertPort = rotationGraph.getPort(portId);
		ArrayList<Demand> newDemands = new ArrayList<Demand>();
		ArrayList<Integer> newDemandSizes = new ArrayList<Integer>();
		for(Demand d : oldDemands){
			Port org = rotationGraph.getPort(d.getOrigin().getPortId());			
			Port dest = rotationGraph.getPort(d.getDestination().getPortId());
			int omission = d.getOmissionFFEs();
			Demand newD = rotationGraph.getDemand(org, dest);
			if(newD == null){
				newD = new Demand(d, org, dest, omission);
				rotationGraph.addDemand(newD);

			} else {
				newD.addDemand(omission);
			}
			newDemands.add(newD);
			newDemandSizes.add(omission);
		}

		int closestPortId = findClosestPort(portId);
		int bestObj = -Integer.MAX_VALUE;
		ArrayList<Edge> rotationEdges = subRotation.getRotationEdges();
		for(int i = rotationEdges.size()-1; i >= 0; i--){
			Edge e = rotationEdges.get(i);
			if(e.isSail() && e.getFromNode().getPortId() == closestPortId){
				if(checkInsertPort(e, insertPort)){
					int obj = insertPortObjective(e, insertPort, 1);
					if(obj > bestObj){
						bestObj = obj;
					}
				}
			}
		}
		for(int i = 0; i < newDemands.size(); i++){
			Demand d = newDemands.get(i);
			int dSize = newDemandSizes.get(i);
			d.removeDemand(dSize);
			if(d.getDemand() == 0){
				rotationGraph.removeDemand(d);
			}
		}
		return bestObj;
	}

	public void implementServiceOmissionDemand(ArrayList<Demand> oldDemands, int portId) throws InterruptedException{
		for(Demand d : oldDemands){
			Port org = rotationGraph.getPort(d.getOrigin().getPortId());			
			Port dest = rotationGraph.getPort(d.getDestination().getPortId());
			int omission = d.getOmissionFFEs();
			Demand newD = rotationGraph.getDemand(org, dest);
			if(newD == null){
				newD = new Demand(d, org, dest, omission);
				rotationGraph.addDemand(newD);
			} else {
				newD.addDemand(omission);
			}
		}
		int closestPortId = findClosestPort(portId);
		int bestObj = -Integer.MAX_VALUE;
		int noInRot = -1;
		ArrayList<Edge> rotationEdges = subRotation.getRotationEdges();
		for(int i = rotationEdges.size()-1; i >= 0; i--){
			Edge e = rotationEdges.get(i);
			if(e.isSail() && e.getFromNode().getPortId() == closestPortId){
				int obj = insertPortObjective(e, rotationGraph.getPort(portId), 1);
				if(obj > bestObj){
					bestObj = obj;
					noInRot = e.getNoInRotation();
				}
			}
		}
		Port insertPortRot = rotationGraph.getPort(portId);
		Edge nextSailRot = subRotation.getEdge(noInRot);
		subRotation.implementInsertPort(rotationGraph, insertPortRot, nextSailRot);
		Port insertPortMain = mainGraph.getPort(portId);
		Edge nextSailMain = this.getEdge(noInRot);
		this.implementInsertPort(mainGraph, insertPortMain, nextSailMain);
		System.out.println("Improvement by SERVICE OMISSION. Rotation: " + nextSailMain.getRotation().getId() + " Port: " + Data.getPort(portId).getUNLocode() + " noInRot: " + nextSailMain.getNoInRotation());
	}

	private boolean checkInsertPort(Edge e, Port insertPort) {
		if(insertPort.getDraft() < vesselClass.getDraft()){
			return false;
		}
		ArrayList<Port> portArray = getInsertionPortArray(e, insertPort);
		int neededVessels = ComputeRotations.calcNumberOfVessels(portArray, vesselClass);
		int noVesselsAvailable = noOfVessels + mainGraph.getNoVesselsAvailable(vesselClass.getId()) - mainGraph.getNoVesselsUsed(vesselClass.getId());
		if(noVesselsAvailable < neededVessels){
			return false;
		}
		return true;
	}

	private int findClosestPort(int portId){
		int bestDist = Integer.MAX_VALUE;
		int bestPortId = -1;
		for(Edge e : rotationEdges){
			if(e.isDwell()){
				int p = e.getFromNode().getPortId();
				if(p != portId){
					int dist = Data.getBestDistanceElement(portId, p, vesselClass).getDistance();
					if(dist < bestDist){
						bestDist = dist;
						bestPortId = p;
					}
				}
			}
		}
		return bestPortId;
	}

	private boolean enoughVessels(int noVessels) {
		int lbNoVessels = calcOptimalSpeed();
		//		System.out.println("lb: " + lbNoVessels + " available: " + noVessels);
		if(lbNoVessels <= noVessels){
			return true;
		}
		return false;
	}

	public Edge getEdge(int noInRot){
		for(Edge e : rotationEdges){
			if(e.getNoInRotation() == noInRot){
				return e;
			}
		}
		return null;
	}

	public int calcOptimalSpeed(){
		int lowestCost = Integer.MAX_VALUE;
		int lbNoVessels = calculateMinNoVessels();
		int ubNoVessels = calculateMaxNoVessels();
		int bestNoVessels = -1;
		//		System.out.println("noAvailable: " + mainGraph.getNoVesselsAvailable(vesselClass.getId()) + " lb: " + lbNoVessels + " ub: " + ubNoVessels);
		if(lbNoVessels > ubNoVessels){
			this.speed = vesselClass.getMinSpeed();
			setNoOfVessels(lbNoVessels);
			setSailTimes();
			setDwellTimes();
			return lbNoVessels;
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
					bestNoVessels = i;
				}
			}
			setSailTimes();
			setDwellTimes();
		}
		return bestNoVessels;
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

	public Graph getRotationGraph() {
		return rotationGraph;
	}

	public int getCanalCost(){
		int suezCost = 0;
		int panamaCost = 0;
		for (Edge e : rotationEdges){
			if(e.isSail() && e.isActive()){
				if(e.isSuez()){
					suezCost += vesselClass.getSuezFee();
				}
				if(e.isPanama()){
					panamaCost += vesselClass.getPanamaFee();
				}
			}
		}
		return suezCost + panamaCost;
	}

	public int getTCCost(){
		return noOfVessels * 7 * vesselClass.getTCRate();
	}

	public int getSailingBunkerCost(){
		return calcSailingBunkerCost(speed, noOfVessels);
	}

	public int getIdleBunkerCost(){
		double idleCost = vesselClass.getFuelConsumptionIdle() * Data.getFuelPrice() * getNoOfPortStays();
		return (int) idleCost;
	}

	public int getPortCallCost(){
		int portCost = 0;
		for (Edge e : rotationEdges){
			if(e.isSail() && e.isActive()){
				Port p = e.getToNode().getPort();
				portCost += p.getFixedCallCost() + p.getVarCallCost() * vesselClass.getCapacity();
			}
		}
		return portCost;
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

	public double getLoadFactor(){
		int load = 0;
		int cap = 0;
		for(Edge e : rotationEdges){
			if(e.isSail()){
				load += e.getLoad();
				cap += e.getCapacity();
			}
		}
		double lf = (double) load / (double) cap;
		return lf;
	}

	public void delete(){
		if(!rotationNodes.isEmpty() || !rotationEdges.isEmpty()){
			throw new RuntimeException("Use deleteRotation in Graph class!");
		}
		setNoOfVessels(0);
		distance = 0;
		setInactive();
		if(subRotation != null && rotationGraph != null){
			rotationGraph.deleteRotation(subRotation);
		}
	}

	public ArrayList<Integer> addCallsToList(ArrayList<Integer> portIds){
		for(Node n : rotationNodes){
			if(n.isActive() && n.isDeparture()){
				portIds.add(n.getPortId());
			}
		}
		return portIds;
	}

	public boolean calls(ArrayList<Integer> portIds){
		for(int i : portIds){
			if(calls(i)){
				return true;
			}
		}
		return false;
	}

	public boolean calls(int portId) {
		for(Node n : rotationNodes){
			if(n.isActive() && n.isDeparture() && n.getPortId() == portId){
				return true;
			}
		}
		return false;
	}

	public void removeRotationGraph(){
		this.rotationGraph = null;
	}

	public void checkNoInRotation() {
		int prevNo = -1;
		for(Edge e : rotationEdges){
			if(e.isSail()){
				int no = e.getNoInRotation();
				if(no == prevNo){
					throw new RuntimeException("Duplicate number in rotation! Rotation no. " + id + ", duplicate ID " + no);
				}
				prevNo = no;
			}
		}

	}
}
