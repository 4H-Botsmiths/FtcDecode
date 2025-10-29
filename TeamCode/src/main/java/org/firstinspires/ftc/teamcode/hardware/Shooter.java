package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;

/**
 * Shooter subsystem that controls the shooter motors and feeder servos.
 * The feeder servos advance 5" balls toward the shooter motors.
 */
public class Shooter {
    
    private final DcMotorEx leftShooter;
    private final DcMotorEx rightShooter;
    private final CRServo feederLeft;
    private final CRServo feederRight;
    
    // Shooter constants
    public static final double DEFAULT_SHOOTER_POWER = 0.8;
    public static final double DEFAULT_FEEDER_POWER = 0.7;
    
    /**
     * Create a Shooter subsystem from a Robot instance
     * @param robot The robot instance containing all hardware
     */
    public Shooter(Robot robot) {
        this.leftShooter = robot.leftShooter;
        this.rightShooter = robot.rightShooter;
        this.feederLeft = robot.feederLeft;
        this.feederRight = robot.feederRight;
    }
    
    /**
     * Start the shooter motors at specified power
     * @param power Power level (0.0 to 1.0)
     */
    public void startShooterMotors(double power) {
        leftShooter.setPower(power);
        rightShooter.setPower(power);
    }
    
    /**
     * Start shooter motors at default power
     */
    public void startShooterMotors() {
        startShooterMotors(DEFAULT_SHOOTER_POWER);
    }
    
    /**
     * Stop the shooter motors
     */
    public void stopShooterMotors() {
        leftShooter.setPower(0);
        rightShooter.setPower(0);
    }
    
    /**
     * Start the feeder servos to advance balls toward shooter
     * @param power Power level (-1.0 to 1.0, positive advances balls forward)
     */
    public void startFeeder(double power) {
        feederLeft.setPower(power);
        feederRight.setPower(power);
    }
    
    /**
     * Start feeder at default power
     */
    public void startFeeder() {
        startFeeder(DEFAULT_FEEDER_POWER);
    }
    
    /**
     * Reverse the feeder to back balls away from shooter
     */
    public void reverseFeeder() {
        feederLeft.setPower(-DEFAULT_FEEDER_POWER);
        feederRight.setPower(-DEFAULT_FEEDER_POWER);
    }
    
    /**
     * Stop the feeder servos
     */
    public void stopFeeder() {
        feederLeft.setPower(0);
        feederRight.setPower(0);
    }
    
    /**
     * Start the complete shooting sequence (shooter motors + feeder)
     */
    public void startShooting() {
        startShooterMotors();
        startFeeder();
    }
    
    /**
     * Start shooting with custom powers
     * @param shooterPower Power for shooter motors
     * @param feederPower Power for feeder servos
     */
    public void startShooting(double shooterPower, double feederPower) {
        startShooterMotors(shooterPower);
        startFeeder(feederPower);
    }
    
    /**
     * Stop all shooting components
     */
    public void stopShooting() {
        stopShooterMotors();
        stopFeeder();
    }
    
    /**
     * Get current shooter motor power (average of both motors)
     */
    public double getShooterPower() {
        return (leftShooter.getPower() + rightShooter.getPower()) / 2.0;
    }
    
    /**
     * Get status for telemetry
     */
    public String getStatus() {
        return String.format("Shooter: %.2f, Feeder L: %.2f, R: %.2f",
            getShooterPower(), feederLeft.getPower(), feederRight.getPower());
    }
    
    /**
     * Check if shooter motors are running
     */
    public boolean isShooterRunning() {
        return leftShooter.getPower() > 0.01 || rightShooter.getPower() > 0.01;
    }
    
    /**
     * Check if feeder is running
     */
    public boolean isFeederRunning() {
        return Math.abs(feederLeft.getPower()) > 0.01 || Math.abs(feederRight.getPower()) > 0.01;
    }
}