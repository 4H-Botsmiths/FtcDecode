package org.firstinspires.ftc.teamcode.programs.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.MechanumDrive;

@Autonomous(name = "Decode - Drive Only", group = "Competition")
public class DecodeAutoDriveOnly extends LinearOpMode {
    
    // Hardware and subsystems
    private Robot robot;
    private MechanumDrive drive;
    
    @Override
    public void runOpMode() {
        // Initialize telemetry
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        
        try {
            // Initialize robot and subsystems
            robot = new Robot(hardwareMap);
            drive = new MechanumDrive(robot);
            
            telemetry.addData("Status", "Robot Ready");
            telemetry.addData("Mode", "Autonomous - Drive Only");
            telemetry.addData("Strategy", "Cross starting line");
            telemetry.addData("Info", "Works from any starting position");
            telemetry.addData("", "Waiting for start...");
            
        } catch (Exception e) {
            telemetry.addData("ERROR", "Initialization failed: " + e.getMessage());
        }
        
        telemetry.update();
        
        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        
        // Run autonomous sequence
        if (opModeIsActive()) {
            runAutonomousSequence();
        }
        
        // Clean up
        cleanup();
    }
    
    /**
     * Main autonomous sequence - Drive Only Strategy
     * Alliance Strategy: SIMPLE & SAFE
     * 
     * Sequence:
     * 1. Drive forward to cross the starting line
     * 
     * Benefits:
     * - Earns points for leaving starting line
     * - Works from any starting position (motif or human player)
     * - Extremely reliable and simple
     * - Allows alliance partner to focus on scoring
     * - Zero risk of penalties
     */
    private void runAutonomousSequence() {
        telemetry.addData("Status", "Drive Only Mode");
        telemetry.addData("Info", "Crossing starting line");
        telemetry.update();
        
        // TODO: Step 1 - Drive forward to cross starting line
        // - Drive straight forward for a set distance
        // - Use encoders or time-based movement
        // - Ensure we've crossed the line completely
        // - Stop motors
        
        // TODO: Step 2 - Optional - Move to safe position
        // - Position robot out of the way of alliance partner
        // - Ensure we don't interfere with partner's scoring
        
        // For now, just wait until autonomous period ends
        while (opModeIsActive()) {
            telemetry.addData("Status", "Drive Only - Waiting for implementation");
            telemetry.addData("Time Remaining", "%.1f sec", 30 - getRuntime());
            telemetry.addData("Points", "Will earn points for crossing line");
            telemetry.update();
            sleep(50);
        }
    }
    
    /**
     * Clean up resources when autonomous ends
     */
    private void cleanup() {
        if (drive != null) {
            drive.stop();
        }
        
        telemetry.addData("Status", "Autonomous Complete");
        telemetry.addData("Result", "Crossed starting line");
        telemetry.update();
    }
}
