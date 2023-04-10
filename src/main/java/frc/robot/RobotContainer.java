// In Java We Trust

package frc.robot;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.ArmConstants;
import frc.robot.Constants.ButtonBoardConstants;
import frc.robot.Constants.DrivebaseConstants;
import frc.robot.GlobalVars.DebugInfo;
import frc.robot.GlobalVars.GameStates;
import frc.robot.GlobalVars.SniperMode;
import frc.robot.commands.ArcadeCommand;
import frc.robot.commands.AutoEngageCommand;
// import frc.robot.commands.PeriodicArmCommand;
import frc.robot.commands.TurnCommand;
// import frc.robot.commands.PeriodicArmCommand;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.AutonSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.IntakeSubsystem;

public class RobotContainer {

  private CommandXboxController controller;
  private CommandGenericHID buttonBoard;

  private DriveSubsystem robotDrive;
  private ArmSubsystem robotArm;
  private IntakeSubsystem robotIntake;
  private AutonSubsystem robotAuton;

  private PowerDistribution PDH;

  private SendableChooser<Command> autonChooser;

  Trigger chosenButton;

  // private Command PeriodicArmCommand;

  public RobotContainer() {

    
    controller = new CommandXboxController(DrivebaseConstants.CONTROLLER_PORT);
    buttonBoard = new CommandGenericHID(ButtonBoardConstants.BUTTON_BOARD_PORT);

    robotDrive = new DriveSubsystem();
    robotArm = new ArmSubsystem();
    robotIntake = new IntakeSubsystem();
    robotAuton = new AutonSubsystem(robotDrive, robotArm, robotIntake);

    PDH = new PowerDistribution(DrivebaseConstants.PDH_PORT_CANID, ModuleType.kRev);

    autonChooser = new SendableChooser<>();

    robotDrive.setDefaultCommand(new ArcadeCommand(
      () -> controller.getLeftY(),
      () -> controller.getRightX(),
      robotDrive
      ));

    Shuffleboard.getTab("Autonomous: ").add(autonChooser);

    autonChooser.addOption("CONE MOBILITY", 
      robotAuton.autonomousCmd(1));

    autonChooser.addOption("CONE MOBILITY DOCK", 
      robotAuton.autonomousCmd(2));

    autonChooser.addOption("CONE SCORE ONLY", 
      robotAuton.autonomousCmd(3));
    
    autonChooser.addOption("CUBE SCORE ONLY", 
      robotAuton.autonomousCmd(4));

    autonChooser.addOption("CONE MOBILITY EXTEND GRAB", 
      robotAuton.autonomousCmd(5));

    autonChooser.addOption("CONE MOBILITY TURN EXTEND", 
      robotAuton.autonomousCmd(6));

    autonChooser.addOption("RED CONE MOBILITY TURN", 
      robotAuton.autonomousCmd(7));

    configureBindings();
  }

  private void configureBindings() {

    //////////////////// XBOX CONTROLLER //////////////////// 
    controller.leftTrigger()
      .whileTrue(new InstantCommand( () -> { SniperMode.driveSniperMode = true; }))
      .whileFalse(new InstantCommand( () -> { SniperMode.driveSniperMode = false; }));
    
    controller.leftBumper()
      .whileTrue(new InstantCommand( () -> {
        if (GameStates.isCube) { robotIntake.spinout(); }
        else { robotIntake.spinin(); }
      }))
      .whileFalse(new InstantCommand( () -> { robotIntake.spinoff(); }));

    controller.rightBumper()
      .whileTrue(new InstantCommand( () -> {
        if (GameStates.isCube) { robotIntake.spinin(); }
        else { robotIntake.spinout(); }
      }))
      .whileFalse(new InstantCommand( () -> { robotIntake.spinoff(); }));

    controller.a()
      .whileTrue(new AutoEngageCommand(robotDrive))
      .whileFalse(new InstantCommand( () -> {}));

    /*
     * NOTE: Leave this as onTrue, the timeout
     * will interrupt the command so no while
     * false or while true is needed here
     */
    controller.b()
    .whileTrue(new TurnCommand(robotDrive, 180));
    //.whileFalse(new InstantCommand( () -> { robotArm.setArm(0); }));

    // Test Button
    controller.y()
    .whileTrue(new InstantCommand( () -> holdAtSetpoint("high")))
    .whileFalse(new InstantCommand( () -> {
      holdCurrentPos();
    }));

    //////////////////// BUTTON BOARD ////////////////////

    pidArmInit(ButtonBoardConstants.SCORE_HIGH_BUTTON, "high");
    pidArmInit(ButtonBoardConstants.SCORE_MID_BUTTON, "mid");
    pidArmInit(ButtonBoardConstants.SCORE_LOW_BUTTON, "low");
    pidArmInit(ButtonBoardConstants.PICKUP_GROUND_BUTTON, "ground");
    pidArmInit(ButtonBoardConstants.PICKUP_SUBSTATION_BUTTON, "substation");
    pidArmInit(ButtonBoardConstants.RETURN_TO_IDLE_BUTTON, "idle");

    armMoveInit(ButtonBoardConstants.ARM_UP_BUTTON, 1);
    armMoveInit(ButtonBoardConstants.ARM_DOWN_BUTTON, -1);

    buttonBoard.button(ButtonBoardConstants.TOGGLE_INTAKE_BUTTON)
      .whileTrue(new InstantCommand( () -> {
        if (GameStates.isCube) { robotIntake.spinin(); }
        else { robotIntake.spinout();
      }
      }))
      .whileFalse(new InstantCommand( () -> { robotIntake.spinoff(); }));

    buttonBoard.button(ButtonBoardConstants.TOGGLE_OUTAKE_BUTTON)
      .whileTrue(new InstantCommand( () -> {
        if (GameStates.isCube) { robotIntake.spinout(); }
        else { robotIntake.spinin(); }
      }))
      .whileFalse(new InstantCommand( () -> { robotIntake.spinoff(); }));

    buttonBoard.button(ButtonBoardConstants.TOGGLE_CONE_MODE_BUTTON)
      .toggleOnTrue(new InstantCommand( () -> { 
        GameStates.isCube = false; 
        PDH.setSwitchableChannel(true);
      }))
      .toggleOnFalse(new InstantCommand( () -> { 
        GameStates.isCube = true;
        PDH.setSwitchableChannel(false);
      }));
    
    
    
    
    buttonBoard.button(ButtonBoardConstants.TOGGLE_SNIPER_MODE_BUTTON)
      .toggleOnTrue(new InstantCommand( () -> { SniperMode.armSniperMode = true; }))
      .toggleOnFalse(new InstantCommand( () -> { SniperMode.armSniperMode = false; }));
  }

  private double returnAngle(String pos){
    switch(pos){
      case "high":
        if (GameStates.isCube) return ArmConstants.CUBE_HIGH_ANGLE;
        else return ArmConstants.CONE_HIGH_ANGLE;
        
      case "mid":
        if (GameStates.isCube) return ArmConstants.CUBE_MID_ANGLE;
        else return ArmConstants.CONE_MID_ANGLE;
        
      case "low":
        if (GameStates.isCube) return ArmConstants.CUBE_LOW_ANGLE;
        else return ArmConstants.CONE_LOW_ANGLE;
        
      case "ground":
        if (GameStates.isCube) return ArmConstants.CUBE_GROUND_ANGLE;
        else return ArmConstants.CONE_GROUND_ANGLE;
        
      case "substation":
        if (GameStates.isCube) return ArmConstants.CUBE_SUBSTATION_ANGLE;
        else return ArmConstants.CONE_SUBSTATION_ANGLE;

      case "idle":
        return ArmConstants.IDLE;
        
      default:
        System.out.println("CODE ERROR! INVALID POSITION! CHECK ROBOTCONTAINER!");
        return 0;
    }
  }

  // #region CUSTOM ABSTRACTION FUNCTIONS
  
  // Simply abstracts PID positions arm must go to when button pressed
  private void pidArmInit(int btnPort, String desiredAngle) {
    chosenButton = buttonBoard.button(btnPort);

    chosenButton
    .whileTrue(new InstantCommand( () -> holdAtSetpoint(desiredAngle)))
    .onFalse(new InstantCommand( () -> holdCurrentPos()));
  }

  // Moves arm motor based on spee/d on button press
  private void armMoveInit(int btnPort, int speedPar) {
    chosenButton = buttonBoard.button(btnPort);

    chosenButton
    .whileTrue(new InstantCommand( () -> { 
      GameStates.shouldHoldArm = false;
      DebugInfo.currentArmSpeed = speedPar; 
      robotArm.setArm(DebugInfo.currentArmSpeed); 
    }))
    .onFalse(new InstantCommand( () -> { holdCurrentPos(); }));
  }

  private void holdCurrentPos() {
    robotArm.getPeriodicArmCommand().resetPID();
    double curPos =  robotArm.getBicepEncoderPosition();

    if (!chosenButton.getAsBoolean()){      
      GameStates.shouldHoldArm = false;
      GameStates.armSetpoint = curPos;
      GameStates.shouldHoldArm = true;
    }
  } 

  private void holdAtSetpoint(String setpoint_par){
    robotArm.getPeriodicArmCommand().resetPID();
    GameStates.shouldHoldArm = false;
    GameStates.armSetpoint = returnAngle(setpoint_par);
    GameStates.shouldHoldArm = true;
  }
  // endregion

  public DriveSubsystem getRobotDrive() {
    return robotDrive;
  }

  public Command getAutonomousCommand() {
    return autonChooser.getSelected();
  }
}
