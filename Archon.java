package Bots;
import battlecode.common.*;
import static halimBot.RobotPlayer.controller;
import Mechanics.*;

public class Archon {	
	static int gardenerCount = 0;
	static boolean headArchon = false;
	
	public static void runCluster() {
		Behavior.initialize();
		
		while(true) {
			try {
				if(controller.getRoundNum() < 2) {
					MapLocation[]init = controller.getInitialArchonLocations(controller.getTeam());
					
					if(init.length == 2) {
						if(controller.senseRobotAtLocation(init[1]).getID() == controller.getID()) {
							headArchon = true;
						}
					}else if(init.length > 2) {
						if(controller.senseRobotAtLocation(init[2]).getID() == controller.getID()) {
							headArchon = true;
						}
					}else {
						headArchon = true;
					}
				}
				Network.updateNetwork();
				
				Direction step = Behavior.randomDirection();
				
				if(controller.readBroadcast(Ports.TEAM_GARDENER_COUNT) < 5) {
					if(controller.canHireGardener(step)) {
						controller.hireGardener(step);
					}
				}
				
				/*if(controller.getRoundNum()%50 == 0) {
					controller.broadcastInt(Ports.TEAM_GARDENER_COUNT, 0);
				}*/
				
				if(Behavior.checkDirection(step) && headArchon) {
					controller.broadcastFloat(Ports.TEAM_ARCHON_PORT_START, controller.getLocation().x);
					controller.broadcastFloat(Ports.TEAM_ARCHON_PORT_START+1, controller.getLocation().y);
				}else {
					Behavior.checkDirection(step);
				}
				Clock.yield();
			}catch(Exception e) {
				System.out.println("Saved Archon clustering");
				e.printStackTrace();
			}
		}
	}
}
