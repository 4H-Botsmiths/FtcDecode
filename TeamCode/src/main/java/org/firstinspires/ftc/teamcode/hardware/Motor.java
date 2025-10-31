package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Lightweight wrapper around a DcMotorEx that lets you add your own helpers/logic
 * without trying to extend an interface. Use composition and delegate as needed.
 */
public class Motor {
  private final DcMotorEx motor;
  private final Light lights;
  private double ticksPerRotation;

  public Motor(DcMotorEx motor) {
    this.motor = motor;
    this.lights = null;
    this.ticksPerRotation = motor.getMotorType().getTicksPerRev();
  }

  public Motor(DcMotorEx motor, double ticksPerRotation) {
    this.motor = motor;
    this.lights = null;
    this.ticksPerRotation = ticksPerRotation;
  }

  public Motor(DcMotorEx motor, Light lights) {
    this.motor = motor;
    this.lights = lights;
    this.ticksPerRotation = motor.getMotorType().getTicksPerRev();
  }

  public Motor(DcMotorEx motor, double ticksPerRotation, Light lights) {
    this.motor = motor;
    this.lights = lights;
    this.ticksPerRotation = ticksPerRotation;
  }

  // Expose the underlying motor when direct access is needed
  public DcMotorEx asDcMotorEx() {
    return motor;
  }

  // Common delegated operations — add more as you need
  public void setPower(double power) {
    motor.setPower(power);
  }

  public double getPower() {
    return motor.getPower();
  }

  public void setMode(DcMotor.RunMode mode) {
    motor.setMode(mode);
  }

  public DcMotor.RunMode getMode() {
    return motor.getMode();
  }

  public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
    motor.setZeroPowerBehavior(behavior);
  }

  public DcMotor.ZeroPowerBehavior getZeroPowerBehavior() {
    return motor.getZeroPowerBehavior();
  }

  public void setDirection(DcMotorSimple.Direction direction) {
    motor.setDirection(direction);
  }

  public DcMotorSimple.Direction getDirection() {
    return motor.getDirection();
  }

  // DcMotorEx-specific helpers

  public static final double TOLERANCE = 0.1;// percent tolerance of velocity

  public boolean atSpeed(double targetVelocity) {
    double currentVelocity = getVelocity();
    return Math.abs(currentVelocity - targetVelocity) <= TOLERANCE * targetVelocity;
  }

  public boolean atSpeedRPM(double targetRPM) {
    double currentRPM = getRPM();
    return Math.abs(currentRPM - targetRPM) <= TOLERANCE * targetRPM;
  }

  public void setVelocity(double angularRate) {
    motor.setVelocity(angularRate);
    if (lights != null) {
      if (angularRate != 0) {
        if (atSpeed(angularRate)) {
          lights.setRed(true);
          lights.setGreen(false);
        } else {
          lights.setGreen(true);
          lights.setRed(false);
        }
      } else {
        lights.setGreen(false);
        lights.setRed(false);
      }
    }
  }

  public double getVelocity() {
    return motor.getVelocity();
  }

  public void setTargetPosition(int position) {
    motor.setTargetPosition(position);
  }

  public int getCurrentPosition() {
    return motor.getCurrentPosition();
  }

  public double setRPM(double rpm) {
    double ticksPerMinute = rpm * ticksPerRotation;
    double ticksPerSecond = ticksPerMinute / 60.0;
    motor.setVelocity(ticksPerSecond);
    return ticksPerSecond;
  }

  public double getRPM() {
    double ticksPerSecond = motor.getVelocity();
    double ticksPerMinute = ticksPerSecond * 60.0;
    return ticksPerMinute / ticksPerRotation;
  }

  public void setSpeed(double speed) {
    setRPM(speed * ticksPerRotation);
  }

  public double getSpeed() {
    return getRPM() / ticksPerRotation;
  }
}
