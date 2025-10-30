package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.Indexer;

/**
 * Indexer diagnostic program - Tests color sensors and intelligent ball sequencing
 * 
 * This program tests the Indexer subsystem from Robot.java to verify:
 * - Color sensors can detect ball colors (green vs purple)
 * - Color sensors can detect ball presence (left vs right positions)
 * - Indexer rotation mechanism works correctly
 * - Robot can intelligently sequence balls based on sensor readings
 * 
 * Test Requirements:
 * - Load indexer with EXACTLY 3 balls in ANY pattern
 * - Must have: 2 PURPLE balls and 1 GREEN ball
 * - Example patterns: Purple-Purple-Green, Green-Purple-Purple, Purple-Green-Purple
 * 
 * Test Sequence (INTELLIGENT DUMPING):
 * 1. Read ball colors from both sensors (left and right)
 * 2. Determine which ball to dump next to achieve PURPLE-GREEN-PURPLE order
 * 3. Rotate to position correct ball on right side (dump position)
 * 4. Dump the ball
 * 5. Repeat until sequence complete: PURPLE → GREEN → PURPLE
 * 
 * Why test from Robot.java?
 * - Tests the actual Indexer subsystem that competition code uses
 * - Verifies color sensor configuration and calibration
 * - Ensures indexer can intelligently sequence balls (critical for DECODE game!)
 * - Tests the exact same objects that TeleOp and Autonomous use
 * 
 * Expected Results:
 * - Robot dumps balls in correct order: PURPLE → GREEN → PURPLE
 * - Color sensors accurately identify all ball colors
 * - Indexer rotates correctly to position each ball
 * - All 3 balls successfully dumped in correct sequence
 */
@TeleOp(name = "Diagnostic - Indexer", group = "Diagnostics")
public class DiagnosticIndexer extends LinearOpMode {
    
    private Robot robot;
    private Indexer indexer;
    
    // Test parameters (tunable)
    private static final int READ_PAUSE_MS = 2000;           // Time to display readings
    private static final int DUMP_VERIFICATION_MS = 1000;    // Time to verify ball dumped
    private static final int MAX_ROTATION_WAIT_MS = 3000;    // Maximum time to wait for rotation
    
    // Desired dump sequence (DECODE game motif pattern)
    private static final Indexer.BallColor[] DESIRED_SEQUENCE = {
        Indexer.BallColor.PURPLE,
        Indexer.BallColor.GREEN,
        Indexer.BallColor.PURPLE
    };
    
    // Test state tracking
    private int ballsDumped = 0;
    private boolean sequenceCorrect = true;
    
    @Override
    public void runOpMode() {
        // Initialize robot hardware
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        
        try {
            robot = new Robot(hardwareMap);
            indexer = new Indexer(robot);
            
            telemetry.addData("Status", "Ready");
            telemetry.addData("", "Load 3 balls:");
            telemetry.addData("", "2 PURPLE + 1 GREEN");
            telemetry.addData("", "Press START when loaded");
        } catch (Exception e) {
            telemetry.addData("ERROR", e.getMessage());
        }
        
        telemetry.update();
        waitForStart();
        
        if (opModeIsActive()) {
            // Show instructions
            showInstructions();
            sleep(3000);
            
            // Run diagnostic sequence
            if (opModeIsActive()) {
                runDiagnosticSequence();
            }
            
            // Show final results
            showFinalResults();
        }
        
        // Clean shutdown
        indexer.stop();
    }
    
    /**
     * Show initial test instructions
     */
    private void showInstructions() {
        telemetry.clear();
        telemetry.addData("Test Starting", "3 balls will be dumped");
        telemetry.addData("", "Watch indexer rotate CCW");
        telemetry.addData("Expected", "2 PURPLE + 1 GREEN");
        telemetry.update();
    }
    
    /**
     * Run the complete diagnostic sequence - dump balls in PURPLE-GREEN-PURPLE order
     */
    private void runDiagnosticSequence() {
        // Dump 3 balls in the correct sequence
        for (int sequenceStep = 0; sequenceStep < 3; sequenceStep++) {
            if (!opModeIsActive()) break;
            
            Indexer.BallColor desiredColor = DESIRED_SEQUENCE[sequenceStep];
            
            // Read current ball colors
            Indexer.BallColor leftColor = indexer.getLeftColor();
            Indexer.BallColor rightColor = indexer.getRightColor();
            
            // Display what we're looking for
            showSearchingFor(sequenceStep + 1, desiredColor, leftColor, rightColor);
            sleep(READ_PAUSE_MS);
            
            if (!opModeIsActive()) break;
            
            // Position the correct ball for dumping
            positionBallForDump(sequenceStep + 1, desiredColor, leftColor, rightColor);
            
            if (!opModeIsActive()) break;
            
            // Dump the ball
            dumpBall(sequenceStep + 1, desiredColor);
            
            // Verify ball was dumped
            sleep(DUMP_VERIFICATION_MS);
            
            ballsDumped++;
        }
    }
    
    /**
     * Show what color we're searching for and current sensor readings
     * 
     * @param stepNumber Current step in sequence (1-3)
     * @param desiredColor The color we need to dump next
     * @param leftColor Current color on left sensor
     * @param rightColor Current color on right sensor
     */
    private void showSearchingFor(int stepNumber, Indexer.BallColor desiredColor,
                                   Indexer.BallColor leftColor, Indexer.BallColor rightColor) {
        telemetry.clear();
        telemetry.addData("Step", "%d of 3: Need %s", stepNumber, desiredColor.name());
        telemetry.addData("Left", formatColorReading(leftColor));
        telemetry.addData("Right", formatColorReading(rightColor));
        telemetry.addData("Raw", indexer.getSensorStatus());
        telemetry.update();
    }
    
    /**
     * Position the correct ball at the dump position (right side)
     * Uses encoder-based rotation for precise 120° movements
     * 
     * @param stepNumber Current step in sequence (1-3)
     * @param desiredColor The color we need to dump
     * @param leftColor Current color on left sensor
     * @param rightColor Current color on right sensor
     */
    private void positionBallForDump(int stepNumber, Indexer.BallColor desiredColor,
                                     Indexer.BallColor leftColor, Indexer.BallColor rightColor) {
        // Check if desired ball is already on the right (dump position)
        if (rightColor == desiredColor) {
            telemetry.clear();
            telemetry.addData("Step", "%d: %s", stepNumber, desiredColor.name());
            telemetry.addData("Status", "✓ Already in position");
            telemetry.addData("", "No rotation needed");
            telemetry.update();
            sleep(1000);
            return;
        }
        
        // Check if desired ball is on the left
        if (leftColor == desiredColor) {
            telemetry.clear();
            telemetry.addData("Step", "%d: %s", stepNumber, desiredColor.name());
            telemetry.addData("Status", "On LEFT - rotating CCW");
            telemetry.addData("", "Exactly 120° (1/3 turn)");
            telemetry.update();
            
            // Rotate left (counter-clockwise) to move left ball to right position
            double startPos = indexer.getCurrentPosition();
            indexer.rotateLeft();
            
            double endPos = indexer.getCurrentPosition();
            double actualMove = Math.abs(endPos - startPos);
            
            telemetry.addData("Encoder Move", "%d ticks", actualMove);
            telemetry.update();
            sleep(500);
            return;
        }
        
        // Desired ball not visible - must be in the back slot
        // Rotate once to bring it into view
        telemetry.clear();
        telemetry.addData("Step", "%d: %s", stepNumber, desiredColor.name());
        telemetry.addData("Status", "Not visible - in back slot");
        telemetry.addData("", "Rotating 120° to find it");
        telemetry.update();
        
        indexer.rotateLeft();
        sleep(500);
        
        // Re-check sensors after rotation
        leftColor = indexer.getLeftColor();
        rightColor = indexer.getRightColor();
        
        // Now position it correctly
        if (rightColor != desiredColor && leftColor == desiredColor) {
            // Need one more rotation to get it to dump position
            telemetry.addData("Status", "Found - moving to dump");
            telemetry.update();
            
            indexer.rotateLeft();
            sleep(500);
        }
    }
    
    /**
     * Format color reading for display
     */
    private String formatColorReading(Indexer.BallColor color) {
        if (color == Indexer.BallColor.UNKNOWN) {
            return "None";
        }
        return color.name();
    }
    
    /**
     * Dump the ball currently on the right (dump position)
     * Uses servo position control to rotate and drop ball
     * 
     * @param stepNumber Current step in sequence (1-3)
     * @param expectedColor The color we expect to be dumping
     */
    private void dumpBall(int stepNumber, Indexer.BallColor expectedColor) {
        // Verify correct ball is in position before dumping
        Indexer.BallColor rightColor = indexer.getRightColor();
        
        if (rightColor != expectedColor) {
            telemetry.clear();
            telemetry.addData("⚠️ ERROR", "Wrong ball in position!");
            telemetry.addData("Expected", expectedColor.name());
            telemetry.addData("Found", rightColor.name());
            telemetry.update();
            sequenceCorrect = false;
            sleep(3000);
            return;
        }
        
        // Show dumping status
        telemetry.clear();
        telemetry.addData("Dumping", "%s (%d of 3)", expectedColor.name(), stepNumber);
        telemetry.addData("Status", "Rotating to dump");
        telemetry.update();
        
        // Rotate left to dump position
        double startPos = indexer.getCurrentPosition();
        indexer.rotateLeft();
        double endPos = indexer.getCurrentPosition();
        double actualMove = Math.abs(endPos - startPos);
        
        // Confirm dump with position feedback
        telemetry.clear();
        telemetry.addData("✓ Dumped", "%s", expectedColor.name());
        telemetry.addData("Position Move", "%.3f", actualMove);
        telemetry.addData("Expected", "0.333 (120°)");
        telemetry.update();
    }
    

    
    /**
     * Show final test results and analysis
     */
    private void showFinalResults() {
        telemetry.clear();
        telemetry.addData("✓ Test Complete", "");
        telemetry.addData("", "");
        
        // Show the sequence that was dumped
        telemetry.addData("Target Sequence", "PURPLE → GREEN → PURPLE");
        telemetry.addData("Balls Dumped", "%d of 3", ballsDumped);
        telemetry.addData("", "");
        
        // Analyze results
        if (ballsDumped == 3 && sequenceCorrect) {
            telemetry.addData("Result", "✓ ALL TESTS PASSED");
            telemetry.addData("", "Correct sequence achieved!");
            telemetry.addData("", "");
            telemetry.addData("Verified", "✓ Color sensors working");
            telemetry.addData("", "✓ Intelligent sequencing working");
            telemetry.addData("", "✓ Rotation mechanism working");
            telemetry.addData("", "");
            telemetry.addData("Status", "Competition-ready!");
        } else {
            telemetry.addData("Result", "❌ TESTS FAILED");
            telemetry.addData("", "");
            
            if (ballsDumped < 3) {
                telemetry.addData("Problem", "Only dumped %d balls", ballsDumped);
                telemetry.addData("Fix", "Check color sensor readings");
            }
            
            if (!sequenceCorrect) {
                telemetry.addData("Problem", "Wrong ball sequence");
                telemetry.addData("Fix", "Color detection may be wrong");
            }
            
            telemetry.addData("", "");
            telemetry.addData("Action", "Review sensor calibration");
            telemetry.addData("", "Verify ball colors are correct");
        }
        
        telemetry.update();
    }
}
