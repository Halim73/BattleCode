package utilityBot;

public class StreamHashSet {
	public Node[] stream;
	public int size;
	
	private static final int MAX_SIZE = 98;
	
	public StreamHashSet() {
		this.stream = new Node[MAX_SIZE];
		size = 0;
	}
	
	public Node searchNode(int key) {
		int hash = hash(key);
		Node current = this.stream[hash];
		
		if(current == null) {
			return null;
		}
		return current;
	}
	
	public Stream search(int key,Stream stream) {
		int hash = hash(key);
		Node current = this.stream[hash];
		
		while(this.stream[hash].data != stream) {
			current = current.next;
		}
		return current.data;
	}
	
	public Stream searchStream(int key) {
		int hash = hash(key);
		Node current = this.stream[hash];
		
		if(current.data != null) {
			return current.data;
		}
		return null;
	}
	
	public boolean isFull() {return size == MAX_SIZE-1;}
	
	public void clear() {
		if(size == MAX_SIZE-1) {
			this.stream = new Node[MAX_SIZE];
			size = 0;
		}
	}
	
	public void delete(int key,Stream stream) {
		int hash = hash(key);
		
		if(this.stream[hash].data == stream) {
			this.stream[hash] = this.stream[hash].next;
			size--;
		}else {
			Node current = this.stream[hash];
			
			while(current.next.data != stream) {
				current = current.next;
				if(current.next.data == stream) {
					current.next = null;
					size--;
				}
			}
		}
	}
	
	public void add(int key,Stream stream) {
		clear();
		
		int hash = hash(key);

		if(this.stream[hash] == null) {
			this.stream[hash] = new Node(stream,null);
			size++;
		}else {
			Node current = this.stream[hash];
			
			while(current.next != null) {
				current = current.next;
			}
			current = new Node(stream,current);
			size++;
		}
	}
	
	public int hash(int key) {
		return key%(MAX_SIZE-1);
	}
}

