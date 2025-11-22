package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.Camera.CameraNotAttachedException;

@TeleOp(name = "Shooter Speed Tester", group = "Diagnostics")
public class ShooterSpeedTester extends LinearOpMode {

  public Robot robot;
  public Camera camera;

  public double RPM = 1500;

  @Override
  public void runOpMode() {
    telemetry.addData("Status", "Initializing...");
    robot = new Robot(hardwareMap);
    camera = new Camera(hardwareMap);
    try {
      camera.initAprilTag();
    } catch (CameraNotAttachedException e) {
      telemetry.speak("Camera not attached. Range will not be logged.");
    }
    telemetry.update();
    telemetry.addData("Status", "Initialized!");
    telemetry.update();
    waitForStart(); //IMPORTANT
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

      robot.intake.setPowerAll(gamepad1.right_trigger - gamepad1.left_trigger);
      if (gamepad1.left_stick_x < -0.5) {
        robot.indexer.left();
      } else if (gamepad1.left_stick_x > 0.5) {
        robot.indexer.right();
      } else if (gamepad1.left_stick_y < -0.5) {
        robot.indexer.top();
      } else if (gamepad1.left_stick_y > 0.5) {
        robot.indexer.load();
      }
      robot.shooter.setRPM(gamepad1.b ? RPM : 0);

      telemetry.addData("Target RPM", RPM);
      telemetry.addData("Current RPM", robot.shooter.getRPM());
      telemetry.addData("Shooter At Speed", robot.shooter.atSpeedRPM(RPM));
      telemetry.addData("Shooter Left RPM", robot.leftShooter.getRPM());
      telemetry.addData("Shooter Right RPM", robot.rightShooter.getRPM());
      telemetry.addData("Shooter Velocity", robot.shooter.getVelocity());
      telemetry.addData("Shooter Power", robot.shooter.getPower());
      telemetry.addData("Camera Status", camera.visionPortal.getCameraState().toString());
      try {
        Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
        telemetry.addData("Distance to Target (in)", tag.ftcPose.range);
        telemetry.addData("Tag X (in?)", tag.ftcPose.x);
        telemetry.addData("Tag Yaw (deg)", tag.ftcPose.yaw);
        if (gamepad1.a) {
          robot.drive(0, tag.ftcPose.range < 70 ? -Range.clip((70 - tag.ftcPose.range) * 0.025, -0.15, 0.15) : 0,
              Range.clip(tag.ftcPose.x * -0.025, -0.15, 0.15));
        } else {
          robot.drive(0, 0, 0);
        }
      } catch (CameraNotAttachedException e) {
        telemetry.addLine("Camera not attached.");
      } catch (Camera.CameraNotStreamingException e) {
        telemetry.addLine("Camera not streaming.");
      } catch (Camera.TagNotFoundException e) {
        telemetry.addLine("Tag not found.");
        robot.drive(0, 0, 0);

      }
      telemetry.update();
    }
  }
}
