package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.MechanumDrive;
import org.firstinspires.ftc.teamcode.hardware.Shooter;
import org.firstinspires.ftc.teamcode.hardware.Indexer;

@TeleOp(name = "Decode", group = "Competition")
public class DecodeTeleop extends OpMode {
    
    // Hardware and subsystems
    private Robot robot;
    private MechanumDrive drive;
    private Shooter shooter;
    private Indexer indexer;
    
    // Control settings
    private static final double NORMAL_DRIVE_SPEED = 0.8;
    private static final double SLOW_DRIVE_SPEED = 0.3;
    private static final double TURBO_DRIVE_SPEED = 1.0;
    
    // Button state tracking for toggles
    private boolean lastShooterButton = false;
    private boolean shooterActive = false;
    
    @Override
    public void init() {
        telemetry.addData("Status", "Initializing...");
        telemetry.update();
        
        try {
            // Initialize robot and subsystems
            robot = new Robot(hardwareMap);
            drive = new MechanumDrive(robot);
            shooter = new Shooter(robot);
            indexer = new Indexer(robot);
            
            telemetry.addData("Status", "Robot Ready");
            telemetry.addData("Operator", "A=Shooter, Triggers=Indexer");
            telemetry.addData("Feeder", "Bumpers=Manual Control");
            
        } catch (Exception e) {
            telemetry.addData("ERROR", "Failed to initialize: " + e.getMessage());
        }
        
        telemetry.update();
    }
    
    @Override
    public void init_loop() {
        // Show ball detection for operator preparation
        if (indexer != null) {
            telemetry.addData("Balls Ready", indexer.getBallStatusMessage());
            telemetry.addData("Next Action", indexer.getDriverInstruction());
            telemetry.update();
        }
    }
    
    @Override
    public void start() {
        telemetry.addData("Status", "Started - Ready to Drive!");
        telemetry.update();
    }
    
    @Override
    public void loop() {
        // Handle drive controls (Gamepad 1)
        handleDriveControls();
        
        // Handle shooter and indexer controls (Gamepad 2)
        handleShooterControls();
        handleIndexerControls();
        
        // Update telemetry
        updateTelemetry();
    }
    
    /**
     * Handle mecanum drive controls using Gamepad 1
     */
    private void handleDriveControls() {
        // Get drive inputs from gamepad 1
        double strafe = gamepad1.left_stick_x;      // Left/right movement
        double forward = -gamepad1.left_stick_y;    // Forward/backward (inverted)
        double rotate = gamepad1.right_stick_x;     // Rotation
        
        // Determine speed mode based on triggers
        double speedMultiplier = NORMAL_DRIVE_SPEED;
        
        if (gamepad1.right_trigger > 0.1) {
            // Turbo mode
            speedMultiplier = TURBO_DRIVE_SPEED;
        } else if (gamepad1.left_trigger > 0.1) {
            // Slow mode
            speedMultiplier = SLOW_DRIVE_SPEED;
        }
        
        // Drive the robot
        drive.drive(strafe, forward, rotate, speedMultiplier);
    }
    
    /**
     * Handle shooter controls using Gamepad 2
     */
    private void handleShooterControls() {
        // Toggle shooter with A button
        boolean currentShooterButton = gamepad2.a;
        if (currentShooterButton && !lastShooterButton) {
            // A button was just pressed - toggle shooter
            shooterActive = !shooterActive;
            
            if (shooterActive) {
                shooter.startShooting();
            } else {
                shooter.stopShooting();
            }
        }
        lastShooterButton = currentShooterButton;
        
        // Manual feeder control with bumpers (overrides automatic feeder)
        if (gamepad2.right_bumper) {
            // Forward feeder
            shooter.startFeeder();
        } else if (gamepad2.left_bumper) {
            // Reverse feeder
            shooter.reverseFeeder();
        } else if (!shooterActive) {
            // Only stop feeder if shooter isn't running automatically
            shooter.stopFeeder();
        }
        
        // Emergency stop with Y button
        if (gamepad2.y) {
            shooter.stopShooting();
            shooterActive = false;
        }
    }
    
    /**
     * Handle indexer controls using Gamepad 2
     */
    private void handleIndexerControls() {
        // Manual indexer rotation with triggers
        if (gamepad2.left_trigger > 0.1) {
            // Left trigger: rotate counter-clockwise for GREEN balls
            indexer.rotateCounterClockwise();
        } else if (gamepad2.right_trigger > 0.1) {
            // Right trigger: rotate clockwise for PURPLE balls
            indexer.rotateClockwise();
        } else {
            // Stop indexer when triggers are released
            indexer.stop();
        }
        
        // Emergency stop indexer with X button
        if (gamepad2.x) {
            indexer.stop();
        }
    }
    
    /**
     * Update telemetry optimized for the gamepad 2 operator (who holds the driver station)
     */
    private void updateTelemetry() {
        // Shooter status (primary concern for operator)
        telemetry.addData("Shooter", shooterActive ? "ACTIVE" : "STOPPED");
        
        // Ball detection (critical for operator decisions)
        telemetry.addData("Balls", indexer.getBallStatusMessage());
        
        // Clear action instruction (what the operator should do next)
        telemetry.addData("Action", indexer.getDriverInstruction());
        
        // Indexer activity status (operator feedback)
        telemetry.addData("Indexer", indexer.isRotating() ? "SPINNING" : "READY");
        
        telemetry.update();
    }
    
    @Override
    public void stop() {
        // Make sure everything stops when OpMode ends
        if (drive != null) {
            drive.stop();
        }
        if (shooter != null) {
            shooter.stopShooting();
        }
        if (indexer != null) {
            indexer.stop();
        }
        
        telemetry.addData("Status", "Stopped");
        telemetry.update();
    }
}