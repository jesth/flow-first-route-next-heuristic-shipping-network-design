package RotationFlow;

import java.util.ArrayList;

import Data.Demand;
import Graph.Edge;
import Graph.Node;

public class RotationBellmanFord {
	private ArrayList<RotationNode> unprocessedNodes = new ArrayList<RotationNode>();
	private ArrayList<RotationNode> unprocessedNodesRep = new ArrayList<RotationNode>();
	private RotationGraph graph;

	public RotationBellmanFord(RotationGraph newGraph){
		graph = newGraph;
		reset();
	}

	public void reset(){
		for(RotationNode i : graph.getRotationNodes()){
			if( i!= null){
				for(int j = 0; j < RotationNode.getNoOfCentroids(); j++){
					i.setLabels(j, Integer.MAX_VALUE, null);
				}
				if(i.isFromCentroid()){
					i.setLabels(i.getPort().getPortId(), 0, null);
					i.setUnprocessed(i.getPort().getPortId());
				}
			}
		}
	}

	public void resetRep(){
		for(RotationNode i : graph.getRotationNodes()){
			if( i!= null){
				for(int j = 0; j < RotationNode.getNoOfCentroids(); j++){
					i.setLabelsRep(j, Integer.MAX_VALUE, null);
				}
				if(i.isFromCentroid()){
					i.setLabelsRep(i.getPort().getPortId(), 0, null);
					i.setUnprocessedRep(i.getPort().getPortId());
				}
			}
		}
	}

	public void run(){
		reset();
		while(!unprocessedNodes.isEmpty()){
			RotationNode u = unprocessedNodes.remove(0);
			relaxAll(u);
		}
		for(RotationDemand d : graph.getRotationDemands()){
			ArrayList<RotationEdge> route = getRoute(d);
			d.createRoute(route);
		}
	}

	public void runRep(){
		resetRep();
		while(!unprocessedNodesRep.isEmpty()){
			RotationNode u = unprocessedNodesRep.remove(0);
			relaxAllRep(u);
		}
	}

	public void relaxAll(RotationNode u){
		for(int i = 0; i < RotationNode.getNoOfCentroids(); i++){
			if(u.isUnprocessed(i)){
				for(RotationEdge e : u.getOutgoingEdges()){
					if(e.isActive()){
						relax(i, e);
					}
				}
				u.setProcessed(i);
			}
		}
	}

	public void relaxAllRep(RotationNode u){
		for(int i = 0; i < RotationNode.getNoOfCentroids(); i++){
			if(u.isUnprocessedRep(i)){
				for(RotationEdge e : u.getOutgoingEdges()){
					if(e.isActive()){
						relaxRep(i, e);	
					}
				}
				u.setProcessedRep(i);
			}
		}
	}

	public void relax(int centroidId, RotationEdge e){
		RotationNode u = e.getFromNode();
		RotationNode v = e.getToNode();
		if(u.getDistance(centroidId) < Integer.MAX_VALUE){
			if(v.getDistance(centroidId) > u.getDistance(centroidId) + e.getCost()){
				v.setLabels(centroidId, u.getDistance(centroidId) + e.getCost(), e);
				v.setUnprocessed(centroidId);
			}
		}
	}

	public void relaxRep(int centroidId, RotationEdge e){
		RotationNode u = e.getFromNode();
		RotationNode v = e.getToNode();
		if(u.getDistanceRep(centroidId) < Integer.MAX_VALUE){
			if(v.getDistanceRep(centroidId) > u.getDistanceRep(centroidId) + e.getCost()){
				if(e.getLoad() < e.getCapacity()){
					v.setLabelsRep(centroidId, u.getDistanceRep(centroidId) + e.getCost(), e);
					v.setUnprocessedRep(centroidId);
				}
			}
		}
	}

	public void addUnprocessedNode(RotationNode unprocessedNode){
		if(!unprocessedNodes.contains(unprocessedNode)){
			unprocessedNodes.add(unprocessedNode);
		}
	}

	public void addUnprocessedNodeRep(RotationNode unprocessedNode){
		if(!unprocessedNodesRep.contains(unprocessedNode)){
			unprocessedNodesRep.add(unprocessedNode);
		}
	}

	public static ArrayList<RotationEdge> getRoute(RotationDemand demand){
		RotationNode fromNode = demand.getOrigin();
		RotationNode toNode = demand.getDestination();
		ArrayList<RotationEdge> usedEdges = new ArrayList<RotationEdge>();
		int arrayPos = fromNode.getPort().getPortId();
		RotationEdge predecessor = toNode.getPredecessor(arrayPos);
		usedEdges.add(predecessor);
		while(!predecessor.getFromNode().equals(fromNode)){
			predecessor = predecessor.getFromNode().getPredecessor(arrayPos);
			usedEdges.add(0, predecessor);
		}
		return usedEdges;
	}

	public static ArrayList<RotationEdge> getRouteRep(RotationDemand demand){
		RotationNode fromNode = demand.getOrigin();
		RotationNode toNode = demand.getDestination();
		ArrayList<RotationEdge> usedEdges = new ArrayList<RotationEdge>();
		int arrayPos = fromNode.getPort().getPortId();
		RotationEdge predecessor = toNode.getPredecessorRep(arrayPos);
		usedEdges.add(predecessor);
		while(!predecessor.getFromNode().equals(fromNode)){
			predecessor = predecessor.getFromNode().getPredecessorRep(arrayPos);
			usedEdges.add(0, predecessor);
		}
		return usedEdges;
	}
}
