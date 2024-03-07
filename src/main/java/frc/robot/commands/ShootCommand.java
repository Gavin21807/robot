// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import frc.robot.subsystems.IntakeSubSystem;
import edu.wpi.first.wpilibj2.command.Command;

/** An example command that uses an example subsystem. */
public class ShootCommand extends Command {
  private final IntakeSubSystem subSystem;
  
  /**
   * Creates a new ShootCommand.
   *
   * @param subsystem The subsystem used by this command.
   */
  public ShootCommand(IntakeSubSystem m_subSystem) {

    subSystem = m_subSystem;
    addRequirements(m_subSystem);
    
    // Use addRequirements() here to declare subsystem dependencies.
    
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize()
  {
    subSystem.m_shooter_1.set(1);
    subSystem.m_shooter_2.set(1);
    subSystem.m_midintake.set(1);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {}

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) 
  {
   subSystem.m_shooter_1.set(0);
    subSystem.m_shooter_2.set(0);
    subSystem.m_midintake.set(0);
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
