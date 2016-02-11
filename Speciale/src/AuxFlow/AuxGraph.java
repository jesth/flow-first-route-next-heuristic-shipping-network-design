package AuxFlow;

import java.util.ArrayList;

import Data.Data;
import Data.Distance;
import Data.DistanceElement;
import Data.Port;
import Data.VesselClass;

public class AuxGraph {
	private VesselClass largestVessel;
	private AuxNode[] nodes;
	private ArrayList<AuxEdge> edges;
	private Data data;

	public AuxGraph(Data data){
		this.data = data;
		nodes = new AuxNode[data.getPorts().size()];
		edges = new ArrayList<AuxEdge>();
		ArrayList<VesselClass> vessels = data.getVesselClasses();
		for(int i = 0; i < vessels.size(); i++){
			VesselClass vessel = vessels.get(i);
			if(vessel.getNoAvailable() > 0){
				largestVessel = vessel;
			}
		}
		generateNodes();
		generateEdges();
		AuxDijkstra.initialize(this);
	}

	private void generateNodes(){
		for(Port p : data.getPorts().values()){
			AuxNode newNode = new AuxNode(p);
			nodes[p.getPortId()] = newNode;
		}
	}

	private void generateEdges(){
		Distance[][] distances = data.getDistances();
		for(int i = 0; i < data.getPorts().size(); i++){
			for(int j = 0; j < data.getPorts().size(); j++){
				Distance distance = distances[i][j];
				DistanceElement[] distanceElements = distance.getDistances();
				for(int k = 0; k <= 3; k++){
					DistanceElement distanceElement = distanceElements[k];
					if(distanceElement.getDraft() > 0){
						int fromId = distanceElement.getOrigin().getPortId();
						AuxNode fromNode = nodes[fromId];
						int toId = distanceElement.getDestination().getPortId();
						AuxNode toNode = nodes[toId];
						AuxEdge newEdge = new AuxEdge(this, fromNode, toNode, distanceElement);
						edges.add(newEdge);
					}
				}
			}
		}
	}

	public VesselClass getLargestVessel() {
		return largestVessel;
	}

	public AuxNode[] getNodes(){
		return nodes;
	}

	public AuxNode getNode(int portId){
		return nodes[portId];
	}

	public ArrayList<AuxEdge> getEdges(){
		return edges;
	}

	public Data getData(){
		return data;
	}

}
