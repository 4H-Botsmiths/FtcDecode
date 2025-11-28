package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Indexer {
  private final PositionServo indexerServo;
  private final ColorSensor leftColorSensor;
  private final ColorSensor rightColorSensor;

  /**
   * Creates a new Indexer.
   * @param indexerServo the servo that controls the indexer
   * @param leftColorSensor the color sensor on the left side
   * @param rightColorSensor the color sensor on the right side
   */
  public Indexer(PositionServo indexerServo, ColorSensor leftColorSensor, ColorSensor rightColorSensor) {
    this.indexerServo = indexerServo;
    this.leftColorSensor = leftColorSensor;
    this.rightColorSensor = rightColorSensor;
  }

  /** Enum for the positions of the indexer. */
  public enum Position {
    RESET,
    LEFT,
    TOP,
    RIGHT
  }

  /** Enum for the colors of the balls. */
  public enum BallColor {
    NONE,
    PURPLE,
    GREEN,
    UNKNOWN
  }

  private Position currentPosition = Position.RESET;
  /** How long it takes to move between positions in milliseconds */
  private final double MOVE_TIME = 1.25;
  /** How long it takes to move between positions and get the ball into the intake in milliseconds */
  private final double MOVE_DROP_TIME = MOVE_TIME + 1.75;
  ElapsedTime positionTimer = new ElapsedTime();

  /**
   * Sets the position of the indexer.
   * With the current servo being limited to 300 degrees of rotation, the positions are as follows:
   * 0º - LEFT
   * 60º - RESET (1/6 of a circle)
   * 120º - RIGHT (1/3 of a circle)
   * -120º - TOP (-1/3 of a circle)
   * @apiNote This method will not change the position if the indexer is currently blocked (i.e., has not yet had enough time to move and drop a ball). However, it will still move if it was set to a previous position but has not had enough time to get there yet.
   * @param position desired position
   */
  public void setPosition(Position position) {
    if (!isBusy()) { //Wait a little but before marking the ball as none so that the servo has a chance to actually move
      switch (currentPosition) {
        case LEFT:
          leftBallColor = BallColor.NONE;
          break;
        case RIGHT:
          rightBallColor = BallColor.NONE;
          break;
        case TOP:
          topBallColor = BallColor.NONE;
          break;
        default:
          break;
      }
    }
    if (isBlocked()) {
      return; // Don't allow changing position while blocked
    }
    switch (position) {
      case RESET:
        indexerServo.setPosition(60); //Our servo can't do a full rotation, so we have to use 100/6 instead of 0.
        // indexerServo.setPosition(0); // Full Rotation Servo Version
        break;
      case LEFT:
        indexerServo.setPosition(0);
        // indexerServo.setPosition(-60); // Full Rotation Servo Version
        break;
      case RIGHT:
        indexerServo.setPosition(120);
        // indexerServo.setPosition(60); // Full Rotation Servo Version
        break;
      case TOP:
        indexerServo.setPosition(-120);
        /* Full Rotation Servo Version:
        switch (currentPosition) {
          case TOP:
            return; // Already at top, do nothing
          case LEFT:
            indexerServo.setPosition(-180);
            break;
          case RIGHT:
            indexerServo.setPosition(180);
            break;
          default:
            if (rightBallColor == BallColor.NONE) {
              indexerServo.setPosition(180);
            } else if (leftBallColor == BallColor.NONE) {
              indexerServo.setPosition(-180);
            } else {
              // Both sides are full, don't do anything
              return;
            }
            break;
        }
        */
    }
    if (position != currentPosition) {
      positionTimer.reset();
    }
    currentPosition = position;
  }

  /**
   * Returns the current position of the indexer.
   * @return current position
   */
  public Position getCurrentPosition() {
    return currentPosition;
  }

  /**
   * Alias for `setPosition(Position.TOP)`
   */
  public void top() {
    setPosition(Position.TOP);
  }

  /**
   * Alias for `setPosition(Position.LEFT)`
   */
  public void left() {
    setPosition(Position.LEFT);
  }

  /**
   * Alias for `setPosition(Position.RIGHT)`
   */
  public void right() {
    setPosition(Position.RIGHT);
  }

  /**
   * Alias for `setPosition(Position.RESET)`
   * @apiNote This method is only used internally, use `load()` instead.
   */
  private void reset() {
    setPosition(Position.RESET);
  }

  private BallColor leftBallColor = BallColor.NONE;
  private BallColor rightBallColor = BallColor.NONE;
  private BallColor topBallColor = BallColor.NONE;

  /**
   * Loads balls into the indexer and detects their colors.
   * @apiNote This method should be called when the indexer is NOT YET in the `RESET` position as this is how it knows to reset which balls are where
   * @return true if all positions are filled with balls, false otherwise
   */
  public boolean load() {
    // Reset the indexer to prepare for loading
    reset();
    // Wait a little bit for the indexer to reach the position
    if (!isBusy()) {
      // Check the colors of the balls
      BallColor instantaneousLeftBallColor = detectColor(Position.LEFT);
      leftBallColor = instantaneousLeftBallColor != BallColor.NONE ? instantaneousLeftBallColor : leftBallColor;
      BallColor instantaneousRightBallColor = detectColor(Position.RIGHT);
      rightBallColor = instantaneousRightBallColor != BallColor.NONE ? instantaneousRightBallColor : rightBallColor;
      BallColor instantaneousTopBallColor = detectColor(Position.TOP);
      topBallColor = instantaneousTopBallColor != BallColor.NONE ? instantaneousTopBallColor : topBallColor;
      if (leftBallColor != BallColor.NONE && rightBallColor != BallColor.NONE && topBallColor != BallColor.NONE) {
        return true;
      }
    } else {
      leftBallColor = BallColor.NONE;
      rightBallColor = BallColor.NONE;
      topBallColor = BallColor.NONE;
    }
    return false;
  }

  /**
   * Detects the color of the ball at the given position.
   * @param position position to detect color at
   * @return detected ball color
   */
  public BallColor detectColor(Position position) {
    ColorSensor sensor;
    switch (position) {
      case LEFT:
        sensor = leftColorSensor;
        break;
      case RIGHT:
        sensor = rightColorSensor;
        break;
      case TOP:
        return detectColor(Position.LEFT) != BallColor.NONE || detectColor(Position.RIGHT) != BallColor.NONE
            ? BallColor.UNKNOWN
            : BallColor.NONE;
      default:
        return BallColor.NONE;
    }
    int red = sensor.red();
    int green = sensor.green();
    int blue = sensor.blue();

    // Detect purple (high red and blue, low green)
    if (red > green && blue > green && red > 100 && blue > 100) {
      return BallColor.PURPLE;
    }

    // Detect green (high green, lower red and blue)
    if (green > red && green > blue && green > 100) {
      return BallColor.GREEN;
    }

    return BallColor.NONE;
  }

  /**
   * Sets the indexer to the position of the specified ball color.
   * @param color desired ball color
   * @param allowUnknown whether to allow moving to the top position if the desired color is not found on the sides
   * @return true if the position was set, false otherwise
   */
  public boolean setPosition(BallColor color, boolean allowUnknown) {
    switch (color) {
      case PURPLE:
        if (leftBallColor == BallColor.PURPLE) {
          left();
        } else if (rightBallColor == BallColor.PURPLE) {
          right();
        } else if ((leftBallColor != BallColor.NONE || rightBallColor != BallColor.NONE) && allowUnknown
            && topBallColor != BallColor.NONE) {
          top();
        } else {
          return false;
        }
        return true;
      case GREEN:
        if (leftBallColor == BallColor.GREEN) {
          left();
        } else if (rightBallColor == BallColor.GREEN) {
          right();
        } else if ((leftBallColor != BallColor.NONE || rightBallColor != BallColor.NONE) && allowUnknown
            && topBallColor != BallColor.NONE) {
          top();
        } else {
          return false;
        }
        return true;
      case UNKNOWN:
        if (leftBallColor != BallColor.NONE) {
          left();
        } else if (rightBallColor != BallColor.NONE) {
          right();
        } else if (topBallColor != BallColor.NONE) {
          top();
        } else {
          return false;
        }
        return true;
      default:
        return false;
    }
  }

  /**
   * Alias for `setPosition(BallColor.GREEN, allowUnknown)`
   * @param allowUnknown whether to allow moving to the top position if the desired color is not found on the sides
   * @return true if the position was set, false otherwise
   */
  public boolean green(boolean allowUnknown) {
    return setPosition(BallColor.GREEN, allowUnknown);
  }

  /**
   * Alias for `setPosition(BallColor.PURPLE, allowUnknown)`
   * @param allowUnknown whether to allow moving to the top position if the desired color is not found on the sides
   * @return true if the position was set, false otherwise
   */
  public boolean purple(boolean allowUnknown) {
    return setPosition(BallColor.PURPLE, allowUnknown);
  }

  /**
   * Alias for `setPosition(BallColor.UNKNOWN, true)`
   * @return true if the position was set, false otherwise
   */
  public boolean unknown() {
    return setPosition(BallColor.UNKNOWN, true);
  }

  /**
   * Checks if the indexer is currently moving.
   * @return true if the indexer is moving, false otherwise
   */
  public boolean isBusy() {
    return positionTimer.milliseconds() < MOVE_TIME;
  }

  /**
   * Checks if the indexer is currently blocked (i.e., has not yet had enough time to move and drop a ball).  
   * @return true if the indexer is currently blocked (still moving/dropping), false otherwise
   */
  public boolean isBlocked() {
    return positionTimer.milliseconds() < MOVE_DROP_TIME;
  }

  /**
   * Forces the indexer to assume it has preloaded balls of specific colors.
   * This should be called when initializing autonomous programs if the robot starts with preloaded balls.
   */
  public void forcePreload() {
    indexerServo.setPosition(60); //Our servo can't do a full rotation, so we have to use 100/6 instead of 0.
    // indexerServo.setPosition(0); // Full Rotation Servo Version
    leftBallColor = BallColor.PURPLE;
    rightBallColor = BallColor.GREEN;
    topBallColor = BallColor.PURPLE;
  }
}