
package frc.robot.commands;

import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.ProfiledPIDCommand;
import frc.robot.Constants.DrivebaseConstants;
import frc.robot.GlobalVars.DebugInfo;
import frc.robot.subsystems.DriveSubsystem;
import edu.wpi.first.math.controller.ArmFeedforward;

public class AutoEngageCommand extends ProfiledPIDCommand {
  
  public AutoEngageCommand(DriveSubsystem robotDrive) {
    super(
        new ProfiledPIDController(
            0.0235,// 0.023 //0.0234, //0.027
            0,
            0,
            new TrapezoidProfile.Constraints(1, 0.5)),
        robotDrive::getGyroPitch,
        DebugInfo.initialGyroPitch,
        (output, setpoint) -> {
          ArmFeedforward armFF = new ArmFeedforward(0, 0.2642, 0);
          SmartDashboard.putNumber("GYRO CALC", output);
          if (output < 0) {
            output = output * DrivebaseConstants.AUTO_ENGAGE_DRIVE_BOOST;
          }
          output += armFF.calculate(Math.toRadians(setpoint.position - 90), setpoint.velocity);

          robotDrive.autonomousArcadeDrive(-output, 0);
      });

    addRequirements(robotDrive);

    getController().setTolerance(2.5);
  }

  public void initialize() {}

  @Override
  public boolean isFinished() {
    return false;
  }

  public void end() {}
}
