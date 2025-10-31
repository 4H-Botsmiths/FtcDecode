package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.CRServo;

public class Shooter {
  private final Motor left;
  private final Motor right;
  private final CRServo intakeLeft;
  private final CRServo intakeRight;

  public Shooter(Motor leftShooter, Motor rightShooter, CRServo intakeServoLeft, CRServo intakeServoRight) {
    this.left = leftShooter;
    this.right = rightShooter;
    this.intakeLeft = intakeServoLeft;
    this.intakeRight = intakeServoRight;
  }

  // Motor-like helpers (delegate to both motors)

  public void setPower(double power) {
    left.setPower(power);
    right.setPower(power);
  }

  public double getPower() {
    return (left.getPower() + right.getPower()) / 2.0;
  }

  public void setMode(DcMotor.RunMode mode) {
    left.setMode(mode);
    right.setMode(mode);
  }

  public DcMotor.RunMode getMode() {
    return left.getMode();
  }

  public void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior behavior) {
    left.setZeroPowerBehavior(behavior);
    right.setZeroPowerBehavior(behavior);
  }

  public DcMotor.ZeroPowerBehavior getZeroPowerBehavior() {
    return left.getZeroPowerBehavior();
  }

  public void setDirection(DcMotorSimple.Direction direction) {
    left.setDirection(direction);
    right.setDirection(direction);
  }

  public DcMotorSimple.Direction getDirection() {
    return left.getDirection();
  }

  public void setVelocity(double angularRate) {
    left.setVelocity(angularRate);
    right.setVelocity(angularRate);
  }

  public double getVelocity() {
    return (left.getVelocity() + right.getVelocity()) / 2.0;
  }

  public void setRPM(double rpm) {
    left.setRPM(rpm);
    right.setRPM(rpm);
  }

  public void setFeederPower(double power) {
    intakeLeft.setPower(power);
    intakeRight.setPower(power);
  }

  public void startShooter(double rpm, double power) {
    setRPM(rpm);
    setPower(power);
  }

  public double getRPM() {
    return (left.getRPM() + right.getRPM()) / 2.0;
  }

  public void setSpeed(double speed) {
    left.setSpeed(speed);
    right.setSpeed(speed);
  }

  public double getSpeed() {
    return (left.getSpeed() + right.getSpeed()) / 2.0;
  }

  public void setTargetPosition(int position) {
    left.setTargetPosition(position);
    right.setTargetPosition(position);
  }

  public int getCurrentPosition() {
    return (left.getCurrentPosition() + right.getCurrentPosition()) / 2;
  }
}