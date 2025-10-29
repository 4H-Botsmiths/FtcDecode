package org.firstinspires.ftc.teamcode.hardware;

/**
 * Auto-alignment subsystem that combines AprilTag vision with mecanum drive
 * for automatic alignment to shooting targets.
 */
public class AutoAlign {
    
    private final AprilTagVision vision;
    private final MechanumDrive drive;
    
    // Auto-alignment parameters
    private static final double ALIGNMENT_SPEED = 0.3;
    private static final double ROTATION_SPEED = 0.2;
    private static final double APPROACH_SPEED = 0.25;
    
    // PID-like constants for smooth alignment
    private static final double DISTANCE_GAIN = 0.02;
    private static final double ANGLE_GAIN = 0.03;
    private static final double STRAFE_GAIN = 0.02;
    
    private boolean isAutoAligning = false;
    
    /**
     * Initialize auto-alignment system
     * @param vision AprilTag vision subsystem
     * @param drive Mecanum drive subsystem
     */
    public AutoAlign(AprilTagVision vision, MechanumDrive drive) {
        this.vision = vision;
        this.drive = drive;
    }
    
    /**
     * Start auto-alignment to shooting target
     * Only works if target is visible
     * @return true if alignment started, false if no target visible
     */
    public boolean startAlignment() {
        if (vision.isTargetVisible()) {
            isAutoAligning = true;
            return true;
        }
        return false;
    }
    
    /**
     * Stop auto-alignment and return control to manual driving
     */
    public void stopAlignment() {
        isAutoAligning = false;
        drive.stop();
    }
    
    /**
     * Update auto-alignment - call this in loop() when auto-aligning
     * @return true if still aligning, false if alignment complete or lost target
     */
    public boolean updateAlignment() {
        if (!isAutoAligning) {
            return false;
        }
        
        // Stop if target lost
        if (!vision.isTargetVisible()) {
            stopAlignment();
            return false;
        }
        
        // Stop if already aligned
        if (vision.isAlignedForShooting()) {
            stopAlignment();
            return false;
        }
        
        // Calculate alignment corrections
        double distance = vision.getDistanceToTarget();
        double angle = vision.getAngleToTarget();
        double strafe = vision.getStrafeToTarget();
        
        // Calculate movement commands
        double forward = 0;
        double strafeCommand = 0;
        double rotate = 0;
        
        // Distance correction (forward/backward)
        if (distance > 0) {
            double distanceError = distance - 36.0; // 36 inches optimal
            forward = -distanceError * DISTANCE_GAIN; // Negative because forward is negative
            forward = Math.max(-APPROACH_SPEED, Math.min(APPROACH_SPEED, forward));
        }
        
        // Angle correction (rotation)
        rotate = -angle * ANGLE_GAIN; // Negative to correct toward center
        rotate = Math.max(-ROTATION_SPEED, Math.min(ROTATION_SPEED, rotate));
        
        // Strafe correction (left/right)
        strafeCommand = -strafe * STRAFE_GAIN; // Negative to correct toward center
        strafeCommand = Math.max(-ALIGNMENT_SPEED, Math.min(ALIGNMENT_SPEED, strafeCommand));
        
        // Apply movement
        drive.drive(strafeCommand, forward, rotate, 1.0);
        
        return true;
    }
    
    /**
     * Check if currently auto-aligning
     * @return true if auto-alignment is active
     */
    public boolean isAligning() {
        return isAutoAligning;
    }
    
    /**
     * Get alignment status for telemetry
     * @return status string
     */
    public String getAlignmentStatus() {
        if (isAutoAligning) {
            return "AUTO-ALIGNING";
        } else if (vision.isAlignedForShooting()) {
            return "ALIGNED";
        } else if (vision.isTargetVisible()) {
            return "MANUAL";
        } else {
            return "NO TARGET";
        }
    }
}