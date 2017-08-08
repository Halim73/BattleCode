package utilityBot;
import battlecode.common.*;

//for networking nearby dangerous enemies.
public class Network extends Utility{
	private Stream stream;
	public Node current;
	//public Strategy strategy;
	
	public StreamHashSet network = new StreamHashSet();
	MapLocation[] dangers = new MapLocation[20];
	MapLocation currentArchon;
	
	public int dangerCount;
	
	public boolean isStart = true;
	public boolean buildLumberJack;
	public boolean buildSoldier;
	public boolean chase;
	public boolean congregate;
	public boolean inDanger;
	
	public Network() throws GameActionException{
		//super(myRC);
		//updateNetwork();
	}
	
	public void updateNetwork()throws GameActionException {
		robots = controller.senseNearbyRobots();
		
		if(!isStart) {
			for(RobotInfo robot: robots) {
				Stream stream = buildStreams(robot);
				int startSocket = stream.getStartSocket();
				
				sendStreams(stream,startSocket);
				readStream(startSocket);
				
			}
			
			readDanger();
		}
		
		setBehaviour();
		
		if(network.isFull()) {
			network.clear();
		}
		currentArchon = controller.getInitialArchonLocations(controller.getTeam())[0];
		
	}
	public void setBehaviour() {
		if(allyCount >= 1) {
			isStart = false;
			return;
		}
		
		if(trees.length >= 4) {
			buildLumberJack = true;
		}
		
		if(dangerCount >= 10) {
			inDanger = true;
			buildSoldier = true;
			congregate = true;
		}else if((dangerCount < 10) && (allyCount < 10)) {
			inDanger = false;
			buildLumberJack = true;
			congregate = true;
		}else if((dangerCount >= 10) && (allyCount >= 10)) {
			inDanger = true;
			buildSoldier = true;
			chase = true;
		}else {
			inDanger = false;
			buildSoldier = true;
			buildLumberJack = true;
			congregate = true;
		}
	}
	public void readDanger()throws GameActionException {
		float distance = 0;
		int i=0;
		
		readSoldierStreams();
		addDanger(i,distance);
		
		readTankStreams();
		addDanger(i,distance);
		
		readLumberjackStreams();
		addDanger(i,distance);
		
		readScoutStreams();
		addDanger(i,distance);
		
	}
	public void addDanger(int i,float distance) {
		while(readNext()) {
			distance = this.stream.getLocation().distanceSquaredTo(controller.getLocation());
			if(distance <= controller.getType().strideRadius*10) {
				dangers[i] = this.stream.getLocation();
				i++;
				dangerCount++;
			}
		}
	}
	public void sendStreams(Stream stream,int socket)throws GameActionException {
		if(stream != null && stream.getThreat()) {
			network.add(socket, stream);
		}
	}
	
	public void readArchonStreams()throws GameActionException {
		readStream(Sockets.ARCHON_SOCKET_START);
	}
	
	public void readGardenerStreams()throws GameActionException {
		readStream(Sockets.GARDENER_SOCKET_START);
	}
	
	public void readLumberjackStreams()throws GameActionException {
		readStream(Sockets.JACK_SOCKET_START);
	}
	
	public void readSoldierStreams()throws GameActionException {
		readStream(Sockets.SOLDIER_SOCKET_START);
	}
	
	public void readScoutStreams()throws GameActionException {
		readStream(Sockets.SCOUT_SOCKET_START);
	}
	
	public void readTankStreams()throws GameActionException {
		readStream(Sockets.TANK_SOCKET_START);
	}
	
	public boolean readNext() {
		if(this.current != null && this.current.next != null) {
			this.current = this.current.next;
			return true;
		}
		return false;
	}
	
	public void readStream(int socket) throws GameActionException{
	    this.current = network.searchNode(socket);
	    
	    if(this.current == null) {
	    	return;
	    }else {
	    	this.stream = current.data;
	    }
	    
	}
	
	public Stream getStream() {return this.stream;}
	
	public void clearStreams(int socket)throws GameActionException {
		Node node = network.searchNode(socket);
		network.delete(socket,node.data);
	}
	
	public void sendStrategy(int strategy)throws GameActionException {
		controller.broadcastInt(Sockets.STRATEGY_SOCKET, strategy);
	}
	
	public static Stream buildStreams(RobotInfo robot) {
		switch(robot.getType()) {
		case ARCHON:
			int id = robot.getID();
			MapLocation loc = robot.getLocation();
			int round = controller.getRoundNum();
			int start = Sockets.ARCHON_SOCKET_START;
			RobotType type = robot.getType();
			
			float distance = controller.getLocation().distanceSquaredTo(loc);
			
			if(distance <= controller.getType().strideRadius*5) {
				Stream stream = new Stream(id,loc,start,round,type,true);
				return stream;
			}
			
			Stream stream = new Stream(id,loc,start,round,type,false);
			return stream;
		case GARDENER:
			id = robot.getID();
			loc = robot.getLocation();
			round = controller.getRoundNum();
			start = Sockets.GARDENER_SOCKET_START;
			type = robot.getType();
			
			distance = controller.getLocation().distanceSquaredTo(loc);
			
			if(distance <= controller.getType().strideRadius*3) {
				stream = new Stream(id,loc,start,round,type,true);
				return stream;
			}
			
			stream = new Stream(id,loc,start,round,type,false);
			return stream;
		case LUMBERJACK:
			id = robot.getID();
			loc = robot.getLocation();
			round = controller.getRoundNum();
			start = Sockets.JACK_SOCKET_START;
			type = robot.getType();
			
			distance = controller.getLocation().distanceSquaredTo(loc);
			
			if(distance <= controller.getType().strideRadius*3) {
				stream = new Stream(id,loc,start,round,type,true);
				return stream;
			}
			
			stream = new Stream(id,loc,start,round,type,false);
			return stream;
		case SOLDIER:
			id = robot.getID();
			loc = robot.getLocation();
			round = controller.getRoundNum();
			start = Sockets.SOLDIER_SOCKET_START;
			type = robot.getType();
			
			distance = controller.getLocation().distanceSquaredTo(loc);
			
			if(distance <= controller.getType().strideRadius*3) {
				stream = new Stream(id,loc,start,round,type,true);
				return stream;
			}
			
			stream = new Stream(id,loc,start,round,type,false);
			return stream;
		case SCOUT:
			id = robot.getID();
			loc = robot.getLocation();
			round = controller.getRoundNum();
			start = Sockets.SCOUT_SOCKET_START;
			type = robot.getType();
			
			distance = controller.getLocation().distanceSquaredTo(loc);
			
			if(distance <= controller.getType().strideRadius*3) {
				stream = new Stream(id,loc,start,round,type,true);
				return stream;
			}
			
			stream = new Stream(id,loc,start,round,type,false);
			return stream;
		case TANK:
			id = robot.getID();
			loc = robot.getLocation();
			round = controller.getRoundNum();
			start = Sockets.TANK_SOCKET_START;
			type = robot.getType();
			
			distance = controller.getLocation().distanceSquaredTo(loc);
			
			if(distance <= controller.getType().strideRadius*3) {
				stream = new Stream(id,loc,start,round,type,true);
				return stream;
			}
			
			stream = new Stream(id,loc,start,round,type,false);
			return stream;
		default:
			return null;
		}
	}
}
