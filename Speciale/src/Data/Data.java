package Data;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Data {
	private HashMap<String, Port> ports;
	private Distance[][] distances;
	private ArrayList<Demand> demands;
	private ArrayList<VesselClass> vesselClasses;
	private int portStay = 24;
	
	public Data(String demandFileName, String vesselNoFileName) throws FileNotFoundException{
		ports = ReadData.readPorts();
		distances = ReadData.readDistances(ports);
		demands = ReadData.readDemands(demandFileName, ports);
		vesselClasses = ReadData.readVesselClass(vesselNoFileName);
	}
	
	public HashMap<String, Port> getPorts() {
		return ports;
	}

	public Distance[][] getDistances() {
		return distances;
	}
	
	public Distance getDistance(int portId1, int portId2){
		return distances[portId1][portId2];
	}
	
	public Distance getDistance(String port1UNLo, String port2UNLo){
		int portId1 = ports.get(port1UNLo).getPortId();
		int portId2 = ports.get(port2UNLo).getPortId();
		return getDistance(portId1, portId2);
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
		return demands;
	}

	public ArrayList<VesselClass> getVesselClasses() {
		return vesselClasses;
	}
	
	public int getPortStay(){
		return portStay;
	}
}
