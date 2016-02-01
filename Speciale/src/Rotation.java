import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Rotation {
	private int id;
	private VesselClass vesselClass;
	private ArrayList<Node> rotationNodes;
	private ArrayList<Edge> rotationEdges;
	private double rotationTime;

	private int noVessels;
	private static AtomicInteger idCounter = new AtomicInteger();
	private boolean active;
	
	public Rotation(){
	}

	public Rotation(VesselClass vesselClass) {
		super();
		this.id = idCounter.getAndIncrement();
		this.vesselClass = vesselClass;
		this.rotationNodes = new ArrayList<Node>();
		this.rotationEdges = new ArrayList<Edge>();
		this.rotationTime = 0;
		this.noVessels = 0;
		this.active = true;
	}

	public double calculateRotationTime(){
		double designRotationTime = 0;
		for(Edge i : rotationEdges){
			designRotationTime += i.getTravelTime();
		}
		this.rotationTime = designRotationTime;
		designRotationTime = Math.ceil(designRotationTime/168.0);
		designRotationTime = designRotationTime*7;
		return designRotationTime;
	}
	
	public int calculateNoVessels(){
		//TODO hardcoded 168 hours per week.
		// also runs calculateRotationTime() every time number of vessels are needed. smart? 
		calculateRotationTime();
		this.noVessels = (int) Math.ceil(rotationTime/168.0);
		return this.noVessels;
	}

	public VesselClass getVesselClass() {
		return vesselClass;
	}
	
	public void addRotationNode(Node node){
		rotationNodes.add(node);
	}
	
	public void addRotationEdge(Edge edge){
		rotationEdges.add(edge);
	}

	public ArrayList<Node> getRotationNodes() {
		return rotationNodes;
	}

	public ArrayList<Edge> getRotationEdges() {
		return rotationEdges;
	}

	/**
	 * @return the rotationTime
	 */
	public double getRotationTime() {
		return rotationTime;
	}
	
	public int calcCost(){
		int obj = 0;
		VesselClass v = this.getVesselClass();
		ArrayList<Edge> rotationEdges = this.getRotationEdges();
		double sailingTime = 0;
		double idleTime = 0;
		int portCost = 0;
		double distance = 0;
		int suezCost = 0;
		int panamaCost = 0;
		for (Edge e : rotationEdges){
			if(e.isSail()){
				sailingTime += e.getTravelTime();
				distance += e.getDistance().getDistance();
				Port p = e.getToNode().getPort();
				portCost += p.getFixedCallCost() + p.getVarCallCost() * v.getCapacity();
				if(e.isSuez()){
					suezCost += v.getSuezFee();
				}
				if(e.isPanama()){
					panamaCost += v.getPanamaFee();
				}
			}
			if(e.isDwell()){
				idleTime += e.getTravelTime();
			}
		}
		//TODO USD per metric tons fuel = 600
		double sailingBunkerCost = (int) Math.ceil(sailingTime/24.0) * v.getFuelConsumptionDesign() * 600;
		double idleBunkerCost = (int) Math.ceil(idleTime/24.0) * v.getFuelConsumptionIdle() * 600;
		
		int rotationDays = (int) Math.ceil((sailingTime+idleTime)/24.0);
		int TCCost = rotationDays * v.getTCRate();
		
		System.out.println("Rotation number "+ this.id);
		System.out.println("Voyage duration in nautical miles " + distance);
		System.out.println(this.calculateNoVessels() + " ships needed for rotationTime of " + this.getRotationTime());
		System.out.println("Port call cost " + portCost);
		System.out.println("Bunker idle burn in Ton " + idleBunkerCost/600.0);
		System.out.println("Bunker fuel burn in Ton " + sailingBunkerCost/600.0);
		System.out.println("Total TC cost " + TCCost);
		System.out.println();
		
		obj -= sailingBunkerCost + idleBunkerCost + portCost + suezCost + panamaCost + TCCost;
		
		return obj;
	}
	
	public int getNoVessels() {
		return noVessels;
	}
	
	public int getId(){
		return id;
	}
	
	public double getSailTime(){
		double sailTime = 0;
		for(Edge e : rotationEdges){
			if(e.isSail()){
				sailTime += e.getTravelTime();
			}
		}
		
		return sailTime;
	}
	
	public ArrayList<Port> getPorts(){
		ArrayList<Port> ports = new ArrayList<Port>();
		for(Edge e : rotationEdges){
			if(e.getToNode().isArrival()){
				ports.add(e.getToNode().getPort());
			}
		}
		
		return ports;
	}

	/**
	 * @return active or not
	 */
	public boolean isActive() {
		return active;
	}


	/**
	 * Set rotation to active
	 */
	public void setActive() {
		this.active = true;
	}

	
	/**
	 * Set rotation to inactive
	 */
	public void setInactive(){
		this.active = false;
	}
	
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		String print = "Rotation [vesselClass=" + vesselClass.getName() + ", noVessels=" + noVessels + "]\n";
		int counter = 0;
		for(Node i : rotationNodes){
			if(i.isDeparture()){
				print += "Port no. " + counter + ": " + i.getPort() + "\n";
				counter++;
			}
		}
		return print;
	}
	
}
