package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.Camera.AprilTagPosition;
import org.firstinspires.ftc.teamcode.hardware.Camera.CameraNotAttachedException;
import org.firstinspires.ftc.teamcode.hardware.Indexer;

@TeleOp(name = "Decode Pattern TeleOp", group = "A")
/**
 * TeleOp: Driver/Operator control loop with camera-assisted alignment and shooting.
 *
 * High-level overview
 * - Uses a {@link Robot} abstraction for drivetrain and mechanisms, and a {@link Camera} wrapper
 *   for AprilTag detection via the FTC VisionPortal.
 * - Driver (gamepad1): normal mecanum drive on sticks; hold Right Bumper to enable tag-aligned "assist"
 *   that slowly rotates the robot to center the goal AprilTag (x-axis only) and rumbles until aligned.
 * - Operator (gamepad2): manual intake with LT (reverse) by default; hold Right Bumper to enter
 *   auto-shoot mode which estimates shooter RPM from tag range and auto-feeds when at speed and aligned.
 *   While not ready to shoot, the operator's gamepad rumbles for feedback.
 * - Pre-start (INIT loop): shows camera state and AprilTag telemetry to verify detections before PLAY.
 *
 * Notes and assumptions
 * - This OpMode attempts to fetch the GOAL tag. If the camera isn't attached or streaming, we
 *   speak/telemetry warnings and try to resume streaming where appropriate to keep driving safe.
 * - Units: {@code tag.ftcPose.range} is in inches (FTC SDK). {@code tag.ftcPose.x} is also in inches
 *   left/right from the camera centerline, but this code treats {@code xTolerance} as "pixels".
 *   Consider calibrating to inches or computing a pixel offset from the detection center to avoid mismatch.
 * - Shooter RPM math is a placeholder (see TODO). Expect to replace with a calibrated mapping from range
 *   to velocity based on your launcher and game element aerodynamics.
 */
public class DecodePattern extends OpMode {
  /** Aggregate access to drivetrain and mechanisms. */
  public Robot robot;
  /** Camera/vision wrapper for AprilTag via VisionPortal. */
  public Camera camera;

  private Camera.OBELISK_MOTIF obeliskMotif = Camera.OBELISK_MOTIF.PURPLE_PURPLE_GREEN;

  // Lifecycle: init -> init_loop -> start -> loop (repeats) -> stop

  @Override
  public void init() {
    // Basic UI feedback while hardware is constructed
    telemetry.addData("Status", "Initializing");
    telemetry.update();

    // Instantiate our robot hardware abstraction and camera wrapper.
    // - Robot uses the hardwareMap to find motors/servos/sensors.
    // - Camera sets up the VisionPortal and AprilTag processors.
    this.robot = new Robot(hardwareMap);
    this.camera = new Camera(hardwareMap);
    try {
      // Prepare AprilTag pipeline. If the webcam isn't present, we'll catch and warn below.
      camera.initAprilTag();
    } catch (CameraNotAttachedException e) {
      telemetry.speak("WARNING: Camera not attached!");
    }
    if (blackboard.containsKey(Camera.OBELISK_STORAGE_KEY)) {
      obeliskMotif = Camera.OBELISK_MOTIF.fromId((int) blackboard.get(Camera.OBELISK_STORAGE_KEY));
    } else {
      telemetry.speak("Obelisk pattern not found in blackboard!");
    }
    telemetry.addData("Status", "Initialized");
    telemetry.update();
  }

  /**
   * Repeated during INIT (before PLAY):
   * - Displays the camera state
   * - Surfaces AprilTag telemetry for on-field alignment checks
   */
  @Override
  public void init_loop() {
    // Also show AprilTag telemetry while waiting on start so you can verify detections.
    camera.telemetryAprilTag(telemetry);
  }

  /**
   * One-time start hook. Currently no additional actions are required.
   */
  @Override
  public void start() {
    try {
      //Pause the camera to save resources during active driving.
      camera.pause();
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.speak("WARNING: Camera not attached!");
    }
  }

  int classifiedArtifacts = 3;
  private int baseRPM = 2500;

  /**
   * Main control loop (called repeatedly during PLAY):
   * - Reads driver controls and applies alignment assist when requested
   * - Handles operator auto-shoot logic
   * - Publishes telemetry
   */
  @Override
  public void loop() {
    telemetries();
    operatorLoop();
  }

  private boolean aPressed = false;
  private boolean bPressed = false;
  private boolean yPressed = false;

  public void operatorLoop() {
    if (gamepad2.a && !aPressed) {
      classifiedArtifacts = Math.max(0, Math.min(9, classifiedArtifacts + 1));
    }
    if (!gamepad2.a) {
      aPressed = false;
    }

    if (gamepad2.b && !bPressed) {
      classifiedArtifacts = Math.max(0, Math.min(9, classifiedArtifacts - 1));
    }
    if (!gamepad2.b) {
      bPressed = false;
    }
    if (gamepad2.y && !yPressed) {
      classifiedArtifacts = 0;
    }
    if (!gamepad2.y) {
      yPressed = false;
    }
  }

  private double xTolerance = 5;
  private double rangeTolerance = 5;
  private double targetRange = 85;
  private boolean cameraActive = false;
  private boolean tagFound = false;
  private double tagX = 0;
  private double tagRange = 85;

  public void cameraLoop() {
    if (gamepad1.right_bumper) {
      try {
        try {
          Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
          tagX = tag.ftcPose.x;
          tagRange = tag.ftcPose.range;
          tagFound = true;
          cameraActive = true;
        } catch (Camera.CameraNotStreamingException e) {
          // If the camera is paused or briefly unavailable, try to resume streaming.
          camera.resume();
        } catch (Camera.TagNotFoundException e) {
          // For now, keep the tag coordinate that was last seen.
          //tagX = 0;
          tagFound = false;
        }
      } catch (Camera.CameraNotAttachedException e) {
        telemetry.speak("WARNING: Camera not attached!");
      }
    } else {
      try {
        // Pause the camera to save resources during active driving.
        camera.pause();
        cameraActive = false;
        tagFound = false;
        tagX = 0;
        tagRange = 85;
      } catch (Camera.CameraNotAttachedException e) {
        telemetry.speak("WARNING: Camera not attached!");
      }
    }
  }

  public void driverLoop() {
    double x = 0;
    double y = 0;
    double r = 0;
    if (gamepad1.left_bumper) {
      robot.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
      robot.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
      robot.rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
      robot.rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    } else {
      robot.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      robot.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      robot.rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      robot.rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      x = gamepad1.left_stick_x / 3;
      x *= 2;
      x += gamepad1.right_trigger * (gamepad1.left_stick_x / 3);
      x -= gamepad1.left_trigger * (gamepad1.left_stick_x / 3);
      y = -gamepad1.left_stick_y / 3;
      y *= 2;
      y += gamepad1.right_trigger * (-gamepad1.left_stick_y / 3);
      y -= gamepad1.left_trigger * (-gamepad1.left_stick_y / 3);
      r = gamepad1.right_stick_x / 3;
      r *= 2;
      r += gamepad1.right_trigger * (gamepad1.right_stick_x / 3);
      r -= gamepad1.left_trigger * (gamepad1.right_stick_x / 3);
    }

    boolean xReady = false;
    boolean rangeReady = false;
    boolean shooterReady = false;
    if (gamepad1.right_bumper) {
      //-----------------------------------------Align-Assist-----------------------------------------
      // Align-assist: while RB is held, read the GOAL AprilTag and adjust rotation (z)
      // to center the tag. Also provide driver rumble until within tolerance.
      r += (tagX / 30) * (tagFound ? 0.33 : 0.15);
      y += tagRange < 50 ? -0.4 : 0;
      if (tagRange > 50) {
        rangeReady = true;
      }
      if (Math.abs(tagX) > xTolerance) {
        // Centered enough: mark alignment ready for operator auto-feed.
        xReady = true;
      }

      //-----------------------------------------Shooter-----------------------------------------
      double shooterRpm = 0;
      if (gamepad1.right_bumper) {
        if (tagRange < 60) {
          shooterRpm = baseRPM; //2500;
        } else if (tagRange < 70) {
          shooterRpm = baseRPM - 100; //2400;
        } else if (tagRange < 80) {
          shooterRpm = baseRPM - 200; //2300;
        } else if (tagRange < 90) {
          shooterRpm = baseRPM - 50; //2450;
        } else {
          shooterRpm = baseRPM + 200; //2700;
        }
      }
      robot.shooter.setRPM(shooterRpm);
      if (robot.shooter.atSpeedRPM(shooterRpm)) {
        shooterReady = true;
      }

      //-----------------------------------------Feedback-----------------------------------------
      if (!rangeReady || !xReady || !shooterReady) {
        gamepad1.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
      } else {
        gamepad1.stopRumble();
      }
    } else {
      gamepad1.stopRumble();
    }
    robot.drive(x, y, r);

    if (gamepad1.right_trigger > 0.5) {
      //-----------------------------------------Indexer-----------------------------------------
      int patternIndex = classifiedArtifacts % 3;
      Indexer.BallColor desiredColor = classifiedArtifacts < 9 ? obeliskMotif.getPattern()[patternIndex]
          : Indexer.BallColor.UNKNOWN;
      if (!robot.indexer.isShooting()) {
        robot.indexer.setPosition(desiredColor, true);
        classifiedArtifacts++;
      }
    } else if (gamepad1.right_bumper) {
      robot.intake.setPowerAll(0);
    } else {
      robot.indexer.load();
    }
  }

  public void telemetries() {
    // Drivetrain RPMs (per-wheel), shooter speed, and vision-derived alignment/shooting info
    telemetry.addLine(String.format("FL (%6.1f) (%6.1f) FR", robot.frontLeft.getRPM(), robot.frontRight.getRPM()));
    telemetry.addLine(String.format("RL (%6.1f) (%6.1f) RR", robot.rearLeft.getRPM(), robot.rearRight.getRPM()));
    telemetry.addData("Target Shooter RPM", baseRPM);
    telemetry.addLine(String.format("Shooter RPM: %6.1f", robot.shooter.getRPM()));
    telemetry.addData("Tag Found", tagFound);
    telemetry.addLine(String.format("Tag X: (%6.1f) Tag Range: (%6.1f)", tagX, tagRange));
    //telemetry.addLine(String.format("Intake Power: (%6.1f)", robot.intake.getPowers()));
    telemetry.addLine(String.format("Indexer Position: (%s)", robot.indexer.getCurrentPosition()));
    // Camera helper will add its own telemetry (detections, pose, etc.).
    camera.telemetryAprilTag(telemetry);
  }

  /**
   * Ensure all actuators are safely stopped at OpMode end.
   */
  @Override
  public void stop() {
    // Ensure all actuators are commanded to a safe idle state.
    robot.drive(0, 0, 0);
    robot.shooter.setRPM(0);
    //robot.intake.setPowerAll(0);
  }

}
