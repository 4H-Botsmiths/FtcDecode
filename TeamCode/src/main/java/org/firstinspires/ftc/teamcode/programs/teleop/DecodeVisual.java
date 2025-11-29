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

@TeleOp(name = "Decode Camera TeleOp", group = "A")
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
public class DecodeVisual extends OpMode {
  /** Aggregate access to drivetrain and mechanisms. */
  public Robot robot;
  /** Camera/vision wrapper for AprilTag via VisionPortal. */
  public Camera camera;

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

  /**
   * Main control loop (called repeatedly during PLAY):
   * - Reads driver controls and applies alignment assist when requested
   * - Handles operator auto-shoot logic
   * - Publishes telemetry
   */
  @Override
  public void loop() {
    cameraLoop();
    // Driver control
    driverLoop();
    // Operator control
    operatorLoop();
    telemetries();
  }

  private boolean cameraActive = false;
  private boolean tagFound = false;
  private double tagX = 0;
  private double tagRange = 85;

  public void cameraLoop() {
    // Currently no periodic camera actions needed; handled on-demand in driver/operator loops.
    if (gamepad1.right_bumper || gamepad2.right_bumper) {
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

  // Target alignment tolerance on the X-axis when aiming at the goal tag.
  // NOTE: ftcPose.x is in inches (left/right). This tolerance is labeled as "Pixels" but isn't in pixels.
  // Consider converting to inches or computing a pixel offset from detection center.
  /**
   * Driver controls (gamepad1)
   * Inputs:
   * - left_stick_x: strafe (x)
   * - left_stick_y: forward/back (y)
   * - right_stick_x: rotate (z)
   * - right_trigger / left_trigger: modify speed proportionally (fine grain control)
   * - right_bumper: enable AprilTag align-assist (rotational centering on x-axis)
   * Behavior:
   * - When align-assist is active, we read tag X offset and add a clipped proportional rotation
   *   term to drive z until within xTolerance. Gamepad rumbles while outside tolerance.
   */
  double xTolerance = 5;
  double rangeTolerance = 5;
  double targetRange = 85;

  boolean xReady = false;

  public void driverLoop() {
    // Build field-centric-ish inputs (x=strafe, y=forward, z=rotate) with trigger-based scaling.
    // The math below starts with 1/3 scaling, doubles to ~2/3 base speed, then adds/subtracts
    // additional portions based on RT/LT to allow the driver to fine-tune speed on the fly.
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

    if (gamepad1.right_bumper) {
      boolean rumble = true;
      // Align-assist: while RB is held, read the GOAL AprilTag and adjust rotation (z)
      // to center the tag. Also provide driver rumble until within tolerance.
      r += (tagX / 30) * (tagFound ? 0.66 : 0.33);
      y += tagRange < 50 ? -0.4 : 0;
      if (Math.abs(tagX) > xTolerance || tagRange < 50) {
        // Outside tolerance: keep rotating toward center and rumble as feedback.
        xReady = false;
        // Gain/clip: proportional correction from tag X offset, clipped to avoid overshoot.
        // NOTE: tagX currently in inches; tune gain accordingly if you convert units.
      } else {
        // Centered enough: stop rumble and mark alignment ready for operator auto-feed.
        gamepad1.stopRumble();
        xReady = true;
        if (tagFound) {
          rumble = false;
        }
      }
      if (rumble) {
        gamepad1.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
      } else {
        gamepad1.stopRumble();
      }
    } else {
      // Align-assist not active.
      xReady = false;
      gamepad1.stopRumble();
    }
    // Drive the robot with final x/y/z inputs.
    robot.drive(x, y, r);
  }

  /**
   * Operator controls (gamepad2)
   * Inputs:
   * - left_trigger: default intake (reverse pull)
   * - right_bumper: enter auto-shoot mode (uses AprilTag range to set shooter RPM)
   * - right_trigger (when ready): feed intake forward to shoot
   * - left_stick_x: indexer power
   * Behavior:
   * - In auto-shoot, estimate shooter RPM from tag range (placeholder mapping). Only feed when
   *   at speed AND driver align-assist has marked xReady. Operator gamepad rumbles while not ready.
   * - Camera streaming is resumed if needed and paused when exiting auto-shoot to save resources.
   */

  private int baseRPM = 2500;
  boolean upPressed = false;
  boolean downPressed = false;

  public void operatorLoop() {
    if (gamepad2.dpad_up && !upPressed) {
      baseRPM += 100;
      upPressed = true;
    } else if (!gamepad2.dpad_up) {
      upPressed = false;
    }
    if (gamepad2.dpad_down && !downPressed) {
      baseRPM -= 100;
      downPressed = true;
    } else if (!gamepad2.dpad_down) {
      downPressed = false;
    }
    // Default intake: pull game pieces in with LT (negative power indicates direction in this setup).
    double intakePower = gamepad2.right_trigger - gamepad2.left_trigger;
    double shooterRpm = 0;
    if (gamepad2.right_bumper) {
      // Auto-shoot mode: derive target shooter RPM from tag range and gate intake until ready.
      // Placeholder mapping from range (in) -> shooter RPM. Replace with calibrated function/table.
      if (tagRange < 60) {
        shooterRpm = baseRPM; //3000;
      } else if (tagRange < 70) {
        shooterRpm = baseRPM - 100; //2900;
      } else if (tagRange < 80) {
        shooterRpm = baseRPM - 200; //2800;
      } else if (tagRange < 90) {
        shooterRpm = baseRPM - 50; //2950;
      } else {
        shooterRpm = baseRPM + 200; //3200;
      }
      if (robot.shooter.atSpeedRPM(shooterRpm)) {
        // At speed: stop operator rumble.
        gamepad2.stopRumble();
        if (!xReady && !gamepad2.b) {
          // Only feed when we're both at speed AND driver align-assist has centered x.
          intakePower = 0;
        }
      } else {
        // Not ready: notify operator via rumble (gamepad2).
        gamepad2.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
      }
    } else {
      // Exit auto-shoot: stop rumble feedback, pause camera to save processing, and reset state.
      gamepad2.stopRumble();

    }
    /**
     * Publishes drivetrain RPMs, shooter RPM, tag alignment info, and mechanism powers.
     * Also appends camera-specific AprilTag telemetry (poses, detections).
     */
    // Apply mechanism outputs
    //robot.intake.setPowerAll(intakePower);
    robot.shooter.setRPM(shooterRpm);
    if (gamepad2.left_stick_x < -0.5) {
      robot.indexer.left();
    } else if (gamepad2.left_stick_x > 0.5) {
      robot.indexer.right();
    } else if (gamepad2.left_stick_y < -0.5) {
      robot.indexer.top();
    } else if (gamepad2.left_stick_y > 0.5) {
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
