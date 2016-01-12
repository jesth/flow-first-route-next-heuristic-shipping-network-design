
public class Node {
	private Port port;
	private Rotation rotation;
	private boolean centroid;
	private boolean departure;
	private boolean arrival;
	
	public Node(){
	}

	/** Constructor for nodes. i.e. not centroids.
	 * @param port
	 * @param rotation
	 */
	public Node(Port port, Rotation rotation, boolean departure){
		super();
		this.port = port;
		this.rotation = rotation;
		this.centroid = false;
		this.departure = departure;
		this.arrival = !departure;
	}
	
	/** Constructor for centroids.
	 * @param port
	 */
	public Node(Port port){
		super();
		this.port = port;
		this.rotation = null;
		this.centroid = true;
		this.departure = false;
		this.arrival = false;
	}

	/**
	 * @return the port
	 */
	public Port getPort() {
		return port;
	}

	/**
	 * @return the rotation
	 */
	public Rotation getRotation() {
		return rotation;
	}

	/**
	 * @return the centroid
	 */
	public boolean isCentroid() {
		return centroid;
	}
	
	public boolean isDeparture() {
		return departure;
	}

	public boolean isArrival() {
		return arrival;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node [port=" + port + ", rotation=" + rotation + ", centroid=" + centroid + "]";
	}
	
}
