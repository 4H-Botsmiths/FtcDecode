package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;

/**
 * Indexer subsystem that controls the circular paddle servo and monitors ball colors.
 * The paddle spins 3 balls in a circular pattern and can detect green/purple balls.
 */
public class Indexer {
    
    private final CRServo indexerServo;
    private final ColorSensor leftColorSensor;
    private final ColorSensor rightColorSensor;
    
    // Indexer constants
    public static final double ROTATION_POWER = 0.5;
    public static final double ROTATION_DEGREES_PER_SLOT = 120.0; // 360/3 slots
    public static final int COLOR_THRESHOLD = 50; // Minimum difference for color detection
    public static final int DISTANCE_THRESHOLD = 100; // Threshold for ball presence detection
    
    // Ball colors
    public enum BallColor {
        GREEN, 
        PURPLE,
        UNKNOWN
    }
    
    /**
     * Create an Indexer subsystem from a Robot instance
     * @param robot The robot instance containing all hardware
     */
    public Indexer(Robot robot) {
        this.indexerServo = robot.indexer;
        this.leftColorSensor = robot.colorSensorLeft;
        this.rightColorSensor = robot.colorSensorRight;
    }
    
    /**
     * Rotate the indexer paddle clockwise
     * @param power Power level (0.0 to 1.0)
     */
    public void rotateClockwise(double power) {
        indexerServo.setPower(power);
    }
    
    /**
     * Rotate the indexer paddle clockwise at default power
     */
    public void rotateClockwise() {
        rotateClockwise(ROTATION_POWER);
    }
    
    /**
     * Rotate the indexer paddle counter-clockwise
     * @param power Power level (0.0 to 1.0)
     */
    public void rotateCounterClockwise(double power) {
        indexerServo.setPower(-power);
    }
    
    /**
     * Rotate the indexer paddle counter-clockwise at default power
     */
    public void rotateCounterClockwise() {
        rotateCounterClockwise(ROTATION_POWER);
    }
    
    /**
     * Stop the indexer paddle
     */
    public void stop() {
        indexerServo.setPower(0);
    }
    
    /**
     * Detect ball color from left sensor
     */
    public BallColor getLeftBallColor() {
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
    public BallColor getRightBallColor() {
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
     * Get a user-friendly message about detected balls for driver feedback
     */
    public String getBallStatusMessage() {
        BallColor leftColor = getLeftBallColor();
        BallColor rightColor = getRightBallColor();
        
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
        BallColor leftColor = getLeftBallColor();
        BallColor rightColor = getRightBallColor();
        
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
     * Check if indexer is currently rotating
     */
    public boolean isRotating() {
        return Math.abs(indexerServo.getPower()) > 0.01;
    }
}