package AuxFlow;

import java.util.ArrayList;
import Data.Port;

public class AuxNode {
	private Port port;
	private int distance;
	private AuxEdge predecessor;
	private ArrayList<AuxEdge> ingoingEdges;
	private ArrayList<AuxEdge> outgoingEdges;
	private int heapIndex;
	
	public AuxNode(Port port){
		this.port = port;
		this.distance = Integer.MAX_VALUE;
		this.predecessor = null;
		this.ingoingEdges = new ArrayList<AuxEdge>();
		this.outgoingEdges = new ArrayList<AuxEdge>();
		this.heapIndex = -1;
	}
	
	public void addIngoingEdge(AuxEdge edge){
		ingoingEdges.add(edge);
	}	
	
	public void addOutgoingEdge(AuxEdge edge){
		outgoingEdges.add(edge);
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public AuxEdge getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(AuxEdge predecessor) {
		this.predecessor = predecessor;
	}

	public Port getPort() {
		return port;
	}

	public ArrayList<AuxEdge> getIngoingEdges() {
		return ingoingEdges;
	}

	public ArrayList<AuxEdge> getOutgoingEdges() {
		return outgoingEdges;
	}

	public int getHeapIndex() {
		return heapIndex;
	}

	public void setHeapIndex(int heapIndex) {
		this.heapIndex = heapIndex;
	}
	
	
}
