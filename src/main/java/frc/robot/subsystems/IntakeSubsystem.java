
package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants.*;

public class IntakeSubsystem extends SubsystemBase{

  private CANSparkMax grabber;

  public IntakeSubsystem() {
    grabber = new CANSparkMax(
      ArmConstants.GRABBER_MOTOR_CANID, 
      MotorType.kBrushless
    );
  }

  public void spinin() {
    grabber.set(1);
  }

  public void spinout() {
    grabber.set(-1);
  }

  public void spinoff() {
    grabber.set(0);
  }

  public double getIntakeCurrent() {
    return grabber.getOutputCurrent();
  }

  @Override  
  public void periodic() {
      SmartDashboard.putNumber("INTAKE CURRENT: ", getIntakeCurrent());
  }
}