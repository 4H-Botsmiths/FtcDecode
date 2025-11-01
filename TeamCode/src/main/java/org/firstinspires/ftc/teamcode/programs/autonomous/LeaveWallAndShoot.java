package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Robot;

@Autonomous(name = "Leave Wall and Shoot", group = "A", preselectTeleOp = "Decode Camera TeleOp")
public class LeaveWallAndShoot extends OpMode {
  public Robot robot;
  public Camera camera;

  private final ElapsedTime timer = new ElapsedTime();

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
    timer.reset();
  }

  /*
   * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
   */
  @Override
  public void loop() {
    if (timer.milliseconds() < 3000) {
      // Drive forward for the first ~2.5 seconds (no backing up)
      robot.drive(0, 0.25, 0);
      return;
    }
    if (timer.milliseconds() < 3500) {
      // Brief pause to stabilize
      robot.drive(0, 0, 0.25);
      return;
    }

    // After leaving the wall, hold position and align to AprilTag by turning only
    double turn = 0;
    try {
      Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
      double x = tag.ftcPose.x;
      double range = tag.ftcPose.range;
      telemetry.addData("Range", range);
      telemetry.addData("X", x);
      turn = Range.clip(x * -0.025, -0.15, 0.15);
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.addData("Camera", "Not attached");
    } catch (Camera.CameraNotStreamingException e) {
      telemetry.addData("Camera", "Not streaming");
    } catch (Camera.TagNotFoundException e) {
      telemetry.addData("Tag", "Not found");
      turn = 0; // If we don't see a tag, don't spin aimlessly
    }

    // Hold position, only rotate to align
    robot.drive(0, 0, turn);

    // Spin up and feed when at speed
    final int targetRpm = 3300;
    robot.shooter.setRPM(targetRpm);
    if (robot.shooter.atSpeedRPM(targetRpm)) {
      robot.indexer.setPower(1);
      robot.intake.setPowerAll(1);
    } else {
      robot.indexer.setPower(0);
      robot.intake.setPowerAll(0);
    }
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
  }

}