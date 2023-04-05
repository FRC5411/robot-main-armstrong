package frc.robot.commands;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.DriveSubsystem;

public class TurnCommand extends CommandBase {
  
    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})

    private DriveSubsystem robotDrive;

    private PIDController pid;

    private double setpoint;
    private double kP, kI, kD;
  
    public TurnCommand(DriveSubsystem robotDrive, double setpoint) {
      this.robotDrive = robotDrive;
      this.setpoint = setpoint;

      kP = 0.01;
      kI = 0.0;
      kD = 0.0005;

      pid = new PIDController(kP, kI, kD);
      pid.setTolerance(3);

    }
  
    @Override
    public void initialize() {
      System.out.println("Command TURN has started");
      setpoint += robotDrive.getGyroYaw();

      System.out.print("Debug Me!!! ");
      System.out.println(robotDrive.getGyroYaw());
      System.out.println(setpoint);

    //   if(setpoint >= 360) setpoint = setpoint - 360;
   
    }
  
    @Override
    public void execute() {
        double pidCalc = pid.calculate(robotDrive.getGyroYaw(), setpoint);
        pidCalc *= 0.5;

        robotDrive.autonomousArcadeDrive(0, pidCalc);
    }
  
    @Override
    public void end(boolean interrupted) {
      System.out.println("Command TURN has ended");
    }
  
    @Override
    public boolean isFinished() {
      
      return false;
    }
  }
