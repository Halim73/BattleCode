package utilityBot;
import battlecode.common.*;
import java.util.Random;

public class Behavior extends Network{
	public static Direction[] dirList = new Direction[4];
	public static Random rand = new Random();
	
	public static MapLocation[] visited = new MapLocation[20];
	//public MapLocation rallyPoint;
    
	MapLocation[]mapEdges = new MapLocation[4];
	
	public static int count = 0;
    public static boolean hasWatered = false;
    
    public Strategy strategy;
    
	public Behavior() throws GameActionException{
		//super(myRC);
		
		initialize();
		
		MapLocation topLeftCorner = new MapLocation(GameConstants.MAP_MAX_HEIGHT,GameConstants.MAP_MAX_WIDTH);
		MapLocation bottomLeftCorner = new MapLocation(GameConstants.MAP_MIN_HEIGHT,GameConstants.MAP_MIN_WIDTH);
		MapLocation topRightCorner = new MapLocation(GameConstants.MAP_MAX_HEIGHT,GameConstants.MAP_MIN_WIDTH);
		MapLocation bottomRightCorner = new MapLocation(GameConstants.MAP_MIN_HEIGHT,GameConstants.MAP_MAX_WIDTH);
		
		mapEdges[0] = topLeftCorner;
		mapEdges[1] = bottomLeftCorner;
		mapEdges[2] = topRightCorner;
		mapEdges[3] = bottomRightCorner;
	}
	
	public void makeChoice() throws GameActionException{
		if(isStart) {
			strategy = new Strategy();
			build(RobotType.GARDENER,100,randomDirection());
			dictateBehavior();
			Clock.yield();
		}else {
			build(RobotType.SOLDIER,100,randomDirection());
			dictateBehavior();
			Clock.yield();
		}
	}
	
	public void dictateBehavior() throws GameActionException {
		
		if(isStart) {
			strategy.clusterCollect();
			isStart = false;
			Clock.yield();
		}
		
		if(controller.getRoundNum() <= 1000) {
			strategy.clusterCollect();
			Clock.yield();
		}else if(controller.getRoundNum() > 1000 && controller.getRoundNum() <= 2000) {
			strategy.clusterDefend();
			Clock.yield();
		}else {
			strategy.clusterCollect();
			Clock.yield();
		}
		
		/*if(inDanger || (buildSoldier && congregate)) {
			strategy.clusterDefend();
			Clock.yield();
		}else if(!inDanger || (buildLumberJack && congregate)) {
			strategy.clusterCollect();
			Clock.yield();
		}else if(inDanger || (buildSoldier && chase)){
			strategy.swarmDefend();
			Clock.yield();
		}else {
			Clock.yield();
			return;
		}*/
	}
	
	public void lumberJackAttack(boolean status,Direction rallyDir)throws GameActionException {
		if(inDanger == true && dangerCount >= 1) {
			for(MapLocation loc: dangers) {
				Direction dir = controller.getLocation().directionTo(loc);
				
				if(!controller.hasAttacked()) {
					controller.strike();
					break;
				}else {
					checkDirection(dir);
					break;
				}
			}
		}else if(inDanger == false) {
			for(MapLocation loc:enemyGardeners) {
				Direction dir = controller.getLocation().directionTo(loc);
				
				if(checkDirection(dir)) {
					controller.strike();
				}else {
					lumberJackAttack(!inDanger,dir);
				}
			}
		}
	}
	
	public void soldierAttack(boolean status,Direction rallyDir)throws GameActionException {
		if(inDanger == true && dangerCount >= 1) {
			for(MapLocation loc: dangers) {
				if(controller.canFireSingleShot() && controller.getTeamBullets() > GameConstants.SINGLE_SHOT_COST) {
					Direction dir = controller.getLocation().directionTo(loc);
					controller.fireSingleShot(dir);
					checkDirection(dir.opposite());
					break;
				}else {
					checkDirection(controller.getLocation().directionTo(controller.getLocation().add((float)Math.PI/6,controller.getType().strideRadius)));
				}
			}
		}else if(inDanger == false && enemyCount >= 1 ) {
			if(opJacks >= 3) {
				for(MapLocation loc:enemyJacks) {
					if(controller.canFireSingleShot() && controller.getTeamBullets() > GameConstants.SINGLE_SHOT_COST) {
						Direction dir = controller.getLocation().directionTo(loc);
						controller.fireSingleShot(dir);
						checkDirection(dir.opposite());
						break;
					}else {
						checkDirection(controller.getLocation().directionTo(controller.getLocation().add((float)Math.PI/6,controller.getType().strideRadius)));
					}
				}
			}if(generalEnemies.length > 1 ) {
				for(MapLocation loc:generalEnemies) {
					if(controller.canFireSingleShot() && controller.getTeamBullets() > GameConstants.SINGLE_SHOT_COST) {
						Direction dir = controller.getLocation().directionTo(loc);
						controller.fireSingleShot(dir);
						checkDirection(dir);
						break;
					}else {
						checkDirection(controller.getLocation().directionTo(controller.getLocation().add((float)Math.PI/6,controller.getType().strideRadius)));
					}
				}
			}else {
				if(controller.canFireSingleShot() && controller.getTeamBullets() > GameConstants.SINGLE_SHOT_COST) {
					Direction dir = controller.getLocation().directionTo(enemyArchons[0]);
					controller.fireSingleShot(dir);
					checkDirection(dir);
				}else {
					checkDirection(controller.getLocation().directionTo(controller.getLocation().add((float)Math.PI/6,controller.getType().strideRadius)));
				}
			}
		}
	}
	
	public void plant() throws GameActionException{
		if(controller.getTeamBullets()>GameConstants.BULLET_TREE_COST) {//have enough bullets. assuming we haven't built already.
            if(inDanger == false || trees.length <= 1) {
            	Direction direction = randomDirection();
            	if(checkDirection(direction) && controller.canPlantTree(direction)) {
            		controller.plantTree(direction);
            		return;
            	}
            }else if(inDanger == false || trees.length >= 5) {
            	water();
            }
        }

	}
	
	public static void water() throws GameActionException{
		try {
			if(controller.canWater() ) {
				for(TreeInfo tree: trees) {
					if(controller.canWater(tree.getLocation()) && (controller.canShake(tree.getLocation()))) {
						controller.water(tree.getLocation());
						controller.shake(tree.getLocation());
						hasWatered = true;
						break;
					}else if(controller.canWater(tree.getLocation()) && (!controller.canShake(tree.getLocation()))) {
						controller.water(tree.getLocation());
						hasWatered = true;
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
	
	public static void initialize() {
		Direction direction = Direction.getNorth();
		for(int i=0;i<4;i++) {
			//float radians = (float)(-Math.PI+(2*Math.PI)*((float) i)/4);
			dirList[i] = direction.rotateRightDegrees((float)Math.PI/2);
			System.out.println("Created this move -> "+dirList[i]);
		}
	}
	
	public boolean isEdge(Direction direction) {
		for(MapLocation loc:mapEdges) {
			if(controller.getLocation().directionTo(loc).equals(direction,2*controller.getType().sensorRadius)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean checkDirection(Direction direction) throws GameActionException{
		if(controller.hasMoved()) {
			return false;
		}
		
		if(controller.canMove(direction,controller.getType().strideRadius)){
			walk(direction);
			return true;
		}
		
		int checks = 1;
		int stop = 6;
		
		while(checks <= stop) {
			if(controller.canMove(direction.rotateLeftRads((float)(checks*Math.PI/4)),controller.getType().strideRadius)){
				controller.move(direction.rotateLeftRads((float)(checks*Math.PI/4)),controller.getType().strideRadius);
				
				MapLocation visitedLoc = controller.getLocation();
				
				if(count == 0 || !hasVisited(visitedLoc)) {
					addVisited(visitedLoc);
					return true;
				}else {
					checkDirection(direction.rotateLeftDegrees((float)(checks*Math.PI/2)));
				}
				
				
				return true;
			}
			
			if(controller.canMove(direction.rotateRightRads((float)(checks*Math.PI/4)),controller.getType().strideRadius)){
				controller.move(direction.rotateRightRads((float)(checks*Math.PI/4)),controller.getType().strideRadius);
				
				MapLocation visitedLoc = controller.getLocation();
				
				if(count == 0 || hasVisited(visitedLoc) != true) {
					addVisited(visitedLoc);
					return true;
				}else {
					checkDirection(direction.rotateRightDegrees((float)(checks*Math.PI/2)));
				}
				
				return true;
			}
			checks++;
		}
		return false;
	}
	
	public boolean hasVisited(MapLocation location) throws NullPointerException{
		try {
			for(MapLocation loc:visited) {
				if(loc != null && loc.equals(location)){
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
		if(count == 20) {
			clearVisited();
			visited[count++] = loc;
		}else {
			visited[count++] = loc;
		}
	}
	
	public static void clearVisited() {
		visited = new MapLocation[20];
		count = 0;
	}
	
	public static void walk() {
		try {
			Direction direction = randomDirection();
			if(controller.canMove(direction,controller.getType().strideRadius)) {
				controller.move(direction,controller.getType().strideRadius);
				addVisited(controller.getLocation());
			}
		}catch(Exception e) {
			System.out.println("Saved your life");
			e.printStackTrace();
		}
	}
	
	public static void walk(Direction direction) {
		try {
			controller.move(direction,controller.getType().strideRadius);
			
		}catch(Exception e) {
			System.out.println("Saved your life");
			e.printStackTrace();
		}
	}

	public static Direction randomDirection() {
		return dirList[rand.nextInt(4)];
	}
	
	public static void evade() throws GameActionException {
		try {
			if(bullets.length >= 1) {
				for(BulletInfo bullet:bullets) {
					if(collisionCheck(bullet)) {
						MapLocation location = bullet.getLocation();
						MapLocation myLocation = controller.getLocation();

						Direction meToYou = new Direction(location,myLocation);
						Direction move = meToYou.opposite();

						controller.move(move);
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
	
	public static void build(RobotType type,int cost,Direction step)throws GameActionException {
		if((controller.getTeamBullets() > cost) && (controller.canBuildRobot(type,step))) {
			controller.buildRobot(type,step);
		}
	}
	
	public MapLocation findRallyAlly()throws NullPointerException {
		//MapLocation rallyPoint;
		try {
			if(allyCount >= 1) {
				MapLocation loc = allies[allyCount];
				if(loc.isWithinDistance(controller.getLocation(),controller.getType().strideRadius)) {
					return loc.add((float)(-allyCount*Math.PI/3),controller.getType().strideRadius);
				}else {
					return controller.getLocation().add((float)Math.PI/3,controller.getType().strideRadius);
				}
			}else if(allyCount == 0){
				return controller.getLocation().add((float)-Math.PI/3,controller.getType().strideRadius);
			}else {
				return controller.getLocation().add((float)-Math.PI/3,controller.getType().strideRadius);
			}
		}catch(NullPointerException e) {
			System.out.println("saved null");
			float x = controller.getLocation().x + controller.getType().strideRadius;
			float y = controller.getLocation().y + controller.getType().strideRadius;
			MapLocation nullPoint = new MapLocation(x,y);
			
			return nullPoint;
		}
		//MapLocation allElse = this.controller.getLocation().add((float)-Math.PI/3,controller.getType().strideRadius);
		//return allElse;
	}
	
	public MapLocation findSwarmDirection()throws NullPointerException {
		//MapLocation rallyPoint;
		try {
			if(enemyCount >= 1) {
				MapLocation loc = generalEnemies[rand.nextInt(enemyCount)];
				if(loc.isWithinDistance(controller.getLocation(),controller.getType().strideRadius)) {
					return loc.subtract((float)Math.PI/3,controller.getType().strideRadius);
				}else {
					return controller.getLocation().add((float)Math.PI/2,controller.getType().strideRadius);
				}
			}else if(allyCount == 0){
				return controller.getLocation().add((float)Math.PI/2,2*controller.getType().strideRadius);
			}else {
				return controller.getLocation().add((float)-Math.PI/2,3*controller.getType().strideRadius);
			}
		}catch(NullPointerException e) {
			System.out.println("saved null");
			float x = controller.getLocation().x + controller.getType().strideRadius;
			float y = controller.getLocation().y + controller.getType().strideRadius;
			MapLocation nullPoint = new MapLocation(x,y);
			
			return nullPoint;
		}
		//MapLocation allElse = this.controller.getLocation().add((float)-Math.PI/3,controller.getType().strideRadius);
		//return allElse;
	}
}
