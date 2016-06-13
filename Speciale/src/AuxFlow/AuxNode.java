package AuxFlow;

import java.io.Serializable;
import java.util.ArrayList;

import Data.Data;
import Data.Port;
import Data.PortData;

public class AuxNode implements Serializable{
	private static final long serialVersionUID = 1L;

	private transient PortData port;
	private int portId;
	private String UNLocode;
	private transient int distance;
	private AuxEdge predecessor;
	private ArrayList<AuxEdge> ingoingEdges;
	private ArrayList<AuxEdge> outgoingEdges;
	private transient int heapIndex;
	private boolean active;

	public AuxNode(PortData port){
		this.port = port;
		portId = port.getPortId();
		UNLocode = port.getUNLocode();
		this.distance = Integer.MAX_VALUE;
		this.predecessor = null;
		this.ingoingEdges = new ArrayList<AuxEdge>();
		this.outgoingEdges = new ArrayList<AuxEdge>();
		this.heapIndex = -1;
		this.active = false;
	}

	public AuxEdge[] findClosestOutgoing(int noClosest, double minDraft){
		AuxEdge[] closest = new AuxEdge[noClosest];
		int[] closestDist = new int[noClosest];
		for(int j = 0; j < closestDist.length; j++){
			closestDist[j] = Integer.MAX_VALUE;
		}
		for(AuxEdge e : outgoingEdges){
			if(e.getToNode().getPort().getDraft() >= minDraft && e.getToNode().isActive()){
				int dist = e.getDistance().getDistance();
				int highestDist = -Integer.MAX_VALUE;
				int index = -1;
				for(int i = 0; i < closest.length; i++){
					int currDist = closestDist[i];
					if(currDist > highestDist){
						highestDist = currDist;
						index = i;
					}
				}
				if(highestDist > dist){
					closestDist[index] = dist;
					closest[index] = e;
				}
			}
		}
		return closest;
	}

	public AuxEdge[] findClosestIngoing(int noClosest, double minDraft){
		AuxEdge[] closest = new AuxEdge[noClosest];
		int[] closestDist = new int[noClosest];
		for(int j = 0; j < closestDist.length; j++){
			closestDist[j] = Integer.MAX_VALUE;
		}
		for(AuxEdge e : ingoingEdges){
			if(e.getFromNode().getPort().getDraft() >= minDraft && e.getToNode().isActive()){
				int dist = e.getDistance().getDistance();
				int highestDist = -Integer.MAX_VALUE;
				int index = -1;
				for(int i = 0; i < closest.length; i++){
					int currDist = closestDist[i];
					if(currDist > highestDist){
						highestDist = currDist;
						index = i;
					}
				}
				if(highestDist > dist){
					closestDist[index] = dist;
					closest[index] = e;
				}
			}
		}
		return closest;
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

	public PortData getPort() {
		return port;
	}

	public int getPortId(){
		return portId;
	}

	public String getUNLocode(){
		return UNLocode;
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
	
	public boolean isActive(){
		return active;
	}

	public void setActive(){
		active = true;
	}
}
