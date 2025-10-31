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
    // Surface camera state so drivers can confirm streaming before start.
    telemetry.addData("Camera Status", camera.visionPortal.getCameraState().toString());
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
    // Driver control
    driverLoop();
    // Operator control
    operatorLoop();
    telemetries();

    if (!gamepad1.right_bumper && !gamepad2.right_bumper) {
      try {
        // Pause the camera to save resources during active driving.
        camera.pause();
      } catch (Camera.CameraNotAttachedException e) {
        telemetry.speak("WARNING: Camera not attached!");
      }
    }
  }

  double tagX = 0;
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
  double rangeTolerance = 10;
  double targetRange = 80;

  boolean driverAnnounced = false;
  boolean xReady = false;

  public void driverLoop() {
    // Build field-centric-ish inputs (x=strafe, y=forward, z=rotate) with trigger-based scaling.
    // The math below starts with 1/3 scaling, doubles to ~2/3 base speed, then adds/subtracts
    // additional portions based on RT/LT to allow the driver to fine-tune speed on the fly.
    double x = gamepad1.left_stick_x / 3;
    x *= 2;
    x += gamepad1.right_trigger * (gamepad1.left_stick_x / 3);
    x -= gamepad1.left_trigger * (gamepad1.left_stick_x / 3);
    double y = -gamepad1.left_stick_y / 3;
    y *= 2;
    y += gamepad1.right_trigger * (-gamepad1.left_stick_y / 3);
    y -= gamepad1.left_trigger * (-gamepad1.left_stick_y / 3);
    double z = gamepad1.right_stick_x / 3;
    z *= 2;
    z += gamepad1.right_trigger * (gamepad1.right_stick_x / 3);
    z -= gamepad1.left_trigger * (gamepad1.right_stick_x / 3);

    if (gamepad1.right_bumper) {
      // Align-assist: while RB is held, read the GOAL AprilTag and adjust rotation (z)
      // to center the tag. Also provide driver rumble until within tolerance.
      try {
        try {
          Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
          tagX = tag.ftcPose.x;
          tagRange = tag.ftcPose.range;
        } catch (Camera.CameraNotStreamingException e) {
          // If the camera is paused or briefly unavailable, try to resume streaming.
          camera.resume();
        } catch (Camera.TagNotFoundException e) {
          // For now, keep the tag coordinate that was last seen.
          //tagX = 0;
        }
      } catch (CameraNotAttachedException e) {
        telemetry.addLine("ERROR: CAMERA NOT ATTACHED!");
      }
      if (!driverAnnounced) {
        telemetry.speak("Driver Ready");
        driverAnnounced = true;
      }
      if (Math.abs(tagX) > xTolerance || Math.abs(targetRange - tagRange) > rangeTolerance) {
        // Outside tolerance: keep rotating toward center and rumble as feedback.
        xReady = false;
        gamepad1.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
        // Gain/clip: proportional correction from tag X offset, clipped to avoid overshoot.
        // NOTE: tagX currently in inches; tune gain accordingly if you convert units.
        z += Range.clip(tagX * -0.025, -0.15, 0.15);
        y += Range.clip((targetRange - tagRange) * 0.025, -0.15, 0.15);
      } else {
        // Centered enough: stop rumble and mark alignment ready for operator auto-feed.
        gamepad1.stopRumble();
        xReady = true;
      }
    } else {
      // Align-assist not active.
      tagX = 0;
      tagRange = 0;
      gamepad1.stopRumble();
    }
    // Reset so we re-announce the next time align-assist is entered.
    driverAnnounced = false;
    // Drive the robot with final x/y/z inputs.
    robot.drive(x, y, z);
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

  boolean operatorAnnounced = false;
  private final int SHOOTER_MAX_RPM = 3300;
  double tagRange = 0;

  public void operatorLoop() {
    // Default intake: pull game pieces in with LT (negative power indicates direction in this setup).
    double intakePower = -gamepad2.left_trigger;
    double shooterRpm = 0;
    if (gamepad2.right_bumper) {
      // Auto-shoot mode: derive target shooter RPM from tag range and gate intake until ready.
      if (!operatorAnnounced) {
        telemetry.speak("Operator Ready");
        operatorAnnounced = true;
      }
      operatorAnnounced = true;

      // Placeholder mapping from range (in) -> shooter RPM. Replace with calibrated function/table.
      shooterRpm = 3300;//tagRange > 0 ? 3300 : 0; //(tagRange / 100) * SHOOTER_MAX_RPM : SHOOTER_MAX_RPM; // TODO: MATH - calibrate mapping
      if (tagRange > 0 && robot.shooter.atSpeed(shooterRpm)) {
        // At speed: stop operator rumble.
        gamepad2.stopRumble();
        if (xReady) {
          // Only feed when we're both at speed AND driver align-assist has centered x.
          intakePower = gamepad2.right_trigger;
        }
      } else {
        // Not ready: notify operator via rumble (gamepad2).
        gamepad2.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
      }
    } else {
      // Exit auto-shoot: stop rumble feedback, pause camera to save processing, and reset state.
      operatorAnnounced = false;
      gamepad2.stopRumble();

    }
    /**
     * Publishes drivetrain RPMs, shooter RPM, tag alignment info, and mechanism powers.
     * Also appends camera-specific AprilTag telemetry (poses, detections).
     */
    // Apply mechanism outputs
    robot.intake.setPowerAll(intakePower);
    robot.shooter.setRPM(shooterRpm);
    robot.indexer.setPower(gamepad2.left_stick_x);
  }

  public void telemetries() {
    // Drivetrain RPMs (per-wheel), shooter speed, and vision-derived alignment/shooting info
    telemetry.addLine(String.format("FL (%6.1f) (%6.1f) FR", robot.frontLeft.getRPM(), robot.frontRight.getRPM()));
    telemetry.addLine(String.format("RL (%6.1f) (%6.1f) RR", robot.rearLeft.getRPM(), robot.rearRight.getRPM()));
    telemetry.addLine(String.format("Shooter RPM: (%6.1f)", robot.shooter.getRPM()));
    telemetry.addLine(String.format("Tag X: (%6.1f) Tag Range: (%6.1f)", tagX, tagRange));
    //telemetry.addLine(String.format("Intake Power: (%6.1f)", robot.intake.getPowers()));
    telemetry.addLine(String.format("Indexer Power: (%6.1f)", robot.indexer.getPower()));
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
    robot.intake.setPowerAll(0);
    robot.indexer.setPower(0);
  }

}
