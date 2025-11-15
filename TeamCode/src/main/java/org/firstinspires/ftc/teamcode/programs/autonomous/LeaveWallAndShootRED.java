package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Robot;

@Autonomous(name = "Leave Wall and Shoot RED", group = "A", preselectTeleOp = "Decode Camera TeleOp")
public class LeaveWallAndShootRED extends OpMode {
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
    telemetry.addLine("Load the purple balls on the left and the green on the right");
    telemetry.update();
  }

  /*
   * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
   */
  @Override
  public void init_loop() {
    try {
      Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.OBELISK);
      indexerClockwise = tag.id != 23;
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.addData("Camera", "Not attached");
    } catch (Camera.CameraNotStreamingException e) {
      telemetry.addData("Camera", "Not streaming");
    } catch (Camera.TagNotFoundException e) {
      telemetry.addData("Obelisk Tag", "Not found");
    }
    telemetries();
  }

  /*
   * Code to run ONCE when the driver hits PLAY
   */
  @Override
  public void start() {
    timer.reset();
  }

  boolean indexerClockwise = true;

  /*
   * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
   */
  // After leaving the wall, hold position and align to AprilTag by turning only
  double turn = 0;

  @Override
  public void loop() {
    telemetries();
    if (timer.milliseconds() < 4000) {
      // Drive forward for the first ~2.5 seconds (no backing up)
      robot.drive(0, 0.25, 0);
      return;
    }
    if (timer.milliseconds() < 4500) {
      // Brief pause to stabilize
      robot.drive(0, 0, 0.25);
      return;
    }

    try {
      Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
      double x = tag.ftcPose.x;
      telemetry.addData("X", x);
      turn = Range.clip(x * 0.025, -0.15, 0.15);
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.addData("Camera", "Not attached");
    } catch (Camera.CameraNotStreamingException e) {
      telemetry.addData("Camera", "Not streaming");
    } catch (Camera.TagNotFoundException e) {
      telemetry.addData("Goal Tag", "Not found");
      //turn = 0; // If we don't see a tag, don't spin aimlessly
    }

    // Hold position, only rotate to align
    robot.drive(0, 0, turn);

    // Spin up and feed when at speed
    final int targetRpm = 3000;
    robot.shooter.setRPM(targetRpm);
    if (robot.shooter.atSpeedRPM(targetRpm)) {
      robot.indexer.setPower(indexerClockwise ? 0.1 : -0.1);
      robot.intake.setPowerAll(1);
    } else {
      robot.indexer.setPower(0);
      robot.intake.setPowerAll(0);
    }
  }

  void telemetries() {
    telemetry.addData("Indexer Direction", indexerClockwise ? "Clockwise" : "Counter-Clockwise");
    telemetry.addLine(String.format("FL (%6.1f) (%6.1f) FR", robot.frontLeft.getRPM(), robot.frontRight.getRPM()));
    telemetry.addLine(String.format("RL (%6.1f) (%6.1f) RR", robot.rearLeft.getRPM(), robot.rearRight.getRPM()));
    telemetry.addLine(String.format("Shooter RPM: (%6.1f)", robot.shooter.getRPM()));
    telemetry.addData("At Speed", robot.shooter.atSpeedRPM(3000));
    telemetry.addData("Indexer Power", robot.indexer.getPower());
    telemetry.addData("Intake Power", robot.intake.getPowers()[0]);
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
  }

}