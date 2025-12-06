package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Indexer;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.Camera.CameraNotAttachedException;

@TeleOp(name = "Shooter Speed Calibrator", group = "Diagnostics")
public class ShooterSpeedCalibrator extends LinearOpMode {

  public Robot robot;
  public Camera camera;

  public int RPM = 1500;
  public int distance = 50;

  @Override
  public void runOpMode() {
    telemetry.addData("Status", "Initializing...");
    robot = new Robot(hardwareMap);
    camera = new Camera(hardwareMap);
    try {
      camera.initAprilTag();
    } catch (CameraNotAttachedException e) {
      telemetry.speak("Camera not attached.");
    }
    telemetry.update();
    telemetry.addData("Status", "Initialized!");
    telemetry.update();
    waitForStart(); //IMPORTANT
    telemetry.update();
    boolean upPressed = false;
    boolean downPressed = false;
    boolean leftPressed = false;
    boolean rightPressed = false;
    boolean aPressed = false;
    boolean bPressed = false;
    boolean active = false;
    boolean alignActive = false;
    boolean aligning = false;
    boolean loading = false;
    while (opModeIsActive()) {
      telemetry.addData("Status", "Running");
      telemetry.addData("Camera Status", camera.visionPortal.getCameraState().toString());
      if (gamepad1.dpad_up && !upPressed) {
        RPM += 50;
        upPressed = true;
      } else if (!gamepad1.dpad_up) {
        upPressed = false;
      }
      if (gamepad1.dpad_down && !downPressed) {
        RPM -= 50;
        downPressed = true;
      } else if (!gamepad1.dpad_down) {
        downPressed = false;
      }
      if (gamepad1.dpad_left && !leftPressed) {
        distance -= 5;
        leftPressed = true;
      } else if (!gamepad1.dpad_left) {
        leftPressed = false;
      }
      if (gamepad1.dpad_right && !rightPressed) {
        distance += 5;
        rightPressed = true;
      } else if (!gamepad1.dpad_right) {
        rightPressed = false;
      }
      if (gamepad1.a && !aPressed) {
        active = !active;
        aPressed = true;
      } else if (!gamepad1.a) {
        aPressed = false;
      }
      if (gamepad1.b && !bPressed) {
        alignActive = !alignActive;
        bPressed = true;
      } else if (!gamepad1.b) {
        bPressed = false;
      }
      telemetry.addData("Target RPM", RPM);
      telemetry.addData("Target Distance", distance);

      double xSpeed = gamepad1.right_stick_x * 0.5;

      if (!active) {
        robot.shooter.setRPM(0);
        robot.intake.setPowerAll(0);
        robot.statusLed.setGreen(false);
        robot.statusLed.setRed(false);
        robot.drive(xSpeed, 0, 0);
        telemetry.addData("Status", "Paused");
        telemetry.update();
        continue;
      }

      if (loading) {
        telemetry.addData("Status", "Indexer Empty, Load Balls");
        robot.shooter.setRPM(0);
        robot.intake.setPowerAll(0);
        robot.statusLed.setGreen(false);
        robot.statusLed.setRed(true);
        boolean loaded = robot.indexer.load();
        if (loaded) {
          loading = false;
        }
      } else {
        telemetry.addData("Status", "Shooting...");
        robot.shooter.setRPM(RPM);
        robot.intake.setPowerAll(1);
        robot.statusLed.setGreen(true);
        robot.statusLed.setRed(false);
        if (!robot.indexer.isShooting() && !aligning) {
          telemetry.addData("Status", "Indexing...");
          boolean ballsLoaded = robot.indexer.setPosition(Indexer.BallColor.UNKNOWN, true);
          if (!ballsLoaded) {
            loading = true;
          }
        }
      }
      if (alignActive) {
        try {
          Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
          if (Math.abs(distance - tag.ftcPose.range) > 1 || Math.abs(tag.ftcPose.x) > 5) {
            aligning = true;
            telemetry.addData("Status", "Aligning...");
          } else {
            aligning = false;
            robot.drive(xSpeed, 0, 0);
          }
          telemetry.addData("Distance to Target (in)", tag.ftcPose.range);
          telemetry.addData("Tag X (in)", tag.ftcPose.x);
          telemetry.addData("Tag Yaw (deg)", tag.ftcPose.yaw);
          if (aligning) {
            robot.drive(xSpeed, -Range.clip((distance - tag.ftcPose.range) * 0.1, -0.15, 0.15),
                Range.clip(tag.ftcPose.x * 0.025, -0.15, 0.15));
          }

        } catch (CameraNotAttachedException e) {
          telemetry.addLine("Camera not attached.");
        } catch (Camera.CameraNotStreamingException e) {
          telemetry.addLine("Camera not streaming.");
        } catch (Camera.TagNotFoundException e) {
          telemetry.addLine("Tag not found.");
          robot.drive(xSpeed, 0, 0);
        }
      } else {
        aligning = false;
        robot.drive(xSpeed, 0, 0);
      }
      telemetry.addData("Current RPM", robot.shooter.getRPM());
      telemetry.addData("Shooter At Speed", robot.shooter.atSpeedRPM(RPM));
      telemetry.addData("Shooter Left RPM", robot.leftShooter.getRPM());
      telemetry.addData("Shooter Right RPM", robot.rightShooter.getRPM());
      telemetry.addData("Shooter Velocity", robot.shooter.getVelocity());
      telemetry.addData("Shooter Power", robot.shooter.getPower());
      telemetry.addLine();
      telemetry.addLine("");
      telemetry.addLine("Controls:");
      telemetry.addLine("D-Pad Up/Down: Increase/Decrease Target RPM by 50");
      telemetry.addLine("D-Pad Left/Right: Decrease/Increase Target Distance by 5 inches");
      telemetry.addLine("A: Toggle Shooter On/Off");
      telemetry.addLine("B: Toggle Auto-Align On/Off");
      telemetry.update();
    }
  }
}
