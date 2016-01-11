
public class Node {
	private Port port;
	private Rotation rotation;
	private boolean centroid;
	
	public Node(){
	}

	/** Constructor for nodes. i.e. not centroids.
	 * @param port
	 * @param rotation
	 */
	public Node(Port port, Rotation rotation){
		super();
		this.port = port;
		this.rotation = rotation;
		this.centroid = false;
	}
	
	/** Constructor for centroids.
	 * @param port
	 */
	public Node(Port port){
		super();
		this.port = port;
		this.rotation = null;
		this.centroid = true;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node [port=" + port + ", rotation=" + rotation + ", centroid=" + centroid + "]";
	}
	
}
