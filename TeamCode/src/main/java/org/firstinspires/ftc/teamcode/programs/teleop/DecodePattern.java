package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.Camera.AprilTagPosition;
import org.firstinspires.ftc.teamcode.hardware.Camera.CameraNotAttachedException;
import org.firstinspires.ftc.teamcode.hardware.Indexer;

@TeleOp(name = "Decode Pattern TeleOp", group = "A")
public class DecodePattern extends OpMode {
  /** Aggregate access to drivetrain and mechanisms. */
  public Robot robot;
  /** Camera/vision wrapper for AprilTag via VisionPortal. */
  public Camera camera;

  /** The Motif Pattern for the match (set by the autonomous program) */
  private Camera.OBELISK_MOTIF obeliskMotif = Camera.OBELISK_MOTIF.PURPLE_PURPLE_GREEN;

  /**
   * Init:
   * configure hardware --> configure camera --> load obelisk motif from blackboard
   */
  @Override
  public void init() {
    // Basic UI feedback while hardware is constructed
    telemetry.addData("Status", "Initializing");
    telemetry.update();

    // Instantiate our robot hardware abstraction and camera wrapper.
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
   * Init Loop:
   * log motif --> log AprilTag telemetry
   */
  @Override
  public void init_loop() {
    telemetry.addData("Status", "Initialized");

    if (gamepad2.left_bumper) {
      this.obeliskMotif = Camera.OBELISK_MOTIF.GREEN_PURPLE_PURPLE;
    } else if (gamepad2.right_bumper) {
      this.obeliskMotif = Camera.OBELISK_MOTIF.PURPLE_PURPLE_GREEN;
    } else if (gamepad2.back) {
      this.obeliskMotif = Camera.OBELISK_MOTIF.PURPLE_GREEN_PURPLE;
    }
    telemetry.addData("Obelisk Motif", obeliskMotif);

    camera.telemetryAprilTag(telemetry);
    telemetry.update();
  }

  /**
   * Start:
   * Pause the camera
   */
  @Override
  public void start() {
    try {
      //Pause the camera to save resources during active driving.
      camera.pause();
      //camera.visionPortal.stopLiveView(); - Enable to save a few resources
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.speak("WARNING: Camera not attached!");
    }
  }

  int classifiedArtifacts = 0;
  private int baseRPM = 2500;

  /**
   * Main control loop (called repeatedly during PLAY):
   * - Reads driver controls and applies alignment assist when requested
   * - Handles operator auto-shoot logic
   * - Publishes telemetry
   */
  @Override
  public void loop() {
    operatorLoop();
    cameraLoop();
    driverLoop();
    telemetries();
  }

  private boolean aPressed = false;
  private boolean bPressed = false;
  private boolean yPressed = false;
  private boolean dpadLeftPressed = false;
  private boolean dpadRightPressed = false;

  /**
   * Operator control loop:
   * - 'A' to increment classified artifacts
   * - 'B' to decrement classified artifacts
   * - 'Y' to reset classified artifacts to 0
   * - DPad Left/Right to adjust base shooter RPM
   * - Right Stick Y to control lift power
   */
  public void operatorLoop() {
    if (gamepad2.a && !aPressed) {
      classifiedArtifacts = Math.max(0, Math.min(9, classifiedArtifacts + 1));
      aPressed = true;
      telemetry.speak(String.valueOf(classifiedArtifacts));
    }
    if (!gamepad2.a) {
      aPressed = false;
    }

    if (gamepad2.b && !bPressed) {
      classifiedArtifacts = Math.max(0, Math.min(9, classifiedArtifacts - 1));
      bPressed = true;
      telemetry.speak(String.valueOf(classifiedArtifacts));
    }
    if (!gamepad2.b) {
      bPressed = false;
    }
    if (gamepad2.y && !yPressed) {
      classifiedArtifacts = 0;
      yPressed = true;
      telemetry.speak(String.valueOf(classifiedArtifacts));
    }
    if (!gamepad2.y) {
      yPressed = false;
    }

    if (gamepad2.dpad_left && !dpadLeftPressed) {
      baseRPM = Math.max(2000, baseRPM - 50);
      dpadLeftPressed = true;
    }
    if (!gamepad2.dpad_left) {
      dpadLeftPressed = false;
    }

    if (gamepad2.dpad_right && !dpadRightPressed) {
      baseRPM = Math.min(4000, baseRPM + 50);
      dpadRightPressed = true;
    }
    if (!gamepad2.dpad_right) {
      dpadRightPressed = false;
    }

    if (Math.abs(gamepad2.right_stick_y) > 0.2) {
      robot.lift.setPower(-gamepad2.right_stick_y);
    } else {
      robot.lift.setPower(0);
    }
  }

  private double xTolerance = 5;
  private boolean cameraActive = false;
  private boolean tagFound = false;
  private double tagX = 0;
  private double tagRange = 85;

  /**
   * Camera control loop:
   * - While RB is held, read GOAL AprilTag and update tagX and tagRange
   *   - If camera is paused or unavailable, try to resume streaming
   *   - If tag not found, keep last known tagX and tagRange
   * - While RB is not held, pause the camera to save resources
   *   - Reset tagX and tagRange to defaults
   */
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

  private boolean xReady = false;
  private boolean rangeReady = false;
  private boolean shooterReady = false;

  /**
   * Driver control loop:
   * - Standard mecanum drive from left/right sticks and triggers
   * - While LB is held, set drivetrain motors to BRAKE mode and load indexer
   *  - While LB is not held, set drivetrain motors to FLOAT mode
   * - While RB is held, apply alignment assist using tagX to adjust rotation (z)
   *   - Also adjust forward (y) based on tagRange to approach target range
   *   - Provide driver rumble until within xTolerance
   * - While RT is held, auto-shoot based on classified artifacts and vision data
   *   - Set shooter RPM based on tagRange
   *   - When aligned (x within tolerance), at speed, and in range, auto-feed balls
   */
  public void driverLoop() {
    boolean vibrate = false;
    double x = 0;
    double y = 0;
    double r = 0;
    if (gamepad1.left_bumper) {
      robot.statusLed.setGreen(true);
      robot.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
      robot.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
      robot.rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
      robot.rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
      if (!gamepad1.right_bumper && gamepad1.right_trigger < 0.5) {
        boolean loaded = robot.indexer.load();
        robot.statusLed.setRed(loaded);
        if (loaded) {
          vibrate = true;
        }
      } else {
        robot.statusLed.setRed(false);
      }
    } else {
      robot.statusLed.setGreen(false);
      robot.statusLed.setRed(true);
      robot.frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      robot.frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      robot.rearLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      robot.rearRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
      x = gamepad1.left_stick_x / 3;
      x *= 2;
      //x += gamepad1.right_trigger * (gamepad1.left_stick_x / 3);
      x -= gamepad1.left_trigger * (gamepad1.left_stick_x / 3);
      y = -gamepad1.left_stick_y / 3;
      y *= 2;
      //y += gamepad1.right_trigger * (-gamepad1.left_stick_y / 3);
      y -= gamepad1.left_trigger * (-gamepad1.left_stick_y / 3);
      r = gamepad1.right_stick_x / 3;
      r *= 2;
      //r += gamepad1.right_trigger * (gamepad1.right_stick_x / 3);
      r -= gamepad1.left_trigger * (gamepad1.right_stick_x / 3);
    }

    xReady = false;
    rangeReady = false;
    shooterReady = false;
    double shooterRpm = 0;
    if (gamepad1.right_bumper) {
      //-----------------------------------------Align-Assist-----------------------------------------
      // Align-assist: while RB is held, read the GOAL AprilTag and adjust rotation (z)
      // to center the tag. Also provide driver rumble until within tolerance.
      r += (tagX / 30) * (tagFound ? 0.5 : 0.2);
      y += tagRange < 50 ? -0.4 : 0;
      if (tagRange > 50) {
        rangeReady = true;
      }
      if (Math.abs(tagX) > xTolerance) {
        // Centered enough: mark alignment ready for operator auto-feed.
        xReady = true;
      }

      //-----------------------------------------Shooter-----------------------------------------
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
      if (robot.shooter.atSpeedRPM(shooterRpm)) {
        shooterReady = true;
      }

      //-----------------------------------------Feedback-----------------------------------------
      if (!rangeReady || !xReady || !shooterReady) {
        vibrate = true;
      }
    }
    robot.shooter.setRPM(shooterRpm);
    robot.drive(x, y, r);

    if (gamepad1.right_trigger > 0.5) {
      //-----------------------------------------Indexer-----------------------------------------
      int patternIndex = classifiedArtifacts % 3;
      Indexer.BallColor desiredColor = classifiedArtifacts < 9 ? obeliskMotif.getPattern()[patternIndex]
          : Indexer.BallColor.UNKNOWN;
      if (!robot.indexer.isShooting()) {
        boolean success = robot.indexer.setPosition(desiredColor, true);
        if (!success) {
          success = robot.indexer.setPosition(Indexer.BallColor.UNKNOWN, true);
          if (success) {
            telemetry.speak("Unknown ball color!");
          }
        }
      }
    } else if (gamepad1.right_bumper) {
      robot.intake.setPowerAll(0);
    } else {
      robot.indexer.reset();
    }

    //-----------------------------------------Rumble-----------------------------------------
    if (vibrate) {
      gamepad1.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
    } else {
      gamepad1.stopRumble();
    }
  }

  /**
   * Publish telemetry:
   * - Status
   * - Classified Artifacts
   * - Obelisk Motif
   * - Next Ball Color
   * - Target Shooter RPM
   * - Current Shooter RPM
   * - Tag Found
   * - Tag X and Range
   * - Drivetrain RPMs (per-wheel), shooter speed, and vision-derived alignment/shooting info
   * - Camera helper will add its own telemetry (detections, pose, etc.).
   */
  public void telemetries() {
    telemetry.addData("Status", "Running");
    telemetry.addData("Classified Artifacts", classifiedArtifacts);
    telemetry.addData("Obelisk Motif", obeliskMotif);
    telemetry.addData("Next Ball Color",
        classifiedArtifacts < 9 ? obeliskMotif.getPattern()[classifiedArtifacts % 3] : Indexer.BallColor.UNKNOWN);
    telemetry.addData("Balls in Indexer", "Left: %s | Top: %s | Right: %s",
        robot.indexer.getBallColor(Indexer.Position.LEFT), robot.indexer.getBallColor(Indexer.Position.TOP),
        robot.indexer.getBallColor(Indexer.Position.RIGHT));
    telemetry.addData("Shooting Ready", "Aligned: %s | In Range: %s | At Speed: %s",
        xReady ? "Yes" : "No", rangeReady ? "Yes" : "No", shooterReady ? "Yes" : "No");
    telemetry.addData("Base Shooter RPM", baseRPM);
    telemetry.addLine(String.format("Shooter RPM: %6.1f", robot.shooter.getRPM()));
    telemetry.addData("Tag Found", tagFound);
    telemetry.addLine(String.format("Tag X: (%6.1f) Tag Range: (%6.1f)", tagX, tagRange));
    //telemetry.addLine(String.format("Intake Power: (%6.1f)", robot.intake.getPowers()));
    telemetry.addLine(String.format("Indexer Position: (%s)", robot.indexer.getCurrentPosition()));
    // Drivetrain RPMs (per-wheel), shooter speed, and vision-derived alignment/shooting info
    telemetry.addLine(String.format("FL (%6.1f) (%6.1f) FR", robot.frontLeft.getRPM(), robot.frontRight.getRPM()));
    telemetry.addLine(String.format("RL (%6.1f) (%6.1f) RR", robot.rearLeft.getRPM(), robot.rearRight.getRPM()));
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
