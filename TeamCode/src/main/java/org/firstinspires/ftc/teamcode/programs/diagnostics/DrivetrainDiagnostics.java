package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

@Autonomous(name = "Drivetrain Diagnostics", group = "Diagnostics")
public class DrivetrainDiagnostics extends LinearOpMode {

  public DcMotorEx frontLeft = null;
  public DcMotorEx frontRight = null;
  public DcMotorEx rearLeft = null;
  public DcMotorEx rearRight = null;

  public final int RUN_DURATION = 1000;
  public final int MINIMUM_VELOCITY = 100;
  /*
  Pseudo code:
    turn on one motor, check which motor reports moving more than a certain velocity
    repeat with each motor
  */

  @Override
  public void runOpMode() {
    telemetry.setAutoClear(false);
    Telemetry.Item statusItem = telemetry.addData("Status", "Initializing...");
    Telemetry.Item frontLeftItem = telemetry.addData("Front Left", "Waiting...");
    Telemetry.Item frontRightItem = telemetry.addData("Front Right", "Waiting...");
    Telemetry.Item rearLeftItem = telemetry.addData("Rear Left", "Waiting...");
    Telemetry.Item rearRightItem = telemetry.addData("Rear Right", "Waiting...");
    telemetry.update();
    frontLeft = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_2.getDeviceName());
    frontRight = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_3.getDeviceName());
    rearLeft = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_0.getDeviceName());
    rearRight = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_1.getDeviceName());
    statusItem.setValue("Initialized -  Raise Robot Off The Ground, Then Hit Start");
    telemetry.update();
    waitForStart();
    statusItem.setValue("Testing Motors...");
    evaluateMotor(frontLeft, frontLeftItem);
    evaluateMotor(frontRight, frontRightItem);
    evaluateMotor(rearLeft, rearLeftItem);
    evaluateMotor(rearRight, rearRightItem);
    resetMotors();
    statusItem.setValue("Done, See Below For Test Results");
    telemetry.update();
    while (opModeIsActive()) {
      sleep(100);
    }
  }

  public void resetMotors() {
    //Make sure that the encoders won't be used when calling setPower()
    frontLeft.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
    frontRight.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
    rearLeft.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
    rearRight.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
    //Set the motors to brake so that the last motor moved wont trigger a false active
    frontLeft.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    frontRight.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    rearLeft.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    rearRight.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
    //Stop all the motors
    frontLeft.setPower(0);
    frontRight.setPower(0);
    rearLeft.setPower(0);
    rearRight.setPower(0);
  }

  public DcMotorEx getActiveMotor() {
    if (frontLeft.getVelocity() > MINIMUM_VELOCITY) {
      return frontLeft;
    } else if (frontRight.getVelocity() > MINIMUM_VELOCITY) {
      return frontRight;
    } else if (rearLeft.getVelocity() > MINIMUM_VELOCITY) {
      return rearLeft;
    } else if (rearRight.getVelocity() > MINIMUM_VELOCITY) {
      return rearRight;
    } else {
      return null;
    }
  }

  public void evaluateMotor(DcMotorEx motor, Telemetry.Item telemetryItem) {
    telemetryItem.setValue("Testing...");
    telemetry.update();
    resetMotors();
    motor.setPower(1);
    sleep(RUN_DURATION);
    DcMotorEx activeMotor = getActiveMotor();
    if (activeMotor == null) {
      telemetryItem.setValue("Encoder Error");
    } else if (activeMotor != motor) {
      telemetryItem.setValue("Encoder Mismatch, Encoder Connected To Port %d, should be %d",
          activeMotor.getPortNumber(), motor.getPortNumber());
    } else {
      telemetryItem.setValue("Encoder Functioning (Velocity: %.0f)", motor.getVelocity());
    }
    telemetry.update();
  }
}
