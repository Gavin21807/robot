package org.wfrobotics.subsystems.drive.swerve;

import org.wfrobotics.PIDController;
import org.wfrobotics.Utilities;
import org.wfrobotics.Vector;
import org.wfrobotics.commands.drive.*;
import org.wfrobotics.subsystems.drive.DriveSubsystem;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Swerve chassis implementation 
 * @author Team 4818 WFRobotics
 */
public class SwerveDriveSubsystem extends DriveSubsystem 
{
    public class ChassisVector
    {
        Vector velocity;
        double spin;
        double heading;

        public ChassisVector(Vector velocity,  double rotationalSpeed, double heading)
        {
            this.velocity = velocity;
            this.spin = rotationalSpeed;
            this.heading = heading;
        }

        public ChassisVector(Vector velocity,  double rotationalSpeed)
        {
            this(velocity, rotationalSpeed, HEADING_IGNORE);
        }

        public boolean ignoreHeading()
        {
            return heading == HEADING_IGNORE;
        }
    }
    
    public class SwerveConfiguration
    {
        /**
         * Sets shifter to high or low gear (True: High gear, False: Low gear)
         */
        public boolean gearHigh = false;
        public boolean gyroEnable = true;

        private final double AUTO_ROTATION_MIN = .1;  // this will hopefully prevent locking the wheels when slowing down
        private final double HEADING_TIMEOUT = .2;
    }

    public static final double HEADING_IGNORE = -1;

    public SwerveConfiguration configSwerve;
    public WheelManager wheelManager;
    private PIDController m_chassisAngleController;

    private double m_lastHeadingTimestamp;  // this is used to address the counter spinning/snapback
    double m_gyroLast;

    /**
     * sets up individual wheels and their positions relative to robot center
     */
    public SwerveDriveSubsystem()
    {        
        super(true);  // set it into field relative by default
        
        configSwerve = new SwerveConfiguration();
        m_chassisAngleController = new PIDController(.035, .00001, 0, 1.0);
        wheelManager = new WheelManager();
    }

    public void initDefaultCommand() 
    {
        setDefaultCommand(new DriveSwerve(DriveSwerve.MODE.HALO));
    }

    @Override
    public void driveTank(double right, double left)
    {
        //TODO?
    }

    @Override
    public void driveVector(double magnitude, double angle, double rotation)
    {
        driveVector(Vector.NewFromMagAngle(magnitude, angle), rotation);
    }

    @Override
    public void driveVector(Vector velocity, double rotation)
    {
        driveWithHeading(velocity, rotation, HEADING_IGNORE);
    }

    @Override
    public void driveXY(double x, double y, double rotation)
    {
        driveWithHeading(new Vector(x,y), rotation, HEADING_IGNORE);
    }

    /**
     * Main drive update function, allows for xy movement, yaw rotation, and turn to angle/heading
     * @param Velocity {@link Vector} of xy movement of robot
     * @param Rotation robot's rotational movement, -1 to 1 rad/s
     * @param Heading 0-360 of angle to turn to, -1 if not in use
     * @return actual wheel readings
     */
    public Vector[] driveWithHeading(Vector Velocity, double Rotation, double Heading)
    {
        ChassisVector cv = new ChassisVector(Velocity, Rotation, Heading);

        cv.spin = ApplySpinMode(cv);
        cv.velocity = applyVelocityMode(cv);
        
        return wheelManager.setWheelVectors(Velocity, Rotation, configSwerve.gearHigh, m_brake);
    }
    
    private double ApplySpinMode(ChassisVector cv)
    {
        double Error = 0;

        // Determine which drive mode to use between
        if (!cv.ignoreHeading())
        {
            // Rotate to angle
            SmartDashboard.putString("Drive Mode", "Rotate To Heading");

            // This should snap us to a specific angle
            if(configSwerve.gyroEnable)
            {
                // Set the rotation using a PID controller based on current robot heading and new desired heading
                Error = Utilities.wrapToRange(cv.heading - m_gyro.getYaw(), -180, 180);
                cv.spin = m_chassisAngleController.update(Error);
            }
            m_lastHeading = cv.heading;
        }
        else 
        {
            if (Math.abs(cv.spin) > .1)
            {
                // Spinning
                SmartDashboard.putString("Drive Mode", "Spinning");

                // Just take the rotation value from the controller
                m_lastHeading = m_gyro.getYaw();

                // Save off timestamp to counter snapback
                m_lastHeadingTimestamp = Timer.getFPGATimestamp();

                m_gyroLast = m_gyro.getYaw();

                // Square rotation value to give it more control at lower values but keep the same sign since a negative squared is positive
                cv.spin = Math.signum(cv.spin) * Math.pow(cv.spin, 2);
            }
            else // maintain angle
            {
                // Delay the stay at angle to prevent snapback
                if((Timer.getFPGATimestamp() - m_lastHeadingTimestamp) > configSwerve.HEADING_TIMEOUT)
                {
                    SmartDashboard.putString("Drive Mode", "Stay At Angle");

                    if(configSwerve.gyroEnable)
                    {
                        // This should keep us facing the same direction

                        // Set the rotation using a PID controller based on current robot
                        // heading and new desired heading
                        Error = -Utilities.wrapToRange(m_lastHeading - m_gyro.getYaw(), -180, 180);
                        double tempRotation = m_chassisAngleController.update(Error);

                        // Add a deadband to hopefully help with wheel lock after stopping
                        SmartDashboard.putNumber("MaintainRotation", tempRotation);
                        if(tempRotation > configSwerve.AUTO_ROTATION_MIN)
                        {
                            cv.spin = tempRotation;
                        }
                    }
                }
                else
                {
                    m_lastHeading = m_gyro.getYaw(); // Save off the latest heading until the timeout
                }
            }
        }

        SmartDashboard.putNumber("Rotation Error", Error);
        
        return cv.spin;
    }
    
    protected Vector applyVelocityMode(ChassisVector cv)
    {
        SmartDashboard.putNumber("Velocity X", cv.velocity.getX());
        SmartDashboard.putNumber("Velocity Y", cv.velocity.getY());
        SmartDashboard.putNumber("Rotation", cv.spin);

        // If we're relative to the field, we need to adjust the movement vector based on the gyro heading
        if (m_fieldRelative)
        {
            double AdjustedAngle = cv.velocity.getAngle() + m_gyro.getYaw();
            
            AdjustedAngle = Utilities.wrapToRange(AdjustedAngle, -180, 180);
            
            SmartDashboard.putNumber("FieldRelativePre", cv.velocity.getAngle());            
            cv.velocity.setAngle(AdjustedAngle);
            SmartDashboard.putNumber("FieldRelativePost", cv.velocity.getAngle());
        }

        return cv.velocity;
    }

    /**
     * Get the last heading used for the robot. If free spinning, this will constantly update
     * @return angle in degrees of the robot heading, relative to the field
     */
    public double getLastHeading()
    {
        return m_lastHeading;
    }

    /**
     * Get the last movement vector of the robot, relative to the robot heading.
     * the adjustment for field relative mode, if applicable, has already been taken into consideration
     * @return movement vector relative to the robot heading
     */
    public Vector getLastVector()
    {
        return wheelManager.getLastVector();
    }

    /**
     * Do a full wheel calibration, adjusting the angles by the specified values,
     * and save the values for use
     * @param speed speed value to test against, 0-1
     * @param values array of values, -180 to 180, to adjust the wheel angle offsets
     */
    public void fullWheelCalibration(double speed, double values[], boolean save)
    {
        wheelManager.doFullWheelCalibration(speed, values, save);
    }

    public double[] getWheelCalibrations()
    {
        return wheelManager.getWheelCalibrations();
    }
    
    @Override
    public void printDash()
    {
        super.printDash();
        wheelManager.printDash();

        SmartDashboard.putBoolean("Gyro Enabled", configSwerve.gyroEnable);
        SmartDashboard.putBoolean("High Gear", configSwerve.gearHigh);
    }

    /**
     * For unit testing
     */
    public void free()
    {
        wheelManager.free();
    }    
}
