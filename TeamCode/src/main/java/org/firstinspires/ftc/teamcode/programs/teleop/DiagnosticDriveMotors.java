package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * Drive Motors diagnostic program - ROBOT MUST BE SUSPENDED!
 * 
 * This program tests the four basic drive motors individually to verify:
 * - Motor wiring matches Robot.java configuration
 * - Motor directions are correct in Robot.java
 * - Encoders are working properly
 * 
 * Each wheel spins "forward" at 50% power for 2 seconds.
 * If a wheel spins backwards, that motor direction needs to be reversed in Robot.java
 * 
 * Why test from Robot.java?
 * - Tests the basic motor objects (frontRightDrive, frontLeftDrive, etc.)
 * - Ensures the actual configuration that TeleOp and Autonomous use is correct
 * - Tests the exact same motor objects that competition code will use
 * - Verifies Robot.java setup is production-ready
 * 
 * NOTE: This tests the basic drive motors, not the MechanumDrive subsystem.
 * Use DiagnosticMechanumDrive.java (when created) to test the full subsystem.
 * 
 * Test sequence: Front Right → Front Left → Rear Right → Rear Left
 */
@TeleOp(name = "Diagnostic - Drive Motors", group = "Diagnostics")
public class DiagnosticDriveMotors extends LinearOpMode {
    
    private Robot robot;
    
    // Test parameters (tunable for your robot)
    private static final double TEST_POWER = 0.5;           // 50% power for safety
    private static final int TEST_DURATION_MS = 2000;       // 2 seconds per wheel
    private static final int PAUSE_BETWEEN_MS = 1000;       // 1 second pause between tests
    
    @Override
    public void runOpMode() {
        // Initialize robot hardware - same way competition programs do
        telemetry.addData("⚠️ WARNING", "Robot MUST be suspended!");
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        
        try {
            robot = new Robot(hardwareMap);
            telemetry.addData("Status", "Ready");
            telemetry.addData("", "Press START when suspended");
        } catch (Exception e) {
            telemetry.addData("ERROR", e.getMessage());
        }
        
        telemetry.update();
        waitForStart();
        
        if (opModeIsActive()) {
            // Final safety warning - give driver 3 seconds to abort
            telemetry.addData("⚠️ STARTING IN 3 SECONDS", "");
            telemetry.addData("", "Press STOP if not suspended!");
            telemetry.update();
            sleep(3000);
            
            // Run diagnostic sequence if driver hasn't pressed STOP
            if (opModeIsActive()) {
                runDiagnosticSequence();
            }
        }
        
        // Clean shutdown
        stopAllMotors();
        telemetry.addData("Status", "Complete");
        telemetry.update();
    }
    
    /**
     * Run the complete diagnostic sequence for all four wheels.
     * Tests the actual motor objects from Robot.java.
     */
    private void runDiagnosticSequence() {
        // Test each motor using Robot.java's motor objects
        testMotor("Front Right", robot.frontRight, 1);
        pauseBetweenTests();
        
        testMotor("Front Left", robot.frontLeft, 2);
        pauseBetweenTests();
        
        testMotor("Rear Right", robot.rearRight, 3);
        pauseBetweenTests();
        
        testMotor("Rear Left", robot.rearLeft, 4);
        
        // Show completion message - require user confirmation
        telemetry.clear();
        telemetry.addLine("DIAGNOSTIC COMPLETE");
        telemetry.addLine();
        telemetry.addData("?", "Did all wheels spin FORWARD?");
        telemetry.addLine();
        telemetry.addData("YES", "Config correct");
        telemetry.addData("NO", "Fix Robot.java");
        telemetry.update();
    }
    
    /**
     * Test an individual motor from Robot.java
     * 
     * @param motorName Human-readable motor name
     * @param motor The DcMotor object from Robot.java
     * @param motorNumber Test number (1-4) for progress tracking
     */
    private void testMotor(String motorName, DcMotor motor, int motorNumber) {
        if (!opModeIsActive()) return;
        
        // Record starting encoder position
        int startPosition = motor.getCurrentPosition();
        
        // Show test starting
        telemetry.addData("Testing", "%s (%d/4)", motorName, motorNumber);
        telemetry.addData("Start", startPosition);
        telemetry.addData("", "Should spin FORWARD");
        telemetry.update();
        
        // Spin the motor forward using Robot.java's configured direction
        motor.setPower(TEST_POWER);
        
        // Monitor during test
        long testStartTime = System.currentTimeMillis();
        while (opModeIsActive() && 
               (System.currentTimeMillis() - testStartTime) < TEST_DURATION_MS) {
            
            // Update telemetry with current metrics
            int currentPosition = motor.getCurrentPosition();
            int encoderChange = currentPosition - startPosition;
            
            telemetry.clear();
            telemetry.addData("Motor", "%s (%d/4)", motorName, motorNumber);
            telemetry.addData("Encoder", "%+d", encoderChange);
            
            // Provide diagnostic feedback
            if (encoderChange == 0) {
                telemetry.addData("Status", "NOT MOVING");
            } else if (encoderChange < 0) {
                telemetry.addData("Status", "Moving (backward?)");
            } else {
                telemetry.addData("Status", "Moving (forward?)");
            }
            
            telemetry.update();
            sleep(100);  // Update every 100ms
        }
        
        // Stop the motor
        motor.setPower(0);
        
        // Show final results
        int finalPosition = motor.getCurrentPosition();
        int totalChange = finalPosition - startPosition;
        
        telemetry.clear();
        telemetry.addData("Done", "%s (%d/4)", motorName, motorNumber);
        telemetry.addData("Encoder", "%+d", totalChange);
        
        // Interpret results - note that robot cannot self-verify direction
        if (totalChange == 0) {
            telemetry.addData("Result", "NOT MOVING - check wiring");
        } else if (totalChange < 500) {
            telemetry.addData("Result", "Low counts - weak/obstructed?");
        } else {
            telemetry.addData("Result", "Moved - verify visually");
        }
        
        telemetry.update();
        sleep(2000);  // Show results for 2 seconds
    }
    
    /**
     * Pause between motor tests
     */
    private void pauseBetweenTests() {
        if (!opModeIsActive()) return;
        sleep(PAUSE_BETWEEN_MS);
    }
    
    /**
     * Emergency stop all motors using Robot.java's motor objects
     */
    private void stopAllMotors() {
        if (robot != null) {
            robot.frontRight.setPower(0);
            robot.frontLeft.setPower(0);
            robot.rearRight.setPower(0);
            robot.rearLeft.setPower(0);
        }
    }
}
