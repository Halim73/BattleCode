package HalimRobot;
import battlecode.common.*;
import java.util.Random;

public strictfp class HRobotPlayer {
	
	private static RobotController halim;
	
	private static MapLocation rallyPoint;
	private static Random rand;
	
	private static Direction[] moveList = new Direction[4];
	private static Direction step;
	
	private static Direction NORTH;
	private static Direction EAST;
	private static Direction SOUTH;
	private static Direction WEST;
	
	public static int GARDEN_CHANNEL = 1;
	public static int GMAX = 10;
	

	
	public static void run(RobotController myRC) throws GameActionException{
		halim = myRC;
		rand  = new Random(halim.getID());
		
		initialize();
		
		//step = randomDirection();
		
		switch(halim.getType()) {
		case ARCHON:
			runArchon();
			break;
		case GARDENER:
			runGardener();
			break;
		}
	}
	static void runArchon()throws GameActionException{
		while(true) {
			try {
				meander();
				
				step = randomDirection();
				int numOfGardener = halim.readBroadcast(GARDEN_CHANNEL);
				
				halim.broadcast(GARDEN_CHANNEL,0);
				
				if(numOfGardener < GMAX && halim.canHireGardener(step) ) {
					halim.hireGardener(step);
					halim.broadcast(GARDEN_CHANNEL,numOfGardener++);
				}
				
				Clock.yield();
				
			}catch(Exception e) {
				System.out.println("Saved your life");
				e.printStackTrace();
			}
		}
	}
	
	public static void runGardener()throws GameActionException {
		while(true) {
			plant();
			water();
			
			try {
				int prevNum = halim.readBroadcast(GARDEN_CHANNEL);
				
				halim.broadcast(GARDEN_CHANNEL, prevNum++);
				meander();
				
				step = randomDirection();
				
				
				Clock.yield();
			}catch(Exception e) {
				System.out.println("Saved gardener");
				e.printStackTrace();
			}
	}
	
	public static Direction randomDirection() {
		return moveList[rand.nextInt(4)];
	}
	
	public static void initialize() {
		for(int i=0;i<4;i++) {
			float radians = (float)(-Math.PI+(2*Math.PI)*((float) i)/4);
			moveList[i] = new Direction(radians);
			System.out.println("Created this move -> "+moveList[i]);
		}
	}
	public static void build(RobotType type,int cost)throws GameActionException {
		if(halim.getTeamBullets() > cost) {
			for(int i=0;i<4;i++) {
				if(halim.canBuildRobot(type,moveList[i])) {
					halim.buildRobot(type,moveList[i]);
					break;
				}
			}
		}
	}
	public static void plant() throws GameActionException{
		if(halim.getTeamBullets()>GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
            for (int i = 0; i < 4; i++) {
                //only plant trees on a sub-grid
                MapLocation p = halim.getLocation().add(moveList[i],GameConstants.GENERAL_SPAWN_OFFSET+GameConstants.BULLET_TREE_RADIUS+halim.getType().bodyRadius);
                if(modCheck(p.x,6,0.2f) && modCheck(p.y,6,0.2f)) {
                    if (halim.canPlantTree(moveList[i])) {
                        halim.plantTree(moveList[i]);
                        break;
                    }
                }
            }
        }

	}
	public static void water() throws GameActionException{
		if(halim.canWater()) {
            TreeInfo[] nearbyTrees = halim.senseNearbyTrees();
            for (int i = 0; i < nearbyTrees.length; i++)
                if(nearbyTrees[i].getHealth()<GameConstants.BULLET_TREE_MAX_HEALTH-GameConstants.WATER_HEALTH_REGEN_RATE) {
                    if (halim.canWater(nearbyTrees[i].getID())) {
                        halim.water(nearbyTrees[i].getID());
                        break;
                    }
                }
        }
	}
	public static boolean modCheck(float number,float space,float fraction) {
		return (number%space)<space*fraction;
	}
	public static void meander() {
		try {
			Direction direction = randomDirection();
			if(halim.canMove(direction)) {
				halim.move(direction);
			}
		}catch(Exception e) {
			System.out.println("Saved you from untimely death");
			e.printStackTrace();
		}
	}
}
