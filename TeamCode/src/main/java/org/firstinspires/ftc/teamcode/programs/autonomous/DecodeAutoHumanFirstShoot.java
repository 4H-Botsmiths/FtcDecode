package org.firstinspires.ftc.teamcode.programs.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.MechanumDrive;
import org.firstinspires.ftc.teamcode.hardware.Shooter;
import org.firstinspires.ftc.teamcode.hardware.Indexer;
import org.firstinspires.ftc.teamcode.hardware.AprilTagVision;

@Autonomous(name = "Decode - Human First Shoot", group = "Competition")
public class DecodeAutoHumanFirstShoot extends LinearOpMode {
    
    // Hardware and subsystems
    private Robot robot;
    private MechanumDrive drive;
    private Shooter shooter;
    private Indexer indexer;
    private AprilTagVision vision;
    
    @Override
    public void runOpMode() {
        // Initialize telemetry
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        
        try {
            // Initialize robot and subsystems
            robot = new Robot(hardwareMap);
            drive = new MechanumDrive(robot);
            shooter = new Shooter(robot);
            indexer = new Indexer(robot);
            vision = new AprilTagVision(robot);
            
            telemetry.addData("Status", "Robot Ready");
            telemetry.addData("Mode", "Autonomous - Human First Shoot");
            telemetry.addData("Start Position", "Near Human Players");
            telemetry.addData("Strategy", "Shoot first, reload, shoot again");
            telemetry.addData("Note", "Pre-loaded ball order used");
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
     * Main autonomous sequence - Human First Shooter Strategy
     * Starting Position: NEAR HUMAN PLAYERS
     * Alliance Strategy: GO FIRST
     * 
     * Sequence:
     * 1. Navigate to shooting position
     * 2. Shoot balls in correct order (based on motif - partner must communicate!)
     * 3. Return to human player station
     * 4. Reload balls from human player
     * 5. Navigate back to shooting position
     * 6. Shoot second set of balls
     * 7. Park or return to safe position
     * 
     * Note: Cannot read motif from this starting position. Alliance partner
     * at motif position MUST communicate the correct ball order!
     */
    private void runAutonomousSequence() {
        telemetry.addData("Status", "Human First Shooter Active");
        telemetry.addData("Info", "Alliance partner waits for us");
        telemetry.update();
        
        // TODO: Step 1 - Navigate to shooting position
        // - Drive from human player start to optimal shooting location
        // - Use AprilTag alignment for precise positioning
        // - Shorter distance than from motif start
        
        // TODO: Step 2 - Shoot balls (order from motif)
        // - Activate shooter motors
        // - Use indexer to feed balls in correct order
        // - Ball order determined by motif (partner communicates!)
        // - Confirm all balls shot
        
        // TODO: Step 3 - Return to human player station
        // - Navigate back to reload zone (very short distance)
        // - Position for sample pickup
        
        // TODO: Step 4 - Reload from human player
        // - Wait for human player to load balls in same order
        // - Verify balls are loaded (using color sensors)
        // - Check time remaining (need ~15 seconds for second shot)
        
        // TODO: Step 5 - Navigate back to shooting position
        // - Return to same shooting location as first cycle
        // - Use AprilTag alignment again
        
        // TODO: Step 6 - Shoot second set in same order
        // - Activate shooter motors
        // - Feed balls in same order from motif
        // - Confirm completion
        
        // TODO: Step 7 - Park or return to safe position
        // - Move to designated parking area
        // - Stop all motors
        
        // For now, just wait until autonomous period ends
        while (opModeIsActive()) {
            telemetry.addData("Status", "Human First Shoot - Waiting for implementation");
            telemetry.addData("Time Remaining", "%.1f sec", 30 - getRuntime());
            telemetry.addData("Important", "Partner must communicate motif order!");
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
        if (shooter != null) {
            shooter.stopShooting();
        }
        if (indexer != null) {
            indexer.stop();
        }
        if (vision != null) {
            vision.stop();
        }
        
        telemetry.addData("Status", "Autonomous Complete");
        telemetry.update();
    }
}
