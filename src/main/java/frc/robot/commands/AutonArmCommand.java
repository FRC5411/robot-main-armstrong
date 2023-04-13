
package frc.robot.commands;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants.ArmConstants;
// import frc.robot.GlobalVars.GameStates;
// import frc.robot.GlobalVars.DebugInfo;
// import frc.robot.GlobalVars.GameStates;
import frc.robot.subsystems.ArmSubsystem;

public class AutonArmCommand extends CommandBase {

    @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
    
    private ArmSubsystem robotArm;
    private ProfiledPIDController pid;
    private ArmFeedforward FF;
    private double setpoint;

    private double kP;
    private double kI; 
    private double kD;

    public AutonArmCommand(ArmSubsystem robotArm, double setpoint) {
        this.robotArm = robotArm;
        this.setpoint = setpoint;
        SendableRegistry.setName(pid, "ArmSubsystem", "PID");
    }

    @Override
    public void initialize() {
      kP = 0.066;
      kI = 0;
      kD = 0;

      FF = new ArmFeedforward(setpoint, kP, kI, kD);
      pid = new ProfiledPIDController(kP, kI, kD, new TrapezoidProfile.Constraints(ArmConstants.ARM_VELOCITY, ArmConstants.ARM_ACCELERATION));
      pid.setTolerance(2);
      pid.reset(robotArm.getBicepEncoderPosition());

      System.out.println("Command PERIODIC ARM ALIGN has started");
    }
  
    @Override
    public void execute() {
      double calc = pid.calculate(robotArm.getBicepEncoderPosition(), setpoint) 
      + FF.calculate(Math.toRadians(robotArm.getBicepEncoderPosition()), Math.toRadians(robotArm.getEncoderVelocity())/12);
      robotArm.setManualArm(calc);
    }
  
    @Override
    public void end(boolean interrupted) {
      robotArm.setArm(0);
      System.out.println("Command PERIODIC ARM ALIGN has ended");
    }
  
    @Override
    public boolean isFinished() {
      return false;
    }
}