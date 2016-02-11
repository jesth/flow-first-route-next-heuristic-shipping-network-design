package Methods;
import java.util.ArrayList;
import Data.Demand;
import Data.DistanceElement;
import Data.Port;
import Data.VesselClass;
import Graph.Edge;
import Graph.Graph;
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
		
		int bestProfit = -Integer.MAX_VALUE;
		Port bestPort = null;
		Edge bestEdge = null;
		for(Port p : graph.getData().getPorts().values()){
			for(Edge e : edges){
				DistanceElement d = e.getDistance();
				double detour = getDetour(d, p);
				//TODO: Detour of up to 100 % allowed.
				if(detour < (double) d.getDistance()){
					int profit = calcPortInsertProfit(rotation, p, e);
					if(profit > bestProfit){
						bestProfit = profit;
						bestPort = p;
						bestEdge = e;
					}
				}
			}
		}
		//TODO actually insert port
	}

	public static int calcPortInsertProfit(Rotation rotation, Port insertPort, Edge edge){
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

		profit -= (fuelSail + fuelDwell + TCCost + fixedCall + varCall);
		
		return profit;
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
