package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.hardware.MechanumDrive;
import org.firstinspires.ftc.teamcode.hardware.Shooter;
import org.firstinspires.ftc.teamcode.hardware.Indexer;
import org.firstinspires.ftc.teamcode.hardware.AprilTagVision;
import org.firstinspires.ftc.teamcode.hardware.AutoAlign;

@TeleOp(name = "Decode", group = "Competition")
public class DecodeTeleop extends OpMode {
    
    // Hardware and subsystems
    private Robot robot;
    private MechanumDrive drive;
    private Shooter shooter;
    private Indexer indexer;
    private AprilTagVision vision;
    private AutoAlign autoAlign;
    
    // Control settings
    private static final double NORMAL_DRIVE_SPEED = 0.8;
    private static final double SLOW_DRIVE_SPEED = 0.3;
    private static final double TURBO_DRIVE_SPEED = 1.0;
    
    // Button state tracking for toggles
    private boolean lastShooterButton = false;
    private boolean shooterActive = false;
    private boolean lastAlignButton = false;
    private boolean visionActive = false;
    
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
            
            // Try to initialize vision - make it optional for now
            try {
                vision = new AprilTagVision(robot);
                autoAlign = new AutoAlign(vision, drive);
                telemetry.addData("Vision", "Available");
            } catch (Exception visionError) {
                vision = null;
                autoAlign = null;
                telemetry.addData("Vision", "DISABLED - " + visionError.getMessage());
            }
            
            telemetry.addData("Status", "Robot Ready");
            telemetry.addData("Operator", "A=Shooter, Triggers=Indexer");
            if (vision != null) {
                telemetry.addData("Driver", "Right Bumper=Vision On/Off");
                telemetry.addData("Align", "Left Bumper=Auto Align");
            }
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
        // Update vision system only when needed and available
        if (visionActive && vision != null) {
            vision.update();
        }
        
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
        // Toggle vision system with right bumper (for performance) - only if vision available
        if (vision != null) {
            boolean currentAlignButton = gamepad1.right_bumper;
            if (currentAlignButton && !lastAlignButton) {
                visionActive = !visionActive;
                if (!visionActive && autoAlign != null) {
                    // Stop auto-alignment when vision is turned off
                    autoAlign.stopAlignment();
                }
            }
            lastAlignButton = currentAlignButton;
        }
        
        // Handle auto-alignment with left bumper (only works if vision is active and available)
        if (autoAlign != null && gamepad1.left_bumper && visionActive) {
            // Start auto-alignment if not already aligning
            if (!autoAlign.isAligning()) {
                autoAlign.startAlignment();
            }
        } else if (autoAlign != null && autoAlign.isAligning()) {
            // Stop auto-alignment when button released
            autoAlign.stopAlignment();
        }
        
        // Update auto-alignment or handle manual driving
        if (autoAlign != null && autoAlign.isAligning()) {
            // Auto-alignment is handling drive commands
            autoAlign.updateAlignment();
        } else {
            // Manual driving
            handleManualDriving();
        }
    }
    
    /**
     * Handle manual driving controls
     */
    private void handleManualDriving() {
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
            // Left trigger: rotate LEFT (counter-clockwise) for GREEN balls
            indexer.rotateLeft();
        } else if (gamepad2.right_trigger > 0.1) {
            // Right trigger: rotate RIGHT (clockwise) for PURPLE balls
            indexer.rotateRight();
        }
        
        // Manual override with D-pad (if color sensors fail)
        if (gamepad2.dpad_left) {
            indexer.rotateLeft();
            telemetry.addData("⚠️ MANUAL", "Rotated LEFT");
        }
        if (gamepad2.dpad_right) {
            indexer.rotateRight();
            telemetry.addData("⚠️ MANUAL", "Rotated RIGHT");
        }
    }
    
    /**
     * Update telemetry optimized for the gamepad 2 operator (who holds the driver station)
     */
    private void updateTelemetry() {
        // Shooter status (primary concern for operator)
        telemetry.addData("Shooter", shooterActive ? "ACTIVE" : "STOPPED");
        
        // Target alignment status (for driver) - only show if vision is active and available
        if (vision != null && visionActive) {
            telemetry.addData("Target", vision.getAlignmentStatus());
        } else if (vision == null) {
            telemetry.addData("Target", "NO CAMERA");
        } else {
            telemetry.addData("Target", "VISION OFF");
        }
        
        // Ball detection (critical for operator decisions)
        telemetry.addData("Balls", indexer.getBallStatusMessage());
        
        // Clear action instruction (what the operator should do next)
        if (autoAlign != null && autoAlign.isAligning()) {
            telemetry.addData("Action", "AUTO ALIGNING...");
        } else if (vision != null && visionActive && vision.isTargetVisible()) {
            telemetry.addData("Action", vision.getDriverInstruction());
        } else {
            telemetry.addData("Action", indexer.getDriverInstruction());
        }
        
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
        if (autoAlign != null) {
            autoAlign.stopAlignment();
        }
        if (vision != null) {
            vision.stop();
        }
        
        telemetry.addData("Status", "Stopped");
        telemetry.update();
    }
}