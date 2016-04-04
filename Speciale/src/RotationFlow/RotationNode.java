package RotationFlow;

import java.util.ArrayList;

import Data.Port;

public class RotationNode {
	private Port port;
	private ArrayList<RotationEdge> ingoingEdges;
	private ArrayList<RotationEdge> outgoingEdges;
	private int[] distances;
	private RotationEdge[] predecessors;
	private boolean[] unprocessed;
	private static int noOfCentroids;
	
	public RotationNode(Port port){
		this.port = port;
		this.ingoingEdges = new ArrayList<RotationEdge>();
		this.outgoingEdges = new ArrayList<RotationEdge>();
		this.distances = new int[noOfCentroids];
		this.predecessors = new RotationEdge[noOfCentroids];
		this.unprocessed = new boolean[noOfCentroids];
	}
	
	public static void setNoOfCentroids(int newNoOfCentroids){
		noOfCentroids = newNoOfCentroids;
	}
	
	public void addIngoingEdge(RotationEdge e){
		ingoingEdges.add(e);
	}	
	
	public void addOutgoingEdge(RotationEdge e){
		outgoingEdges.add(e);
	}
	
	public void setLabels(int centroidId, int distance, RotationEdge predecessor){
		distances[centroidId] = distance;
		predecessors[centroidId] = predecessor;
	}
	
	public void setProcessed(int centroidId){
		unprocessed[centroidId] = false;
	}
	
	public void setUnprocessed(int centroidId){
//		BellmanFord.addUnprocessedNode(this);
		unprocessed[centroidId] = true;
	}
	
	public Port getPort(){
		return port;
	}
}
