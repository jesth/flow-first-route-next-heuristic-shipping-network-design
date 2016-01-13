import java.io.FileNotFoundException;

public class RunModel {

	public static void main(String[] args) throws FileNotFoundException {
		Graph testGraph = new Graph();
		for(Node i : testGraph.getNodes()){
			System.out.println(i.getDistances().length);
		}
	}

}
