import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class ReadData {
	
	public static void readAll(){
		
	}
	
	public static ArrayList<Port> readPorts() throws FileNotFoundException{
		ArrayList<Port> ports = new ArrayList<Port>();
		File input = new File("LinerLib_Data\\ports.csv");
		Scanner scanner = new Scanner(input);
		scanner.useDelimiter("\t");
		scanner.nextLine();
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
			int moveCost = Integer.parseInt(textIn);
			textIn = scanner.next();
			int transshipCost = Integer.parseInt(textIn);
			textIn = scanner.next();
			int fixedCallCost = Integer.parseInt(textIn);
			textIn = scanner.next();
			int varCallCost = Integer.parseInt(textIn);
			
			scanner.nextLine();
		}
		
		return ports;
		
	}
}
