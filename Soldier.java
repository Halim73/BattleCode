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

	static final float SEPERATION = RobotType.SOLDIER.bodyRadius*2f;
	static final float ALIGNMENT = (float)Math.toRadians(60);
	
	static float cohesion = 0;
	static float centerOfMassX = 0;
	static float centerOfMassY = 0;
	
	static MapLocation current = null;
	static int startRound = 0;
	
	public static void runCluster() {
		Behavior.initialize();
		
		while(true) {
			try {
				prelim();
				leaderCheck();
				
				Behavior.evade();
				
				int counter = controller.getAttackCount();
				int cap = 5;
				
				if(counter < cap
						&& controller.getTeamBullets() > GameConstants.PENTAD_SHOT_COST) {
					fight();
					
					if(isLeader == true) {
						leaderMove(goal);
					}else {
						followerMove();
					}
				}else {
					goal = findGoal(goal);
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
			controller.broadcast(Ports.TEAM_SOLDIER_COUNT, Ports.TEAM_SOLDIER_COUNT+1);
			isStart = false;
			Network.updateNetwork();
			current = controller.getLocation();
			startRound = controller.getRoundNum();
			goal = Behavior.findSwarmDirection();
		}else {
			if(controller.getHealth() < RobotType.SOLDIER.getStartingHealth()/4) {
				controller.broadcast(Ports.TEAM_SOLDIER_COUNT, Ports.TEAM_SOLDIER_COUNT-1);
				isLeader = false;
				Network.updateNetwork();
			}
		}	
	}
	
	public static MapLocation findGoal(MapLocation goal)throws GameActionException{
		float distance = 0;
		float closestDistance = controller.getLocation().distanceTo(goal);
		
		if(controller.getLocation().isWithinDistance(goal, controller.getType().sensorRadius)) {
			if(Behavior.swarmLocs.length > 0) {
				for(MapLocation loc:Behavior.swarmLocs) {
					if(!goal.equals(loc)) {
						distance = controller.getLocation().distanceTo(loc);
						
						if(distance < closestDistance) {
							goal = loc;
							distance = closestDistance;
						}
					}
				}
			}
		}else {
			goal = goal.add(Behavior.randomDirection());
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
		
		return leaderTrail;
	}
	public static void checkStuck() {
		if(controller.getLocation().isWithinDistance(current, controller.getType().sensorRadius/2)) {
			if(controller.getRoundNum() - startRound == 10) {
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
						Behavior.checkDirection(dir.rotateRightDegrees(30));
					}
					break;
				case GARDENER:
					break;
				case LUMBERJACK:
					if(controller.getLocation().isWithinDistance(robot.getLocation(),controller.getType().sensorRadius)) {
						Behavior.checkDirection(dir.rotateRightDegrees(45));
					}
					break;
				case SCOUT:
					break;
				case SOLDIER:
					if(controller.getLocation().isWithinDistance(robot.getLocation(),controller.getType().sensorRadius)) {
						Behavior.checkDirection(dir.rotateRightDegrees(45));
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
