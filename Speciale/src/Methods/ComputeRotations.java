package Methods;

import java.util.ArrayList;

import Data.Demand;
import Data.DistanceElement;
import Data.Port;
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
	
}
