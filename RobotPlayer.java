package halimBot;

import Bots.*;
import battlecode.common.*;

public strictfp class RobotPlayer {
	public static RobotController controller;
	
	public static void run(RobotController myRC) {
		RobotPlayer.controller = myRC;
		
		switch(controller.getType()) {
		case ARCHON:
			Archon.runCluster();
			break;
		case GARDENER:
			Gardener.runCluster();
			break;
		case LUMBERJACK:
			LumberJack.runCluster();
			break;
		case SCOUT:
			Scout.runCluster();
			break;
		case SOLDIER:
			Soldier.runCluster();
			break;
		case TANK:
			Soldier.runCluster();
			break;
		default:
			break;
		}
	}
}
