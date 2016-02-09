package Data;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Data {
	private HashMap<String, Port> ports;
	private Distance[][] distances;
	private Demand[][] demandsArray;
	private ArrayList<Demand> demandsList;
	private ArrayList<VesselClass> vesselClasses;
	private int portStay = 24;
	
	public Data(String demandFileName, String vesselNoFileName) throws FileNotFoundException{
		ports = ReadData.readPorts();
		distances = ReadData.readDistances(ports);
		demandsList = ReadData.readDemands(demandFileName, ports);
		demandsArray = createDemandsArray();
		vesselClasses = ReadData.readVesselClass(vesselNoFileName);
	}
	
	public Demand[][] createDemandsArray(){
		Demand[][] demands = new Demand[ports.size()][ports.size()];
		for(Demand d : demandsList){
			int fromPortId = d.getOrigin().getPortId();
			int toPortId = d.getDestination().getPortId();
			demands[fromPortId][toPortId] = d;
		}
		return demands;
	}
	
	public HashMap<String, Port> getPorts() {
		return ports;
	}

	public Distance[][] getDistances() {
		return distances;
	}
	
	public Distance getDistance(String port1UNLo, String port2UNLo){
		int portId1 = ports.get(port1UNLo).getPortId();
		int portId2 = ports.get(port2UNLo).getPortId();
		return getDistance(portId1, portId2);
	}
	
	public Distance getDistance(int portId1, int portId2){
		return distances[portId1][portId2];
	}
	
	public Demand getDemand(Port fromPort, Port toPort){
		int fromPortId = fromPort.getPortId();
		int toPortId = toPort.getPortId();
		return getDemand(fromPortId, toPortId);
	}
	
	public Demand getDemand(int fromPortId, int toPortId){
		return demandsArray[fromPortId][toPortId];
	}
	
	public DistanceElement getDistanceElement(String port1UNLo, String port2UNLo, boolean suez, boolean panama){
		Distance distance = getDistance(port1UNLo, port2UNLo);
		return distance.getDistanceElement(suez, panama);
	}
	
	public DistanceElement getDistanceElement(int portId1, int portId2, boolean suez, boolean panama){
		Distance distance = getDistance(portId1, portId2);
		return distance.getDistanceElement(suez, panama);
	}
	
	public DistanceElement getDistanceElement(Port port1, Port port2, boolean suez, boolean panama){
		Distance distance = getDistance(port1.getPortId(), port2.getPortId());
		return distance.getDistanceElement(suez, panama);
	}

	public ArrayList<Demand> getDemands() {
		return demandsList;
	}

	public ArrayList<VesselClass> getVesselClasses() {
		return vesselClasses;
	}
	
	public int getPortStay(){
		return portStay;
	}
}
