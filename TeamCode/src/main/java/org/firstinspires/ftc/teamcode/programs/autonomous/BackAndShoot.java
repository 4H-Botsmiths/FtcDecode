package org.firstinspires.ftc.teamcode.programs.teleop;

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
  }

  /*
   * Code to run ONCE when the driver hits PLAY
   */
  @Override
  public void start() {
  }

  double range = 0;
  double x = 0;

  /*
   * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
   */
  @Override
  public void loop() {
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
    if (range < 85) {
      robot.drive(0, -0.25, 0);
    } else {
      robot.drive(0, 0, Range.clip(x * -0.025, -0.15, 0.15));
      robot.shooter.setRPM(3300);
      if (robot.shooter.atSpeedRPM(3300)) {
        robot.indexer.setPower(1);
      } else {
        robot.indexer.setPower(0);
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
