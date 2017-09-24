package Bots;
import battlecode.common.*;
import static halimBot.RobotPlayer.controller;
import Mechanics.*;

public class LumberJack {
	static boolean isStart = true;
	
	public static void runCluster() {
		Behavior.initialize();
		while(true) {
			try {
				if(isStart) {
					controller.broadcastInt(Ports.TEAM_LUMBERJACK_COUNT, controller.readBroadcast(Ports.TEAM_LUMBERJACK_COUNT)+1);
					isStart = false;
				}else {
					if(controller.getHealth() < controller.getType().getStartingHealth()/4) {
						controller.broadcastInt(Ports.TEAM_LUMBERJACK_COUNT, controller.readBroadcast(Ports.TEAM_LUMBERJACK_COUNT)-1);
					}
				}
				
				if(controller.getRoundNum() > 1000 && controller.getTeamBullets() >= 2*controller.getVictoryPointCost()) {
					controller.donate(controller.getVictoryPointCost());
				}
				
				Network.updateNetwork();
				Behavior.evade();
				
				if(!controller.hasAttacked()) {
					for(RobotInfo robot:Network.enemyBots) {
						if(robot.getLocation().isWithinDistance(controller.getLocation(),GameConstants.LUMBERJACK_STRIKE_RADIUS)){
							//Behavior.checkDirection(controller.getLocation().directionTo(robot.getLocation()));
							if(controller.canStrike() && !controller.hasAttacked()) {
								controller.strike();
							}
						}
					}
				}
				
				MapLocation rallyPoint = Behavior.findSwarmDirection();
				
				/*if(controller.getRoundNum()%10 == 0) {
					if(controller.readBroadcastBoolean(Ports.TEAM_IS_STUCK_START) == true) {
						float x = controller.readBroadcastFloat(Ports.TEAM_IS_STUCK_START+1);
						float y = controller.readBroadcastFloat(Ports.TEAM_IS_STUCK_START+1);

						rallyPoint = new MapLocation(x,y);
					}else {
						rallyPoint = Behavior.findSwarmDirection();
					}
					
				}*/
				
				Direction rallyDir = new Direction(controller.getLocation(),rallyPoint);
		
				Behavior.handleTrees();
				
				if(!controller.hasMoved() && !controller.hasAttacked()) {
					Behavior.checkDirection(rallyDir);
				}
				
				Network.broadcast(Ports.TEAM_LUMBERJACK_PORT_START, Ports.TEAM_LUMBERJACK_PORT_END, controller.senseRobotAtLocation(controller.getLocation()));				

				Clock.yield();
			}catch(Exception e) {
				System.out.println("Saved Lumberjack cluster");
				e.printStackTrace();
			}
		}
	}
}
