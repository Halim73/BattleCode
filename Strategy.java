package utilityBot;
import battlecode.common.*;

public class Strategy extends Behavior {
	//MapLocation rallyPoint;
	
	public Strategy() throws GameActionException{
		//super(myRC);
		//super.makeChoice();		
	}
	
	public void clusterDefend()throws GameActionException {
		System.out.println("Running Cluster Defend");
		
		MapLocation rallyPoint = visited[0];
		if(visited[0] != null) {
			rallyPoint = visited[0];
		}else {
			rallyPoint  = new MapLocation(controller.getLocation().y,controller.getLocation().x);
		}
		 
		Direction rallyDir = new Direction(controller.getLocation(),rallyPoint);
		
		super.updateInfo();
		
		switch(controller.getType()) {
		case ARCHON:
			//evade();
			checkDirection(rallyDir);
			
			if(controller.canHireGardener(rallyDir.opposite())) {
				controller.hireGardener(rallyDir.opposite());
			}
			
			Clock.yield();
		case GARDENER:
			evade();
			
			rallyPoint = findRallyAlly();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			if(hasWatered == false) {
				water();
			}
			
			if(numSoldiers < numJacks) {
				produceUnit(RobotType.SOLDIER,rallyDir);
			}else {
				produceUnit(RobotType.LUMBERJACK,rallyDir);
			}
			
			if(controller.isLocationOccupied(rallyPoint)) {
				checkDirection(rallyDir.rotateLeftDegrees((float)Math.PI/6));
			}else {
				plant();
				checkDirection(rallyDir);
			}
			
			if(hasWatered == false) {
				water();
			}
			
		case SOLDIER:
			evade();
			
			rallyPoint = findSwarmDirection();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			if(controller.getHealth() <= 50) {
				walk(rallyDir);
				break;
			}
			if(controller.canFireSingleShot()) {
				controller.fireSingleShot(rallyDir);
			}else {
				soldierAttack(inDanger,rallyDir);
			}
			//soldierAttack(inDanger,rallyDir);
			checkDirection(rallyDir);
			
		case LUMBERJACK:
			evade();
			
			rallyPoint = findSwarmDirection();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			if(controller.getHealth() <= 50) {
				walk(rallyDir);
				break;
			}
			for(TreeInfo tree:trees) {
				if(controller.canChop(tree.getLocation())) {
					controller.chop(tree.getLocation());
				}else {
					controller.shake(tree.getLocation());
				}
			}
			
			//lumberJackAttack(inDanger,rallyDir);
			checkDirection(rallyDir);
		case SCOUT:
			evade();
			
			rallyPoint = findSwarmDirection();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			soldierAttack(inDanger, rallyDir);
			checkDirection(rallyDir);
		case TANK:
			break;
		default:
			break;
		}
	}
    
	public void clusterCollect() throws GameActionException {
		System.out.println("Running Cluster Collect");
		super.updateInfo();
		
		switch(controller.getType()) {
		case ARCHON:
			//evade();
			
			MapLocation rallyPoint = controller.getLocation().add((float)Math.PI/2,controller.getType().strideRadius);
			Direction rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			if(controller.canHireGardener(rallyDir)) {
				controller.hireGardener(rallyDir);
			}
			
			checkDirection(rallyDir);
			Clock.yield();
		case GARDENER:
			//evade();
			
			rallyPoint = findRallyAlly();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			if(numSoldiers < numJacks) {
				produceUnit(RobotType.SOLDIER,rallyDir);
			}else {
				produceUnit(RobotType.LUMBERJACK,rallyDir);
			}
			
			if(controller.isLocationOccupied(rallyPoint)) {
				checkDirection(rallyDir.rotateLeftDegrees((float)Math.PI/6));
			}else {
				plant();
				checkDirection(rallyDir);
			}
			
			if(hasWatered == false) {
				water();
			}
			
			//build(RobotType.LUMBERJACK,100,rallyDir);
		case SOLDIER:
			//evade();
			
			rallyPoint = findRallyAlly();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			soldierAttack(inDanger,rallyDir.opposite());
			
			if(controller.getHealth() <= 50) {
				checkDirection(rallyDir);
				break;
			}
			
			if(controller.canFireSingleShot()) {
				controller.fireSingleShot(rallyDir.opposite());
			}
			
			if(controller.isLocationOccupied(rallyPoint)) {
				checkDirection(rallyDir.rotateLeftDegrees((float)Math.PI/6));
			}else {
				checkDirection(rallyDir);
			}
			
			
		case LUMBERJACK:
			//evade();
			
			rallyPoint = findRallyAlly();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			if(controller.getHealth() <= 50) {
				checkDirection(rallyDir);
				break;
			}
			
			for(TreeInfo tree:neutralTrees) {
				if(tree.getLocation().isWithinDistance(controller.getLocation(), controller.getType().sensorRadius)) {
					if(controller.canChop(tree.getLocation())) {
						controller.chop(tree.getLocation());
						break;
					}else if(controller.canShake(tree.getLocation())) {
						controller.shake(tree.getLocation());
						break;
					}else {
						checkDirection(rallyDir);
					}
				}
			}
			
			for(TreeInfo tree:trees) {
				if(tree.getLocation().isWithinDistance(controller.getLocation(), 5*controller.getType().sensorRadius)) {
					if(controller.canChop(tree.getLocation())) {
						controller.chop(tree.getLocation());
						break;
					}if(controller.canShake(tree.getLocation())) {
						controller.shake(tree.getLocation());
						break;
					}else {
						checkDirection(rallyDir);
					}
				}
			}
			
			/*for(TreeInfo tree:opTrees) {
				if(tree.getLocation().isWithinDistance(controller.getLocation(), controller.getType().sensorRadius)) {
					if(controller.canChop(tree.getLocation())) {
						controller.chop(tree.getLocation());
						break;
					}else if(controller.canShake(tree.getLocation())) {
						controller.shake(tree.getLocation());
						break;
					}else {
						walk(rallyDir);
					}
				}
			}*/
			
			if(!controller.hasMoved()) {
				checkDirection(rallyDir);
			}
			
			//walk(rallyDir);
			break;
		case SCOUT:
			evade();
			
			rallyPoint = findRallyAlly();
			rallyDir = new Direction(controller.getLocation(),rallyPoint);
			
			soldierAttack(inDanger, rallyDir.opposite());
			walk(rallyDir);
			break;
		case TANK:
			break;
		default:
			break;
		}
	}
	
	public void swarmDefend() throws GameActionException{
		System.out.println("Running swarm Defend");
		Direction swarmDir = dirList[rand.nextInt(4)];
		MapLocation bestMove;
		
		float prev = 0;
		
		switch(controller.getType()) {
		case ARCHON:
			evade();
			
			for(MapLocation danger:dangers) {
				float distance = danger.distanceSquaredTo(controller.getLocation());
				if((distance-prev) < 0) {
					bestMove = danger.add((float)Math.PI/2,controller.getType().strideRadius);
					swarmDir = new Direction(controller.getLocation(),bestMove);
				}
			}
			
			walk(swarmDir);
			break;
		case GARDENER:
			evade();
			
			for(MapLocation loc:allies) {
				if(controller.senseRobotAtLocation(loc).getType() == RobotType.ARCHON) {
					swarmDir = new Direction(controller.getLocation(),loc);
					
					walk(swarmDir);
					super.produceUnit(RobotType.SOLDIER,swarmDir.opposite());
				}else if(controller.senseRobotAtLocation(loc).getType() == controller.getType()) {
					swarmDir = new Direction(controller.getLocation(),loc);
					
					walk(swarmDir);
					super.produceUnit(RobotType.SOLDIER,swarmDir.opposite());
				}else {
					break;
				}
				plant();
				water();
			}
			break;
		case LUMBERJACK:
			break;
		case SCOUT:
			break;
		case SOLDIER:
			break;
		case TANK:
			break;
		default:
			break;
		
		}
	}
}
