package utilityBot;
import battlecode.common.*;


public class Utility {
	static RobotController controller;
	
	static MapLocation[] generalEnemies;
	static MapLocation[] enemyGardeners = new MapLocation[100];
	static MapLocation[] enemyArchons = new MapLocation[5];
	static MapLocation[] enemyTanks = new MapLocation[50];
	static MapLocation[] enemySoldiers = new MapLocation[100];
	static MapLocation[] enemyScouts = new MapLocation[50];
	static MapLocation[] enemyJacks = new MapLocation[50];
	static MapLocation[] bulletLoc = new MapLocation[100];
	
	static MapLocation[] allies = new MapLocation[50];
	
	static MapLocation[] treeLoc;
	
	static TreeInfo[] trees = new TreeInfo[100];
	static TreeInfo[] neutralTrees = new TreeInfo[100];
	static TreeInfo[] opTrees;
	
	static BulletInfo[]bullets;
	static RobotInfo[] robots;
	
	static int enemyCount;
	static int bulletCount;
	
	static int opArchons;
	static int opGardeners;
	static int opSoldiers;
	static int opTanks;
	static int opJacks;
	static int opScouts;
	
	static int allyCount;
	static int numGardeners;
	static int numSoldiers;
	static int numTanks;
	static int numArchons;
	static int numJacks;
	static int numScouts;
	static int allyTrees;
	
	public Utility(RobotController me) throws GameActionException{
		controller = me;
		//updateInfo();
	}
	public Utility() {
		return;
	}
	public void clear() {
		allyCount = numGardeners = numSoldiers = numTanks = numJacks = numScouts = allyTrees = 0;
		enemyCount = opArchons = opGardeners = opSoldiers = opTanks = opJacks = opScouts = 0;
		
		enemyGardeners = new MapLocation[100];
		enemyArchons = new MapLocation[5];
		enemyTanks = new MapLocation[50];
		enemySoldiers = new MapLocation[100];
		enemyScouts = new MapLocation[50];
		enemyJacks = new MapLocation[50];
		bulletLoc = new MapLocation[100];
		
		allies = new MapLocation[50];
		
		trees = new TreeInfo[100];
		neutralTrees = new TreeInfo[100];
	}
	public void updateInfo() throws GameActionException{
		try {
			sense();
			senseBullets();
			senseTrees();
		}catch(Exception e) {
			System.out.println("Couldn't update");
			e.printStackTrace();
		}
	}
	
	public void senseTrees() throws GameActionException{
		try {
			trees = controller.senseNearbyTrees(controller.getType().sensorRadius,controller.getTeam());
			neutralTrees = controller.senseNearbyTrees(controller.getType().sensorRadius,Team.NEUTRAL);
			opTrees = controller.senseNearbyTrees(controller.getType().sensorRadius,controller.getTeam().opponent());
		}catch(Exception e) {
			System.out.println("Couldn't sense trees");
			e.printStackTrace();
		}
	}
	
	public void senseBullets() throws GameActionException{
		bullets = controller.senseNearbyBullets();
		
		try {
			for(BulletInfo bullet:bullets) {
				add(bullet.getLocation(),bulletLoc);
				bulletCount++;
			}
		}catch(Exception e) {
			System.out.println("caught");
			e.printStackTrace();
		}
	}
	
	public void sense() throws GameActionException{
		robots = controller.senseNearbyRobots();
		RobotInfo[] badGuys = controller.senseNearbyRobots(controller.getType().sensorRadius,controller.getTeam().opponent());
		try {
			for(RobotInfo robot: robots) {
				if(robot.getTeam() != controller.getTeam()) {
					if((robot.getType() == RobotType.ARCHON) && (controller.canSenseRobot(robot.getID()))) {
						add(robot.getLocation(),enemyArchons);
						opArchons++;
					}else if((robot.getType() == RobotType.GARDENER) && (controller.canSenseRobot(robot.getID()))) {
						add(robot.getLocation(),enemyGardeners);
						opGardeners++;
					}else if((robot.getType() == RobotType.SOLDIER) && (controller.canSenseRobot(robot.getID()))) {
						add(robot.getLocation(),enemySoldiers);
						opSoldiers++;
					}else if((robot.getType() == RobotType.SCOUT) && (controller.canSenseRobot(robot.getID()))) {
						add(robot.getLocation(),enemyScouts);
						opScouts++;
					}else if((robot.getType() == RobotType.LUMBERJACK) && (controller.canSenseRobot(robot.getID()))) {
						add(robot.getLocation(),enemyJacks);
						opJacks++;
					}else if((robot.getType() == RobotType.TANK) && (controller.canSenseRobot(robot.getID()))) {
						add(robot.getLocation(),enemyTanks);
						opTanks++;
					}
					enemyCount++;

				}else if(robot.getTeam() == controller.getTeam()) {
					addAllies(robot.getLocation(), allies,robot.getType());
				}else {
					break;
				}
				
				for(RobotInfo guy:badGuys) {
					add(guy.getLocation(),generalEnemies);
				}
			}
			allyCount = controller.getRobotCount();
		}catch(Exception e) {
			System.out.println("Saved something");
			e.printStackTrace();
		}
		
	}
	
	public void addAllies(MapLocation loc,MapLocation[] array, RobotType type) {
        int i = 0;
		
        if(type == RobotType.GARDENER) {
        	numGardeners++;
        }else if(type == RobotType.SOLDIER) {
        	numSoldiers++;
        }else if(type == RobotType.SCOUT) {
        	numScouts++;
        }else if(type == RobotType.LUMBERJACK) {
        	numJacks++;
        }else if(type == RobotType.TANK) {
        	numTanks++;
        }
        
		do {
			if(i == 0 || array[i] == null) {
				array[i] = loc;
				i++;
			}else {
				array[i] = loc;
				i++;
			}
		}while(array[i] != null);
	}
	
	public void add(MapLocation loc,MapLocation[] array) {
		int i = 0;
		
		do {
			if(i == 0 || array[i] == null) {
				array[i] = loc;
				i++;
			}else {
				array[i] = loc;
				i++;
			}
		}while(array[i] != null);
	}
	
	public boolean produceUnit(RobotType type,Direction direction) throws GameActionException{
		try {
			if(controller.canBuildRobot(type, direction)) {
				controller.buildRobot(type,direction);
				return true;
			}
			
			int checks = 1;
			int stop = 6;
			
			while(checks <= stop) {
				if(controller.canBuildRobot(type, direction.rotateLeftRads((float)(checks*Math.PI/6)))){
					controller.buildRobot(type, direction.rotateLeftRads((float)(checks*Math.PI/6)));
					return true;
				}
				
				if(controller.canBuildRobot(type, direction.rotateRightRads((float)(checks*Math.PI/6)))){
					controller.buildRobot(type, direction.rotateRightRads((float)(checks*Math.PI/6)));
					return true;
				}
				checks++;
			}
		}catch(Exception e) {
			System.out.println("Can't Produce unit of type "+type);
			e.printStackTrace();
		}
		return false;
	}
}
