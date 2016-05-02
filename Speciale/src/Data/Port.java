package Data;
import java.util.ArrayList;

import Graph.Edge;
import Graph.Node;
import Results.Rotation;

public class Port {
	private Node fromCentroidNode;
	private Node toCentroidNode;
	private ArrayList<Node> arrivalNodes = new ArrayList<Node>();
	private ArrayList<Node> departureNodes = new ArrayList<Node>();
	private ArrayList<Edge> dwellEdges = new ArrayList<Edge>();
	private boolean active;
	private int totalDemand = 0;
	private int totalProfitPotential = 0;
	private PortData portData;
	
	public Port(){
		
	}
	
	/**
	 * @param UNLocode
	 * @param name
	 * @param country
	 * @param cabotage
	 * @param region
	 * @param lng
	 * @param lat
	 * @param draft
	 * @param moveCost
	 * @param transshipCost
	 * @param fixedCallCost
	 * @param varCallCost
	 */
	public Port(PortData portData) {
		super();
		this.fromCentroidNode = null;
		this.toCentroidNode = null;
		this.active = false;
		this.portData = portData;
	}

	public int getPortId(){
		return portData.getPortId();
	}
	
	public String getUNLocode() {
		return portData.getUNLocode();
	}

	public double getDraft() {
		return portData.getDraft();
	}

	public int getMoveCost() {
		return portData.getMoveCost();
	}

	public int getTransshipCost() {
		return portData.getTransshipCost();
	}

	public int getFixedCallCost() {
		return portData.getFixedCallCost();
	}

	public int getVarCallCost() {
		return portData.getVarCallCost();
	}
	
	public Node getFromCentroidNode() {
		return fromCentroidNode;
	}
	
	public Node getToCentroidNode() {
		return toCentroidNode;
	}
	
	public ArrayList<Node> getArrivalNodes(){
		return arrivalNodes;
	}
	
	public ArrayList<Node> getDepartureNodes(){
		return departureNodes;
	}
	
	public void addArrivalNode(Node arrivalNode){
		arrivalNodes.add(arrivalNode);
	}
	
	public void addDepartureNode(Node departureNode){
		departureNodes.add(departureNode);
	}
	
	public void setFromCentroidNode(Node fromCentroidNode) {
		this.fromCentroidNode = fromCentroidNode;
	}

	public void setToCentroidNode(Node toCentroidNode) {
		this.toCentroidNode = toCentroidNode;
	}
	
	public void setActive(){
		active = true;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public void addDwellEdge(Edge dwellEdge){
		this.dwellEdges.add(dwellEdge);
	}
	
	public void removeDwellEdge(Edge dwellEdge){
		this.dwellEdges.remove(dwellEdge);
	}
	
	public ArrayList<Edge> getDwellEdges(){
		return dwellEdges;
	}
	
	public int getTotalDemand(){
		return totalDemand;
	}
	
	public void addTotalDemand(int addDemand){
		totalDemand += addDemand;
	}
	
	public int getTotalProfitPotential(){
		return totalProfitPotential;
	}
	
	public void addTotalProfitPotential(int potentialProfit){
		totalProfitPotential += potentialProfit;
	}
	
	public void addDemand(int demand, int rate){
		addTotalDemand(demand);
		addTotalProfitPotential(demand*rate);
	}

	public int findSpareCapacity(boolean outgoing) {
		int spareCapacity = 0;
		for(Edge e : dwellEdges){
			if(outgoing){
				spareCapacity += e.getNextEdge().getSpareCapacity();
			} else {
				spareCapacity += e.getPrevEdge().getSpareCapacity();
			}
		}
		return spareCapacity;
	}

	public PortData getPortData() {
		return portData;
	}
}
