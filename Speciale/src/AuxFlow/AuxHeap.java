package AuxFlow;

public class AuxHeap {
	private int size;		
	private AuxNode[] heap; 	

	public AuxHeap(AuxGraph graph){
		size = graph.getNodes().length;
		heap = new AuxNode[size];
		for(int i = 0; i < size; i++){
			heap[i] = graph.getNodes()[i];
			heap[i].setHeapIndex(i);
		}
		heapify();
	}
	
	public void heapify(){
		for (int i = size/2-1; i >= 0; i--){
			heapifyInput(i);
		}
	}
	
	public void heapifyInput(int index){
		int l = left(index);
		int r = right(index);
		int smallest;
		if (l < size && heap[l].getDistance() < heap[index].getDistance()){
			smallest = l;
		} else {
			smallest = index;
		}
		if (r < size && heap[r].getDistance() < heap[smallest].getDistance()){
			smallest = r;
		} 
		if (smallest != index){
			AuxNode saver = heap[index];
			heap[index] = heap[smallest];
			heap[smallest] = saver;
			heap[index].setHeapIndex(index);
			heap[smallest].setHeapIndex(smallest);
			heapifyInput(smallest);
		}
	}
	
	public void setSource(AuxNode source){
		source.setDistance(0);
		bubbleUp(source.getHeapIndex());
	}
	
	public void bubbleUp(int index){
		AuxNode parent = heap[parent(index)];
		AuxNode bubble = heap[index];

		while(parent.getDistance() > bubble.getDistance()){
			heap[parent(index)] = bubble;
			heap[index] = parent;
			bubble.setHeapIndex(parent(index));	
			parent.setHeapIndex(index);
			index = parent(index);
			parent = heap[parent(index)];
		}
	}
	
	public int parent(int index){
		return (index - 1) / 2;
	}
	
	public int left(int index){
		return index*2+1;
	}

	public int right(int index){
		return index*2+2;
	}

	public AuxNode getMin(){
		return heap[0];
	}
	
	public int getSize() {
		return size;
	}
	
	public AuxNode extractMin(){
		if (size < 1){
			return heap[0];
		}
		AuxNode min = heap[0];
		heap[0] = heap[size-1];
		heap[size-1] = min;
		heap[0].setHeapIndex(0);
		heap[size-1].setHeapIndex(size-1);
		size = size-1;
		heapifyInput(0);
		return min;
	}

	public void reset() {
		size = heap.length;
	}
}
