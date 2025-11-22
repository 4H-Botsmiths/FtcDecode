package org.firstinspires.ftc.teamcode.programs.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Robot;

@Autonomous(name = "Back and Shoot", group = "A", preselectTeleOp = "Decode Camera TeleOp")
public class BackAndShoot extends OpMode {
  public Robot robot;
  public Camera camera;
  boolean goLeft = false;
  boolean goRight = false;

  // 
  /*
   * Code to run ONCE when the driver hits INIT
   */
  @Override
  public void init() {
    telemetry.addData("Status", "Initializing");
    telemetry.update();
    this.robot = new Robot(hardwareMap);
    this.camera = new Camera(hardwareMap);
    try {
      this.camera.initAprilTag();
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.speak("Camera not attached.");
    }
    telemetry.addData("Status", "Initialized");
    telemetry.update();
  }

  /*
   * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
   */
  @Override
  public void init_loop() {
    // Allow quick base RPM tuning via dpad during INIT
    telemetry.addData("Base RPM", baseRPM);
    if (gamepad1.dpad_up && !upPressed) {
      baseRPM += 100;
      upPressed = true;
    } else if (!gamepad1.dpad_up) {
      upPressed = false;
    }
    if (gamepad1.dpad_down && !downPressed) {
      baseRPM -= 100;
      downPressed = true;
    } else if (!gamepad1.dpad_down) {
      downPressed = false;
    }
    if (gamepad1.x) {
      goLeft = true;
      goRight = false;
    } else if (gamepad1.b) {
      goRight = true;
      goLeft = false;
    }
    telemetry.addData("Go Left", goLeft);
    telemetry.addData("Go Right", goRight);
    telemetry.update();
  }

  ElapsedTime timer = new ElapsedTime();

  /*
   * Code to run ONCE when the driver hits PLAY
   */
  @Override
  public void start() {
    timer.reset();
  }

  double range = 0;
  double x = 0;
  int baseRPM = 3000;
  boolean upPressed = false;
  boolean downPressed = false;
  double xTolerance = 5;

  /*
   * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
   */
  @Override
  public void loop() {
    if (timer.milliseconds() > 29000) {
      // Back away from wall
      if (goLeft) {
        robot.drive(-0.33, 0, 0);
      } else if (goRight) {
        robot.drive(0.33, 0, 0);
      } else {
        robot.drive(0, 0, 0);
      }
      return;
    }
    try {
      Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
      range = tag.ftcPose.range;
      x = tag.ftcPose.x;
      telemetry.addData("Range", range);
      telemetry.addData("X", x);
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.addData("Range", "Camera not attached");
    } catch (Camera.CameraNotStreamingException e) {
      telemetry.addData("Range", "Camera not streaming");
    } catch (Camera.TagNotFoundException e) {
      telemetry.addData("Range", "Tag not found");
    }
    if (range < 60) {
      robot.drive(0, -0.25, 0);
    } else {
      double turn = Range.clip(x / 30, -0.15, 0.15);
      boolean xReady = Math.abs(x) <= xTolerance;
      robot.drive(0, 0, turn);
      int shooterRpm;
      if (range < 60) {
        shooterRpm = baseRPM;
      } else if (range < 70) {
        shooterRpm = baseRPM - 100;
      } else if (range < 80) {
        shooterRpm = baseRPM - 200;
      } else if (range < 90) {
        shooterRpm = baseRPM - 50;
      } else {
        shooterRpm = baseRPM + 200;
      }
      robot.shooter.setRPM(shooterRpm);
      if (robot.shooter.atSpeedRPM(shooterRpm) && xReady) {
        robot.indexer.unknown();
        robot.intake.setPowerAll(1);
      } else {
        robot.intake.setPowerAll(0);

      }
    }
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
  }

}
