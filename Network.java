package Mechanics;
import battlecode.common.*;
import static halimBot.RobotPlayer.controller;

public class Network {
	public static RobotInfo[] myRobots;
	public static RobotInfo[] enemyBots;
	
	static Team enemy = controller.getTeam().opponent();
	
	static int myRobotCount = 0;
	static int enemyCount = 0;
	
	static MapLocation[] inititialTeamArchonLoc;
	static MapLocation[] inititialEnemyArchonLoc;
	
	public static TreeInfo[] neutralTrees;
	public static TreeInfo[] myTrees;
	public static TreeInfo[] enemyTrees;
	
	public static void updateNetwork()throws GameActionException{
		senseBots();
		senseTrees();
	}
	public static void senseBots()throws GameActionException {
		enemyBots = controller.senseNearbyRobots(-1,enemy);
		myRobots = controller.senseNearbyRobots(-1,controller.getTeam());
		
		myRobotCount = myRobots.length;
		//enemyCount = enemyBots.length;
		
		controller.broadcastInt(Ports.ALLY_COUNT, myRobotCount);
		
		for(RobotInfo robot: enemyBots) {
			for(int i=Ports.ENEMY_LOC_PORT_START;i<Ports.ENEMY_LOC_PORT_END;i+=2) {
				if(controller.readBroadcast(i) == 0) {
					controller.broadcastFloat(i,robot.getLocation().x);
					controller.broadcastFloat(i+1,robot.getLocation().y);
					break;
				}else {
					if(i == Ports.ENEMY_LOC_PORT_END-2) {
						for(int j=Ports.ENEMY_LOC_PORT_START;j<Ports.ENEMY_LOC_PORT_END;j+=2) {
							if(controller.readBroadcast(j) != 0) {
								controller.broadcastFloat(j,0);
								controller.broadcastFloat(j+1,0);
							}
						}
					}
				}
			}enemyCount++;
		}
		controller.broadcastInt(Ports.ENEMY_COUNT, enemyCount);
		
		for(RobotInfo robot: myRobots) {
			int i = 0;
			int k = 0;
			
			switch(robot.getType()) {
			case ARCHON:
				i = Ports.TEAM_ARCHON_PORT_START;
				k = Ports.TEAM_ARCHON_PPORT_END;
				broadcast(i,k,robot);
				break;
			case GARDENER:
				i = Ports.TEAM_GARDENER_PORT_START;
				k = Ports.TEAM_GARDENER_PORT_END;
				broadcast(i,k,robot);
				break;
			case LUMBERJACK:
				i = Ports.TEAM_LUMBERJACK_PORT_START;
				k = Ports.TEAM_LUMBERJACK_PORT_END;
				broadcast(i,k,robot);
				break;
			case SCOUT:
				i = Ports.TEAM_SCOUT_PORT_START;
				k = Ports.TEAM_SCOUT_PORT_END;
				broadcast(i,k,robot);
				break;
			case SOLDIER:
				i = Ports.TEAM_SOLDIER_PORT_START;
				k = Ports.TEAM_SOLDIER_PORT_END;
				broadcast(i,k,robot);
				break;
			case TANK:
				break;
			default:
				break;
			
			}
			
		}
	}
	public static MapLocation findBroadcast(RobotType type)throws GameActionException {
		int i = 0;
		int k = 0;
		
		switch(type) {
		case ARCHON:
			i = Ports.TEAM_ARCHON_PORT_START;
			k = Ports.TEAM_ARCHON_PPORT_END;
			return readBroadcast(i,k);
		case GARDENER:
			i = Ports.TEAM_GARDENER_PORT_START;
			k = Ports.TEAM_GARDENER_PORT_END;
			return readBroadcast(i,k);			
		case LUMBERJACK:
			i = Ports.TEAM_LUMBERJACK_PORT_START;
			k = Ports.TEAM_LUMBERJACK_PORT_END;
			return readBroadcast(i,k);			
		case SCOUT:
			i = Ports.TEAM_SCOUT_PORT_START;
			k = Ports.TEAM_SCOUT_PORT_END;
			return readBroadcast(i,k);
		case SOLDIER:
			i = Ports.TEAM_SOLDIER_PORT_START;
			k = Ports.TEAM_SOLDIER_PORT_END;
			return readBroadcast(i,k);
		case TANK:
			break;
		default:
			break;
		}
		return controller.getLocation();
	}
	
	public static MapLocation readBroadcast(int start,int end) throws GameActionException{
		int count = 0;
		MapLocation loc = controller.getLocation();
		if(controller.readBroadcastInt(Ports.ALLY_COUNT) >= 1) {
			for(int i=start;i<end;i+=2) {
				if(count != 0 && controller.readBroadcast(i) == 0) {
					float x = controller.readBroadcastFloat(i-1);
					float y = controller.readBroadcastFloat(i-2);
					loc = new MapLocation(x,y);
					
					if(!loc.equals(controller.getLocation()) && loc.isWithinDistance(controller.getLocation(),controller.getType().strideRadius)) {
						return loc.add((float)(Math.PI/4),controller.getType().strideRadius);
					}else {
						count++;
					}
				}else {
					float x = controller.readBroadcastFloat(i);
					float y = controller.readBroadcastFloat(i+1);
					loc = new MapLocation(x,y);
					
					return loc;
				}
			}
		}
		return loc;
	}
	public static void broadcast(int start,int end,RobotInfo robot)throws GameActionException {
		for(int i=start;i<end;i+=2) {
			if(controller.readBroadcast(i) == 0) {
				controller.broadcastFloat(i,robot.getLocation().x);
				controller.broadcastFloat(i+1,robot.getLocation().y);
			}else {
				if(i == end-2) {
					for(int j=start;j<end;j+=2) {
						if(controller.readBroadcast(j) != 0) {
							controller.broadcastFloat(j,0);
							controller.broadcastFloat(j+1,0);
						}
					}
				}
			}
		}
	}
	
	public static void senseTrees()throws GameActionException{
		myTrees = controller.senseNearbyTrees(-1,controller.getTeam());
		neutralTrees = controller.senseNearbyTrees(-1,Team.NEUTRAL);
		enemyTrees = controller.senseNearbyTrees(-1,enemy);
	}
	
}
