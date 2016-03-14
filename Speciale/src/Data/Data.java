package Data;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Data {
	private HashMap<String, Port> portsMap;
	private Port[] ports;
	private Distance[][] distances;
	private Demand[][] demandsArray;
	private ArrayList<Demand> demandsList;
	private ArrayList<VesselClass> vesselClasses;
	private int portStay = 24;

	public Data(String demandFileName, String vesselNoFileName) throws FileNotFoundException{
		portsMap = ReadData.readPorts();
		convertPortsMap();
		distances = ReadData.readDistances(portsMap);
		demandsList = ReadData.readDemands(demandFileName, portsMap);
		demandsArray = createDemandsArray();
		vesselClasses = ReadData.readVesselClass(vesselNoFileName);
	}

	private Demand[][] createDemandsArray(){
		Demand[][] demands = new Demand[portsMap.size()][portsMap.size()];
		for(Demand d : demandsList){
			int fromPortId = d.getOrigin().getPortId();
			int toPortId = d.getDestination().getPortId();
			demands[fromPortId][toPortId] = d;
		}
		return demands;
	}

	private void convertPortsMap(){
		ports = new Port[portsMap.size()];
		for(Port p : portsMap.values()){
			ports[p.getPortId()] = p;
		}
	}

	public HashMap<String, Port> getPortsMap() {
		return portsMap;
	}

	public Port[] getPorts(){
		return ports;
	}

	public Port getPort(int portId){
		return ports[portId];
	}

	public Distance[][] getDistances() {
		return distances;
	}

	public Distance getDistance(String port1UNLo, String port2UNLo){
		int portId1 = portsMap.get(port1UNLo).getPortId();
		int portId2 = portsMap.get(port2UNLo).getPortId();
		return getDistance(portId1, portId2);
	}

	public Distance getDistance(int portId1, int portId2){
		if(portId1 == portId2){
			throw new RuntimeException("Trying to get dwell distance.");
		}
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

	public DistanceElement getBestDistanceElement(String port1UNLo, String port2UNLo, VesselClass vesselClass){
		Distance distance = getDistance(port1UNLo, port2UNLo);
		return distance.getBestDistanceElement(vesselClass);
	}

	public DistanceElement getBestDistanceElement(int portId1, int portId2, VesselClass vesselClass){
		Distance distance = getDistance(portId1, portId2);
		return distance.getBestDistanceElement(vesselClass);
	}

	public DistanceElement getBestDistanceElement(Port port1, Port port2, VesselClass vesselClass){
		Distance distance = getDistance(port1.getPortId(), port2.getPortId());
		return distance.getBestDistanceElement(vesselClass);
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
