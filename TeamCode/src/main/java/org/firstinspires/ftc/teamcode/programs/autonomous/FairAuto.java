package org.firstinspires.ftc.teamcode.programs.autonomous;

import org.firstinspires.ftc.teamcode.hardware.Camera;
import org.firstinspires.ftc.teamcode.hardware.Camera.AprilTag;
import org.firstinspires.ftc.teamcode.hardware.Camera.AprilTagPosition;
import org.firstinspires.ftc.teamcode.hardware.Indexer;
import org.firstinspires.ftc.teamcode.hardware.Robot;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Autonomous(name = "Fair Auto")
public class FairAuto extends OpMode {
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
    camera();
    telemetries();
  }

  /*
   * Code to run ONCE when the driver hits PLAY
   */
  @Override
  public void start() {
    timer.reset();
  }

  double tagBearing = 0;
  double tagRange = 0;
  double tagY = 0;
  double computedYaw = 0;

  double tagX() {
    return tagY * Math.sin(Math.toRadians(computedYaw));
  }

  double lastTagTime = 0;
  boolean tagLost = true;
  Status status = Status.LOADING;

  double targetX = 0;
  double targetY = 0;

  final double fieldX = 12 * 5;
  final double fieldY = 12 * 10;

  boolean enableShooter = false;
  double loadingTime = 0;

  double shooterRpm() {
    return robot.shooter.calculateRPM(tagRange);
  }

  int ballsShot = 0;

  @Override
  public void loop() {
    telemetries();
    camera();

    double driveX = 0;
    double driveY = 0;
    final double driveR = (tagBearing / -30) * 0.5;

    switch (status) {
      case LOADING:
        boolean loaded = robot.indexer.load();
        ballsShot = 0;
        if (!loaded) {
          loadingTime = timer.seconds();
        }
        if (loaded && tagRange > 0 && timer.seconds() - loadingTime > 3) {
          status = Status.PICK_POSITION;
        }
        break;
      case PICK_POSITION:
        targetX = Math.random() * fieldX;
        targetY = Math.random() * (fieldY * 0.75) + (fieldY * 0.25);
        status = Status.MOVING;
        break;
      case MOVING:
        driveX = Range.clip(-(targetX - tagX()) * 0.025, -0.15, 0.15);
        driveY = Range.clip(-(targetY - tagY) * 0.025, -0.15, 0.15);

        if (Math.abs(targetX - tagX()) < 1 && Math.abs(targetY - tagY) < 1 && Math.abs(tagBearing) < 5) {
          status = Status.SHOOT;
          enableShooter = true;
        }
        break;
      case SHOOT:
        if (robot.shooter.atSpeedRPM(shooterRpm())) {
          robot.indexer.unknown();
          status = Status.SHOOTING;
        }
        break;
      case SHOOTING:
        driveX = Range.clip(-(targetX - tagX()) * 0.025, -0.15, 0.15);
        driveY = Range.clip(-(targetY - tagY) * 0.025, -0.15, 0.15);
        if (!robot.indexer.isShooting()) {
          ballsShot++;
          if (ballsShot >= 3) {
            status = Status.DONE;
          } else {
            status = Status.PICK_POSITION;
          }
        }
        break;
      case DONE:
        enableShooter = false;
        robot.intake.setPowerAll(0);
        driveX = Range.clip(-(fieldX - tagX()) * 0.025, -0.15, 0.15);
        driveY = Range.clip(-(0 - tagY) * 0.025, -0.15, 0.15);
        if (Math.abs(fieldX - tagX()) < 1 && Math.abs(0 - tagY) < 1 && Math.abs(tagBearing) < 5) {
          status = Status.LOADING;
        }
        break;
    }

    if (tagLost) {
      robot.drive(driveX * 0.5, driveY * 0.5, driveR * 0.5, Math.toRadians(computedYaw));
    } else {
      robot.drive(0, driveY, driveR, Math.toRadians(computedYaw));
    }
    if (enableShooter) {
      robot.shooter.setRPM(shooterRpm());
      robot.intake.setPowerAll(1);
    } else {
      robot.shooter.setRPM(0);
      robot.intake.setPowerAll(0);
    }
  }

  void camera() {
    try {
      AprilTag tag = camera.getAprilTag(AprilTagPosition.GOAL);
      lastTagTime = timer.seconds();
      tagLost = false;
      tagBearing = tag.targetPose.bearing;
      tagRange = tag.targetPose.range;
      tagY = tag.ftcPose.y;
      computedYaw = tag.ftcPose.yaw + 45;
    } catch (Camera.TagNotFoundException e) {
      if (timer.seconds() - lastTagTime > 0.5) {
        tagLost = true;
      }
    } catch (Camera.CameraNotAttachedException e) {
      telemetry.speak("Camera not Attached.");
      requestOpModeStop();
    } catch (Camera.CameraNotStreamingException e) {
      telemetry.addLine("Camera not streaming.");
    }
  }

  void telemetries() {
    telemetry.addData("Status", status);
    telemetry.addData("Balls in Indexer", "Left: %s | Top: %s | Right: %s",
        robot.indexer.getBallColor(Indexer.Position.LEFT), robot.indexer.getBallColor(Indexer.Position.TOP),
        robot.indexer.getBallColor(Indexer.Position.RIGHT));
    telemetry.addLine(String.format("FL (%6.1f) (%6.1f) FR", robot.frontLeft.getRPM(), robot.frontRight.getRPM()));
    telemetry.addLine(String.format("RL (%6.1f) (%6.1f) RR", robot.rearLeft.getRPM(), robot.rearRight.getRPM()));
    telemetry.addData("Target Shooter RPM", shooterRpm());
    telemetry.addData("Tag Range", tagRange);
    telemetry.addData("Tag Bearing", tagBearing);
    telemetry.addLine(String.format("Tag X %6.1f/%6.1f", tagX(), targetX));
    telemetry.addLine(String.format("Tag Y %6.1f/%6.1f", tagY, targetY));
    telemetry.addLine(String.format("Computed Yaw %6.1f", computedYaw));
    telemetry.addData("Tag Y", tagY);
    telemetry.addLine(String.format("Shooter RPM: (%6.1f)", robot.shooter.getRPM()));
    telemetry.addData("At Speed", robot.shooter.atSpeedRPM(shooterRpm()));
    telemetry.addData("Indexer Position", robot.indexer.getCurrentPosition());
    telemetry.addData("Intake Power", robot.intake.getPowers()[0]);
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
  }

  enum Status {
    LOADING,
    PICK_POSITION,
    MOVING,
    SHOOT,
    SHOOTING,
    DONE
  }
}