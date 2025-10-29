package org.firstinspires.ftc.teamcode.programs.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.MechanumDrive;
import org.firstinspires.ftc.teamcode.hardware.Shooter;
import org.firstinspires.ftc.teamcode.hardware.Indexer;
import org.firstinspires.ftc.teamcode.hardware.AprilTagVision;

@Autonomous(name = "Decode - Motif Second Shoot", group = "Competition")
public class DecodeAutoMotifSecondShoot extends LinearOpMode {
    
    // Hardware and subsystems
    private Robot robot;
    private MechanumDrive drive;
    private Shooter shooter;
    private Indexer indexer;
    private AprilTagVision vision;
    
    // Strategy parameters
    private static final double WAIT_TIME_SECONDS = 10.0; // Wait for partner to shoot & reload
    
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
            telemetry.addData("Mode", "Autonomous - Motif Second Shoot");
            telemetry.addData("Start Position", "Near Motif/Obelisk");
            telemetry.addData("Strategy", "Wait for partner, read motif, shoot");
            telemetry.addData("Wait Time", "%.1f seconds", WAIT_TIME_SECONDS);
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
     * Main autonomous sequence - Motif Second Shooter Strategy
     * Starting Position: NEAR MOTIF/OBELISK
     * Alliance Strategy: GO SECOND
     * 
     * Sequence:
     * 1. Wait for alliance partner to shoot and reload
     * 2. Read the motif pattern to determine shooting order
     * 3. Navigate to shooting position
     * 4. Shoot balls in the order indicated by motif
     * 5. Park or return to safe position
     */
    private void runAutonomousSequence() {
        // TODO: Step 1 - Wait for alliance partner
        // - Wait WAIT_TIME_SECONDS (10 seconds)
        // - This gives partner time to:
        //   * Shoot their first set
        //   * Return to human player
        //   * Reload balls
        //   * Position for second shot
        // - Show countdown on telemetry
        telemetry.addData("Status", "Waiting for alliance partner");
        telemetry.addData("Wait Time", "%.1f seconds remaining", WAIT_TIME_SECONDS);
        telemetry.update();
        
        // TODO: Implement wait with countdown
        // while (getRuntime() < WAIT_TIME_SECONDS && opModeIsActive()) {
        //     telemetry.addData("Status", "GO SECOND - Waiting");
        //     telemetry.addData("Time Until Our Turn", "%.1f sec", WAIT_TIME_SECONDS - getRuntime());
        //     telemetry.update();
        //     sleep(100);
        // }
        
        // TODO: Step 2 - Read the motif pattern
        // - Use AprilTag vision to detect motif pattern
        // - Decode the shooting order (e.g., GREEN-PURPLE-GREEN or PURPLE-GREEN-PURPLE)
        // - Store the order in memory
        // - Confirm successful read with telemetry
        
        // TODO: Step 3 - Navigate to shooting position
        // - Drive from motif start to optimal shooting location
        // - Use AprilTag alignment for precise positioning
        // - Ensure partner has cleared the area
        
        // TODO: Step 4 - Shoot balls in motif order
        // - Activate shooter motors
        // - Use indexer to feed balls in correct order based on motif
        // - Confirm all balls shot
        
        // TODO: Step 5 - Park or return to safe position
        // - Move to designated parking area
        // - Stop all motors
        
        // For now, just wait until autonomous period ends
        while (opModeIsActive()) {
            telemetry.addData("Status", "Motif Second Shoot - Waiting for implementation");
            telemetry.addData("Time Remaining", "%.1f sec", 30 - getRuntime());
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
