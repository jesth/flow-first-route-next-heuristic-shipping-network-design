package RotationFlow;

import java.util.ArrayList;

import Data.Port;
import Graph.Edge;

public class RotationNode {
	private RotationGraph graph;
	private Port port;
	private ArrayList<RotationEdge> ingoingEdges;
	private ArrayList<RotationEdge> outgoingEdges;
	private int[] distances;
	private RotationEdge[] predecessors;
	private boolean[] unprocessed;
	private int[] distancesRep;
	private RotationEdge[] predecessorsRep;
	private boolean[] unprocessedRep;
	private boolean rotation;
	private boolean fromCentroid;
	private static int noOfCentroids;

	public RotationNode(RotationGraph graph, Port port){
		this.graph = graph;
		this.port = port;
		this.ingoingEdges = new ArrayList<RotationEdge>();
		this.outgoingEdges = new ArrayList<RotationEdge>();
		this.distances = new int[noOfCentroids];
		this.predecessors = new RotationEdge[noOfCentroids];
		this.unprocessed = new boolean[noOfCentroids];
		this.distancesRep = new int[noOfCentroids];
		this.predecessorsRep = new RotationEdge[noOfCentroids];
		this.unprocessedRep = new boolean[noOfCentroids];
		this.rotation = false;
		this.fromCentroid = false;
	}

	public static void setNoOfCentroids(int newNoOfCentroids){
		noOfCentroids = newNoOfCentroids;
	}

	public static int getNoOfCentroids(){
		return noOfCentroids;
	}

	public void addIngoingEdge(RotationEdge e){
		ingoingEdges.add(e);
	}	

	public void removeIngoingEdge(RotationEdge e) {
		ingoingEdges.remove(e);
	}

	public void addOutgoingEdge(RotationEdge e){
		outgoingEdges.add(e);
	}

	public void removeOutgoingEdge(RotationEdge e) {
		outgoingEdges.remove(e);
	}

	public ArrayList<RotationEdge> getIngoingEdges(){
		return ingoingEdges;
	}

	public ArrayList<RotationEdge> getOutgoingEdges(){
		return outgoingEdges;
	}

	public void setLabels(int centroidId, int distance, RotationEdge predecessor){
		distances[centroidId] = distance;
		predecessors[centroidId] = predecessor;
	}

	public void setLabelsRep(int centroidId, int distance, RotationEdge predecessor){
		distancesRep[centroidId] = distance;
		predecessorsRep[centroidId] = predecessor;
	}

	public void setProcessed(int centroidId){
		unprocessed[centroidId] = false;
	}

	public void setProcessedRep(int centroidId){
		unprocessedRep[centroidId] = false;
	}

	public void setUnprocessed(int centroidId){
		graph.addUnprocessedNode(this);
		unprocessed[centroidId] = true;
	}

	public void setUnprocessedRep(int centroidId){
		graph.addUnprocessedNodeRep(this);
		unprocessedRep[centroidId] = true;
	}

	public Port getPort(){
		return port;
	}

	public void setRotation(){
		rotation = true;
	}

	public boolean isRotation(){
		return rotation;
	}

	public void setFromCentroid(){
		fromCentroid = true;
	}

	public boolean isFromCentroid(){
		return fromCentroid;
	}

	public boolean isUnprocessed(int centroidId){
		return unprocessed[centroidId];
	}

	public boolean isUnprocessedRep(int centroidId){
		return unprocessedRep[centroidId];
	}

	public int getDistance(int centroidId){
		return distances[centroidId];
	}

	public int getDistanceRep(int centroidId){
		return distancesRep[centroidId];
	}

	public RotationEdge getPredecessor(int centroidId){
		return predecessors[centroidId];
	}

	public RotationEdge getPredecessorRep(int centroidId){
		return predecessorsRep[centroidId];
	}

	public String getUNLocode() {
		return port.getUNLocode();
	}

	public RotationEdge getOutgoingSailEdge(int noInRotation) {
		for(RotationEdge e : outgoingEdges){
			if(e.getNoInRotation() == noInRotation){
				return e;
			}
		}
		for(RotationEdge e : outgoingEdges){
			if(e.getNoInRotation() == 0){
				return e;
			}
		}
		return null;
	}

	public boolean noSailEdges() {
		for(RotationEdge e : ingoingEdges){
			if(e.isSail()){
				return false;
			}
		}
		for(RotationEdge e : outgoingEdges){
			if(e.isSail()){
				return false;
			}
		}
		return true;
	}

	public void delete(){
		for(RotationEdge e : ingoingEdges){
			e.delete();
		}
		for(RotationEdge e : outgoingEdges){
			e.delete();
		}
		graph.removeRotationNode(this);
	}


}
