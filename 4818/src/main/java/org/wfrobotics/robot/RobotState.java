package org.wfrobotics.robot;


import org.wfrobotics.reuse.RobotStateBase;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** Preferred provider of global, formatted state about the robot. Commands can get information from one place rather than from multiple subsystems. **/
public final class RobotState extends RobotStateBase
{

    private static final RobotState instance = new RobotState();

    // Robot-specific state
    
    private double timeSinceRumbleOn;

    public RobotState()
    {
        
    }

    public static RobotState getInstance()
    {
        return instance;
    }

    public void reportState()
    {
        super.reportState();

        // TODO Prints all vision based on if we have vision - Flag in VisionProcessor constructor?
        SmartDashboard.putBoolean("Targets In View", visionInView);
        if (visionInView)
        {
            SmartDashboard.putNumber("Vision Error", getVisionError());
        }
    }

    protected synchronized void resetRobotSpecificState()
    {

    }

    @Override
    public double getKCameraAngle() {
        return 34.5;
    }

    /**    |~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~|
     *     |             Now entering Vision code territory                         |
     *     |   WF robotic is not responsible for anything cased by confusion!       |
     *     |________________________________________________________________________|
     */


}