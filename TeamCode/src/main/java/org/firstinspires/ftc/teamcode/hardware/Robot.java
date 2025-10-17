package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LED;

public class Robot {

  public final Motor leftShooter;
  private final LED leftShooterRed;
  private final LED leftShooterGreen;
  public final Motor rightShooter;
  private final LED rightShooterRed;
  private final LED rightShooterGreen;
  public final Shooter shooter;

  public final Motor frontLeft;
  public final Motor frontRight;
  public final Motor rearLeft;
  public final Motor rearRight;

  public final Motor intake;

  public Robot(HardwareMap hardwareMap) {
    // Initialize hardware here
    this.leftShooterRed = hardwareMap.get(LED.class, "DIGITAL_0");
    this.leftShooterGreen = hardwareMap.get(LED.class, "DIGITAL_1");
    this.leftShooter = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_0"),
        new Lights(leftShooterGreen, leftShooterRed));
    this.rightShooterRed = hardwareMap.get(LED.class, "DIGITAL_2");
    this.rightShooterGreen = hardwareMap.get(LED.class, "DIGITAL_3");
    this.rightShooter = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_1"),
        new Lights(rightShooterGreen, rightShooterRed));
    this.rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

    this.shooter = new Shooter(this.leftShooter, this.rightShooter);

    this.frontLeft = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_4"));
    this.frontRight = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_5"));
    this.rearLeft = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_6"));
    this.rearRight = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_7"));

    this.intake = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_2"));
    this.intake.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
  }

  public final int DRIVE_MAX_RPM = 300;

  public void drive(double x, double y, double rotate) {
    double frontLeftPower = y + x + rotate;
    double frontRightPower = y - x - rotate;
    double rearLeftPower = y - x + rotate;
    double rearRightPower = y + x - rotate;

    frontLeft.setRPM(frontLeftPower * this.DRIVE_MAX_RPM);
    frontRight.setRPM(frontRightPower * this.DRIVE_MAX_RPM);
    rearLeft.setRPM(rearLeftPower * this.DRIVE_MAX_RPM);
    rearRight.setRPM(rearRightPower * this.DRIVE_MAX_RPM);
  }

  public void drive(double x, double y, double rotate, double gyro) {
    double tempX = x * Math.cos(gyro) + y * Math.sin(gyro);
    double tempY = -x * Math.sin(gyro) + y * Math.cos(gyro);

    this.drive(tempX, tempY, rotate);
  }
}