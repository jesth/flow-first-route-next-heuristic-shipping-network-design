package RotationFlow;

import java.util.ArrayList;

import Data.Demand;
import Data.Port;
import Graph.Edge;
import Results.Rotation;
import Results.Route;

public class RotationGraph {
	private RotationNode[] rotationNodes;
	private ArrayList<RotationEdge> rotationEdges;
	private ArrayList<RotationDemand> rotationDemands;
	private ArrayList<Route> orgRoutes;
	private Rotation rotation;
	private static int noOfCentroids;

	public RotationGraph(Rotation rotation, int newNoOfCentroids){
		noOfCentroids = newNoOfCentroids;
		RotationNode.setNoOfCentroids(noOfCentroids);
		this.rotation = rotation;
		this.rotationNodes = new RotationNode[noOfCentroids];
		this.rotationEdges = new ArrayList<RotationEdge>();
		this.rotationDemands = new ArrayList<RotationDemand>();
		this.orgRoutes = new ArrayList<Route>();
		this.createGraph();
		
	}

	public void createGraph(){
		this.createDemands();
		this.createNodes();
		this.createFeederEdges();
	}

	private void createDemands(){
		for(Edge e : rotation.getRotationEdges()){
			if(e.isSail()){
				for(Route r : e.getRoutes()){
					if(!orgRoutes.contains(r)){
						orgRoutes.add(r);
					}
				}
			}
		}
		for(Route r : orgRoutes){
			Demand d = r.getDemand();
			RotationDemand rd = getRotationDemand(d);
			if(rd == null){
				rd = new RotationDemand(d, r.getFFE());
				rotationDemands.add(rd);
			} else {
				rd.addDemand(r.getFFE());
			}
		}
	}
	
	private void createNodes(){
		for(RotationDemand d : rotationDemands){
			Port fromPort = d.getOrgDemand().getOrigin();
			RotationNode fromNode = getRotationNode(fromPort);
			Port toPort = d.getOrgDemand().getDestination();
			RotationNode toNode = getRotationNode(toPort);
			createOmissionEdge(fromNode, toNode);
		}
		for(Edge e : rotation.getRotationEdges()){
			Port fromPort = e.getFromNode().getPort();
			RotationNode fromNode = getRotationNode(fromPort);
			Port toPort = e.getToNode().getPort();
			RotationNode toNode = getRotationNode(toPort);
			createSailEdge(fromNode, toNode, e.getCapacity());
		}
	}
	
	private void createFeederEdges(){
		for(Route r : orgRoutes){
			Port fromPort = r.getRoute().get(0).getFromNode().getPort();
			Port toPort;
			for(Edge e : r.getRoute()){
				if(!e.getRotation().equals(rotation)){
					toPort = e.getToNode().getPort();
				} else {
					if(fromPort != null && toPort != null && !fromPort.equals(toPort)){
						createFeederEdge();
					}
					fromPort = e.getToNode().getPort();
					toPort = null;
				}
			}
		}
	}
	
	private RotationNode getRotationNode(Port port){
		RotationNode n = rotationNodes[port.getPortId()];
		if(n == null){
			n = new RotationNode(port);
			rotationNodes[port.getPortId()] = n;
		}
		return n;
	}

	private RotationDemand getRotationDemand(Demand d){
		for(RotationDemand rd : rotationDemands){
			if(rd.getOrgDemand().equals(d)){
				return rd;
			}
		}
		return null;
	}
	
	private void createOmissionEdge(RotationNode fromNode, RotationNode toNode){
		RotationEdge omission = new RotationEdge(fromNode, toNode, Integer.MAX_VALUE, 1000, false, false, true);
		rotationEdges.add(omission);
	}
	
	private void createSailEdge(RotationNode fromNode, RotationNode toNode, int capacity){
		RotationEdge sail = new RotationEdge(fromNode, toNode, capacity, 1, true, false, false);
		rotationEdges.add(sail);
	}
	
	private void createFeederEdge(RotationNode fromNode, RotationNode toNode){
		int cost = computeFeederCost();
		RotationEdge feeder = new RotationEdge(fromNode, toNode, Integer.MAX_VALUE, cost, false, true, false);
		rotationEdges.add(feeder);
	}
	

}
