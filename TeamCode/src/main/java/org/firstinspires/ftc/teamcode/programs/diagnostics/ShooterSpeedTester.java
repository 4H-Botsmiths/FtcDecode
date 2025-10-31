package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.Robot;

@TeleOp(name = "Shooter Speed Tester", group = "Diagnostics")
public class ShooterSpeedTester extends LinearOpMode {

  public Robot robot;

  public double RPM = 1500;

  @Override
  public void runOpMode() {
    telemetry.addData("Status", "Initializing...");
    robot = new Robot(hardwareMap);
    telemetry.update();
    telemetry.addData("Status", "Initialized!");
    telemetry.update();
    waitForStart(); //IMPORTANT
    telemetry.addData("Status", "Running - Press A for Full Power, B for Half Power, Nothing to Stop");
    telemetry.update();
    boolean upPressed = false;
    boolean downPressed = false;
    while (opModeIsActive()) { //IMPORTANT
      if (gamepad1.dpad_up && !upPressed) {
        RPM += 100;
        upPressed = true;
      } else if (!gamepad1.dpad_up) {
        upPressed = false;
      }
      if (gamepad1.dpad_down && !downPressed) {
        RPM -= 100;
        downPressed = true;
      } else if (!gamepad1.dpad_down) {
        downPressed = false;
      }

      robot.shooter.setRPM(RPM);
      telemetry.addData("Target RPM", RPM);
      telemetry.addData("Current RPM", robot.shooter.getRPM());
      telemetry.addData("Shooter Velocity", robot.shooter.getVelocity());
      telemetry.addData("Shooter Power", robot.shooter.getPower());
      telemetry.update();
    }
  }
}
