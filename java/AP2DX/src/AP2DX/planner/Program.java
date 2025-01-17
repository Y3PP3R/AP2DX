package AP2DX.planner;

import java.util.ArrayList;
import java.util.Random;

import AP2DX.AP2DXBase;
import AP2DX.AP2DXMessage;
import AP2DX.Message;
import AP2DX.Module;
import AP2DX.specializedMessages.ActionMotorMessage;
import AP2DX.specializedMessages.ClearMessage;
import AP2DX.specializedMessages.InsSensorMessage;
import AP2DX.specializedMessages.OdometrySensorMessage;
import AP2DX.specializedMessages.ResetMessage;
import AP2DX.specializedMessages.SonarSensorMessage;

public class Program extends AP2DXBase {

	private static final double BACKWARDSPEED = 3.0;

	private static final double FORWARDSPEED = 10.0;

	/**
	 * threshold for turning in angle direction, robot should drive in a
	 * direction between destinationAngle - ANGLEUNCERTAIN and destinationAngle
	 * + ANGLEUNCERTAIN
	 */
	private static final double ANGLEUNCERTAIN = Math.toRadians(5);

	/**
	 * This much should two sonardata results of one side sensor differ,
	 * before it will check the hole
	 */
	private static final double TURNTHRESHOLD = 0.75;
	
	/**
	 * Mid four sonar sensors should  be at least this deep to drive into hole
	 */
	private static final double NEEDEDDEPTH = 0.4;

	/**
	 * drive this much past a hole before restarting scan of environment
	 */
	private static final double PASSHOLEDISTANCE = 0.3;

	/**
	 * drive this much past a hole before scanning it
	 */
	private static final double PASSHOLECORNER = 0.4;

	/**
	 * If distance from bot to wall (side sonar sensors) is larger than this,
	 * don't do hole checking.
	 */
	private static final double IGNOREDISTANCE = 0.5;

	/**
	 * if movement is only this much between two INS data, then it is 'no movement'
	 */
	private static final double MOVEMENTTHRESHOLD = 0.01;

	/**
	 * Did not move after this many INS data? Then action has to be taken.
	 */
	private static final int NOMOVECOUNT = 10;

	/**
	 * If stuck, drive this much back
	 */
	private static final double BACKWARDDISTANCE = 0.15;

	private InsLocationData locData;

	private double travelDistance;

	private double distanceGoal;

	/** Tho start the first drive after spawning */
	private boolean firstMessage = false;

	private double startAngle;

	private double currentAngle;

	private double destinationAngle;

	private boolean startedTurningToHole = false;

	/** permission to save sonardata for direction determining */
	private boolean sonarPermission = false;

	/** will contain sonar scan data for determining direction */
	private double[] sonarData;
	private double[] lastSonarData;

	private boolean doHoleScan = false;

	private int lastDirection;

	private boolean startedTurningBackFromHole;

	private boolean getInFrontOfHole;

	private boolean drivePastHole;

	private boolean stopBot;

	private boolean cruising;

	private boolean reverse;
	
	private int noMovementCount = -20;

	private boolean stuck; 

	/**
	 * Entrypoint of planner
	 */
	public static void main(String[] args) {
		new Program();
	}

	/**
	 * constructor
	 */
	public Program() {
		super(Module.PLANNER); // explicitly calls base constructor

		AP2DXBase.logger.info(" Running Planner... ");
	}

	@Override
	public ArrayList<AP2DXMessage> componentLogic(Message message) {
		ArrayList<AP2DXMessage> messageList = new ArrayList<AP2DXMessage>();
		// AP2DXBase.logger.info("Received message " + message.getMessageString());
		// AP2DXBase.logger.info(String.format("In Queue: %s",
		// this.getReceiveQueue().size()));

		switch (message.getMsgType()) {
		case AP2DX_PLANNER_STOP:

			if (!stuck) {
				
				startAngle = currentAngle;
	
				messageList.add(new ResetMessage(IAM, Module.REFLEX));
				
				AP2DXMessage msg7 = new ActionMotorMessage(IAM,
						Module.REFLEX,
						ActionMotorMessage.ActionType.BACKWARD, BACKWARDSPEED);
				msg7.compileMessage();
				messageList.add(msg7);
				
				setDistanceGoal(BACKWARDDISTANCE);
				setTravelDistance(0.0);
				
				noMovementCount = 0;
				
				//this changed
				stopBot = true;
				reverse = true;
				
				sonarPermission = false;
				stuck = false;
				cruising = false;
				drivePastHole = false;
				doHoleScan = false;
				startedTurningToHole = false;
				startedTurningBackFromHole = false;
				getInFrontOfHole = false;
			
			}
			break;
		
		// INS is used to calculated distance travelled
		case AP2DX_SENSOR_INS:

			InsSensorMessage msg = (InsSensorMessage) message;
			double[] loc = msg.getLocation();
			double[] ori = msg.getOrientation();

			if (locData == null) {

				locData = new InsLocationData(loc, ori);
			} else {
				locData.setLocationData(loc, ori);

				setTravelDistance(getTravelDistance()
						+ locData.travelDistance());

				if (locData.travelDistance() < MOVEMENTTHRESHOLD && !stuck && !reverse && !sonarPermission && !stopBot && !doHoleScan && !startedTurningBackFromHole) {
				//if (locData.travelDistance() < MOVEMENTTHRESHOLD && !stuck && !stopBot) {
					noMovementCount++;
				} else {
					noMovementCount = 0;
				}
				
				if (getDistanceGoal() > 0) {
					if (getTravelDistance() >= getDistanceGoal() && drivePastHole) {
						AP2DXBase.logger.info("Passed the hole (we hope)");
						drivePastHole = false;
						doHoleScan = false;
						cruising = true;
						
						setDistanceGoal(0);
						
						
					} else if (getTravelDistance() >= PASSHOLECORNER && getInFrontOfHole) {
						AP2DXBase.logger.info("Maybe infront of hole");

						
						startedTurningToHole = true;
						
						getInFrontOfHole = false;
						reverse = false;
						stopBot = false;
						stuck = false;
						cruising = false;
						drivePastHole = false;
						sonarPermission = false;
						doHoleScan = false;
						startedTurningBackFromHole = false;
						
						
						startAngle = currentAngle;
						
						if (lastDirection == -1) {
							destinationAngle = startAngle - (0.5*Math.PI);
							if (destinationAngle < -Math.PI) {
								destinationAngle = Math.PI + (destinationAngle + Math.PI);
							}
						}
						else {
							destinationAngle = startAngle + (0.5*Math.PI);
							if (destinationAngle > Math.PI) {
								destinationAngle = -Math.PI + (destinationAngle - Math.PI);
							}
						}
						
						messageList.add(new ResetMessage(IAM, Module.REFLEX));
						
						AP2DXMessage msg7 = new ActionMotorMessage(IAM,
								Module.REFLEX,
								ActionMotorMessage.ActionType.TURN, lastDirection);
						msg7.compileMessage();
						messageList.add(msg7);
						
						setDistanceGoal(0);
				}
				else if (reverse && (getTravelDistance() >= getDistanceGoal())) {
						AP2DXBase.logger.info("Wend backward for distanceGoal meters...");

						if (stopBot) {
							reverse = false;
							sonarPermission = true;
						}
						else { 
							messageList.add(new ResetMessage(IAM, Module.REFLEX));
	
							Random rand = new Random();
							
							int direction = (rand.nextBoolean() ? 1 : -1);
							
							startAngle = currentAngle;
							destinationAngle = startAngle + direction * 0.5 * Math.PI ;
							
							if (destinationAngle < -Math.PI) {
								destinationAngle = Math.PI - (destinationAngle + Math.PI);
							} else if (destinationAngle > Math.PI) {
								destinationAngle = -Math.PI + (destinationAngle - Math.PI);
							}
							
							AP2DXMessage msg7 = new ActionMotorMessage(IAM,
									Module.REFLEX,
									ActionMotorMessage.ActionType.TURN, direction);
							msg7.compileMessage();
							messageList.add(msg7);
							
							sonarPermission = false;
							stuck = true;
							
							stopBot = false;
							reverse = false;
							cruising = false;
							drivePastHole = false;
							doHoleScan = false;
							startedTurningToHole = false;
							startedTurningBackFromHole = false;
							getInFrontOfHole = false;
						}
					}
				}
				
				if (noMovementCount > NOMOVECOUNT && !stopBot && !reverse) {
					AP2DXBase.logger.info("No movement, going backward");
					
					reverse = true;
					
					stopBot = false;
					stuck = false;
					cruising = false;
					drivePastHole = false;
					sonarPermission = false;
					doHoleScan = false;
					startedTurningToHole = false;
					startedTurningBackFromHole = false;
					getInFrontOfHole = false;
					
					AP2DXMessage msg7 = new ActionMotorMessage(IAM,
							Module.REFLEX,
							ActionMotorMessage.ActionType.BACKWARD, BACKWARDSPEED);
					msg7.compileMessage();
					messageList.add(msg7);
					
					setDistanceGoal(BACKWARDDISTANCE);
					setTravelDistance(0.0);
					
					noMovementCount = 0;
				}
			}
			break;
			
		//sonar is used to calculate if path is free
		case AP2DX_SENSOR_SONAR:
			SonarSensorMessage msgs = (SonarSensorMessage) message;

			lastSonarData = sonarData;
			sonarData = msgs.getRangeArray();
			
			if (stopBot && !reverse) {
				AP2DXBase.logger.info("Bot stopped!");
				/*
				 * First field is the value of the sonar, second field is the index
				 * of the value in sonarData
				 */
				double longestSonar[] = { 0, 0 };

				// IMPORTANT: do or don't use outer most sonar sensors?
				for (int i = 1; i < sonarData.length-1; i++) {
					if (sonarData[i] > longestSonar[0]) {
						longestSonar[0] = sonarData[i];
						longestSonar[1] = i;
					}
				}

				/*
				 * Decides on the last acquired sonarData if to turn right or left
				 * Default is right
				 */
				if (longestSonar[1] < Math.round(sonarData.length / 2)) {
					AP2DXMessage msgt = new ActionMotorMessage(IAM, Module.REFLEX,
							ActionMotorMessage.ActionType.TURN, -1);
					msgt.compileMessage();
					messageList.add(msgt);
				} else {
					AP2DXMessage msgt = new ActionMotorMessage(IAM, Module.REFLEX,
							ActionMotorMessage.ActionType.TURN, 1);
					msgt.compileMessage();
					messageList.add(msgt);
				}
				
				sonarPermission = true;
				
				reverse = false;
				stopBot = false;
				stuck = false;
				cruising = false;
				drivePastHole = false;
				doHoleScan = false;
				startedTurningToHole = false;
				startedTurningBackFromHole = false;
				getInFrontOfHole = false;
			}
			else if (sonarPermission && !reverse) {
				AP2DXBase.logger.info("Having sonarPermissions");
				boolean[] space = new boolean[4];
				for (int i = 2; i < 6; i++) {
					space[i-2] = (sonarData[i] >= NEEDEDDEPTH);  
				}
				boolean succeed = true;
				for (boolean hole : space) {
					if (hole == false) {
						succeed = false;
						break;
					}
				}
				if (succeed) {
					AP2DXMessage msg5 = new ClearMessage(IAM, Module.REFLEX);
					msg5.compileMessage();
					messageList.add(msg5);

					AP2DXMessage msg6 = new ActionMotorMessage(IAM,
							Module.REFLEX,
							ActionMotorMessage.ActionType.FORWARD, FORWARDSPEED);
					msg6.compileMessage();
					messageList.add(msg6);

					cruising = true;
					
					reverse = false;
					stopBot = false;
					stuck = false;
					drivePastHole = false;
					sonarPermission = false;
					doHoleScan = false;
					startedTurningToHole = false;
					startedTurningBackFromHole = false;
					getInFrontOfHole = false;
				}
			} else if (doHoleScan && !reverse) {
				AP2DXBase.logger.info("Doing a holescan");
				doHoleScan = false;
				
				boolean[] space = new boolean[4];
				for (int i = 2; i < 6; i++) {
					space[i-2] = (sonarData[i] >= NEEDEDDEPTH);  
				}
				boolean succeed = true;
				for (boolean hole : space) {
					if (hole == false) {
						succeed = false;
						break;
					}
				}
				if (succeed) {
					AP2DXBase.logger.info("Yay a hole");
					
					startedTurningBackFromHole = false;
					startedTurningToHole = false;
					
					AP2DXMessage msg8 = new ClearMessage(IAM, Module.REFLEX);
					msg8.compileMessage();
					messageList.add(msg8);
					
					AP2DXMessage msg5 = new ActionMotorMessage(IAM, Module.REFLEX,
							ActionMotorMessage.ActionType.FORWARD, FORWARDSPEED);
					msg5.compileMessage();
					messageList.add(msg5);
				}
				else {
					AP2DXBase.logger.info("Not a nice hole, going back");
					
					startedTurningBackFromHole = true;
					startedTurningToHole = false;
					
					destinationAngle = startAngle;
					startAngle = currentAngle;
					
					AP2DXMessage msg5 = new ActionMotorMessage(IAM, Module.REFLEX,
							ActionMotorMessage.ActionType.TURN, -lastDirection);
					msg5.compileMessage();
					messageList.add(msg5);
					
					
				}
			}
			else if(cruising && !reverse) {
				AP2DXBase.logger.info("Just cruising");
				// 0 = no hole
				int hole = 0;
				
				//hole on the left
				if (lastSonarData[0] < IGNOREDISTANCE && (sonarData[0] - lastSonarData[0]) >= TURNTHRESHOLD) {
					hole = -1;
				}
				//hole on the right
				else if (lastSonarData[lastSonarData.length-1] < IGNOREDISTANCE && (sonarData[sonarData.length-1] - lastSonarData[lastSonarData.length-1]) >= TURNTHRESHOLD) {
					hole = 1;
				}
				
				// there is a hole
				if (hole != 0) {
					AP2DXBase.logger.info("Hey, is that a hole?");
					
					getInFrontOfHole = true;
					cruising = false;
					
					lastDirection = hole;
					
					setDistanceGoal(PASSHOLECORNER);
					setTravelDistance(0);
				}
			}
			break;
			
		// odometry is used to calculate turn angle
		case AP2DX_SENSOR_ODOMETRY:
			//AP2DXBase.logger.info("parsing odometry message in planner");

			OdometrySensorMessage msgo = (OdometrySensorMessage) message;

			currentAngle = msgo.getTheta();
			
			if (stuck)
			{
				AP2DXBase.logger.info("turning away from stuck position");
				
				boolean success = false;
				
				double minAngle = destinationAngle - ANGLEUNCERTAIN;
				double maxAngle = destinationAngle + ANGLEUNCERTAIN;
				
				// check if the current angle is +- the destination angle
				if (minAngle >= -Math.PI && maxAngle <= Math.PI) {
					if (currentAngle >= minAngle && currentAngle <= maxAngle) {
						success = true;
					}
				}
				// if current value exceeds whole circle, something ingenious is needed
				else {
					boolean min = false, max = false;
					double minPart1 = 0, maxPart1 = 0, minPart2 = 0, maxPart2 = 0 ;
					
					if (minAngle < -Math.PI) {
						minPart1 = -Math.PI;
						maxPart1 = Math.PI + (minAngle + Math.PI);
						min = true;
					}
					if (maxAngle > Math.PI) {
						minPart2 = -Math.PI + (maxAngle - Math.PI);
						maxPart2 = Math.PI;
						max = true;
					}
					
					if (min && (currentAngle >= minPart1 || currentAngle >= maxPart1)) {
						if(max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
							success = true;
						}
						else if (currentAngle <= maxAngle) {
							success = true;
						}
					}
					else if (max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
						if (currentAngle >= minAngle) {
							success = true;
						}
					}					
				}
				
				if (success) {
					
					stuck = false;
					sonarPermission = true;

					AP2DXBase.logger.info("turned successfully away from stuck position");
				}
				
			}
			else if (startedTurningToHole) {
				AP2DXBase.logger.info("Checking hole");
				boolean success = false;
				
				double minAngle = destinationAngle - ANGLEUNCERTAIN;
				double maxAngle = destinationAngle + ANGLEUNCERTAIN;
				
				// check if the current angle is +- the destination angle
				if (minAngle >= -Math.PI && maxAngle <= Math.PI) {
					if (currentAngle >= minAngle && currentAngle <= maxAngle) {
						success = true;
					}
				}
				// if current value exceeds whole circle, something ingenious is needed
				else {
					boolean min = false, max = false;
					double minPart1 = 0, maxPart1 = 0, minPart2 = 0, maxPart2 = 0 ;
					
					if (minAngle < -Math.PI) {
						minPart1 = -Math.PI;
						maxPart1 = Math.PI + (minAngle + Math.PI);
						min = true;
					}
					if (maxAngle > Math.PI) {
						minPart2 = -Math.PI + (maxAngle - Math.PI);
						maxPart2 = Math.PI;
						max = true;
					}
					
					if (min && (currentAngle >= minPart1 || currentAngle >= maxPart1)) {
						if(max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
							success = true;
						}
						else if (currentAngle <= maxAngle) {
							success = true;
						}
					}
					else if (max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
						if (currentAngle >= minAngle) {
							success = true;
						}
					}					
				}
				
				if (success) {
					
					startedTurningToHole = false;
					
					doHoleScan  = true;

					AP2DXMessage msg6 = new ActionMotorMessage(IAM,
							Module.REFLEX,
							ActionMotorMessage.ActionType.STOP, 666);
					msg6.compileMessage();
					messageList.add(msg6);

					System.out
							.println("Sending stop message for scanning");
				}
			}
			else if (startedTurningBackFromHole) {
				AP2DXBase.logger.info("Hole was not interesting");
				
				boolean success = false;
				
				double minAngle = destinationAngle - ANGLEUNCERTAIN;
				double maxAngle = destinationAngle + ANGLEUNCERTAIN;
				
				// check if the current angle is +- the destination angle
				if (minAngle >= -Math.PI && maxAngle <= Math.PI) {
					if (currentAngle >= minAngle && currentAngle <= maxAngle) {
						success = true;
					}
				}
				// if current value exceeds whole circle, something ingenious is needed
				else {
					boolean min = false, max = false;
					double minPart1 = 0, maxPart1 = 0, minPart2 = 0, maxPart2 = 0 ;
					
					if (minAngle < -Math.PI) {
						minPart1 = -Math.PI;
						maxPart1 = Math.PI + (minAngle + Math.PI);
						min = true;
					}
					if (maxAngle > Math.PI) {
						minPart2 = -Math.PI + (maxAngle - Math.PI);
						maxPart2 = Math.PI;
						max = true;
					}
					
					if (min && (currentAngle >= minPart1 || currentAngle >= maxPart1)) {
						if(max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
							success = true;
						}
						else if (currentAngle <= maxAngle) {
							success = true;
						}
					}
					else if (max && (currentAngle <= minPart2 || currentAngle <= maxPart2)) {
						if (currentAngle >= minAngle) {
							success = true;
						}
					}					
				}
				
				if (success) {	
					startedTurningBackFromHole = false;

					AP2DXMessage msg5 = new ClearMessage(IAM, Module.REFLEX);
					msg5.compileMessage();
					messageList.add(msg5);
					
					AP2DXMessage msg6 = new ActionMotorMessage(IAM,
							Module.REFLEX,
							ActionMotorMessage.ActionType.FORWARD, FORWARDSPEED);
					msg6.compileMessage();
					messageList.add(msg6);
					
					drivePastHole = true;
					
					setDistanceGoal(PASSHOLEDISTANCE);
					setTravelDistance(0);

					AP2DXBase.logger.info("Turned back, going forward again");
				}
			}
			if (!firstMessage) {
				// for now, lets just drive forward, OKAY?!
				AP2DXMessage msg5 = new ActionMotorMessage(IAM, Module.REFLEX,
						ActionMotorMessage.ActionType.FORWARD, FORWARDSPEED);
				msg5.compileMessage();
				messageList.add(msg5);

				AP2DXBase.logger.info("Sending message first message");

				cruising = true;
				
				firstMessage = true;

			}

			break;
		default:
			AP2DXBase.logger
					.severe("Error in AP2DX.reflex.Program.componentLogic(Message message) Couldn't deal with message: "
							+ message.getMsgType());
		}

		return messageList;
	}

	/**
	 * @param travelDistance
	 *            the travelDistance to set
	 */
	public void setTravelDistance(double travelDistance) {
		this.travelDistance = travelDistance;
	}

	/**
	 * @return the travelDistance
	 */
	public double getTravelDistance() {
		return travelDistance;
	}

	/**
	 * @param distanceGoal
	 *            the distanceGoal to set
	 */
	public void setDistanceGoal(double distanceGoal) {
		this.distanceGoal = distanceGoal;
	}

	/**
	 * @return the distanceGoal
	 */
	public double getDistanceGoal() {
		return distanceGoal;
	}
}
