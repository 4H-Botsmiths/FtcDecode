package org.firstinspires.ftc.teamcode.hardware;

import com.android.annotations.Nullable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Lightweight wrapper around a DcMotorEx that lets you add your own helpers/logic
 * without trying to extend an interface. Use composition and delegate as needed.
 */
public class Motor {
  private final DcMotorEx motor;
  private final Lights lights;

  public Motor(DcMotorEx motor) {
    this.motor = motor;
    this.lights = null;
  }

  public Motor(DcMotorEx motor, Lights lights) {
    this.motor = motor;
    this.lights = lights;
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
  public void setVelocity(double angularRate) {
    motor.setVelocity(angularRate);
    if (lights != null) {
      if (angularRate != 0) {
        if (this.getVelocity() < angularRate * 0.9) {
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
    double ticksPerRev = motor.getMotorType().getTicksPerRev();
    double ticksPerMinute = rpm * ticksPerRev;
    double ticksPerSecond = ticksPerMinute / 60.0;
    motor.setVelocity(ticksPerSecond);
    return ticksPerSecond;
  }

  public double getRPM() {
    double ticksPerSecond = motor.getVelocity();
    double ticksPerRev = motor.getMotorType().getTicksPerRev();
    double ticksPerMinute = ticksPerSecond * 60.0;
    return ticksPerMinute / ticksPerRev;
  }

  public void setSpeed(double speed) {
    setRPM(speed * motor.getMotorType().getMaxRPM());
  }

  public double getSpeed() {
    return getRPM() / motor.getMotorType().getMaxRPM();
  }
}
