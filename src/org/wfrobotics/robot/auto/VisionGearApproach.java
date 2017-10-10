package org.wfrobotics.robot.auto;

import org.wfrobotics.Utilities;
import org.wfrobotics.Vector;
import org.wfrobotics.reuse.commands.drive.swerve.AutoDrive;
import org.wfrobotics.reuse.hardware.led.LEDs;
import org.wfrobotics.reuse.hardware.led.LEDs.Effect;
import org.wfrobotics.reuse.hardware.led.LEDs.Effect.EFFECT_TYPE;
import org.wfrobotics.reuse.utilities.PIDController;
import org.wfrobotics.robot.Robot;
import org.wfrobotics.robot.RobotState;
import org.wfrobotics.robot.subsystems.LED;

import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class VisionGearApproach extends CommandGroup
{
    private RobotState state = RobotState.getInstance();
    private AutoDrive drive;
    private PIDController pidX;
    private boolean fieldRelative = true;

    private boolean done;

    public VisionGearApproach()
    {
        pidX = new PIDController(2.5, 0.125, 0, .35);
        drive = new AutoDrive(0, 0, 0, -1, 999);

        addSequential(drive);
    }

    protected void initialize()
    {
        LED.getInstance().set(new Effect(EFFECT_TYPE.OFF, LEDs.BLACK, 1));
        fieldRelative = Robot.driveSubsystem.getFieldRelative();
        Robot.driveSubsystem.setFieldRelative(false);
        done = false;
    }

    protected void execute()
    {
        double distanceFromCenter = state.visionError;
        double visionWidth = state.visionWidth;
        double valueY = -.315;
        double valueX = 0;
        boolean found = state.visionInView;

        SmartDashboard.putBoolean("GearFound", found);

        if(!found)
        {
            done = true;
            return;
        }

        // we think we've found at least one target, so get an estimate speed to line us up
        valueX = pidX.update(distanceFromCenter);

        if(Math.abs(distanceFromCenter) < .05)
        {
            // if we started really far away from center, this will then reduce that overshoot maybe
            pidX.resetError();
        }

        // we can still see a target
        if(visionWidth > 15 && visionWidth < 335)
        {
            Vector vector = new Vector(valueY, valueX);

            drive.set(vector, 0, -1);
        }
        else
        {
            // if the detected target width is less than 15, we're either at the target or something went wrong
            done = true;
        }

        Utilities.PrintCommand("VisionGearDetect", this, state.visionInView + "");
        SmartDashboard.putNumber("GearDistanceX", distanceFromCenter);
        SmartDashboard.putNumber("VisionWidth", visionWidth);
        SmartDashboard.putNumber("VisionGearY", valueY);
        SmartDashboard.putNumber("VisionGearX", valueX);
    }

    protected boolean isFinished()
    {
        return done;
    }

    protected void end()
    {
        Robot.driveSubsystem.setFieldRelative(fieldRelative);
        LED.getInstance().set(LED.defaultLEDEffect);
    }

    protected void interrupted()
    {
        end();
    }
}
