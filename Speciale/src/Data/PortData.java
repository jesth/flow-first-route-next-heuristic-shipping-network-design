package Data;

import java.io.Serializable;

public class PortData implements Serializable{
	private static final long serialVersionUID = 1L;
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
	private int portId;
	
	public PortData(String UNLocode, String name, String country, String cabotage, String region, double lng, double lat,
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
		this.portId = portId;
	}
	
	public int getPortId(){
		return portId;
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
}
