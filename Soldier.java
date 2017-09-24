package Bots;
import battlecode.common.*;
import static halimBot.RobotPlayer.controller;
import Mechanics.*;

public strictfp class Soldier {
	static boolean isStart = true;
	static boolean isLeader = false;
	static boolean isStuck = false;
	static int timeInSensorArea = 0;
	static MapLocation loc = null;
	static int next = 0;
	
	static RobotInfo leader = null;
	
	static MapLocation[] leaders = new MapLocation[100];
	static MapLocation[] nearbyFlock = new MapLocation[50];
	
	static MapLocation goal = null;
	static MapLocation spawnLoc = null;
	
	static final float SEPERATION = controller.getType().bodyRadius*2.5f;
	static final float ALIGNMENT = (float)Math.toRadians(30);
	
	static float cohesion = 0;
	static float centerOfMassX = 0;
	static float centerOfMassY = 0;
	
	static MapLocation current = null;
	
	static int startRound = 0;
	static int start = 0;
	
	public static void runCluster() {
		Behavior.initialize();
		
		while(true) {
			try {
				prelim();
				leaderCheck();
				checkStuck();
				
				Behavior.evade();
				
				start = controller.getAttackCount();
				int cap = 3;
				
				if(start < cap
						&& controller.getTeamBullets() > GameConstants.PENTAD_SHOT_COST
						&& isStuck == false) {
					
					if(isLeader == true) {
						//goal = avoidObstacles(goal.x,goal.y);
						leaderMove(goal);
					}else {
						followerMove();
					}
					
					fight();
				}else {
					if(isLeader == true 
							&& Clock.getBytecodesLeft() >= 10000
							|| isStuck == true) {
						goal = findGoal(goal);
					}else {
						newLeader();
					}
					Clock.yield();
				}
				
			}catch(Exception e) {
				System.out.println("Saved Soldier Cluster");
				e.printStackTrace();
			}
		}
	}
	
	public static void prelim()throws GameActionException {
		if(isStart) {
			controller.broadcast(Ports.TEAM_SOLDIER_COUNT, controller.readBroadcast(Ports.TEAM_SOLDIER_COUNT)+1);
			
			isStart = false;
			Network.updateNetwork();
			current = controller.getLocation();
			startRound = controller.getRoundNum();
			goal = Behavior.findSwarmDirection();
			spawnLoc = controller.getLocation();
			
			controller.broadcastFloat(Ports.LAST_KNOWN_GOAL_X, goal.x);
			controller.broadcastFloat(Ports.LAST_KNOWN_GOAL_Y, goal.y);
		}else {
			Network.updateNetwork();
			
			
			if(Network.enemyBots.length > 0) {
				goal = Network.enemyBots[0].getLocation().subtract(controller.getLocation().directionTo(Network.enemyBots[0].getLocation()).opposite(), controller.getType().strideRadius/2);
			}
			
			if(controller.getHealth() < RobotType.SOLDIER.getStartingHealth()/4) {
				controller.broadcast(Ports.TEAM_SOLDIER_COUNT, controller.readBroadcast(Ports.TEAM_SOLDIER_COUNT)-1);
				isLeader = false;
			}
			if(isStuck == false) {
				spawnLoc = controller.getLocation();
			}
		}	
	}
	
	public static MapLocation findGoal(MapLocation goal)throws GameActionException{
		boolean shouldMove = MapLocation.doCirclesCollide(controller.getLocation(), RobotType.TANK.sensorRadius, goal, RobotType.TANK.sensorRadius/2);
		
		if(shouldMove == true) {
			if(Network.numArchons > 1) {
				goal = Network.initialEnemyArchonLoc[Network.numArchons-1];
			}else if(Network.numArchons <= 1){
				//goal = controller.senseNearbyRobots(controller.getType().sensorRadius,controller.getTeam().opponent())[0].getLocation();
				goal = goal.add(controller.getLocation().directionTo(Behavior.findSwarmDirection()),controller.getType().strideRadius);
			}else {
				//goal = goal.add(controller.getLocation().directionTo(Behavior.findSwarmDirection()),controller.getType().strideRadius);
				goal = controller.senseNearbyRobots(controller.getType().sensorRadius)[0].getLocation();
			}
			return goal;
		}else if(isStuck == true) {
			goal = spawnLoc;
		}else {
			goal = Network.initialEnemyArchonLoc[0];
			//goal = goal.add(controller.getLocation().directionTo(goal).rotateRightDegrees(30),controller.getType().strideRadius);
		}
		return goal;
	}
	public static void leaderMove(MapLocation goal)throws GameActionException {
		for(int i=Ports.TEAM_SOLDIER_PORT_START;i<Ports.TEAM_SOLDIER_PORT_END;i++) {
			if(controller.readBroadcast(i) == 0) {
				controller.broadcastInt(i,controller.getID());
				break;
			}
		}
		
		Direction rallyDirection = controller.getLocation().directionTo(goal);
		MapLocation choice = controller.getLocation().add(rallyDirection);
		
		if(controller.isLocationOccupied(choice)) {
			rallyDirection = controller.getLocation().directionTo(goal.add((float)Math.PI/2,controller.getType().strideRadius));
		}
		
		Behavior.checkDirection(rallyDirection);
	}
	
	public static void followerMove()throws GameActionException{
		MapLocation leaderTrail = controller.getLocation();
		
		if(leader != null) {
			leaderTrail = leader.getLocation().subtract(ALIGNMENT, SEPERATION);
		}else {
			newLeader();
			leaderTrail = leader.getLocation().subtract(ALIGNMENT, SEPERATION);
		}
	
		float x = leaderTrail.x;
		float y = leaderTrail.y;
		
		leaderTrail = balanceTrail(x,y);
		
		Direction rallyDirection = controller.getLocation().directionTo(leaderTrail);
		MapLocation choice = controller.getLocation().add(rallyDirection,controller.getType().strideRadius);
		
		if(controller.isLocationOccupied(choice)) {
			rallyDirection = controller.getLocation().directionTo(leaderTrail.add((float)Math.PI/4,controller.getType().strideRadius));
		}
		
		Behavior.checkDirection(rallyDirection);
	}
	
	public static void fight()throws GameActionException{
		RobotInfo enemy = findClosestEnemy();
		if(enemy != null) {
			moveToEnemy(enemy);
			Behavior.soldierShoot(enemy);
		}
	}
	public static MapLocation avoidObstacles(float x, float y) {
		int size = Network.neutralTrees.length + Network.myTrees.length + Network.enemyBots.length + Network.myRobots.length;
		int count = 0;
		
		MapLocation[]obstacles = new MapLocation[size];
		MapLocation bestPath = new MapLocation(x,y);
		
		obstacles = fillObstacles(obstacles,count);
		
		for(MapLocation obs:obstacles) {
			if(obs != null && controller.getLocation().distanceTo(loc) < SEPERATION) {
				x = x-(obs.x-controller.getLocation().x);
				y = y-(obs.x-controller.getLocation().x);
			}
		}
		bestPath = new MapLocation(x,y);
		
		return bestPath;
	}
	
	public static MapLocation[] fillObstacles(MapLocation[] obstacles,int count) {
		for(TreeInfo tree:Network.neutralTrees) {
			obstacles[count++] = tree.getLocation();
		}
		
		for(TreeInfo tree:Network.myTrees) {
			obstacles[count++] = tree.getLocation();
		}	
		
		for(RobotInfo robot:Network.enemyBots) {
			obstacles[count++] = robot.getLocation();
		}
		
		for(RobotInfo robot:Network.myRobots) {
			obstacles[count++] = robot.getLocation();
		}
		return obstacles;
	}
	public static MapLocation balanceTrail(float x,float y) {
		MapLocation leaderTrail = new MapLocation(x,y);
		RobotInfo[] robots = controller.senseNearbyRobots(controller.getType().sensorRadius, controller.getTeam());
		int i = 0;
		
		nearbyFlock = new MapLocation[robots.length];
		for(RobotInfo robot:robots){
			nearbyFlock[i++] = robot.getLocation();
		}
		
		if(nearbyFlock.length > 0) {
			for(MapLocation loc:nearbyFlock) {
				if(loc != null && controller.getLocation().distanceTo(loc) < SEPERATION) {
					x = x-(loc.x-controller.getLocation().x);
					y = y-(loc.x-controller.getLocation().x);
				}
			}
		}
		leaderTrail = new MapLocation(x,y);
		return leaderTrail;
	}
	public static void checkStuck() {
		float sameArea = controller.getType().strideRadius/2;
		float currentDistance = controller.getLocation().distanceTo(current);
		
		if(currentDistance < sameArea) {
			if(controller.getRoundNum() - startRound >= 5) {
				timeInSensorArea++;
				startRound = controller.getRoundNum();
				
				if(timeInSensorArea >= 5) {
					isLeader = false;
					isStuck = true;
					timeInSensorArea = 0;
					current = controller.getLocation();
				}else if(timeInSensorArea >= 2 && timeInSensorArea < 5) {
					isStuck = true;
					current = controller.getLocation();
				}else {
					isStuck = false;
				}
			}
		}
	}
	
	public static RobotInfo findClosestEnemy() {
		Network.enemyBots = controller.senseNearbyRobots(controller.getType().sensorRadius,controller.getTeam().opponent());
		
		if(Network.enemyBots.length > 0) {
			RobotInfo bot = Network.enemyBots[0];
			float closestDistance = 9999;
			float distance = 0;
			
			for(RobotInfo robot:Network.enemyBots) {
				MapLocation loc = robot.getLocation();
				distance = controller.getLocation().distanceTo(loc);
				
				if(distance < closestDistance) {
					closestDistance = distance;
					bot = robot;
				}
			}
			return bot;
		}
		return null;
	}
	
	public static void moveToEnemy(RobotInfo robot) throws GameActionException{
		if(!controller.hasMoved()) {
			if(Network.enemyBots.length > 0) {
				MapLocation loc = robot.getLocation();
				Direction dir = controller.getLocation().directionTo(loc);
				
				switch(robot.getType()) {
				case ARCHON:
					if(controller.getLocation().isWithinDistance(robot.getLocation(),controller.getType().sensorRadius)) {
						Behavior.checkDirection(dir.opposite().rotateRightDegrees(30));
					}
					break;
				case GARDENER:
					break;
				case LUMBERJACK:
					if(controller.getLocation().isWithinDistance(robot.getLocation(),controller.getType().sensorRadius)) {
						Behavior.checkDirection(dir.opposite().rotateRightDegrees(45));
					}
					break;
				case SCOUT:
					break;
				case SOLDIER:
					if(controller.getLocation().isWithinDistance(robot.getLocation(),controller.getType().sensorRadius)) {
						Behavior.checkDirection(dir.opposite().rotateRightDegrees(60));
					}
					break;
				case TANK:
					if(controller.getLocation().isWithinDistance(robot.getLocation(),controller.getType().sensorRadius)) {
						Behavior.checkDirection(dir.opposite().rotateRightDegrees(60));
					}
					break;
				default:
					break;
				
				}
			}
		}
	}
	public static void leaderCheck()throws GameActionException {
		int id = findTheLeader();
		if(id == controller.getID()) {
			isLeader = true;
			leader = controller.senseRobot(id);
		}else {
			leader = controller.senseRobot(id);
		}
	}
	public static void newLeader()throws GameActionException {
		RobotInfo[] leaders = new RobotInfo[50];
		int count = 0;
		for(int i=Ports.TEAM_SOLDIER_PORT_START;i<Ports.TEAM_SOLDIER_PORT_END;i++) {
			if(controller.readBroadcast(i) != 0) {
				int id = controller.readBroadcastInt(i);
				if(controller.canSenseRobot(id)) {
					leaders[count++] = controller.senseRobot(id);
				}
			}
		}
		float distance = 0;
		float closestDistance = 9999;
		
		for(int i=0;i<count;i++) {
			distance = controller.getLocation().distanceTo(leaders[i].getLocation());
			if(distance < closestDistance) {
				closestDistance = distance;
				if(leader != leaders[i]) {
					leader = leaders[i];
				}
			}
		}
	}
	public static int findTheLeader()throws GameActionException {
		RobotInfo[] robots = controller.senseNearbyRobots(controller.getType().sensorRadius/3,controller.getTeam());
		int lowestId = controller.getID();
		
		for(RobotInfo robot:robots) {
			if(robot.getType() == controller.getType()
					&& robot.getID() < lowestId) {
				lowestId = robot.getID();
			}
		}
		return lowestId;
	}
}
