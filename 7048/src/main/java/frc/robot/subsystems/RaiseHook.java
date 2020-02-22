/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Config.Constants;

public class RaiseHook extends SubsystemBase {
  /**
   * Creates a new ExampleSubsystem.
   */
  enum HookPosition {
    UP,
    MIDDLE,
    DOWN
  };
  private DoubleSolenoid raisePiston1;
  private DoubleSolenoid raisePiston2;
  private HookPosition position;
  
  public RaiseHook() {
    position = HookPosition.DOWN;
    raisePiston1 = new DoubleSolenoid(Constants.hook[0], Constants.hook[1]);
    raisePiston2 = new DoubleSolenoid(Constants.hook[0], Constants.hook[2]);
  }

  public void moveHook() {
    switch(position) {
      case UP: // if(position == UP)
        // raisePiston.set(Constants.hookDownValue);
        raisePiston2.set(DoubleSolenoid.Value.kForward);
        position = HookPosition.DOWN;
      case MIDDLE:
      // raisePiston.set(Constants.hookDownValue);
      raisePiston1.set(DoubleSolenoid.Value.kForward);
      position = HookPosition.UP;
      case DOWN: // if(position == DOWN)
        // raisePiston.set(Constants.hookUpValue);
        raisePiston1.set(DoubleSolenoid.Value.kReverse);
        raisePiston2.set(DoubleSolenoid.Value.kReverse);
        position = HookPosition.MIDDLE;
    }
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }
}
