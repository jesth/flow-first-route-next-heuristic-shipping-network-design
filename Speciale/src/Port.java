
public class Port {
	private String UNLocode;
	private String name;
	private String country;
	private String cabotage;
	private String region;
	private double lng;
	private double lat;
	private double draft;
	private int moveCost;
	private int transshipCost;
	private int fixedCallCost;
	private int varCallCost;
	private Node centroidNode;
	private int portId;
	
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
	public Port(String UNLocode, String name, String country, String cabotage, String region, double lng, double lat,
			double draft, int moveCost, int transshipCost, int fixedCallCost, int varCallCost, int portId) {
		super();
		this.UNLocode = UNLocode;
		this.name = name;
		this.country = country;
		this.cabotage = cabotage;
		this.region = region;
		this.lng = lng;
		this.lat = lat;
		this.draft = draft;
		this.moveCost = moveCost;
		this.transshipCost = transshipCost;
		this.fixedCallCost = fixedCallCost;
		this.varCallCost = varCallCost;
		this.centroidNode = null;
		this.portId = portId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Port [UNLocode=" + UNLocode + ", name=" + name + ", country=" + country + ", cabotage=" + cabotage
				+ ", region=" + region + ", lng=" + lng + ", lat=" + lat + ", draft=" + draft + ", moveCost=" + moveCost
				+ ", transshipCost=" + transshipCost + ", fixedCallCost=" + fixedCallCost + ", varCallCost="
				+ varCallCost + "]";
	}

	public String getUNLocode() {
		return UNLocode;
	}

	public String getName() {
		return name;
	}

	public String getCountry() {
		return country;
	}

	public String getCabotage() {
		return cabotage;
	}

	public String getRegion() {
		return region;
	}

	public double getLng() {
		return lng;
	}

	public double getLat() {
		return lat;
	}

	public double getDraft() {
		return draft;
	}

	public int getMoveCost() {
		return moveCost;
	}

	public int getTransshipCost() {
		return transshipCost;
	}

	public int getFixedCallCost() {
		return fixedCallCost;
	}

	public int getVarCallCost() {
		return varCallCost;
	}
	
	public Node getCentroidNode() {
		return centroidNode;
	}
	
	public int getPortId(){
		return portId;
	}

	public void setCentroidNode(Node centroidNode) {
		this.centroidNode = centroidNode;
	}
}
