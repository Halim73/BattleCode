package Bots;
import battlecode.common.*;
import static halimBot.RobotPlayer.controller;
import Mechanics.*;

public class Scout {
	static MapLocation[] goals = controller.getInitialArchonLocations(controller.getTeam().opponent());

	public static void runCluster() {
		while(true) {
			try {
				int current = 0;
				MapLocation currentGoal = goals[current++];
				Behavior.evade();
				
				while(!controller.getLocation().isWithinDistance(currentGoal, controller.getType().strideRadius)) {
					Behavior.checkDirection(controller.getLocation().directionTo(currentGoal));
					Network.updateNetwork();
					
					if(controller.getLocation().isWithinDistance(currentGoal, controller.getType().sensorRadius/2)) {
						Network.updateNetwork();
						if(goals.length > 1) {
							currentGoal = goals[current++];
						}else {
							currentGoal = Behavior.mapEdges[current++];
						}
					}
					
					if(!controller.hasAttacked() && controller.canFireSingleShot() && Network.enemyBots.length > 1) {
						for(RobotInfo robot:Network.enemyBots) {
							controller.fireSingleShot(controller.getLocation().directionTo(robot.getLocation()));
						}
					}
					Clock.yield();
				}
			}catch(Exception e) {
				System.out.println("Saved Scout cluster");
				e.printStackTrace();
			}
		}
	}
}
