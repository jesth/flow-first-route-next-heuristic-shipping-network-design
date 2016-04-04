package RotationFlow;

public class RotationEdge {
	private RotationNode fromNode;
	private RotationNode toNode;
	private boolean sail;
	private boolean feeder;
	private boolean omission;
	private int capacity;
	private int cost;
	
	public RotationEdge(RotationNode fromNode, RotationNode toNode, int capacity, int cost, boolean sail, boolean feeder, boolean omission) {
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.capacity = capacity;
		this.cost = cost;
		this.sail = sail;
		this.feeder = feeder;
		this.omission = omission;
		fromNode.addOutgoingEdge(this);
		toNode.addIngoingEdge(this);
	}

	public RotationNode getFromNode() {
		return fromNode;
	}

	public RotationNode getToNode() {
		return toNode;
	}

	public boolean isSail() {
		return sail;
	}

	public boolean isFeeder() {
		return feeder;
	}

	public boolean isOmission() {
		return omission;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getCost() {
		return cost;
	}
	
}
