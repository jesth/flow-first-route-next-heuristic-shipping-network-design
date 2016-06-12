package AuxFlow;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import Data.Data;
import Data.Demand;
import Data.Distance;
import Data.DistanceElement;
import Data.Port;
import Data.PortData;
import Data.VesselClass;
import Graph.Edge;
import Graph.Graph;
import Results.Rotation;
import Sortables.SortableAuxEdge;

public class AuxGraph implements Serializable{
	private static final long serialVersionUID = 1L;

	private transient VesselClass largestVessel;
	private AuxNode[] nodes;
	private ArrayList<AuxEdge> edges;
	private transient ArrayList<Demand> demandsList;
	private transient AuxDijkstra dijkstra;

	public AuxGraph(Graph orgGraph){
		this.demandsList = orgGraph.getDemands();
		nodes = new AuxNode[Data.getPorts().length];
		edges = new ArrayList<AuxEdge>();
		ArrayList<VesselClass> vessels = Data.getVesselClasses();
		for(int i = 0; i < vessels.size(); i++){
			VesselClass vessel = vessels.get(i);
			if(vessel.getNoAvailable() > 0){
				largestVessel = vessel;
			}
		}
		generateNodes();
		generateEdges();
		dijkstra = new AuxDijkstra(this);
	}

	public void setEdgesUsed(ArrayList<Rotation> rotationsToKeep){
		for(Rotation r : rotationsToKeep){
			for(Edge e : r.getRotationEdges()){
				if(e.isSail()){
					int fromPortId = e.getFromNode().getPortId();
					int toPortId = e.getToNode().getPortId();
					AuxNode fromNode = nodes[fromPortId];
					AuxNode toNode = nodes[toPortId];
					for(AuxEdge ae : fromNode.getOutgoingEdges()){
						if(ae.getToNode().equals(toNode)){
							ae.setUsedInRotation();
							break;
						}
					}
				}
			}
		}
	}

	public void runDijkstra(int iterations, int rand){
		dijkstra.run(rand);
		dijkstra.convert(iterations);
	}

	private void generateNodes(){
		for(PortData p : Data.getPorts()){
			AuxNode newNode = new AuxNode(p);
			nodes[p.getPortId()] = newNode;
		}
	}

	private void generateEdges(){
		Distance[][] distances = Data.getDistances();
		for(int i = 0; i < Data.getPorts().length; i++){
			for(int j = 0; j < Data.getPorts().length; j++){
				Distance distance = distances[i][j];
				DistanceElement[] distanceElements = distance.getDistances();
				for(int k = 0; k <= 3; k++){
					DistanceElement distanceElement = distanceElements[k];
					if(distanceElement.getDraft() > 0){
						int fromId = distanceElement.getOrigin().getPortId();
						AuxNode fromNode = nodes[fromId];
						int toId = distanceElement.getDestination().getPortId();
						AuxNode toNode = nodes[toId];
						if(fromNode != null && toNode != null){
							new AuxEdge(this, fromNode, toNode, distanceElement);
						}
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

	public void addFirstRotationManual() {
		VesselClass vessel = Data.getVesselClasses().get(2);
		int capacity = vessel.getCapacity();
		AuxEdge edge1 = findEdge("EGPSD", "MACAS");
		AuxEdge edge2 = findEdge("MACAS", "MAPTM");
		AuxEdge edge3 = findEdge("MAPTM", "ESALG");
		AuxEdge edge4 = findEdge("ESALG", "EGALY");
		AuxEdge edge5 = findEdge("EGALY", "EGPSD");
		new AuxEdge(edge1, capacity);
		new AuxEdge(edge2, capacity);
		new AuxEdge(edge3, capacity);
		new AuxEdge(edge4, capacity);
		new AuxEdge(edge5, capacity);
	}

	public void addSecondRotationManual() {
		VesselClass vessel = Data.getVesselClasses().get(1);
		int capacity = vessel.getCapacity();
		AuxEdge edge1 = findEdge("TRAMB", "EGALY");
		AuxEdge edge2 = findEdge("EGALY", "EGPSD");
		AuxEdge edge3 = findEdge("EGPSD", "EGALY");
		AuxEdge edge4 = findEdge("EGALY", "TRAMB");
		new AuxEdge(edge1, capacity);
		new AuxEdge(edge2, capacity);
		new AuxEdge(edge3, capacity);
		new AuxEdge(edge4, capacity);
	}

	public void addThirdRotationManual() {
		VesselClass vessel = Data.getVesselClasses().get(1);
		int capacity = vessel.getCapacity();
		AuxEdge edge1 = findEdge("ESALG", "TNTUN");
		AuxEdge edge2 = findEdge("TNTUN", "ITGIT");
		AuxEdge edge3 = findEdge("ITGIT", "ITGOA");
		AuxEdge edge4 = findEdge("ITGOA", "ESALG");
		new AuxEdge(edge1, capacity);
		new AuxEdge(edge2, capacity);
		new AuxEdge(edge3, capacity);
		new AuxEdge(edge4, capacity);
	}

	public void addFourthRotationManual() {
		VesselClass vessel = Data.getVesselClasses().get(1);
		int capacity = vessel.getCapacity();
		AuxEdge edge1 = findEdge("ESALG", "DZORN");
		AuxEdge edge2 = findEdge("DZORN", "TNTUN");
		AuxEdge edge3 = findEdge("TNTUN", "ITGIT");
		AuxEdge edge4 = findEdge("ITGIT", "EGPSD");
		AuxEdge edge5 = findEdge("EGPSD", "EGALY");
		AuxEdge edge6 = findEdge("EGALY", "GRPIR");
		AuxEdge edge7 = findEdge("GRPIR", "ITGIT");
		AuxEdge edge8 = findEdge("ITGIT", "ITGOA");
		AuxEdge edge9 = findEdge("ITGOA", "ESBCN");
		AuxEdge edge10 = findEdge("ESBCN", "ESVLC");
		AuxEdge edge11 = findEdge("ESVLC", "ESALG");
		new AuxEdge(edge1, capacity);
		new AuxEdge(edge2, capacity);
		new AuxEdge(edge3, capacity);
		new AuxEdge(edge4, capacity);
		new AuxEdge(edge5, capacity);
		new AuxEdge(edge6, capacity);
		new AuxEdge(edge7, capacity);
		new AuxEdge(edge8, capacity);
		new AuxEdge(edge9, capacity);
		new AuxEdge(edge10, capacity);
		new AuxEdge(edge11, capacity);
	}

	public void addFifthRotationManual() {
		VesselClass vessel = Data.getVesselClasses().get(0);
		int capacity = vessel.getCapacity();
		AuxEdge edge1 = findEdge("ESALG", "ESAGP");
		AuxEdge edge2 = findEdge("ESAGP", "DZORN");
		AuxEdge edge3 = findEdge("DZORN", "DZALG");
		AuxEdge edge4 = findEdge("DZALG", "ITGIT");
		AuxEdge edge5 = findEdge("ITGIT", "EGPSD");
		AuxEdge edge6 = findEdge("EGPSD", "LBBEY");
		AuxEdge edge7 = findEdge("LBBEY", "TRMER");
		AuxEdge edge8 = findEdge("TRMER", "ILHFA");
		AuxEdge edge9 = findEdge("ILHFA", "EGPSD");
		AuxEdge edge10 = findEdge("EGPSD", "ESALG");
		new AuxEdge(edge1, capacity);
		new AuxEdge(edge2, capacity);
		new AuxEdge(edge3, capacity);
		new AuxEdge(edge4, capacity);
		new AuxEdge(edge5, capacity);
		new AuxEdge(edge6, capacity);
		new AuxEdge(edge7, capacity);
		new AuxEdge(edge8, capacity);
		new AuxEdge(edge9, capacity);
		new AuxEdge(edge10, capacity);
	}

	private AuxEdge findEdge(String fromPort, String toPort){
		for(AuxEdge e : edges){
			if(!e.isRotation()){
				if(e.getFromNode().getPort().getUNLocode().equals(fromPort) && e.getToNode().getPort().getUNLocode().equals(toPort)){
					return e;
				}
			}
		}
		return null;
	}


	public void addEdge(AuxEdge edge){
		edges.add(edge);
	}

	public void serialize(){
		try{
			FileOutputStream fileOut = new FileOutputStream("AuxGraph.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in AuxGraph.ser");
		}catch(IOException i){
			i.printStackTrace();
		}
	}

	public static AuxGraph deserialize(){
		AuxGraph auxGraph = null;
		try{
			FileInputStream fileIn = new FileInputStream("AuxGraph.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			auxGraph = (AuxGraph) in.readObject();
			in.close();
			fileIn.close();
		}catch(IOException i){
			i.printStackTrace();
		}catch(ClassNotFoundException c){
			System.out.println("AuxGraph class not found");
			c.printStackTrace();
		}
		return auxGraph;
	}

	public ArrayList<AuxEdge> getSortedAuxEdges(){

		AuxGraph auxGraph = deserialize();
		ArrayList<SortableAuxEdge> sortableAuxEdges = new ArrayList<SortableAuxEdge>();
		for(int i=0; i<auxGraph.getEdges().size(); i++){
			int auxAvgLoad = (int) (auxGraph.getEdges().get(i).getAvgLoad()*1000);
			if(auxAvgLoad > 0){
				SortableAuxEdge e = new SortableAuxEdge(auxAvgLoad, auxGraph.getEdges().get(i));
				sortableAuxEdges.add(e);
			}
		}
		Collections.sort(sortableAuxEdges);

		ArrayList<AuxEdge> auxEdges = new ArrayList<AuxEdge>(sortableAuxEdges.size());
		for(int i=0; i<sortableAuxEdges.size(); i++){
			auxEdges.add(sortableAuxEdges.get(i).getAuxEdge());
		}
		return auxEdges;
	}

	public ArrayList<Demand> getDemands() {
		return demandsList;
	}

	public int getTotalDemand(PortData port) {
		int demand = 0;
		for(Demand d : demandsList){
			if(d.getOrigin().getPortData().equals(port) || d.getDestination().getPortData().equals(port)){
				demand += d.getDemand();
			}
		}
		return demand;
	}
}
