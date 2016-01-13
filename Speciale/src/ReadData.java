import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ReadData {

	public static void readAll(){

	}

	public static HashMap<String, Port> readPorts() throws FileNotFoundException{
		HashMap<String, Port> ports = new HashMap<String, Port>();
		File input = new File("LinerLib_Data\\ports.csv");
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("\t|\n");
		scanner.nextLine();
		int counter = 0;
		while(scanner.hasNextLine()){
			String UNLocode = scanner.next();
			String name = scanner.next();
			String country = scanner.next();
			String cabotage = scanner.next();
			String region = scanner.next();
			String textIn = scanner.next();
			double lng = Double.parseDouble(textIn);
			textIn = scanner.next();
			double lat = Double.parseDouble(textIn);
			textIn = scanner.next();
			double draft = Double.parseDouble(textIn);
			textIn = scanner.next();
			int moveCost = (int) Double.parseDouble(textIn);
			textIn = scanner.next();
			int transshipCost = (int) Double.parseDouble(textIn);
			textIn = scanner.next();
			int fixedCallCost = (int) Double.parseDouble(textIn);
			textIn = scanner.next();
			int varCallCost = (int) Double.parseDouble(textIn);
			Port newPort = new Port(UNLocode, name, country, cabotage, region, lng, lat, 
					draft, moveCost, transshipCost, fixedCallCost, varCallCost, counter);
			ports.put(UNLocode, newPort);
			counter++;
			scanner.nextLine();
		}
		scanner.close();
		return ports;
	}

	public static ArrayList<Demand> readDemands(String fileName, HashMap<String, Port> ports) throws FileNotFoundException{
		ArrayList<Demand> demands = new ArrayList<Demand>();
		File input = new File("LinerLib_Data\\"+fileName);
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("\t|\n");
		scanner.nextLine();
		while(scanner.hasNextLine()){
			String originUNLo = scanner.next();
			Port origin = ports.get(originUNLo);
			String destinationUNLo = scanner.next();
			Port destination = ports.get(destinationUNLo);
			String textIn = scanner.next();
			int demand = Integer.parseInt(textIn);	
			textIn = scanner.next();
			int rate = Integer.parseInt(textIn);	
			textIn = scanner.next();
			int maxTransitTime = Integer.parseInt(textIn);
			Demand newDemand = new Demand(origin, destination, demand, rate, maxTransitTime);
			demands.add(newDemand);
			scanner.nextLine();
		}
		scanner.close();
		return demands;
	}

	public static Distance[][] readDistances(HashMap<String, Port> ports) throws FileNotFoundException{
		Distance[][] distances = new Distance[ports.size()][ports.size()];
		File input = new File("LinerLib_Data\\dist_dense.csv");
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("\t|\n");
		scanner.nextLine();
		while(scanner.hasNextLine()){
			String originUNLo = scanner.next();
			Port origin = ports.get(originUNLo);
			String destinationUNLo = scanner.next();
			Port destination = ports.get(destinationUNLo);
			String textIn = scanner.next();
			int distance = Integer.parseInt(textIn);	
			textIn = scanner.next();
			double draft = 14;
			if(!textIn.isEmpty()){
				draft = Double.parseDouble(textIn);
			}
			textIn = scanner.next();
			int panamaInt = Integer.parseInt(textIn);
			boolean panama = false;
			if(panamaInt == 1){
				panama = true;
			}
			textIn = scanner.next();
			int suezInt = Integer.parseInt(textIn);
			boolean suez = false;
			if(suezInt == 1){
				suez = true;
			}
			if(distances[origin.getPortId()][destination.getPortId()] == null){
				distances[origin.getPortId()][destination.getPortId()] = new Distance(origin, destination);
			}
			if(suez == false && panama == false){
				distances[origin.getPortId()][destination.getPortId()].setNone(distance, draft);
			} else if(suez == true && panama == false){
				distances[origin.getPortId()][destination.getPortId()].setSuez(distance, draft);
			} else if(suez == false && panama == true){
				distances[origin.getPortId()][destination.getPortId()].setPanama(distance, draft);
			} else {
				distances[origin.getPortId()][destination.getPortId()].setSuezPanama(distance, draft);
			}
			
			scanner.nextLine();
		}
		scanner.close();
		return distances;
	}

	public static ArrayList<VesselClass> readVesselClass(String fileName) throws FileNotFoundException{
		ArrayList<VesselClass> vesselClasses = new ArrayList<VesselClass>();
		File input = new File("LinerLib_Data\\fleet_data.csv");
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("\t|\n");
		scanner.nextLine();
		while(scanner.hasNextLine()){
			String name = scanner.next();
			String textIn = scanner.next();
			int capacity = Integer.parseInt(textIn);	
			textIn = scanner.next();
			int TCRate = Integer.parseInt(textIn);
			textIn = scanner.next();
			double draft = Double.parseDouble(textIn);
			textIn = scanner.next();
			double minSpeed = Double.parseDouble(textIn);
			textIn = scanner.next();
			double maxSpeed = Double.parseDouble(textIn);
			textIn = scanner.next();
			double designSpeed = Double.parseDouble(textIn);
			textIn = scanner.next();
			double fuelConsumptionDesign = Double.parseDouble(textIn);
			textIn = scanner.next();
			double fuelConsumptionIdle = Double.parseDouble(textIn);
			textIn = scanner.next();
			int panamaFee;
			if(textIn.isEmpty()){
				panamaFee = -1;
			} else {
				panamaFee = Integer.parseInt(textIn);
			}
			textIn = scanner.next();
			int suezFee = Integer.parseInt(textIn);
			VesselClass newVesselClass = new VesselClass(name, capacity, TCRate, draft, minSpeed, maxSpeed, 
					designSpeed, fuelConsumptionDesign, fuelConsumptionIdle, panamaFee, suezFee);
			vesselClasses.add(newVesselClass);
			scanner.nextLine();
		}
		scanner.close();
		input = new File("LinerLib_Data\\"+fileName);
		Scanner scanner2 = new Scanner(input);
		scanner2.useDelimiter("\t|\n");
		scanner2.nextLine();
		while(scanner2.hasNextLine()){
			String name = scanner2.next();
			String textIn = scanner2.next();
			int noAvailable = Integer.parseInt(textIn);	
			for(VesselClass i : vesselClasses){
				if(i.getName()==name){
					i.setNoAvailable(noAvailable);
					break;
				}
			}
			scanner2.nextLine();
		}
		scanner2.close();
		return vesselClasses;
	}

}
