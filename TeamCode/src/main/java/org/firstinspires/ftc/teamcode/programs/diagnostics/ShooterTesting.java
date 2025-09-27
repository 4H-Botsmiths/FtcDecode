package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.Telemetry;

@Autonomous(name = "Shooter Testing", group = "Diagnostics")
public class ShooterTesting extends LinearOpMode {

  public DcMotorEx shooter1 = null;
  public DcMotorEx shooter2 = null;
  public DcMotorEx shooter3 = null;

  public double POWER = 0;
  /*
  Pseudo code:
    turn on one motor, check which motor reports moving more than a certain velocity
    repeat with each motor
  */

  @Override
  public void runOpMode() {
    telemetry.setAutoClear(false);
    Telemetry.Item statusItem = telemetry.addData("Status", "Initializing...");
    telemetry.update();
    //IMPORTANT \/
    shooter1 = hardwareMap.get(DcMotorEx.class, "SHOOTER_1");
    shooter2 = hardwareMap.get(DcMotorEx.class, "SHOOTER_2");
    shooter3 = hardwareMap.get(DcMotorEx.class, "SHOOTER_3");
    //IMPORTANT /\
    statusItem.setValue("Initialized!");
    telemetry.update();
    waitForStart(); //IMPORTANT
    telemetry.update();
    while (opModeIsActive()) { //IMPORTANT
      if (gamepad1.a) {
        POWER = 1;
      } else if (gamepad1.b) {
        POWER = 0.5;
      } else {
        POWER = 0;
      }
      shooter1.setPower(POWER);
      shooter2.setPower(POWER);
      shooter3.setPower(POWER);
      telemetry.addData("Shooter 1 Velocity", shooter1.getVelocity());
      telemetry.addData("Shooter 2 Velocity", shooter2.getVelocity());
      telemetry.addData("Shooter 3 Velocity", shooter3.getVelocity());
      telemetry.update();
    }
  }
}
