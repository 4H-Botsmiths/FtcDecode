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

  public Robot(HardwareMap hardwareMap) {
    // Initialize hardware here
    this.leftShooter = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_0"),
        new Lights(leftShooterGreen, leftShooterRed));
    this.leftShooterRed = hardwareMap.get(LED.class, "DIGITAL_0");
    this.leftShooterGreen = hardwareMap.get(LED.class, "DIGITAL_1");
    this.rightShooter = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_1"),
        new Lights(rightShooterGreen, rightShooterRed));
    this.rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);
    this.rightShooterRed = hardwareMap.get(LED.class, "DIGITAL_2");
    this.rightShooterGreen = hardwareMap.get(LED.class, "DIGITAL_3");
  }
}