package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Shooter PIDF Tuning Test OpMode
 * 
 * This OpMode helps you test and tune PIDF values specifically for your shooter motors.
 * Shooters need more aggressive tuning than drive motors because they must:
 * - Handle load resistance from balls
 * - Spin up quickly without overshoot
 * - Maintain consistent speed shot-to-shot
 * 
 * HOW TO USE:
 * 1. Start with recommended PIDF values in Robot.java (shooter section)
 * 2. Run this OpMode
 * 3. Use gamepad controls to test spin-up and load response
 * 4. Watch telemetry for speed consistency and recovery
 * 5. Adjust PIDF values in Robot.java based on observations
 * 6. Re-run until performance meets targets
 * 
 * GAMEPAD CONTROLS:
 * - A Button: Start/Stop shooter at 3000 RPM
 * - B Button: Quick spin-up test (measures acceleration time)
 * - X Button: Continuous run test (monitors consistency)
 * - Y Button: Emergency stop (stops shooter and intake)
 * - DPad Up: Increase target RPM by 100
 * - DPad Down: Decrease target RPM by 100
 * - Left Bumper: Feed ball test (runs intake, measures real load recovery)
 * - Right Bumper: Reset statistics
 * 
 * WHAT TO LOOK FOR:
 * - "Spin-up Time": Should be 0.3-0.5 seconds to reach 3000 RPM
 * - "Overshoot": Should be < 5% (< 150 RPM above target)
 * - "Steady Speed": Should maintain ±50 RPM at 3000 RPM
 * - "Recovery Time": < 0.2 seconds after ball shot (real measurement)
 * - "L/R Difference": Should be < 50 RPM
 * - No oscillation or vibration
 * 
 * TROUBLESHOOTING:
 * - Slow spin-up (> 0.7s): Increase P by 30%
 * - Speed drops with load: Increase P by 25% or I by 30%
 * - Overshoots and oscillates: Increase D by 50%
 * - Inconsistent speed: Increase I by 30%
 * - One motor slower: Check wiring, may need individual tuning
 */
@TeleOp(name = "Shooter PIDF Tuning Test", group = "Diagnostics")
public class ShooterPIDFTuningTest extends LinearOpMode {

    private Robot robot;
    private double targetRPM = 3000;
    private boolean shooterRunning = false;

    // Timing and statistics
    private ElapsedTime spinUpTimer = new ElapsedTime();
    private ElapsedTime recoveryTimer = new ElapsedTime();
    private double spinUpTime = 0;
    private double peakRPM = 0;
    private double minRPM = 99999;
    private double maxRPM = 0;
    private int samplesAtSpeed = 0;
    private boolean measuringSpinUp = false;
    private boolean measuringRecovery = false;
    private double recoveryTime = 0;

    // Real ball feed test (intake control)
    private boolean intakeRunning = false;
    private boolean ballDetected = false;
    private double preLoadRPM = 0;
    private double minRPMDuringLoad = 99999;

    @Override
    public void runOpMode() {
        // Initialize robot hardware
        robot = new Robot(hardwareMap);

        // Set shooter motors to run using encoders
        robot.leftShooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rightShooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Display current PIDF coefficients
        telemetry.addLine("=== Shooter PIDF Tuning Test ===");
        telemetry.addLine();
        telemetry.addLine("Current Shooter PIDF Coefficients:");

        PIDFCoefficients leftPIDF = robot.leftShooter.getPIDFCoefficients();
        telemetry.addData("P", "%.6f", leftPIDF.p);
        telemetry.addData("I", "%.6f", leftPIDF.i);
        telemetry.addData("D", "%.6f", leftPIDF.d);
        telemetry.addData("F", "%.6f", leftPIDF.f);
        telemetry.addLine();
        telemetry.addLine("Press PLAY to start testing");
        telemetry.addLine();
        telemetry.addLine("Target: 3000 RPM for consistency");
        telemetry.addLine("See OpMode comments for controls");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Handle gamepad input
            handleGamepadInput();

            // Control shooter motors
            if (shooterRunning) {
                robot.shooter.setRPM(targetRPM);
            } else {
                robot.shooter.setPower(0);
            }

            // Handle real ball feed test
            handleBallFeedTest();

            // Update statistics
            updateStatistics();

            // Display telemetry
            displayTelemetry();

            sleep(20); // Update at ~50 Hz for smooth monitoring
        }

        // Stop shooter and intake when OpMode ends
        robot.shooter.setPower(0);
        robot.intake.stopAll();
    }

    private void handleGamepadInput() {
        // Toggle shooter on/off
        if (gamepad1.a && !shooterRunning) {
            shooterRunning = true;
            startSpinUpMeasurement();
        } else if (gamepad1.y) {
            shooterRunning = false;
            intakeRunning = false;
            robot.intake.stopAll();
            resetStatistics();
        }

        // Quick spin-up test
        if (gamepad1.b) {
            shooterRunning = true;
            startSpinUpMeasurement();
            sleep(200); // Debounce
        }

        // Continuous run test
        if (gamepad1.x) {
            shooterRunning = true;
            resetStatistics();
            sleep(200); // Debounce
        }

        // Adjust target RPM
        if (gamepad1.dpad_up) {
            targetRPM = Math.min(4000, targetRPM + 100);
            sleep(200); // Debounce
        } else if (gamepad1.dpad_down) {
            targetRPM = Math.max(1000, targetRPM - 100);
            sleep(200); // Debounce
        }

        // Real ball feed test - runs intake to feed balls
        if (gamepad1.left_bumper && shooterRunning && !intakeRunning) {
            startBallFeedTest();
            sleep(200); // Debounce
        }

        // Reset statistics
        if (gamepad1.right_bumper) {
            resetStatistics();
            sleep(200); // Debounce
        }

        if (gamepad1.start) {
            PIDFCoefficients shooterPIDF = new PIDFCoefficients(
                    0.040, // P - High for quick load response
                    0.0015, // I - Strong for consistent performance
                    0.0008, // D - Moderate to prevent overshoot
                    0.0007 // F - Feedforward for 3000 RPM baseline
            );
            robot.leftShooter.setPIDFCoefficients(shooterPIDF);
            robot.rightShooter.setPIDFCoefficients(shooterPIDF);
        }
    }

    private void handleBallFeedTest() {
        if (intakeRunning) {
            double currentRPM = robot.shooter.getRPM();

            // Track minimum RPM during load
            if (currentRPM < minRPMDuringLoad) {
                minRPMDuringLoad = currentRPM;
            }

            // Detect when ball hits shooter (velocity drop)
            if (!ballDetected && currentRPM < preLoadRPM * 0.85) {
                // Ball just made contact - significant velocity drop detected
                ballDetected = true;
                measuringRecovery = true;
                recoveryTimer.reset();
            }

            // Auto-stop intake after ball is shot (velocity recovers)
            if (ballDetected && currentRPM >= targetRPM * 0.95) {
                // Ball has been shot and shooter recovered to target speed
                robot.intake.stopAll();
                intakeRunning = false;
                measuringRecovery = false;
                recoveryTime = recoveryTimer.seconds();
            }

            // Safety timeout - stop intake after 3 seconds
            if (recoveryTimer.seconds() > 3.0) {
                robot.intake.stopAll();
                intakeRunning = false;
                measuringRecovery = false;
            }
        }
    }

    private void startBallFeedTest() {
        // Start intake to feed a ball
        robot.intake.setPowerAll(1.0);
        intakeRunning = true;
        ballDetected = false;
        preLoadRPM = robot.shooter.getRPM();
        minRPMDuringLoad = preLoadRPM;
        recoveryTimer.reset();
    }

    private void startSpinUpMeasurement() {
        measuringSpinUp = true;
        spinUpTimer.reset();
        peakRPM = 0;
        resetStatistics();
    }

    private void updateStatistics() {
        double currentRPM = robot.shooter.getRPM();

        // Track spin-up time
        if (measuringSpinUp && shooterRunning) {
            if (currentRPM >= targetRPM * 0.95) {
                spinUpTime = spinUpTimer.seconds();
                measuringSpinUp = false;
            }
            // Track peak (overshoot)
            if (currentRPM > peakRPM) {
                peakRPM = currentRPM;
            }
        }

        // Note: Recovery time is now tracked in handleBallFeedTest() when ball is actually shot

        // Track steady-state statistics (only when at speed and not feeding balls)
        if (shooterRunning && !measuringSpinUp && !intakeRunning) {
            if (currentRPM >= targetRPM * 0.90 && currentRPM <= targetRPM * 1.10) {
                samplesAtSpeed++;
                if (currentRPM > maxRPM)
                    maxRPM = currentRPM;
                if (currentRPM < minRPM)
                    minRPM = currentRPM;
            }
        }
    }

    private void resetStatistics() {
        spinUpTime = 0;
        peakRPM = 0;
        minRPM = 99999;
        maxRPM = 0;
        samplesAtSpeed = 0;
        measuringSpinUp = false;
        measuringRecovery = false;
        recoveryTime = 0;
        intakeRunning = false;
        ballDetected = false;
        minRPMDuringLoad = 99999;
        robot.intake.stopAll();
    }

    private void displayTelemetry() {
        double leftRPM = robot.leftShooter.getRPM();
        double rightRPM = robot.rightShooter.getRPM();
        double avgRPM = (leftRPM + rightRPM) / 2.0;
        double rpmDifference = Math.abs(leftRPM - rightRPM);
        double error = targetRPM - avgRPM;
        double percentError = (targetRPM != 0) ? (error / targetRPM) * 100.0 : 0;

        // Calculate overshoot
        double overshoot = peakRPM - targetRPM;
        double overshootPercent = (targetRPM != 0) ? (overshoot / targetRPM) * 100.0 : 0;

        // Calculate consistency (range at steady state)
        double rpmRange = (samplesAtSpeed > 10) ? (maxRPM - minRPM) : 0;

        // Display header
        telemetry.addLine("=== Shooter PIDF Tuning Test ===");
        telemetry.addLine();

        // Display status
        if (!shooterRunning) {
            telemetry.addLine("Status: ⏸ STOPPED");
        } else if (measuringSpinUp) {
            telemetry.addLine("Status: ⟳ SPINNING UP...");
            telemetry.addData("  Time", "%.2f sec", spinUpTimer.seconds());
        } else if (intakeRunning && !ballDetected) {
            telemetry.addLine("Status: 🟢 INTAKE ON - Waiting for ball...");
            telemetry.addData("  Intake Power", "%.1f", robot.intake.getPowers()[0]);
        } else if (intakeRunning && ballDetected) {
            telemetry.addLine("Status: 🔴 BALL DETECTED - Shooting...");
            telemetry.addData("  Recovery Time", "%.2f sec", recoveryTimer.seconds());
        } else if (Math.abs(percentError) < 3.0) {
            telemetry.addLine("Status: ✓ AT TARGET");
        } else {
            telemetry.addLine("Status: ⚠ NOT AT TARGET");
        }
        telemetry.addLine();

        // Display current speed
        telemetry.addData("Target RPM", "%.0f", targetRPM);
        telemetry.addData("Current RPM", "%.0f (%.1f%% error)", avgRPM, percentError);
        telemetry.addData("Left Motor", "%.0f RPM", leftRPM);
        telemetry.addData("Right Motor", "%.0f RPM", rightRPM);
        telemetry.addData("L/R Difference", "%.0f RPM %s", rpmDifference,
                rpmDifference > 50 ? "⚠ HIGH" : "✓");
        telemetry.addLine();

        // Display performance metrics
        telemetry.addLine("Performance Metrics:");
        if (spinUpTime > 0) {
            String spinUpStatus = spinUpTime < 0.5 ? "✓ Excellent" : spinUpTime < 0.7 ? "⚠ Acceptable" : "✗ Too Slow";
            telemetry.addData("  Spin-up Time", "%.2f sec %s", spinUpTime, spinUpStatus);
        } else {
            telemetry.addLine("  Spin-up Time: Not measured");
        }

        if (peakRPM > targetRPM) {
            String overshootStatus = overshootPercent < 5 ? "✓ Excellent"
                    : overshootPercent < 10 ? "⚠ Acceptable" : "✗ Too High";
            telemetry.addData("  Overshoot", "%.0f RPM (%.1f%%) %s",
                    overshoot, overshootPercent, overshootStatus);
        } else {
            telemetry.addLine("  Overshoot: None detected");
        }

        if (samplesAtSpeed > 10) {
            String consistencyStatus = rpmRange < 60 ? "✓ Excellent"
                    : rpmRange < 100 ? "⚠ Acceptable" : "✗ Inconsistent";
            telemetry.addData("  Consistency", "±%.0f RPM range %s", rpmRange / 2, consistencyStatus);
            telemetry.addData("  Range", "%.0f - %.0f RPM", minRPM, maxRPM);
        } else {
            telemetry.addLine("  Consistency: Need more samples");
        }

        // Display real ball feed test results
        if (recoveryTime > 0) {
            String recoveryStatus = recoveryTime < 0.2 ? "✓ Excellent"
                    : recoveryTime < 0.4 ? "⚠ Acceptable" : "✗ Too Slow";
            telemetry.addData("  Recovery Time", "%.2f sec %s", recoveryTime, recoveryStatus);
            double rpmDrop = preLoadRPM - minRPMDuringLoad;
            double dropPercent = (preLoadRPM != 0) ? (rpmDrop / preLoadRPM) * 100.0 : 0;
            telemetry.addData("  Speed Drop", "%.0f RPM (%.1f%%)", rpmDrop, dropPercent);
        } else if (intakeRunning) {
            telemetry.addLine("  Recovery: Measuring real ball shot...");
        } else {
            telemetry.addLine("  Recovery: Press Left Bumper to test");
        }
        telemetry.addLine();

        // Display current PIDF values
        PIDFCoefficients pidf = robot.leftShooter.getPIDFCoefficients();
        telemetry.addLine("Current PIDF:");
        telemetry.addData("  P", "%.6f", pidf.p);
        telemetry.addData("  I", "%.6f", pidf.i);
        telemetry.addData("  D", "%.6f", pidf.d);
        telemetry.addData("  F", "%.6f", pidf.f);
        telemetry.addLine();

        // Display targets
        telemetry.addLine("Performance Targets:");
        telemetry.addLine("  Spin-up: < 0.5 sec (Excellent) < 0.7 sec (OK)");
        telemetry.addLine("  Overshoot: < 5% (Excellent) < 10% (OK)");
        telemetry.addLine("  Consistency: ±30 RPM (Excellent) ±50 RPM (OK)");
        telemetry.addLine("  Recovery: < 0.2 sec (Excellent) < 0.4 sec (OK)");
        telemetry.addLine("  L/R Sync: < 50 RPM difference");
        telemetry.addLine();

        // Display controls
        telemetry.addLine("Controls:");
        telemetry.addLine("  A: Start shooter | Y: Stop all");
        telemetry.addLine("  B: Spin-up test | X: Continuous test");
        telemetry.addLine("  DPad Up/Down: Adjust target ±100");
        telemetry.addLine("  Left Bumper: Feed ball (real test!)");
        telemetry.addLine("  Right Bumper: Reset stats");

        telemetry.update();
    }
}
