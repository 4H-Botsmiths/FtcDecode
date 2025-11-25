import com.qualcomm.robotcore.hardware.Servo;

public class PositionServo {
  private final Servo servo;
  private final int servoRange;
  private final ServoMode mode;

  /**
   * A wrapper for a servo that allows setting and getting position in degrees
   * @param servo the servo to wrap
   * @param servoRange the range of the servo (in degrees)
   * @param mode the mode of the servo (CENTERED or STANDARD)
  
   */
  public PositionServo(Servo servo, int servoRange, ServoMode mode) {
    this.servo = servo;
    this.servoRange = servoRange;
    this.mode = mode;
  }

  /**
   * Set the position of the servo
   * @param position the position to set (in degrees)
   */
  public void setPosition(double position) {
    servo.setPosition(scaleAngleToServoPosition(position));
  }

  /**
   * @return the current position of the servo (in degrees)
   */
  public double getPosition() {
    return scaleServoPositionToAngle(servo.getPosition());
  }

  private double scaleServoPositionToAngle(double position) {
    switch (mode) {
      case CENTERED:
        return (position * servoRange) - (servoRange / 2.0);
      case STANDARD:
        return position * servoRange;
      default:
        throw new IllegalStateException("Unexpected value: " + mode);
    }
  }

  private double scaleAngleToServoPosition(double angle) {
    switch (mode) {
      case CENTERED:
        return (angle + (servoRange / 2.0)) / servoRange;
      case STANDARD:
        return angle / servoRange;
      default:
        throw new IllegalStateException("Unexpected value: " + mode);
    }
  }

  public enum ServoMode {
    /** 
     * 0 is the middle
     * ex. -180 to 180
     */
    CENTERED,
    /** 
     * 0 is the start
     * ex. 0 to 360
     */
    STANDARD
  }
}