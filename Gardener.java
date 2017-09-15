package Bots;
import battlecode.common.*;
import static halimBot.RobotPlayer.controller;
import Mechanics.*;

public class Gardener {	
	static int jackCount = 0;
	static int soldierCount = 0;
	static boolean isStart = true;
	static boolean isLeader = false;
	public static void runCluster() {		
		Behavior.initialize();
		while(true) {
			try {
				if(isStart) {	
					if(controller.readBroadcastInt(Ports.TEAM_GARDENER_COUNT)%2 != 0) {
						isLeader = true;
					}
					
					isStart = false;
				}else {
					if(controller.getHealth() < controller.getType().getStartingHealth()/4) {
						controller.broadcastInt(Ports.TEAM_GARDENER_COUNT, controller.readBroadcast(Ports.TEAM_GARDENER_COUNT)-1);
					}
				}
				
				if(controller.getRoundNum() > 1000 && controller.getTeamBullets() >= 3*controller.getVictoryPointCost()) {
					controller.donate(controller.getVictoryPointCost());
				}
				
				jackCount = controller.readBroadcastInt(Ports.TEAM_LUMBERJACK_COUNT);
				soldierCount = controller.readBroadcastInt(Ports.TEAM_SOLDIER_COUNT);
				
				Network.updateNetwork();
				Behavior.evade();
				
				MapLocation rallyPoint = controller.getLocation().translate(controller.getType().strideRadius, controller.getType().strideRadius);
				Direction rallyDirection = controller.getLocation().directionTo(rallyPoint);
				
				if(isLeader) {
					controller.broadcastInt(Ports.TEAM_GARDENER_COUNT, controller.readBroadcast(Ports.TEAM_GARDENER_COUNT)+1);
					
					Network.broadcast(Ports.TEAM_GARDENER_PORT_START, Ports.TEAM_GARDENER_PORT_END, controller.senseRobot(controller.getID()));
					
					rallyPoint.add((float)Math.PI/4,controller.getType().sensorRadius);
					rallyDirection = controller.getLocation().directionTo(rallyPoint);
										
					/*if(!controller.isLocationOccupied(controller.getLocation().add(rallyDirection.opposite(), GameConstants.GENERAL_SPAWN_OFFSET*2))){
						Behavior.plant(rallyDirection.opposite());
					}*/
					
					if((jackCount < soldierCount )) {
						Behavior.produceUnit(RobotType.SOLDIER,rallyDirection);
					}
					
					if(!controller.isCircleOccupiedExceptByThisRobot(controller.getLocation(), GameConstants.GENERAL_SPAWN_OFFSET*3)){
						Behavior.plant();
					}else {
						Behavior.checkDirection(rallyDirection);
					}
					
					if(controller.getRoundNum()%10 == 0 && Behavior.rand.nextInt(5) == 3) {
						Behavior.produceUnit(RobotType.SCOUT, rallyDirection);
					}
				}else {
					int round = controller.getRoundNum();
					
					if(round%20 == 0) {
						rallyPoint = Network.findBroadcast(RobotType.GARDENER).add((float)Math.PI, controller.getType().sensorRadius/2);
					}else {
						rallyPoint = controller.getLocation().translate(controller.getType().strideRadius, controller.getType().strideRadius);
					}
					
					if(rallyPoint.compareTo(controller.getLocation()) == 0) {
						rallyPoint = Network.findBroadcast(RobotType.ARCHON).add((float)Math.PI, controller.getType().sensorRadius/2);
					}
					
					rallyDirection = controller.getLocation().directionTo(rallyPoint);
					
					if((jackCount < soldierCount )) {
						Behavior.produceUnit(RobotType.LUMBERJACK,rallyDirection);
					}else {
						Behavior.produceUnit(RobotType.SOLDIER,rallyDirection);
					}
					
					if(!controller.isCircleOccupiedExceptByThisRobot(controller.getLocation(), GameConstants.GENERAL_SPAWN_OFFSET*3)){
						Behavior.plant();
					}else {
						Behavior.checkDirection(rallyDirection);
					}
				}
				
				
				if(controller.canWater()) {
					Behavior.water();
				}
				Clock.yield();
			}catch(Exception e) {
				System.out.println("Saved Gardener cluster");
				e.printStackTrace();
			}
		}
	}
	
	public static boolean canPlant() throws GameActionException{
		Network.updateNetwork();
		
		if(!(Network.neutralTrees.length == 0) && !(Network.myTrees.length == 0)) {
			Network.neutralTrees = controller.senseNearbyTrees(controller.getLocation(), GameConstants.GENERAL_SPAWN_OFFSET*2, Team.NEUTRAL);
			Network.myTrees = controller.senseNearbyTrees(controller.getLocation(),  GameConstants.GENERAL_SPAWN_OFFSET*2, controller.getTeam());

			if(Network.neutralTrees.length > 0 && Network.myTrees.length > 0) {
				return false;
			}
		}else {
			return true;
		}
		return true;
	}
}
