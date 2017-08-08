package utilityBot;
import battlecode.common.*;

public class Stream {
	private static final int DENSITY = 5;
	
	private int ID;
	private MapLocation location;
	private int round;
	private RobotType type;
	private int SOCKET_START;
	private boolean danger;
	
	public Stream(int id, MapLocation loc, int round, int start,RobotType type,boolean danger) {
		this.ID = id;
		this.location = loc;
		this.round = round;
		this.SOCKET_START = start;
		this.type = type;
		this.danger = danger;
	}
	
	public int getDensity() {return DENSITY;}
	public int getID() {return this.ID;}
	public MapLocation getLocation() {return this.location;}
	public int getRound() {return this.round;}
	public int getStartSocket() {return this.SOCKET_START;}
	public RobotType getType() {return this.type;}
	public boolean getThreat() {return this.danger;}
	
	public int getNumType() {
		switch(this.getType()) {
		case ARCHON:
			return 1;
		case GARDENER:
			return 2;
		case LUMBERJACK:
			return 3;
		case SCOUT:
			return 4;
		case SOLDIER:
			return 5;
		case TANK:
			return 6;
		default:
			return 0;
		}
	}
}
