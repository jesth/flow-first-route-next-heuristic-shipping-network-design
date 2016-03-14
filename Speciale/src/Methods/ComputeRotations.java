package Methods;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.tools.ToolProvider;
import AuxFlow.AuxEdge;
import AuxFlow.AuxGraph;
import AuxFlow.AuxNode;
import Data.Demand;
import Data.Distance;
import Data.DistanceElement;
import Data.Port;
import Data.VesselClass;
import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Results.Result;
import Results.Rotation;
import Results.Route;

public class ComputeRotations {
	private static Graph graph;

	public static void intialize(Graph inputGraph){
		graph = inputGraph;
	}

	public static void createRotations(int[] durationWeeks, ArrayList<AuxEdge> sortedEdges, VesselClass vesselClass){
		for(int i = 0; i < durationWeeks.length; i++){
			createAuxFlowRotation(durationWeeks[i], sortedEdges, vesselClass);
		}

	}

	public static Rotation createAuxFlowRotation(int durationWeeks, ArrayList<AuxEdge> sortedEdges, VesselClass vesselClass){
		int durationHours = (durationWeeks-1) * 7 * 24;
		ArrayList<AuxNode> rotationNodes = new ArrayList<AuxNode>();
		AuxEdge firstEdge = getFirstUnusedEdge(sortedEdges, vesselClass);
		firstEdge.setUsedInRotation();
		AuxNode node1 = firstEdge.getFromNode();
		AuxNode node2 = firstEdge.getToNode();
		rotationNodes.add(node1);
		rotationNodes.add(node2);
		//TODO: Hardcoded - no canals.
		DistanceElement leg1 = graph.getData().getDistanceElement(node1.getPortId(), node2.getPortId(), false, false);
		DistanceElement leg2 = graph.getData().getDistanceElement(node2.getPortId(), node1.getPortId(), false, false);
		//TODO: Port stay hardcoded at 24 hrs.
		double currentDuration = (leg1.getDistance() + leg2.getDistance()) / vesselClass.getDesignSpeed() + 2 * 24;
		while(currentDuration < durationHours){
			currentDuration += addBestLeg(rotationNodes, sortedEdges, vesselClass);
		}
		ArrayList<Integer> ports = convertAuxNodes(rotationNodes);
		return graph.createRotationFromPorts(ports, vesselClass);
	}

	private static AuxEdge getFirstUnusedEdge(ArrayList<AuxEdge> sortedEdges, VesselClass vesselClass){
		for(AuxEdge e : sortedEdges){
			Port fromPort = graph.getPort(e.getFromNode().getPortId());
			Port toPort = graph.getPort(e.getToNode().getPortId());
			if(!e.isUsedInRotation() && fromPort.getDraft() >= vesselClass.getDraft() && toPort.getDraft() >= vesselClass.getDraft()){
				return e;
			}
		}
		return null;
	}

	private static double addBestLeg(ArrayList<AuxNode> rotationNodes, ArrayList<AuxEdge> sortedEdges, VesselClass vesselClass){
		AuxNode firstNode = rotationNodes.get(0);
		AuxNode lastNode = rotationNodes.get(rotationNodes.size()-1);
		double bestRatio = 0;
		AuxNode bestNode = null;
		AuxEdge bestEdge = null;
		double extraDuration = 0;
		for(AuxEdge e : firstNode.getIngoingEdges()){
			AuxNode newNode = e.getFromNode();
			if(!e.isUsedInRotation() && !newNode.equals(lastNode) && graph.getPort(newNode.getPortId()).getDraft() >= vesselClass.getDraft()){
				double newDemand = e.getAvgLoad();
				double detourTime = getDetourTime(lastNode.getPortId(), firstNode.getPortId(), newNode.getPortId(), vesselClass);
				double ratio = newDemand / detourTime;
				if(ratio > bestRatio){
					bestRatio = ratio;
					bestNode = newNode;
					bestEdge = e;
					extraDuration = detourTime;
				}
			}
		}
		for(AuxEdge e : lastNode.getOutgoingEdges()){
			AuxNode newNode = e.getToNode();
			if(!e.isUsedInRotation() && !newNode.equals(firstNode) && graph.getPort(newNode.getPortId()).getDraft() >= vesselClass.getDraft()){
				double newDemand = e.getAvgLoad();
				double detourTime = getDetourTime(lastNode.getPortId(), firstNode.getPortId(), newNode.getPortId(), vesselClass);
				double ratio = newDemand / detourTime;
				if(ratio > bestRatio){
					bestRatio = ratio;
					bestNode = newNode;
					bestEdge = e;
					extraDuration = detourTime;
				}
			}
		}
		rotationNodes.add(bestNode);
		bestEdge.setUsedInRotation();
		return extraDuration;
	}

	public static void addPorts(){
		ArrayList<Port> unservicedPorts = findUnservicedPorts();
		for(Port p : unservicedPorts){
			int profitPotential = p.getTotalProfitPotential();
			Rotation bestRotation = null;
			Edge bestEdge = null;
			int bestProfit = 0;
			for(Rotation r : graph.getResult().getRotations()){
				for(Edge e : r.getRotationEdges()){
					if(e.isSail()){
						int detourCost = calcCostOfPortInsert(r.getVesselClass(), e.getDistance(), p);
						int profit = profitPotential - detourCost;
						if(profit > bestProfit && vesselsAvailable(r, e, p)){
							bestProfit = profit;
							bestRotation = r;
							bestEdge = e;
						}
					}
				}
			}
			if(bestRotation != null){
				graph.insertPort(bestRotation, bestEdge, p);
			}
		}
	}
	
	public static void removePorts(){
		ArrayList<Port> servicedPorts = findServicedPorts();
		for(Port p : servicedPorts){
			int bestProfit = 0;
			Edge bestEdge = null;
			int spareCapacityIn = p.findSpareCapacity(false);
			int spareCapacityOut = p.findSpareCapacity(true);
			for(Edge e : p.getDwellEdges()){
				Rotation r = e.getRotation();
				int saving = calcSavingOfPortRemoval(r, e);
				Node fromNode = e.getFromNode();
				Node toNode = e.getToNode();
				int lostFFEIn = fromNode.getUnloadedFFE() + fromNode.getTransshippedFromFFE() - spareCapacityIn;
				int lostFFEOut = toNode.getLoadedFFE() + toNode.getTransshippedToFFE() - spareCapacityOut;
				int lostFFE = Math.max(lostFFEIn, 0) + Math.max(lostFFEOut, 0);
				int cost = lostFFE * p.getTotalProfitPotential() / p.getTotalDemand();
				int profit = saving - cost;
				if(profit > bestProfit){
					bestProfit = profit;
					bestEdge = e;
				}
			}
			if(bestProfit > 0){
				graph.removePort(bestEdge);
			}
		}
		
	}

	private static boolean vesselsAvailable(Rotation r, Edge e, Port p){
		ArrayList<Port> ports = new ArrayList<Port>();
		for(Edge edge : r.getRotationEdges()){
			if(edge.isSail()){
				Port port = edge.getFromNode().getPort();
				ports.add(port);
				if(edge.equals(e)){
					ports.add(p);
				}
			}
		}
		int newNoOfVessels = calcNumberOfVessels(ports, r.getVesselClass());
		int deltaVessels = newNoOfVessels - r.getNoOfVessels();
		//		System.out.println("Rotation " + r.getId() + " newNoOfVessels " + newNoOfVessels + " r.getNoOfVessels() " + r.getNoOfVessels());
		if(deltaVessels <= r.getVesselClass().getNetNoAvailable()){
			return true;
		}
		return false;
	}

	private static ArrayList<Port> findUnservicedPorts(){
		ArrayList<Port> unservicedPorts = new ArrayList<Port>();
		for(Port p : graph.getData().getPorts()){
			if(p.isActive() && p.getDwellEdges().isEmpty() && p.getTotalDemand() > 0){
				/*
				double rand = Math.random() * (unservicedPorts.size() + 1);
				int index = (int) rand;
				unservicedPorts.add(index, p);
				*/
				
				unservicedPorts.add(p);
				
				/*
				int profitPotential = p.getTotalProfitPotential();
				if(unservicedPorts.isEmpty()){
					unservicedPorts.add(p);
				} else {
					int size = unservicedPorts.size();
					for(int i = 0; i < size; i++){
//						System.out.println("Entering for-loop at i=" + i + " with unservicedPort.size()=" + unservicedPorts.size());
						if(profitPotential > unservicedPorts.get(i).getTotalProfitPotential()){
							unservicedPorts.add(i, p);
							break;

						}
						if(i == size){
							unservicedPorts.add(p);
						}
					}
				}
				*/
			}
		}
		return unservicedPorts;
	}
	
	private static ArrayList<Port> findServicedPorts(){
		ArrayList<Port> servicedPorts = new ArrayList<Port>();
		for(Port p : graph.getData().getPorts()){
			if(p.isActive() && !p.getDwellEdges().isEmpty() && p.getTotalDemand() > 0){
				servicedPorts.add(p);
			}
		}
		return servicedPorts;
	}

	private static ArrayList<Integer> convertAuxNodes(ArrayList<AuxNode> nodes){
		ArrayList<Integer> ports = new ArrayList<Integer>();
		for(AuxNode i : nodes){
			ports.add(i.getPortId());
		}
		return ports;
	}

	public static Rotation createLargestLossRotation(){
		Demand od = graph.getResult().getLargestODLoss();
		Port org = od.getOrigin();
		Port dest = od.getDestination();
		DistanceElement headLeg = graph.getData().getDistanceElement(org, dest, false, false);
		DistanceElement backLeg = graph.getData().getDistanceElement(dest, org, false, false);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		distances.add(headLeg);
		distances.add(backLeg);
		Rotation r = graph.createRotation(distances, graph.getData().getVesselClasses().get(1));
		System.out.println(r);

		return r;
	}


	public static void insertBestPort(Rotation rotation){

		//TODO hardcoded 95%
		ArrayList<Edge> edges = findPotentialEdges(rotation, 0.95);
		ArrayList<Port> rotationPorts = rotation.getPorts();

		int bestProfit = -Integer.MAX_VALUE/2;
		Port bestPort = null;
		Edge bestEdge = null;
		for(Port p : graph.getData().getPortsMap().values()){
			if(p.getDraft() + 0.0001 < rotation.getVesselClass().getDraft() || rotationPorts.contains(p)){
				continue;
			}
			for(Edge e : edges){
				DistanceElement d = e.getDistance();
				double detour = getDetour(d, p.getPortId());
				//TODO: Detour of up to 100 % allowed.
				if(detour < (double) d.getDistance() * 0.5){
					int profit = calcPortInsertProfitSmart(rotation, p, e);
					if(profit > bestProfit){
						bestProfit = profit;
						bestPort = p;
						bestEdge = e;
					}
				}
			}
		}
		System.out.println("Port: " + bestPort.getUNLocode() + " edge: " + bestEdge.simplePrint() + " profit: " + bestProfit);

		Node arrival = graph.createRotationNode(bestPort, rotation, false);
		graph.createLoadUnloadEdge(arrival, bestPort.getToCentroidNode());
		Node departure = graph.createRotationNode(bestPort, rotation, true);
		graph.createLoadUnloadEdge(bestPort.getFromCentroidNode(), departure);
		DistanceElement toPortDistance = graph.getData().getDistanceElement(bestEdge.getFromNode().getPortId(), bestPort.getPortId(), false, false);
		graph.createRotationEdge(rotation, bestEdge.getFromNode(), arrival, 0, rotation.getVesselClass().getCapacity(), -1, toPortDistance);
		graph.createRotationEdge(rotation, arrival, departure, 0, rotation.getVesselClass().getCapacity(), -1, null);
		DistanceElement fromPortDistance = graph.getData().getDistanceElement(bestPort.getPortId(), bestEdge.getToNode().getPortId(), false, false);
		graph.createRotationEdge(rotation, departure, bestEdge.getToNode(), 0, rotation.getVesselClass().getCapacity(), -1, fromPortDistance);

		bestEdge.delete();
		graph.getEdges().remove(bestEdge);
		rotation.calcOptimalSpeed();

	}

	public static ArrayList<Edge> findPotentialEdges(Rotation rotation, double maxLoadFactor){
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		double loadFactor = 0;
		for(Edge e : rotation.getRotationEdges()){
			if(e.isSail()){
				loadFactor = (double) e.getLoad() / (double) e.getCapacity();
				if(loadFactor < maxLoadFactor){
					distances.add(e.getDistance());
					edges.add(e);
				}
			}
		}

		return edges;
	}

	public static int calcPortInsertProfitNaive(Rotation rotation, Port insertPort, Edge edge){
		Port fromPort = edge.getFromNode().getPort();
		Port toPort = edge.getToNode().getPort();
		Demand toInsert = graph.getData().getDemand(fromPort.getPortId(), insertPort.getPortId());
		Demand fromInsert = graph.getData().getDemand(insertPort.getPortId(), toPort.getPortId());

		int roomLeft = edge.getCapacity() - edge.getLoad();

		int profit = 0;
		profit += Math.min(toInsert.getDemand(), roomLeft) * toInsert.getRate();
		profit += Math.min(fromInsert.getDemand(), roomLeft) * fromInsert.getRate();

		VesselClass v = rotation.getVesselClass();

		int detourDays = (int) ((getDetour(edge.getDistance(), insertPort.getPortId()) / v.getDesignSpeed())/ 24.0);

		int fuelSail = detourDays * (int) v.getFuelConsumptionDesign();
		int fuelDwell = (int) v.getFuelConsumptionIdle();
		int TCCost = detourDays * v.getTCRate();
		int fixedCall = insertPort.getFixedCallCost();
		int varCall = insertPort.getVarCallCost() * v.getCapacity();

		int cost = fuelSail + fuelDwell + TCCost + fixedCall + varCall;

		return profit - cost;
	}

	public static int calcPortInsertProfitSmart(Rotation rotation, Port insertPort, Edge edge){
		int roomLeft = edge.getCapacity() - edge.getLoad();

		int insertPortID = insertPort.getPortId();
		int bestProfit = -Integer.MAX_VALUE/2;

		Edge preEdge = edge;
		int preEdgeIndex = rotation.getRotationEdges().indexOf(edge);
		Edge toEdge = edge;
		int toEdgeIndex = rotation.getRotationEdges().indexOf(edge);
		for(Port p : rotation.getPorts()){
			int fromPortCounter = 0;
			int toPortCounter = 0;
			Demand from = graph.getData().getDemand(insertPortID, p.getPortId());
			if(from == null){
				continue;
			}
			boolean pairNotFound = edge.getFromNode().getPortId() != p.getPortId();
			while (pairNotFound){
				preEdgeIndex--;
				if(preEdgeIndex == -1){
					preEdgeIndex = rotation.getRotationEdges().size()-1;
				}
				preEdge = rotation.getRotationEdges().get(preEdgeIndex);
				if(preEdge.isDwell()){
					fromPortCounter++;
				}
				if(preEdge.getFromNode().getPortId() == p.getPortId()){
					pairNotFound = false;
				}
			}		
			int fromProfit = (int) (( Math.min(from.getDemand(), roomLeft) * from.getRate()) * Math.pow(0.9, fromPortCounter));

			Demand to = graph.getData().getDemand(p.getPortId(), insertPortID);
			if(to == null){
				continue;
			}
			pairNotFound = edge.getToNode().getPortId() != p.getPortId();
			while (pairNotFound){
				toEdgeIndex++;
				if(toEdgeIndex == rotation.getRotationEdges().size()){
					toEdgeIndex = 0;
				}
				toEdge = rotation.getRotationEdges().get(toEdgeIndex);
				if(toEdge.isDwell()){
					toPortCounter++;
				}
				if(toEdge.getToNode().getPortId() == p.getPortId()){
					pairNotFound = false;
				}
			}
			int toProfit = (int) (( Math.min(to.getDemand(), roomLeft) * to.getRate()) * Math.pow(0.9, toPortCounter));

			int profit = fromProfit + toProfit;
			if(profit > bestProfit){
				bestProfit = profit;
			}
		}
		int cost = calcCostOfPortInsert(rotation.getVesselClass(), edge.getDistance(), insertPort);

		return bestProfit - cost;
	}

	public static int calcCostOfPortInsert(VesselClass v, DistanceElement leg, Port insertPort){
		int detourDays = (int) ((getDetour(leg, insertPort.getPortId()) / v.getDesignSpeed())/ 24.0);
		int fuelSail = detourDays * (int) v.getFuelConsumptionDesign();
		int fuelDwell = (int) v.getFuelConsumptionIdle();
		int TCCost = detourDays * v.getTCRate();
		int fixedCall = insertPort.getFixedCallCost();
		int varCall = insertPort.getVarCallCost() * v.getCapacity();

		int cost = fuelSail + fuelDwell + TCCost + fixedCall + varCall;

		return cost;
	}
	
	public static int calcSavingOfPortRemoval(Rotation r, Edge dwell){
		Port removePort = dwell.getFromNode().getPort();
		Port prevPort = dwell.getPrevEdge().getFromNode().getPort();
		Port nextPort = dwell.getNextEdge().getToNode().getPort();
		if(!prevPort.equals(nextPort)){
			DistanceElement newDist = graph.getData().getDistanceElement(prevPort, nextPort, false, false);
			return calcCostOfPortInsert(r.getVesselClass(), newDist, removePort);
		}
		Edge newDwell = null;
		for(Edge e : dwell.getNextEdge().getToNode().getOutgoingEdges()){
			if(e.isDwell()){
				newDwell = e;
				break;
			}
		}
		nextPort = newDwell.getFromNode().getPort();
		if(prevPort.equals(nextPort)){
			return r.calcCost();
		}
		DistanceElement newDist = graph.getData().getDistanceElement(prevPort, nextPort, false, false);
		int cost = calcCostOfPortInsert(r.getVesselClass(), newDist, removePort);
		newDist = graph.getData().getDistanceElement(removePort, nextPort, false, false);
		cost += calcCostOfPortInsert(r.getVesselClass(), newDist, prevPort);
		return cost;
	}

	/*
	public static double getDetourPct(Rotation rotation, DistanceElement currentLeg, Port addPort){
		double currLength = rotation.getDistance();
		double detour = getDetour(currentLeg, addPort);
		return (detour / currLength);
	}
	 */

	public static int getDetour(DistanceElement currentLeg, int addPortId){
		int port1 = currentLeg.getOrigin().getPortId();
		int port2 = addPortId;
		int port3 = currentLeg.getDestination().getPortId();
		//TODO: Hardcoded - no canals.
		boolean suez = false;
		boolean panama = false;
		DistanceElement prevLeg = graph.getData().getDistanceElement(port1, port3, suez, panama);
		DistanceElement leg1 = graph.getData().getDistanceElement(port1, port2, suez, panama);
		DistanceElement leg2 = graph.getData().getDistanceElement(port2, port3, suez, panama);
		int extraDist = leg1.getDistance() + leg2.getDistance() - prevLeg.getDistance();
		return extraDist;
	}

	public static double getDetourTime(int fromPortId, int toPortId, int addPortId, VesselClass vesselClass){
		//TODO: Hardcoded - no canals.
		DistanceElement prevLeg = graph.getData().getDistanceElement(fromPortId, toPortId, false, false);
		double extraDist = getDetour(prevLeg, addPortId);
		double extraTime = extraDist / vesselClass.getDesignSpeed() + 24;
		return extraTime;
	}

	public static double costOfCallingPort(Rotation rotation, int portId){
		double cost = 0;
		Edge preEdge = null;
		Edge postEdge = null;
		Edge currentEdge = null;
		for(int i=0; i<rotation.getRotationEdges().size(); i++){
			currentEdge = rotation.getRotationEdges().get(i);
			if(currentEdge.isDwell()){
				continue;
			}
			if(currentEdge.getFromNode().getPortId() == portId){
				preEdge = currentEdge;
				postEdge = rotation.getRotationEdges().get(i+2);
				break;
			}
		}
		int prePortId = preEdge.getFromNode().getPortId();
		int postPortId = postEdge.getFromNode().getPortId();
		DistanceElement leg = graph.getData().getDistanceElement(prePortId, postPortId, false, false);
		Port port = graph.getData().getPort(portId);
		cost = calcCostOfPortInsert(rotation.getVesselClass(), leg, port);

		return cost;
	}

	public static int calcNumberOfVessels(ArrayList<Port> ports, VesselClass vesselClass){
		double distance = getRotationLength(ports);

		double minRotationTime = (24 * ports.size()+ (distance / vesselClass.getMaxSpeed())) / 168.0;
		int lbNoVessels = (int) Math.ceil(minRotationTime);
		double maxRotationTime = (24 * ports.size()+ (distance / vesselClass.getMinSpeed())) / 168.0;
		int ubNoVessels = (int) Math.floor(maxRotationTime);

		int lowestCost = Integer.MAX_VALUE;
		int noVessels = Integer.MAX_VALUE;
		if(lbNoVessels > ubNoVessels){
			return lbNoVessels;
		} else {
			for(int i = lbNoVessels; i <= ubNoVessels; i++){
				double availableTime = 168 * i - 24 * ports.size();
				double speed = distance/availableTime;
				double fuelConsumption = vesselClass.getFuelConsumption(speed);
				double sailTimeDays = (distance / speed) / 24;
				double bunkerConsumption = sailTimeDays * fuelConsumption;
				int bunkerCost = (int) (bunkerConsumption * 600);
				int TCRate = i * vesselClass.getTCRate();
				int cost = bunkerCost + TCRate;
				if(cost < lowestCost){
					lowestCost = cost;
					noVessels = i;
				}
			}
		}

		return noVessels;
	}

	public static int getRotationLength(ArrayList<Port> ports){
		int distance = 0;
		for(int i = 0; i < ports.size()-1; i++){
			int prePortId = ports.get(i).getPortId();
			int postPortId = ports.get(i+1).getPortId();
			DistanceElement dist = graph.getData().getDistanceElement(prePortId, postPortId, false, false);
			distance += dist.getDistance();
			//			System.out.println("Distance from " + ports.get(i).getUNLocode() + " to " + ports.get(i+1).getUNLocode() + " is " + dist.getDistance());
		}
		distance += graph.getData().getDistanceElement(ports.get(ports.size()-1).getPortId(), ports.get(0).getPortId(), false, false).getDistance();
		return distance;
	}
}
