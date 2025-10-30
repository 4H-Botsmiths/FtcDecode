package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * Indexer subsystem that controls the circular paddle servo and monitors ball colors.
 * The paddle has 3 slots for balls and uses position-based servo control.
 * 
 * Physical Setup:
 * - GoBUILDA 5-turn servo (2000-0024-0502) with 1800° range (0.0 to 1.0 position)
 * - Home position (0.0) blocks the drop hole
 * - Rotating left/right moves balls to drop position (0.1667) where they fall
 * - Two color sensors detect LEFT and RIGHT ball colors
 * 
 * Usage:
 * - Read colors at HOME position
 * - Rotate LEFT or RIGHT to bring desired color to drop position
 * - Ball falls through hole automatically
 * - Return to HOME to block hole again
 */
public class Indexer {
    
    private final Servo indexerServo;
    private final ColorSensor leftColorSensor;
    private final ColorSensor rightColorSensor;
    
    // Servo positions for 3-slot circular paddle
    private static final double HOME_POSITION = 0.0;        // Blocks drop hole
    private static final double DROP_POSITION = 0.1667;     // Ball at drop hole (can fall)
    private static final double SLOT_INCREMENT = 0.3333;    // 120° per slot (1/3 rotation)
    private static final int ROTATION_DELAY_MS = 500;       // Time for servo to reach position
    
    // Color detection constants
    private static final int COLOR_THRESHOLD = 50;          // Minimum difference for color detection
    private static final int DISTANCE_THRESHOLD = 100;      // Threshold for ball presence detection
    
    // Current servo position tracking
    private double currentPosition = HOME_POSITION;
    
    // Ball colors
    public enum BallColor {
        GREEN, 
        PURPLE,
        UNKNOWN
    }
    
    // OpMode types for different fallback behaviors
    public enum OpModeType {
        DIAGNOSTIC,   // Strict - must find color or FAIL
        AUTONOMOUS,   // Try color, fallback to RIGHT if not found
        TELEOP        // Try color, show error, allow manual override
    }
    
    // Result of drop operation
    public enum DropResult {
        SUCCESS_LEFT,      // Found and dropped from left
        SUCCESS_RIGHT,     // Found and dropped from right
        FALLBACK_RIGHT,    // Color not found, dropped from right (autonomous)
        ERROR_NOT_FOUND    // Color not found (diagnostic/teleop)
    }
    
    /**
     * Create an Indexer subsystem from a Robot instance
     * @param robot The robot instance containing all hardware
     */
    public Indexer(Robot robot) {
        this.indexerServo = robot.indexer;
        this.leftColorSensor = robot.colorSensorLeft;
        this.rightColorSensor = robot.colorSensorRight;
        
        // Initialize servo to home position (blocking hole)
        returnToHome();
    }
    
    /**
     * Rotate the indexer paddle counter-clockwise (LEFT) by one slot.
     * This is a public method for manual control in TeleOp.
     */
    public void rotateLeft() {
        // Rotate counter-clockwise (subtract position)
        currentPosition -= SLOT_INCREMENT;
        
        // Wrap around if needed (5-turn servo, 0.0 to 1.0 range)
        if (currentPosition < 0.0) {
            currentPosition += 1.0;
        }
        
        indexerServo.setPosition(currentPosition);
        
        // Wait for servo to reach position
        sleep(ROTATION_DELAY_MS);
    }
    
    /**
     * Rotate the indexer paddle clockwise (RIGHT) by one slot.
     * This is a public method for manual control in TeleOp.
     */
    public void rotateRight() {
        // Rotate clockwise (add position)
        currentPosition += SLOT_INCREMENT;
        
        // Wrap around if needed
        if (currentPosition > 1.0) {
            currentPosition -= 1.0;
        }
        
        indexerServo.setPosition(currentPosition);
        
        // Wait for servo to reach position
        sleep(ROTATION_DELAY_MS);
    }
    
    /**
     * Return to HOME position (blocks the drop hole).
     * Call this after dropping a ball to prevent accidental drops.
     */
    public void returnToHome() {
        currentPosition = HOME_POSITION;
        indexerServo.setPosition(currentPosition);
        sleep(ROTATION_DELAY_MS);
    }
    
    /**
     * Smart ball drop with mode-specific fallback behavior.
     * 
     * @param desiredColor The color of ball to drop (GREEN or PURPLE)
     * @param mode The OpMode type (DIAGNOSTIC, AUTONOMOUS, or TELEOP)
     * @return DropResult indicating success or failure
     */
    public DropResult dropBall(BallColor desiredColor, OpModeType mode) {
        // Read current colors at HOME position
        BallColor leftColor = getLeftColor();
        BallColor rightColor = getRightColor();
        
        // Try to find the desired color
        if (leftColor == desiredColor) {
            // Desired ball is on LEFT
            rotateLeft();  // Move to drop position
            // Ball drops automatically!
            returnToHome(); // Block hole again
            return DropResult.SUCCESS_LEFT;
            
        } else if (rightColor == desiredColor) {
            // Desired ball is on RIGHT
            rotateRight();  // Move to drop position
            // Ball drops automatically!
            returnToHome(); // Block hole again
            return DropResult.SUCCESS_RIGHT;
            
        } else {
            // ERROR: Desired color not found!
            // Handle based on mode
            switch (mode) {
                case DIAGNOSTIC:
                    // Strict mode - fail the test
                    return DropResult.ERROR_NOT_FOUND;
                    
                case AUTONOMOUS:
                    // Fallback mode - drop from right anyway
                    rotateRight();
                    returnToHome();
                    return DropResult.FALLBACK_RIGHT;
                    
                case TELEOP:
                    // Show error, wait for manual override
                    return DropResult.ERROR_NOT_FOUND;
                    
                default:
                    return DropResult.ERROR_NOT_FOUND;
            }
        }
    }
    
    /**
     * Detect ball color from left sensor
     */
    public BallColor getLeftColor() {
        if (!isBallPresentLeft()) {
            return BallColor.UNKNOWN;
        }
        
        int red = leftColorSensor.red();
        int green = leftColorSensor.green();
        int blue = leftColorSensor.blue();
        
        // Purple detection (more red and blue than green)
        if ((red + blue) > (green + COLOR_THRESHOLD)) {
            return BallColor.PURPLE;
        }
        // Green detection (more green than red and blue)
        else if (green > (red + blue + COLOR_THRESHOLD)) {
            return BallColor.GREEN;
        }
        
        return BallColor.UNKNOWN;
    }
    
    /**
     * Detect ball color from right sensor
     */
    public BallColor getRightColor() {
        if (!isBallPresentRight()) {
            return BallColor.UNKNOWN;
        }
        
        int red = rightColorSensor.red();
        int green = rightColorSensor.green();
        int blue = rightColorSensor.blue();
        
        // Purple detection (more red and blue than green)
        if ((red + blue) > (green + COLOR_THRESHOLD)) {
            return BallColor.PURPLE;
        }
        // Green detection (more green than red and blue)
        else if (green > (red + blue + COLOR_THRESHOLD)) {
            return BallColor.GREEN;
        }
        
        return BallColor.UNKNOWN;
    }
    
    /**
     * Check if a ball is present at the left sensor using distance/brightness
     */
    public boolean isBallPresentLeft() {
        int totalColor = leftColorSensor.red() + leftColorSensor.green() + leftColorSensor.blue();
        return totalColor > DISTANCE_THRESHOLD;
    }
    
    /**
     * Check if a ball is present at the right sensor using distance/brightness
     */
    public boolean isBallPresentRight() {
        int totalColor = rightColorSensor.red() + rightColorSensor.green() + rightColorSensor.blue();
        return totalColor > DISTANCE_THRESHOLD;
    }
    
    /**
     * Get current servo position (0.0 to 1.0)
     */
    public double getCurrentPosition() {
        return currentPosition;
    }
    
    /**
     * Get a user-friendly message about detected balls for driver feedback
     */
    public String getBallStatusMessage() {
        BallColor leftColor = getLeftColor();
        BallColor rightColor = getRightColor();
        
        StringBuilder message = new StringBuilder();
        
        if (leftColor != BallColor.UNKNOWN) {
            message.append("LEFT: ").append(leftColor.name()).append(" ");
        }
        
        if (rightColor != BallColor.UNKNOWN) {
            message.append("RIGHT: ").append(rightColor.name());
        }
        
        if (leftColor == BallColor.UNKNOWN && rightColor == BallColor.UNKNOWN) {
            message.append("No balls detected");
        }
        
        return message.toString().trim();
    }
    
    /**
     * Get driver instruction based on detected colors
     */
    public String getDriverInstruction() {
        BallColor leftColor = getLeftColor();
        BallColor rightColor = getRightColor();
        
        if (leftColor == BallColor.GREEN || rightColor == BallColor.GREEN) {
            return "Press LEFT TRIGGER for GREEN";
        } else if (leftColor == BallColor.PURPLE || rightColor == BallColor.PURPLE) {
            return "Press RIGHT TRIGGER for PURPLE";
        }
        
        return "No balls ready";
    }
    
    /**
     * Get detailed sensor readings for telemetry
     */
    public String getSensorStatus() {
        return String.format("L(R:%d G:%d B:%d) R(R:%d G:%d B:%d)",
            leftColorSensor.red(), leftColorSensor.green(), leftColorSensor.blue(),
            rightColorSensor.red(), rightColorSensor.green(), rightColorSensor.blue());
    }
    
    /**
     * Check if indexer is currently moving (always false for servos - they move automatically)
     * Kept for compatibility with existing code
     */
    public boolean isRotating() {
        return false;  // Servos don't report busy status
    }
    
    /**
     * Stop the indexer (servos hold position automatically)
     * Kept for compatibility with existing code
     */
    public void stop() {
        // Servos hold their position automatically, nothing to do
    }
    
    /**
     * Helper method for sleep delays
     */
    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}