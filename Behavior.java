package Mechanics;
import battlecode.common.*;
import java.util.Random;
import static halimBot.RobotPlayer.controller;

public class Behavior {	
	static Direction[] dirList = new Direction[8];
	public static Random rand = new Random();
	
	static MapLocation[] visited = new MapLocation[50];
	public static MapLocation[] mapEdges = new MapLocation[4];
	public static MapLocation[] swarmLocs = new MapLocation[100];
	
	static int count = 0;
	
	public static boolean checkDirection(Direction direction) throws GameActionException{
		assert controller != null;
		if(direction == null) {
			direction = new Direction(rand.nextFloat(),rand.nextFloat());
		}
		
		try {
			if(controller.hasMoved()) {
				return false;
			}
			
			//MapLocation goal = controller.getLocation().add(direction);
			
			if(controller.canMove(direction,controller.getType().sensorRadius)){
				controller.move(direction,controller.getType().sensorRadius);
				return true;
			}
			
			int checks = 1;
			int stop = 24;
			
			while(checks <= stop) {
				if(controller.canMove(direction.rotateLeftDegrees((float)(checks*10)),controller.getType().sensorRadius)){
					controller.move(direction.rotateLeftDegrees((float)(checks*10)),controller.getType().sensorRadius);
					
					MapLocation visitedLoc = controller.getLocation();
					
					if(!hasVisited(visitedLoc)) {
						addVisited(visitedLoc);
						return true;
					}
					
					return true;
				}
				
				if(controller.canMove(direction.rotateRightDegrees((float)(checks*10)),controller.getType().strideRadius)){
					controller.move(direction.rotateRightDegrees((float)(checks*10)),controller.getType().strideRadius);
					
					MapLocation visitedLoc = controller.getLocation();
					
					if(!hasVisited(visitedLoc)) {
						addVisited(visitedLoc);
						return true;
					}
					
					return true;
				}
				checks++;
			}
			return false;
		}catch(GameActionException e) {
			System.out.println("cannot move");
			e.printStackTrace();
			return false;
		}catch(Exception e) {
			System.out.println("Unknown check direction error:");
			e.printStackTrace();
			Direction dir = Direction.getNorth();
			checkDirection(dir);
			return false;
		}
	}
	
	public static void dodge(RobotInfo robot)throws GameActionException {
		MapLocation loc = robot.getLocation();
		float distance = controller.getLocation().distanceTo(loc);
		
		if(distance < controller.getType().sensorRadius/3) {
			Direction dodge = controller.getLocation().directionTo(loc.add((float)Math.PI/3, controller.getType().strideRadius*2)).opposite();
			checkDirection(dodge);
		}else {
			MapLocation sideStep = controller.getLocation().add((float)Math.PI/4, controller.getType().sensorRadius/2);
			Direction dodge = controller.getLocation().directionTo(sideStep);
			checkDirection(dodge);
		}
	}
	
	public static void soldierShoot(RobotInfo robot)throws GameActionException {
		
		switch(robot.getType()) {
		case ARCHON:
			if(controller.canFireTriadShot()) {
				controller.fireTriadShot(controller.getLocation().directionTo(robot.getLocation()));
			}else {
				controller.fireSingleShot(controller.getLocation().directionTo(robot.getLocation()));
			}
			break;
		case GARDENER:
			if(controller.canFireTriadShot()) {
				controller.fireTriadShot(controller.getLocation().directionTo(robot.getLocation()));
			}else {
				controller.fireSingleShot(controller.getLocation().directionTo(robot.getLocation()));
			}
			break;
		case LUMBERJACK:
			if(controller.canFireTriadShot()) {
				controller.fireTriadShot(controller.getLocation().directionTo(robot.getLocation()));
			}else {
				controller.fireSingleShot(controller.getLocation().directionTo(robot.getLocation()));
			}
			break;
		case SCOUT:
			if(controller.canFireSingleShot()) {
				controller.fireSingleShot(controller.getLocation().directionTo(robot.getLocation()));
			}
			break;
		case SOLDIER:
			if(controller.canFireSingleShot()) {
				controller.fireSingleShot(controller.getLocation().directionTo(robot.getLocation()));
			}
			break;
		case TANK:
			if(controller.canFirePentadShot()) {
				controller.firePentadShot(controller.getLocation().directionTo(robot.getLocation()));
			}else {
				controller.fireSingleShot(controller.getLocation().directionTo(robot.getLocation()));
			}
			break;
		default:
			break;
		
		}
	}
	public static boolean hasVisited(MapLocation location) throws NullPointerException{
		try {
			for(MapLocation loc:visited) {
				if(loc != null && (loc.isWithinDistance(controller.getLocation(), controller.getType().bodyRadius*2))){
					return true;
				}
			}
		}catch(NullPointerException e) {
			System.out.println("saved null");
			return false;
		}
		return false;
	}
	
	public static void addVisited(MapLocation loc) {
		if(count == 50) {
			clearVisited();
			visited[count++] = loc;
		}else {
			visited[count++] = loc;
		}
	}
	
	public static void clearVisited() {
		visited = new MapLocation[50];
		count = 0;
	}
	
	public static void initialize() {
		Direction direction = Direction.getNorth();
		for(int i=0;i<8;i++) {
			dirList[i] = direction.rotateRightDegrees((float)i*45);
			System.out.println(controller.getType()+" Created this move -> "+dirList[i]);
		}
		
		MapLocation topLeftCorner = new MapLocation(GameConstants.MAP_MAX_HEIGHT,GameConstants.MAP_MAX_WIDTH);
		MapLocation bottomLeftCorner = new MapLocation(GameConstants.MAP_MIN_HEIGHT,GameConstants.MAP_MIN_WIDTH);
		MapLocation topRightCorner = new MapLocation(GameConstants.MAP_MAX_HEIGHT,GameConstants.MAP_MIN_WIDTH);
		MapLocation bottomRightCorner = new MapLocation(GameConstants.MAP_MIN_HEIGHT,GameConstants.MAP_MAX_WIDTH);
		
		mapEdges[0] = topLeftCorner;
		mapEdges[1] = bottomLeftCorner;
		mapEdges[2] = topRightCorner;
		mapEdges[3] = bottomRightCorner;
	}
	
	public static Direction randomDirection() {
		return dirList[rand.nextInt(dirList.length)];
	}
	
	public static void evade() throws GameActionException {
		try {
			BulletInfo[] bullets = controller.senseNearbyBullets(controller.getLocation(),controller.getType().bulletSightRadius);
			
			if(bullets.length >= 1) {
				for(BulletInfo bullet:bullets) {
					if(collisionCheck(bullet)) {
						MapLocation location = bullet.getLocation();
						MapLocation myLocation = controller.getLocation();

						Direction meToYou = new Direction(myLocation,location);
						Direction move = meToYou.opposite().rotateLeftDegrees(60);

						checkDirection(move);
					}
				}
			}
		}catch(Exception e) {
			System.out.println("saved");
			e.printStackTrace();
		}
	}
	
	public static boolean collisionCheck(BulletInfo bullet) {
		MapLocation location = controller.getLocation();
		
		Direction bulletDir = bullet.dir;
		MapLocation bulletLoc = bullet.location;
		
		Direction thisDir = bulletLoc.directionTo(location);
		float distance = bulletLoc.distanceTo(location);
		float theta = bulletDir.radiansBetween(thisDir);
		
		if(Math.abs(theta) > (Math.PI/2)){
			return false;
		}
		
		float directDist = (float) Math.abs(distance*Math.sin(theta));
		
		return (directDist <= controller.getType().bodyRadius);
	}
	
	public static boolean modCheck(float number,float space,float fraction) {
		return (number%space)<space*fraction;
	}
	
	public static boolean plant() throws GameActionException{
		int check = 0;
		Direction dir = Direction.getNorth();
		
		if(controller.getTeamBullets()>GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
			for(int i=1;i<4;i++) {         		
        		if(controller.canPlantTree(dir.rotateLeftDegrees(i*90))) {
        			dir = dir.rotateLeftDegrees(i*90);
        			controller.plantTree(dir);
        			
        		}else if(controller.canPlantTree(dir.rotateRightDegrees(i*90))) {
        			dir = dir.rotateRightDegrees(i*90);
        			controller.plantTree(dir);
        			
        		}
        		check++;
        		if(check > 3) {
    				break;
    			}
        	}
			return true;
		}else {
			return false;
		}
	}
	
	public static void water() throws GameActionException{
		try {
			if(controller.canWater() ) {
				for(TreeInfo tree: Network.myTrees) {
					if(controller.canWater(tree.getLocation()) && (controller.canShake(tree.getLocation()))) {
						controller.water(tree.getLocation());
						controller.shake(tree.getLocation());
						break;
					}else if(controller.canWater(tree.getLocation()) && (!controller.canShake(tree.getLocation()))) {
						controller.water(tree.getLocation());
						break;
					}else {
						break;
					}
				}
			}
		}catch(Exception e) {
			System.out.println("save");
			e.printStackTrace();
		}
	}
	
	public static MapLocation findRallyAlly()throws GameActionException,NullPointerException {
		try {
			int count = 0;
			if(controller.readBroadcastInt(Ports.ALLY_COUNT) > 0) {
				for(int i=Ports.MY_LOC_START;i<Ports.MY_LOC_END;i+=2) {
					if(count != 0 && controller.readBroadcast(i) == 0) {
						float x = controller.readBroadcastFloat(i-1);
						float y = controller.readBroadcastFloat(i-2);
						MapLocation loc = new MapLocation(x,y);
						
						if(!loc.equals(controller.getLocation()) && loc.isWithinDistance(controller.getLocation(),controller.getType().strideRadius)) {
							return loc.add((float)(Math.PI/4),controller.getType().strideRadius);
						}else {
							count++;
						}
					}else {
						float x = controller.readBroadcastFloat(i);
						float y = controller.readBroadcastFloat(i+1);
						MapLocation loc = new MapLocation(x,y);
						
						return loc;
					}
				}
			}else if(controller.readBroadcastInt(Ports.ALLY_COUNT) == 0){
				return controller.getLocation().add((float)Math.PI/4,controller.getType().strideRadius);
			}else {
				return controller.getLocation().add((float)-Math.PI/4,controller.getType().strideRadius);
			}
			return controller.getLocation();
		}catch(NullPointerException e) {
			System.out.println("saved null");
			float x = controller.getLocation().x + controller.getType().strideRadius;
			float y = controller.getLocation().y + controller.getType().strideRadius;
			MapLocation nullPoint = new MapLocation(x,y);
			
			return nullPoint;
		}
	}
	
	public static MapLocation findSwarmDirection()throws GameActionException, NullPointerException{
		try {
			float closestDistance = 9999f;
			float distance = 0;
			int count = 0;
			
			MapLocation[] generalEnemies = controller.getInitialArchonLocations(controller.getTeam().opponent());
			MapLocation generalEnemy = generalEnemies[0];
			
			if(controller.readBroadcastInt(Ports.ENEMY_COUNT) < 3){
				if(generalEnemy != null) {
					return generalEnemy;
				}else {
					return controller.getLocation().add(randomDirection());
				}
			}else {
				for(int i=Ports.ENEMY_LOC_PORT_START;i<Ports.ENEMY_LOC_PORT_END;i+=2) {
					if(controller.readBroadcast(i) != 0) {
						float x = controller.readBroadcastFloat(i);
						float y = controller.readBroadcastFloat(i+1);
						
						MapLocation loc = new MapLocation(x,y);
						distance = controller.getLocation().distanceTo(loc);
						
						if(distance < closestDistance) {
							generalEnemy = loc;
							closestDistance = distance;
							swarmLocs[count++] = loc;
						}
					}
				}
				return generalEnemy;
			}
		}catch(NullPointerException e) {
			System.out.println("saved null");
			float x = controller.getLocation().x + controller.getType().strideRadius;
			float y = controller.getLocation().y + controller.getType().strideRadius;
			MapLocation nullPoint = new MapLocation(x,y);
			
			return nullPoint;
		}catch(GameActionException e) {
			System.out.println("Saved finding of swarm direction");
			e.printStackTrace();
			return controller.getLocation();
		}
	}
	
	public static void handleTrees()throws GameActionException {
		try {
			if(Network.neutralTrees.length >= 1) {
				for(TreeInfo tree:Network.neutralTrees) {
					if(controller.canInteractWithTree(tree.getLocation())) {
						if(controller.canShake(tree.getLocation()) && tree.getContainedBullets() >= 1) {
							controller.shake(tree.getLocation());
						}else if(controller.canChop(tree.getLocation())){
							controller.chop(tree.getLocation());
						}
					}
				}return;
			}
			
			if(Network.myTrees.length >= 6) {
				for(TreeInfo tree:Network.myTrees) {
					if(controller.canInteractWithTree(tree.getLocation())) {
						if(controller.canShake(tree.getLocation()) && tree.getContainedBullets() >= 1) {
							controller.shake(tree.getLocation());
						}else if(controller.canChop(tree.getLocation())){
							controller.chop(tree.getLocation());
						}
					}
				}return;
			}
			
			if(Network.enemyTrees.length >= 3) {
				for(TreeInfo tree:Network.enemyTrees) {
					if(controller.canInteractWithTree(tree.getLocation())) {
						if(controller.canShake(tree.getLocation()) && tree.getContainedBullets() >= 1) {
							controller.shake(tree.getLocation());
						}else if(controller.canChop(tree.getLocation())){
							controller.chop(tree.getLocation());
						}
					}
				}return;
			}
		}catch(Exception e) {
			System.out.println("Saved tree handle");
			e.printStackTrace();
		}
	}
	public static boolean produceUnit(RobotType type,Direction direction) throws GameActionException{
		try {
			if(direction == null) {
				direction = Direction.getNorth();
			}
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