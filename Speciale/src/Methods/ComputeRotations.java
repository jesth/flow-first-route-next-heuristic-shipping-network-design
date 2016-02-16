package Methods;
import java.util.ArrayList;

import javax.tools.ToolProvider;

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

public class ComputeRotations {
	private static Graph graph;
	
	public static void intialize(Graph inputGraph){
		graph = inputGraph;
	}
	
	public static Rotation createLargestLossRotation(){
		Demand od = Result.getLargestODLoss();
		Port org = od.getOrigin();
		Port dest = od.getDestination();
		DistanceElement headLeg = graph.getData().getDistanceElement(org, dest, false, false);
		DistanceElement backLeg = graph.getData().getDistanceElement(dest, org, false, false);
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		distances.add(headLeg);
		distances.add(backLeg);
		Rotation r = graph.createRotation(distances, graph.getData().getVesselClasses().get(1));
		Result.addRotation(r);
		System.out.println(r);
		
		return r;
	}
	

	public static void insertBestPort(Rotation rotation){
		ArrayList<DistanceElement> distances = new ArrayList<DistanceElement>();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		ArrayList<Port> rotationPorts = rotation.getPorts();
		double loadFactor = 0;
		for(Edge e : rotation.getRotationEdges()){
			if(e.isSail()){
				loadFactor = (double) e.getLoad() / (double) e.getCapacity();
				//TODO hardcoded 95%
				if(loadFactor < 0.95){
					distances.add(e.getDistance());
					edges.add(e);
				}
			}
		}
		
		int bestProfit = -Integer.MAX_VALUE/2;
		Port bestPort = null;
		Edge bestEdge = null;
		for(Port p : graph.getData().getPorts().values()){
			if(p.getDraft() + 0.0001 < rotation.getVesselClass().getDraft() || rotationPorts.contains(p))
			{
				continue;
			}
			for(Edge e : edges){
				DistanceElement d = e.getDistance();
				double detour = getDetour(d, p);
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
		
		int detourDays = (int) ((getDetour(edge.getDistance(), insertPort) / v.getDesignSpeed())/ 24.0);
		
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
		int cost = calcCostOfPortInsert(rotation, edge.getDistance(), insertPort);
		
		return bestProfit - cost;
	}
	
	public static int calcCostOfPortInsert(Rotation rotation, DistanceElement leg, Port insertPort){
		VesselClass v = rotation.getVesselClass();
		int detourDays = (int) ((getDetour(leg, insertPort) / v.getDesignSpeed())/ 24.0);
		int fuelSail = detourDays * (int) v.getFuelConsumptionDesign();
		int fuelDwell = (int) v.getFuelConsumptionIdle();
		int TCCost = detourDays * v.getTCRate();
		int fixedCall = insertPort.getFixedCallCost();
		int varCall = insertPort.getVarCallCost() * v.getCapacity();

		int cost = fuelSail + fuelDwell + TCCost + fixedCall + varCall;
		
		return cost;
	}
	
	/*
	public static double getDetourPct(Rotation rotation, DistanceElement currentLeg, Port addPort){
		double currLength = rotation.getDistance();
		double detour = getDetour(currentLeg, addPort);
		return (detour / currLength);
	}
	*/
	
	public static int getDetour(DistanceElement currentLeg, Port addPort){
		Port port1 = currentLeg.getOrigin();
		Port port2 = addPort;
		Port port3 = currentLeg.getDestination();
		boolean suez = currentLeg.isSuez();
		boolean panama = currentLeg.isPanama();
		DistanceElement leg1 = graph.getData().getDistanceElement(port1, port2, suez, panama);
		DistanceElement leg2 = graph.getData().getDistanceElement(port2, port3, suez, panama);
		int newDist = leg1.getDistance() + leg2.getDistance();
		return (newDist - currentLeg.getDistance());
	}
}
