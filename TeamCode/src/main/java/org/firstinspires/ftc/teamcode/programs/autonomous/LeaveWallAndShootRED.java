package org.firstinspires.ftc.teamcode.programs.autonomous;

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
  private int baseRPM = 3000;
  private int shooterRpm = 0;
  private double tagRange = 85;
  private double tagX = 0;
  private boolean upPressed = false;
  private boolean downPressed = false;
  private double xTolerance = 5;
  Camera.OBELISK_MOTIF obeliskMotif = Camera.OBELISK_MOTIF.PURPLE_PURPLE_GREEN;
  int patternIndex = 0;

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
      obeliskMotif = tag.obeliskMotif;
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.addData("Camera", "Not attached");
    } catch (Camera.CameraNotStreamingException e) {
      telemetry.addData("Camera", "Not streaming");
    } catch (Camera.TagNotFoundException e) {
      telemetry.addData("Obelisk Tag", "Not found");
    }
    // Base RPM tuning during INIT via dpad
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
    telemetries();
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
    if (timer.milliseconds() > 29000) {
      robot.drive(0.33, 0, 0);
      return;
    }

    try {
      Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
      double x = tag.ftcPose.x;
      tagX = x;
      tagRange = tag.ftcPose.range;
      telemetry.addData("X", x);
      turn = Range.clip(x / 30, -0.15, 0.15);
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

    // Spin up and feed when at speed using range-based RPM
    if (tagRange < 60) {
      shooterRpm = baseRPM;
    } else if (tagRange < 70) {
      shooterRpm = baseRPM - 100;
    } else if (tagRange < 80) {
      shooterRpm = baseRPM - 200;
    } else if (tagRange < 90) {
      shooterRpm = baseRPM - 50;
    } else {
      shooterRpm = baseRPM + 200;
    }
    robot.shooter.setRPM(shooterRpm);
    boolean xReady = Math.abs(tagX) <= xTolerance;
    if (robot.shooter.atSpeedRPM(shooterRpm) && xReady && patternIndex < obeliskMotif.getPattern().length) {
      if (!robot.indexer.isBlocked()) {
        robot.indexer.setPosition(obeliskMotif.getPattern()[patternIndex], true);
        patternIndex++;
      }
      robot.intake.setPowerAll(1);
    } else {
      robot.intake.setPowerAll(0);
    }
  }

  void telemetries() {
    telemetry.addData("Obelisk Motif", obeliskMotif.toString());
    telemetry.addLine(String.format("FL (%6.1f) (%6.1f) FR", robot.frontLeft.getRPM(), robot.frontRight.getRPM()));
    telemetry.addLine(String.format("RL (%6.1f) (%6.1f) RR", robot.rearLeft.getRPM(), robot.rearRight.getRPM()));
    telemetry.addData("Target Shooter RPM", shooterRpm);
    telemetry.addData("Tag Range", tagRange);
    telemetry.addLine(String.format("Shooter RPM: (%6.1f)", robot.shooter.getRPM()));
    telemetry.addData("At Speed", robot.shooter.atSpeedRPM(shooterRpm));
    telemetry.addData("Indexer Position", robot.indexer.getCurrentPosition());
    telemetry.addData("Intake Power", robot.intake.getPowers()[0]);
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
  }

}