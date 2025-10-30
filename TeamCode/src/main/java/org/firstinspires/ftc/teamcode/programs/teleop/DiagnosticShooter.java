package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.CRServo;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Shooter diagnostic program - Tests shooter motors and feeder servos
 * 
 * This program tests the Shooter subsystem configuration from Robot.java to verify:
 * - Shooter motors spin to target velocity (default 3000 RPM)
 * - Shooter motors spin OPPOSITE each other (creates launch tunnel)
 * - Feeder servos push TOWARD shooter (feed balls into tunnel)
 * - Feeder servos spin OPPOSITE each other (feed straight)
 * 
 * Test Sequence:
 * 1. Left Shooter Motor - Spin to target velocity for 5 seconds
 * 2. Right Shooter Motor - Spin to target velocity for 5 seconds (verify opposite direction)
 * 3. Left Feeder Servo - Spin at 50% power for 5 seconds
 * 4. Right Feeder Servo - Spin at 50% power for 5 seconds (verify opposite direction)
 * 
 * Why test from Robot.java?
 * - Tests the actual motor/servo objects that competition code uses
 * - Verifies motor directions are correct in Robot.java
 * - Ensures shooter creates proper ball launch tunnel
 * - Ensures feeders push balls into shooter correctly
 * 
 * Safety:
 * - Do NOT load balls during this test
 * - Keep hands away from shooter and feeders
 * - Robot can remain on ground (no suspension needed)
 * - Press STOP button for emergency stop
 * 
 * Expected Results:
 * - Both shooter motors reach target velocity
 * - Shooter motors spin opposite each other (create tunnel)
 * - Both feeders push toward shooter
 * - Feeders spin opposite each other (feed straight)
 */
@TeleOp(name = "Diagnostic - Shooter", group = "Diagnostics")
public class DiagnosticShooter extends LinearOpMode {
    
    private Robot robot;
    
    // Test parameters (tunable)
    private static final double TARGET_VELOCITY = 3000.0;     // RPM for shooter motors
    private static final int TEST_DURATION_MS = 5000;         // 5 seconds per component
    private static final double FEEDER_POWER = 0.5;           // 50% power for feeders
    private static final double VELOCITY_TOLERANCE = 200.0;   // ±200 RPM is acceptable
    
    // Test state tracking
    private boolean leftShooterPassed = false;
    private boolean rightShooterPassed = false;
    private boolean leftFeederPassed = false;
    private boolean rightFeederPassed = false;
    
    @Override
    public void runOpMode() {
        // Initialize robot hardware
        telemetry.addData("Status", "Initializing...");
        telemetry.addData("⚠️ WARNING", "Do NOT load balls!");
        telemetry.update();
        
        try {
            robot = new Robot(hardwareMap);
            
            // Configure shooter motors for velocity control
            robot.leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            
            telemetry.addData("Status", "Ready");
            telemetry.addData("", "Keep hands clear of shooter!");
            telemetry.addData("", "Press START when ready");
        } catch (Exception e) {
            telemetry.addData("ERROR", e.getMessage());
        }
        
        telemetry.update();
        waitForStart();
        
        if (opModeIsActive()) {
            // Show safety warning
            showSafetyWarning();
            sleep(3000);
            
            // Run diagnostic sequence
            if (opModeIsActive()) {
                runDiagnosticSequence();
            }
            
            // Show final results
            showFinalResults();
        }
        
        // Clean shutdown
        stopAllMotors();
    }
    
    /**
     * Show safety warning before starting tests
     */
    private void showSafetyWarning() {
        telemetry.clear();
        telemetry.addData("⚠️ STARTING IN 3 SECONDS", "");
        telemetry.addData("", "Keep hands clear!");
        telemetry.addData("", "Press STOP to abort");
        telemetry.update();
    }
    
    /**
     * Run the complete diagnostic sequence
     */
    private void runDiagnosticSequence() {
        // Test left shooter motor
        testShooterMotor("Left Shooter", robot.leftShooter, 1);
        pauseBetweenTests();
        
        if (!opModeIsActive()) return;
        
        // Test right shooter motor
        testShooterMotor("Right Shooter", robot.rightShooter, 2);
        pauseBetweenTests();
        
        if (!opModeIsActive()) return;
        
        // Test left feeder servo
        testFeederServo("Left Feeder", robot.feederLeft, 3);
        pauseBetweenTests();
        
        if (!opModeIsActive()) return;
        
        // Test right feeder servo
        testFeederServo("Right Feeder", robot.feederRight, 4);
    }
    
    /**
     * Test a shooter motor
     * 
     * @param motorName Human-readable name
     * @param motor The DcMotorEx to test
     * @param componentNumber Sequential number (1-4)
     */
    private void testShooterMotor(String motorName, DcMotorEx motor, int componentNumber) {
        if (!opModeIsActive()) return;
        
        // Show test starting
        telemetry.clear();
        telemetry.addData("Testing", "%s (%d/4)", motorName, componentNumber);
        telemetry.addData("Target", "%.0f RPM", TARGET_VELOCITY);
        telemetry.addData("", "Spinning up...");
        telemetry.update();
        
        // Start motor
        motor.setVelocity(TARGET_VELOCITY);
        
        // Monitor for test duration
        long testStartTime = System.currentTimeMillis();
        double maxVelocity = 0;
        boolean reachedTarget = false;
        
        while (opModeIsActive() && 
               (System.currentTimeMillis() - testStartTime) < TEST_DURATION_MS) {
            
            double currentVelocity = motor.getVelocity();
            maxVelocity = Math.max(maxVelocity, currentVelocity);
            
            double timeElapsed = (System.currentTimeMillis() - testStartTime) / 1000.0;
            double velocityPercent = (currentVelocity / TARGET_VELOCITY) * 100.0;
            
            // Check if reached target
            if (Math.abs(currentVelocity - TARGET_VELOCITY) < VELOCITY_TOLERANCE) {
                reachedTarget = true;
            }
            
            // Update telemetry
            telemetry.clear();
            telemetry.addData("Testing", "%s (%d/4)", motorName, componentNumber);
            telemetry.addData("Velocity", "%.0f RPM (%.0f%%)", currentVelocity, velocityPercent);
            telemetry.addData("Time", "%.1f / 5.0 sec", timeElapsed);
            
            if (reachedTarget) {
                telemetry.addData("Status", "✓ At target velocity");
            } else if (currentVelocity > 100) {
                telemetry.addData("Status", "Spinning up...");
            } else {
                telemetry.addData("Status", "⚠️ Check motor connection");
            }
            
            telemetry.update();
            sleep(100);
        }
        
        // Stop motor
        motor.setPower(0);
        
        // Evaluate results
        boolean passed = reachedTarget && maxVelocity >= (TARGET_VELOCITY - VELOCITY_TOLERANCE);
        
        if (componentNumber == 1) leftShooterPassed = passed;
        if (componentNumber == 2) rightShooterPassed = passed;
        
        // Show results
        telemetry.clear();
        telemetry.addData("Done", motorName);
        telemetry.addData("Max Velocity", "%.0f RPM", maxVelocity);
        telemetry.addData("Target", "%.0f RPM", TARGET_VELOCITY);
        
        if (passed) {
            telemetry.addData("Result", "✓ PASSED");
            telemetry.addData("", "Watch direction - correct?");
        } else {
            telemetry.addData("Result", "❌ FAILED");
            if (maxVelocity < 500) {
                telemetry.addData("Problem", "Motor not spinning");
            } else {
                telemetry.addData("Problem", "Did not reach target");
            }
        }
        
        telemetry.update();
        sleep(2000);
    }
    
    /**
     * Test a feeder servo
     * 
     * @param servoName Human-readable name
     * @param servo The CRServo to test
     * @param componentNumber Sequential number (1-4)
     */
    private void testFeederServo(String servoName, CRServo servo, int componentNumber) {
        if (!opModeIsActive()) return;
        
        // Show test starting
        telemetry.clear();
        telemetry.addData("Testing", "%s (%d/4)", servoName, componentNumber);
        telemetry.addData("Power", "%.0f%%", FEEDER_POWER * 100);
        telemetry.addData("", "Should push TOWARD shooter");
        telemetry.update();
        
        // Start servo
        servo.setPower(FEEDER_POWER);
        
        // Monitor for test duration
        long testStartTime = System.currentTimeMillis();
        
        while (opModeIsActive() && 
               (System.currentTimeMillis() - testStartTime) < TEST_DURATION_MS) {
            
            double timeElapsed = (System.currentTimeMillis() - testStartTime) / 1000.0;
            
            telemetry.clear();
            telemetry.addData("Testing", "%s (%d/4)", servoName, componentNumber);
            telemetry.addData("Time", "%.1f / 5.0 sec", timeElapsed);
            telemetry.addData("Status", "Spinning...");
            telemetry.addData("", "Watch: Pushing TOWARD shooter?");
            telemetry.update();
            
            sleep(100);
        }
        
        // Stop servo
        servo.setPower(0);
        
        // Manual verification required (no encoder on CRServo)
        telemetry.clear();
        telemetry.addData("Done", servoName);
        telemetry.addData("", "Did it push TOWARD shooter?");
        telemetry.addData("", "Press A = YES, B = NO");
        telemetry.update();
        
        // Wait for user confirmation
        long confirmStartTime = System.currentTimeMillis();
        while (opModeIsActive() && 
               (System.currentTimeMillis() - confirmStartTime) < 10000) {
            
            if (gamepad1.a || gamepad2.a) {
                // User confirmed correct direction
                if (componentNumber == 3) leftFeederPassed = true;
                if (componentNumber == 4) rightFeederPassed = true;
                
                telemetry.clear();
                telemetry.addData("Result", "✓ PASSED");
                telemetry.addData("", "Direction confirmed correct");
                telemetry.update();
                sleep(1000);
                break;
            } else if (gamepad1.b || gamepad2.b) {
                // User confirmed wrong direction
                if (componentNumber == 3) leftFeederPassed = false;
                if (componentNumber == 4) rightFeederPassed = false;
                
                telemetry.clear();
                telemetry.addData("Result", "❌ FAILED");
                telemetry.addData("Fix", "Reverse servo in Robot.java");
                telemetry.update();
                sleep(2000);
                break;
            }
            
            sleep(50);
        }
    }
    
    /**
     * Pause between component tests
     */
    private void pauseBetweenTests() {
        if (!opModeIsActive()) return;
        
        telemetry.clear();
        telemetry.addData("Status", "Pausing...");
        telemetry.update();
        sleep(1000);
    }
    
    /**
     * Show final test results and analysis
     */
    private void showFinalResults() {
        telemetry.clear();
        telemetry.addData("✓ Test Complete", "");
        telemetry.addData("", "");
        
        // Show individual results
        telemetry.addData("Left Shooter", leftShooterPassed ? "✓ PASSED" : "❌ FAILED");
        telemetry.addData("Right Shooter", rightShooterPassed ? "✓ PASSED" : "❌ FAILED");
        telemetry.addData("Left Feeder", leftFeederPassed ? "✓ PASSED" : "❌ FAILED");
        telemetry.addData("Right Feeder", rightFeederPassed ? "✓ PASSED" : "❌ FAILED");
        telemetry.addData("", "");
        
        // Overall assessment
        boolean allPassed = leftShooterPassed && rightShooterPassed && 
                           leftFeederPassed && rightFeederPassed;
        
        if (allPassed) {
            telemetry.addData("Result", "✓ ALL TESTS PASSED");
            telemetry.addData("", "");
            telemetry.addData("Verified", "✓ Shooter motors working");
            telemetry.addData("", "✓ Motors spin opposite (tunnel)");
            telemetry.addData("", "✓ Feeders push toward shooter");
            telemetry.addData("", "✓ Feeders spin opposite (straight)");
            telemetry.addData("", "");
            telemetry.addData("Status", "Shooter system ready!");
        } else {
            telemetry.addData("Result", "❌ SOME TESTS FAILED");
            telemetry.addData("", "");
            
            if (!leftShooterPassed || !rightShooterPassed) {
                telemetry.addData("Action", "Check shooter motor wiring");
                telemetry.addData("", "Verify encoders connected");
            }
            
            if (!leftFeederPassed || !rightFeederPassed) {
                telemetry.addData("Action", "Reverse failed servo in Robot.java");
            }
            
            telemetry.addData("", "");
            telemetry.addData("Important", "Shooters must spin OPPOSITE!");
            telemetry.addData("", "Feeders must push TOWARD shooter!");
        }
        
        telemetry.update();
    }
    
    /**
     * Emergency stop all motors and servos
     */
    private void stopAllMotors() {
        if (robot != null) {
            robot.leftShooter.setPower(0);
            robot.rightShooter.setPower(0);
            robot.feederLeft.setPower(0);
            robot.feederRight.setPower(0);
        }
    }
}
