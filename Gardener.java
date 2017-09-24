package Bots;
import battlecode.common.*;
import static halimBot.RobotPlayer.controller;
import Mechanics.*;

public class Gardener {	
	static int jackCount = 0;
	static int soldierCount = 0;
	
	static boolean isStart = true;
	static boolean isLeader = false;
	static boolean shouldBuild = false;
	
	static Direction start = null;
	static RobotInfo leader = null;
	static int starter = 0;
	
	public static void runCluster() {		
		Behavior.initialize();
		while(true) {
			try {
				prelim();
				
				Behavior.evade();
				
				//boolean shouldBuild = controller.readBroadcastBoolean(Ports.SHOULD_BUILD);
				
				if(isLeader == true && shouldBuild == true) {										
					bestBuild();
					//Behavior.checkDirection(start.opposite());
				}else {	
					if(!controller.isCircleOccupiedExceptByThisRobot(controller.getLocation(), GameConstants.GENERAL_SPAWN_OFFSET*3)
							&& Network.neutralTrees.length < 3
							&& Network.myTrees.length < 4){
						Behavior.plant();
						if(controller.canWater()) {
							Behavior.water();
						}
						
					}else {
						Behavior.checkDirection(start);
						isLeader = true;
						shouldBuild = true;
					}	
				}
				
				starter = Clock.getBytecodeNum();
				if(starter > 10000) {
					Clock.yield();
				}
				
			}catch(Exception e) {
				System.out.println("Saved Gardener cluster");
				e.printStackTrace();
			}
		}
	}
	
	public static void prelim()throws GameActionException {
		if(isStart) {	
			leaderCheck();
			isStart = false;
			Network.updateNetwork();
			
			start = Behavior.randomDirection();
		
			Network.neutralTrees = controller.senseNearbyTrees(GameConstants.GENERAL_SPAWN_OFFSET*2, Team.NEUTRAL);
			
			Behavior.produceUnit(RobotType.SOLDIER, Behavior.randomDirection());
			
			controller.broadcastInt(Ports.TEAM_GARDENER_COUNT, controller.readBroadcast(Ports.TEAM_GARDENER_COUNT)+1);
		}else {
			Network.updateNetwork();
			
			if(controller.getHealth() < controller.getType().getStartingHealth()/4) {
				controller.broadcastInt(Ports.TEAM_GARDENER_COUNT, controller.readBroadcast(Ports.TEAM_GARDENER_COUNT)-1);
			}
			
			float max = controller.getVictoryPointCost()+RobotType.SOLDIER.bulletCost;
			if(controller.getRoundNum() > 1500 && controller.getTeamBullets() > max) {
				controller.donate(controller.getVictoryPointCost());
			}
			
			Network.neutralTrees = controller.senseNearbyTrees(GameConstants.GENERAL_SPAWN_OFFSET*2, Team.NEUTRAL);
			if(Network.myTrees.length > 2 || Network.neutralTrees.length < 5) {
				shouldBuild = true;
				isLeader = true;
			}else {
				shouldBuild = false;
				start.rotateLeftDegrees(60);
				isLeader = false;
			}
			leaderCheck();
			
			jackCount = controller.readBroadcastInt(Ports.TEAM_LUMBERJACK_COUNT);
			soldierCount = controller.readBroadcastInt(Ports.TEAM_SOLDIER_COUNT);
			
		}
	}
	
	public static void bestBuild()throws GameActionException{
		if((jackCount < soldierCount )) {
			Behavior.produceUnit(RobotType.LUMBERJACK,Behavior.randomDirection());
		}else {
			Behavior.produceUnit(RobotType.SOLDIER,Behavior.randomDirection());
		}
	}
	public static void leaderCheck()throws GameActionException {
		int id = findTheLeader();
		if(id == controller.getID()) {
			isLeader = true;
			leader = controller.senseRobot(id);
		}else if(id == 0){
			if(controller.getRoundNum()%2 == 0) {
				isLeader = true;
				leader = controller.senseRobot(controller.getID());
			}else {
				isLeader = false;
			}
		}else {
			leader = controller.senseRobot(id);
		}
	}
	
	public static int findTheLeader()throws GameActionException {
		RobotInfo[] robots = controller.senseNearbyRobots(controller.getType().sensorRadius/3,controller.getTeam());
		int lowestId = controller.getID();
		
		if(robots.length <= 1) {
			lowestId = 0;
			return lowestId;
		}
		
		for(RobotInfo robot:robots) {
			if(robot.getType() == controller.getType()
					&& robot.getID() < lowestId) {
				lowestId = robot.getID();
			}
		}
		
		
		return lowestId;
	}
}
