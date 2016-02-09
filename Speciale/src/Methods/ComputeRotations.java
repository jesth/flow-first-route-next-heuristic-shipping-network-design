package Methods;
import java.util.ArrayList;
import Data.Demand;
import Data.DistanceElement;
import Data.Port;
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
		double loadFactor = 0;
		for(Edge e : rotation.getRotationEdges()){
			if(e.isSail()){
				loadFactor = (double) e.getLoad() / (double) e.getCapacity();
				//TODO hardcoded 95%
				if(loadFactor < 0.95){
					distances.add(e.getDistance());
				}
			}
		}
		
		for(Port p : graph.getData().getPorts().values()){
			
		}
		
		
		ArrayList<Demand> demands = graph.getData().getDemands();
		
		
		ArrayList<Port> rotationPorts = rotation.getPorts();
	}

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
