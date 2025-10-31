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
public class DecodeVisual extends OpMode {
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
      camera.initAprilTag();
    } catch (CameraNotAttachedException e) {
      telemetry.speak("WARNING: Camera not attached!");
    }
    telemetry.addData("Status", "Initialized");
    telemetry.update();
  }

  /*
   * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
   */
  @Override
  public void init_loop() {
    telemetry.addData("Camera Status", camera.visionPortal.getCameraState().toString());
  }

  /*
   * Code to run ONCE when the driver hits PLAY
   */
  @Override
  public void start() {
  }

  /*
   * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
   */
  @Override
  public void loop() {
    // Driver control
    driverLoop();
    // Operator control
    operatorLoop();
    telemetries();
  }

  double tagX = 0;
  double xTolerance = 10; //Pixels

  boolean driverAnnounced = false;
  boolean xReady = false;

  public void driverLoop() {
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
      try {
        try {
          Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
          tagX = tag.ftcPose.x;
        } catch (Camera.CameraNotStreamingException e) {
          camera.resume();
        }
      } catch (CameraNotAttachedException e) {
        telemetry.addLine("ERROR: CAMERA NOT ATTACHED!");
      }
      if (!driverAnnounced) {
        telemetry.speak("Driver Ready");
        driverAnnounced = true;
      }
      if (Math.abs(tagX) > xTolerance) {
        xReady = false;
        gamepad1.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
        z += Range.clip(tagX * 0.05, -0.15, 0.15);
      } else {
        gamepad1.stopRumble();
        xReady = true;
      }
    } else {
      tagX = 0;
      gamepad1.stopRumble();
    }
    driverAnnounced = false;
    robot.drive(x, y, z);
  }

  boolean operatorAnnounced = false;
  private final int SHOOTER_MAX_RPM = 3000;
  double tagRange = 0;

  public void operatorLoop() {
    double intakePower = -gamepad2.left_trigger;
    if (gamepad2.right_bumper) {
      try {
        try {
          Camera.AprilTag tag = camera.getAprilTag(Camera.AprilTagPosition.GOAL);
          tagRange = tag.ftcPose.range;
        } catch (Camera.CameraNotStreamingException e) {
          camera.resume();
        }
      } catch (CameraNotAttachedException e) {
        telemetry.addLine("ERROR: CAMERA NOT ATTACHED!");
      }
      if (!operatorAnnounced) {
        telemetry.speak("Operator Ready");
        operatorAnnounced = true;
      }
      operatorAnnounced = true;

      double shooterRpm = tagRange > 0 ? (tagRange / 100) * SHOOTER_MAX_RPM : SHOOTER_MAX_RPM; //TODO: MATH - THIS IS NOT RIGHT
      robot.shooter.setRPM(shooterRpm);
      if (tagRange > 0 && robot.shooter.atSpeed(shooterRpm)) {
        gamepad1.stopRumble();
        if (xReady) {
          intakePower = gamepad2.right_trigger;
        }
      } else {
        gamepad1.rumble(1, 1, Gamepad.RUMBLE_DURATION_CONTINUOUS);
      }
    } else {
      tagRange = 0;
      operatorAnnounced = false;
      gamepad1.stopRumble();
      try {
        camera.pause();
      } catch (Camera.CameraNotAttachedException e) {
        telemetry.addLine("WARNING: CAMERA NOT ATTACHED!");
      }
    }
    robot.intake.setPowerAll(intakePower);
    robot.indexer.setPower(gamepad2.left_stick_x);
  }

  public void telemetries() {
    telemetry.addLine(String.format("FL (%6.1f) (%6.1f) FR", robot.frontLeft.getRPM(), robot.frontRight.getRPM()));
    telemetry.addLine(String.format("RL (%6.1f) (%6.1f) RR", robot.rearLeft.getRPM(), robot.rearRight.getRPM()));
    telemetry.addLine(String.format("Shooter RPM: (%6.1f)", robot.shooter.getRPM()));
    telemetry.addLine(String.format("Tag X: (%6.1f) Tag Range: (%6.1f)", tagX, tagRange));
    telemetry.addLine(String.format("Intake Power: (%6.1f)", robot.intake.getPowers()));
    telemetry.addLine(String.format("Indexer Power: (%6.1f)", robot.indexer.getPower()));
    camera.telemetryAprilTag(telemetry);
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
    robot.drive(0, 0, 0);
    robot.shooter.setRPM(0);
    robot.intake.setPowerAll(0);
    robot.indexer.setPower(0);
  }

}
