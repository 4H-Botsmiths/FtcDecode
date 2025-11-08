package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * PIDF Tuning Test OpMode
 * 
 * This OpMode helps you test and tune PIDF values for your drive motors.
 * It allows you to:
 * - Test motors at different target velocities
 * - See real-time velocity vs. target velocity
 * - Observe motor response and stability
 * 
 * HOW TO USE:
 * 1. Start with the recommended PIDF values in Robot.java
 * 2. Run this OpMode
 * 3. Use gamepad controls to test different speeds
 * 4. Watch telemetry to see how well motors track target speed
 * 5. Adjust PIDF values in Robot.java based on observations
 * 6. Repeat until motors respond smoothly and accurately
 * 
 * GAMEPAD CONTROLS:
 * - Left Stick Y: Set target speed for all motors
 * - A Button: Test at 50 RPM
 * - B Button: Test at 100 RPM
 * - X Button: Test at 150 RPM
 * - Y Button: Test at 200 RPM
 * - DPad Up: Increase test speed by 10 RPM
 * - DPad Down: Decrease test speed by 10 RPM
 * - Right Bumper: Stop all motors
 * 
 * WHAT TO LOOK FOR:
 * - "Velocity Error": Should be close to 0 when motor reaches target
 * - "% Error": Should be less than 5% for good control
 * - "Response Time": Time to reach within 5% of target (should be < 0.5 sec)
 * - Motors should not vibrate, oscillate, or make unusual sounds
 * 
 * TROUBLESHOOTING:
 * - If error is consistently positive: Increase F
 * - If error is consistently negative: Decrease F
 * - If motor oscillates: Decrease P or I
 * - If response is too slow: Increase P or F
 * - If motor overshoots: Increase D or decrease P
 */
@TeleOp(name = "PIDF Tuning Test", group = "Diagnostics")
public class PIDFTuningTest extends LinearOpMode {

  private Robot robot;
  private double targetRPM = 0;
  private long lastSpeedChangeTime = 0;
  private double lastTargetRPM = 0;

  @Override
  public void runOpMode() {
    // Initialize robot hardware
    robot = new Robot(hardwareMap);

    // Set motors to run using encoders for velocity control
    robot.frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    robot.frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    robot.rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    robot.rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

    robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    robot.rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    robot.rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    // Display current PIDF coefficients
    telemetry.addLine("=== PIDF Tuning Test ===");
    telemetry.addLine();
    telemetry.addLine("Current PIDF Coefficients:");

    PIDFCoefficients frontLeftPIDF = robot.frontLeft.getPIDFCoefficients();
    telemetry.addData("P", "%.6f", frontLeftPIDF.p);
    telemetry.addData("I", "%.6f", frontLeftPIDF.i);
    telemetry.addData("D", "%.6f", frontLeftPIDF.d);
    telemetry.addData("F", "%.6f", frontLeftPIDF.f);
    telemetry.addLine();
    telemetry.addLine("Press PLAY to start testing");
    telemetry.addLine();
    telemetry.addLine("See OpMode comments for controls");
    telemetry.update();

    waitForStart();

    lastSpeedChangeTime = System.currentTimeMillis();

    while (opModeIsActive()) {
      // Handle gamepad input
      handleGamepadInput();

      // Set motor velocities
      if (targetRPM != 0) {
        robot.frontLeft.setRPM(targetRPM);
        robot.frontRight.setRPM(targetRPM);
        robot.rearLeft.setRPM(targetRPM);
        robot.rearRight.setRPM(targetRPM);
      } else {
        robot.frontLeft.setPower(0);
        robot.frontRight.setPower(0);
        robot.rearLeft.setPower(0);
        robot.rearRight.setPower(0);
      }

      // Calculate and display telemetry
      displayTelemetry();

      sleep(50); // Update at ~20 Hz
    }

    // Stop motors when OpMode ends
    robot.frontLeft.setPower(0);
    robot.frontRight.setPower(0);
    robot.rearLeft.setPower(0);
    robot.rearRight.setPower(0);
  }

  private void handleGamepadInput() {
    // Preset speed buttons
    if (gamepad1.a) {
      setTargetRPM(50);
    } else if (gamepad1.b) {
      setTargetRPM(100);
    } else if (gamepad1.x) {
      setTargetRPM(150);
    } else if (gamepad1.y) {
      setTargetRPM(200);
    }
    // Joystick control (scaled to max 250 RPM)
    double stickInput = -gamepad1.left_stick_y;
    if (Math.abs(stickInput) > 0.1) {
      setTargetRPM(stickInput * 250);
    }

    // Fine adjustment
    if (gamepad1.dpad_up) {
      setTargetRPM(targetRPM + 10);
      sleep(200); // Debounce
    } else if (gamepad1.dpad_down) {
      setTargetRPM(targetRPM - 10);
      sleep(200); // Debounce
    }

    // Stop button
    if (gamepad1.right_bumper) {
      setTargetRPM(0);
    }

    if (gamepad1.start) {
      PIDFCoefficients drivePIDF = new PIDFCoefficients(
          0.015, // P - Proportional gain
          0.0003, // I - Integral gain
          0.0002, // D - Derivative gain
          0.0008 // F - Feedforward gain
      );

      // Apply PIDF to each drive motor
      // Note: Requires REV Expansion Hub/Control Hub with firmware 1.8.2 or higher
      robot.frontLeft.setPIDFCoefficients(drivePIDF);
      robot.frontRight.setPIDFCoefficients(drivePIDF);
      robot.rearLeft.setPIDFCoefficients(drivePIDF);
      robot.rearRight.setPIDFCoefficients(drivePIDF);

    }
  }

  private void setTargetRPM(double rpm) {
    rpm = Math.max(-Robot.DRIVE_MAX_RPM, Math.min(Robot.DRIVE_MAX_RPM, rpm));
    if (Math.abs(rpm - targetRPM) > 5) { // Only update if significant change
      lastSpeedChangeTime = System.currentTimeMillis();
      lastTargetRPM = targetRPM;
    }
    targetRPM = rpm;
  }

  private void displayTelemetry() {
    // Get current velocities
    double flRPM = robot.frontLeft.getRPM();
    double frRPM = robot.frontRight.getRPM();
    double rlRPM = robot.rearLeft.getRPM();
    double rrRPM = robot.rearRight.getRPM();

    // Calculate average and error
    double avgRPM = (flRPM + frRPM + rlRPM + rrRPM) / 4.0;
    double error = targetRPM - avgRPM;
    double percentError = (targetRPM != 0) ? (error / targetRPM) * 100.0 : 0;

    // Calculate time since speed change
    long timeSinceChange = System.currentTimeMillis() - lastSpeedChangeTime;
    double timeInSeconds = timeSinceChange / 1000.0;

    // Check if at target (within 5%)
    boolean atTarget = Math.abs(percentError) < 5.0 && targetRPM != 0;

    // Display header
    telemetry.addLine("=== PIDF Tuning Test ===");
    telemetry.addLine();

    // Display target and actual
    telemetry.addData("Target RPM", "%.1f", targetRPM);
    telemetry.addData("Average RPM", "%.1f", avgRPM);
    telemetry.addData("Velocity Error", "%.1f RPM", error);
    telemetry.addData("Percent Error", "%.1f%%", percentError);
    telemetry.addLine();

    // Display status indicator
    if (targetRPM == 0) {
      telemetry.addLine("Status: STOPPED");
    } else if (atTarget) {
      telemetry.addLine("Status: ✓ AT TARGET");
    } else if (timeInSeconds < 0.5) {
      telemetry.addLine("Status: ⟳ ACCELERATING");
    } else {
      telemetry.addLine("Status: ⚠ NOT AT TARGET");
    }
    telemetry.addData("Time Since Change", "%.2f sec", timeInSeconds);
    telemetry.addLine();

    // Display individual motor RPMs
    telemetry.addLine("Individual Motors:");
    telemetry.addData("  Front Left", "%.1f RPM (%.1f%% err)",
        flRPM, ((targetRPM - flRPM) / targetRPM) * 100.0);
    telemetry.addData("  Front Right", "%.1f RPM (%.1f%% err)",
        frRPM, ((targetRPM - frRPM) / targetRPM) * 100.0);
    telemetry.addData("  Rear Left", "%.1f RPM (%.1f%% err)",
        rlRPM, ((targetRPM - rlRPM) / targetRPM) * 100.0);
    telemetry.addData("  Rear Right", "%.1f RPM (%.1f%% err)",
        rrRPM, ((targetRPM - rrRPM) / targetRPM) * 100.0);
    telemetry.addLine();

    // Display current PIDF values
    PIDFCoefficients pidf = robot.frontLeft.getPIDFCoefficients();
    telemetry.addLine("Current PIDF:");
    telemetry.addData("  P", "%.6f", pidf.p);
    telemetry.addData("  I", "%.6f", pidf.i);
    telemetry.addData("  D", "%.6f", pidf.d);
    telemetry.addData("  F", "%.6f", pidf.f);
    telemetry.addLine();

    // Display controls
    telemetry.addLine("Controls:");
    telemetry.addLine("  A/B/X/Y: 50/100/150/200 RPM");
    telemetry.addLine("  DPad Up/Down: ±10 RPM");
    telemetry.addLine("  Left Stick: Variable speed");
    telemetry.addLine("  Right Bumper: Stop");

    telemetry.update();
  }
}