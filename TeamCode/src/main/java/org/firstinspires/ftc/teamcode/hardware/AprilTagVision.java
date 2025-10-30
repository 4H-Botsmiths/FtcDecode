package org.firstinspires.ftc.teamcode.hardware;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;

/**
 * AprilTag vision subsystem for target detection and alignment assistance.
 * Focused on TeleOp alignment - autonomous features will be added later.
 */
public class AprilTagVision {
    
    // Target AprilTag ID for shooting alignment
    private static final int SHOOTING_TARGET_TAG_ID = 1;
    
    // Optimal shooting distance in inches
    private static final double OPTIMAL_SHOOTING_DISTANCE = 36.0;
    private static final double DISTANCE_TOLERANCE = 3.0;
    
    // Optimal shooting angle tolerance in degrees
    private static final double ANGLE_TOLERANCE = 5.0;
    
    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTag;
    
    // Current detection state
    private AprilTagDetection currentTarget = null;
    private long lastDetectionTime = 0;
    
    // Initialization state
    private boolean initialized = false;
    private String lastError = "";
    
    /**
     * Initialize AprilTag vision system
     * @param robot Robot instance containing camera hardware
     * @throws Exception if camera initialization fails (caller should catch this)
     */
    public AprilTagVision(Robot robot) throws Exception {
        // Check if webcam is available first
        if (robot.webcam == null) {
            aprilTag = null;
            visionPortal = null;
            initialized = false;
            lastError = "Webcam not available in Robot.java";
            throw new Exception("AprilTag vision init failed: Webcam not initialized");
        }
        
        try {
            // Create AprilTag processor
            aprilTag = new AprilTagProcessor.Builder()
                    .setDrawAxes(true)
                    .setDrawCubeProjection(true)
                    .setDrawTagOutline(true)
                    .build();
            
            // Create vision portal - let camera use its native resolution/FPS
            // If camera only supports 120fps modes, it will automatically use those
            // VisionPortal will negotiate the best available settings
            visionPortal = new VisionPortal.Builder()
                    .setCamera(robot.webcam)
                    .addProcessor(aprilTag)
                    // Don't specify resolution - let camera use native settings
                    .build();
            
            initialized = true;
            
        } catch (Exception e) {
            // Camera initialization failed - set everything to null and throw
            aprilTag = null;
            visionPortal = null;
            initialized = false;
            lastError = "Camera init failed: " + e.getMessage();
            throw new Exception("AprilTag vision init failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update detection - call this regularly in loop()
     */
    public void update() {
        if (!initialized || aprilTag == null) {
            return; // Vision not initialized, skip update
        }
        
        List<AprilTagDetection> detections = aprilTag.getDetections();
        
        // Look for our target tag
        currentTarget = null;
        for (AprilTagDetection detection : detections) {
            if (detection.id == SHOOTING_TARGET_TAG_ID) {
                currentTarget = detection;
                lastDetectionTime = System.currentTimeMillis();
                break;
            }
        }
    }
    
    /**
     * Check if shooting target is currently visible
     * @return true if target AprilTag is detected
     */
    public boolean isTargetVisible() {
        if (!initialized) {
            return false; // Vision not initialized
        }
        return currentTarget != null && 
               (System.currentTimeMillis() - lastDetectionTime) < 250; // 250ms timeout
    }
    
    /**
     * Get distance to shooting target in inches
     * @return distance in inches, or -1 if no target visible
     */
    public double getDistanceToTarget() {
        if (!isTargetVisible()) {
            return -1;
        }
        return currentTarget.ftcPose.range;
    }
    
    /**
     * Get angle to shooting target in degrees
     * Positive = target is to the right, Negative = target is to the left
     * @return angle in degrees, or 0 if no target visible
     */
    public double getAngleToTarget() {
        if (!isTargetVisible()) {
            return 0;
        }
        return Math.toDegrees(currentTarget.ftcPose.bearing);
    }
    
    /**
     * Get strafe offset to shooting target
     * Positive = target is to the right, Negative = target is to the left
     * @return strafe offset, or 0 if no target visible
     */
    public double getStrafeToTarget() {
        if (!isTargetVisible()) {
            return 0;
        }
        return currentTarget.ftcPose.x;
    }
    
    /**
     * Check if robot is aligned for optimal shooting
     * @return true if distance and angle are within tolerance
     */
    public boolean isAlignedForShooting() {
        if (!isTargetVisible()) {
            return false;
        }
        
        double distance = getDistanceToTarget();
        double angle = Math.abs(getAngleToTarget());
        
        boolean distanceOK = Math.abs(distance - OPTIMAL_SHOOTING_DISTANCE) <= DISTANCE_TOLERANCE;
        boolean angleOK = angle <= ANGLE_TOLERANCE;
        
        return distanceOK && angleOK;
    }
    
    /**
     * Get alignment status message for telemetry
     * @return status string describing current alignment
     */
    public String getAlignmentStatus() {
        if (!isTargetVisible()) {
            return "NO TARGET";
        }
        
        if (isAlignedForShooting()) {
            return String.format("ALIGNED (%.1f in)", getDistanceToTarget());
        }
        
        double distance = getDistanceToTarget();
        double angle = getAngleToTarget();
        
        String distanceStatus = Math.abs(distance - OPTIMAL_SHOOTING_DISTANCE) <= DISTANCE_TOLERANCE ? 
                               "DIST OK" : String.format("%.1f in", distance);
        String angleStatus = Math.abs(angle) <= ANGLE_TOLERANCE ? 
                            "ANGLE OK" : String.format("%.1f°", angle);
        
        return String.format("%s | %s", distanceStatus, angleStatus);
    }
    
    /**
     * Get driver instruction for alignment
     * @return instruction string for telemetry
     */
    public String getDriverInstruction() {
        if (!isTargetVisible()) {
            return "Look for target";
        }
        
        if (isAlignedForShooting()) {
            return "Ready to shoot!";
        }
        
        double distance = getDistanceToTarget();
        double angle = getAngleToTarget();
        
        String instruction = "";
        
        // Distance instruction
        if (distance < OPTIMAL_SHOOTING_DISTANCE - DISTANCE_TOLERANCE) {
            instruction += "Back up ";
        } else if (distance > OPTIMAL_SHOOTING_DISTANCE + DISTANCE_TOLERANCE) {
            instruction += "Move closer ";
        }
        
        // Angle instruction
        if (angle > ANGLE_TOLERANCE) {
            instruction += "Turn left";
        } else if (angle < -ANGLE_TOLERANCE) {
            instruction += "Turn right";
        }
        
        return instruction.isEmpty() ? "Fine tune position" : instruction.trim();
    }
    
    /**
     * Stop the vision portal when done
     */
    public void stop() {
        if (visionPortal != null) {
            visionPortal.close();
        }
    }
    
    /**
     * Check if vision system is initialized and working
     * @return true if vision is ready to use
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get last error message if initialization failed
     * @return error message or empty string if no error
     */
    public String getLastError() {
        return lastError;
    }
}