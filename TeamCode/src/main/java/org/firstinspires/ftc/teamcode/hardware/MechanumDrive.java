package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.Range;

/**
 * MechanumDrive subsystem for controlling the robot's movement.
 * Handles all four drive motors using mecanum drive kinematics.
 */
public class MechanumDrive {
    
    private final DcMotorEx frontLeft;
    private final DcMotorEx frontRight;
    private final DcMotorEx rearLeft;
    private final DcMotorEx rearRight;
    
    // Drive constants
    public static final double MAX_POWER = 1.0;
    public static final double SLOW_MODE_MULTIPLIER = 0.3;
    public static final double NORMAL_MODE_MULTIPLIER = 0.8;
    public static final double TURBO_MODE_MULTIPLIER = 1.0;
    
    /**
     * Create a MechanumDrive subsystem from a Robot instance
     * @param robot The robot instance containing all hardware
     */
    public MechanumDrive(Robot robot) {
        this.frontLeft = robot.frontLeft;
        this.frontRight = robot.frontRight;
        this.rearLeft = robot.rearLeft;
        this.rearRight = robot.rearRight;
    }
    
    /**
     * Drive the robot using mecanum drive kinematics
     * @param strafe Left/right movement (positive = right)
     * @param forward Forward/backward movement (positive = forward)
     * @param rotate Rotation (positive = clockwise)
     */
    public void drive(double strafe, double forward, double rotate) {
        // Calculate individual wheel powers using mecanum kinematics
        double frontLeftPower = forward + strafe + rotate;
        double frontRightPower = forward - strafe - rotate;
        double rearLeftPower = forward - strafe + rotate;
        double rearRightPower = forward + strafe - rotate;
        
        // Find the maximum power to scale if needed
        double maxPower = Math.max(Math.max(Math.abs(frontLeftPower), Math.abs(frontRightPower)),
                                  Math.max(Math.abs(rearLeftPower), Math.abs(rearRightPower)));
        
        // Scale powers if any exceed 1.0 to maintain ratios
        if (maxPower > 1.0) {
            frontLeftPower /= maxPower;
            frontRightPower /= maxPower;
            rearLeftPower /= maxPower;
            rearRightPower /= maxPower;
        }
        
        // Set motor powers
        frontLeft.setPower(frontLeftPower);
        frontRight.setPower(frontRightPower);
        rearLeft.setPower(rearLeftPower);
        rearRight.setPower(rearRightPower);
    }
    
    /**
     * Drive with a speed multiplier (for slow/turbo modes)
     */
    public void drive(double strafe, double forward, double rotate, double speedMultiplier) {
        drive(strafe * speedMultiplier, forward * speedMultiplier, rotate * speedMultiplier);
    }
    
    /**
     * Stop all drive motors
     */
    public void stop() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        rearLeft.setPower(0);
        rearRight.setPower(0);
    }
    
    /**
     * Get current power levels for telemetry
     */
    public String getPowerStatus() {
        return String.format("FL:%.2f FR:%.2f RL:%.2f RR:%.2f", 
            frontLeft.getPower(), frontRight.getPower(), 
            rearLeft.getPower(), rearRight.getPower());
    }
}