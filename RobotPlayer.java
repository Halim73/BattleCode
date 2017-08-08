package utilityBot;

//import java.util.Random;
import battlecode.common.*;

public strictfp class RobotPlayer {
	/*public static RobotController control;
	public static Utility util;
	
	private static Random rand;

	private static Direction[] dirList;
	
	public static final int GARDENER_MAX = 5;
	public static final int LUMBERJACK_MAX = 10;*/
	
	@SuppressWarnings("unused")
	public static void run(RobotController myRC) throws GameActionException{
		Utility util = new Utility(myRC);
		Network network = new Network();
		Behavior behavior = new Behavior();
		//Strategy strategy = new Strategy(myRC);
		
		switch(myRC.getType()) {
		case ARCHON:
			while(true) {
				util.updateInfo();
				network.updateNetwork();
				behavior.makeChoice();
				util.clear();
			}
			
			//break;
		case GARDENER:
			while(true) {
				util.updateInfo();
				network.updateNetwork();
				behavior.makeChoice();
				util.clear();
			}
			
			//break;
		case LUMBERJACK:
			while(true) {
				util.updateInfo();
				network.updateNetwork();
				behavior.makeChoice();
				util.clear();
			}
			
			//break;
		case SCOUT:
			while(true) {
				util.updateInfo();
				network.updateNetwork();
				behavior.makeChoice();
				util.clear();
			}
			//break;
		case SOLDIER:
			while(true) {
				util.updateInfo();
				network.updateNetwork();
				behavior.makeChoice();
				util.clear();
			}
			
			//break;
		case TANK:
			while(true) {
				util.updateInfo();
				network.updateNetwork();
				behavior.makeChoice();
				util.clear();
			}
			
			//break;
		default:
			while(true) {
				util.updateInfo();
				network.updateNetwork();
				behavior.makeChoice();
				util.clear();
			}
			
			//break;
		
		}
	}
}
