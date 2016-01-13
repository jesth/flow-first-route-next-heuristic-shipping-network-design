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
