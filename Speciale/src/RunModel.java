import java.io.FileNotFoundException;

public class RunModel {

	public static void main(String[] args) throws FileNotFoundException {
		Graph testGraph = new Graph();
		for(Demand i : testGraph.getData().getDemands()){
			System.out.println(i);
		}
	}

}
