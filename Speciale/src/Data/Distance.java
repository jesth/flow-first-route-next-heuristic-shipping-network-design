package Data;

public class Distance {
	private Port origin;
	private Port destination;
	private DistanceElement[] distances;
	
	public Distance(){
		this.distances = new DistanceElement[4];
		this.distances[0] = new DistanceElement(this, false, false);
		this.distances[1] = new DistanceElement(this, true, false);
		this.distances[2] = new DistanceElement(this, false, true);
		this.distances[3] = new DistanceElement(this, true, true);
	}

	/**
	 * @return the origin
	 */
	public Port getOrigin() {
		return origin;
	}

	/**
	 * @return the destination
	 */
	public Port getDestination() {
		return destination;
	}
	
	/** Sets the ports.
	 * @param origin
	 * @param destination
	 */
	public void setPorts(Port origin, Port destination){
		this.origin = origin;
		this.destination = destination;
	}

	/** Sets the distance and draft for the appropriate DistanceElement.
	 * @param distance
	 * @param draft
	 * @param suez - whether the distance and draft covers a route through the Suez canal.
	 * @param panama - whether the distance and draft covers a route through the Panama canal.
	 */
	public void setDistanceDraft(int distance, double draft, boolean suez, boolean panama){
		if(suez == false && panama == false){
			distances[0].setDistance(distance);
			distances[0].setDraft(draft);
		} else if(suez == true && panama == false){
			distances[1].setDistance(distance);
			distances[1].setDraft(draft);
		} else if(suez == false && panama == true){
			distances[2].setDistance(distance);
			distances[2].setDraft(draft);
		} else {
			distances[3].setDistance(distance);
			distances[3].setDraft(draft);
		}
	}
	
	/**
	 * @param suez - whether the draft should be valid for a route through the Suez canal.
	 * @param panama - whether the draft should be valid for a route through the Panama canal.
	 * @return The draft for the appropriate DistanceElement.
	 */
	public double getDraft(boolean suez, boolean panama){
		if(suez == false && panama == false){
			return distances[0].getDraft();
		} else if(suez == true && panama == false){
			return distances[1].getDraft();
		} else if(suez == false && panama == true){
			return distances[2].getDraft();
		} else {
			return distances[3].getDraft();
		}
	}
	
	/**
	 * @param suez - whether the distance should be valid for a route through the Suez canal.
	 * @param panama - whether the distance should be valid for a route through the Panama canal.
	 * @return The distance for the appropriate DistanceElement.
	 */
	public int getDistance(boolean suez, boolean panama){
		if(suez == false && panama == false){
			return distances[0].getDistance();
		} else if(suez == true && panama == false){
			return distances[1].getDistance();
		} else if(suez == false && panama == true){
			return distances[2].getDistance();
		} else {
			return distances[3].getDistance();
		}
	}
	
	/**
	 * @param suez - whether the DistanceElement should be valid for a route through the Suez canal.
	 * @param panama - whether the DistanceElement should be valid for a route through the Panama canal.
	 * @return The appropriate DistanceElement.
	 */
	public DistanceElement getDistanceElement(boolean suez, boolean panama){
		if(suez == false && panama == false){
			return distances[0];
		} else if(suez == true && panama == false){
			return distances[1];
		} else if(suez == false && panama == true){
			return distances[2];
		} else {
			return distances[3];
		}
	}
}