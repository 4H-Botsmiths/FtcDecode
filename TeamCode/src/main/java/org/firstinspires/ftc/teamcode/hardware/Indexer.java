package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Indexer {
  private final PositionServo indexerServo;
  private final RevColorSensorV3 leftColorSensor;
  private final RevColorSensorV3 rightColorSensor;

  /**
   * Creates a new Indexer.
   * @param indexerServo the servo that controls the indexer
   * @param leftColorSensor the color sensor on the left side
   * @param rightColorSensor the color sensor on the right side
   */

  public Indexer(PositionServo indexerServo, RevColorSensorV3 leftColorSensor, RevColorSensorV3 rightColorSensor) {
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
  private final int MOVE_TIME = 1000;
  /** How long it takes to move between positions and get the ball into the intake in milliseconds */
  private final int MOVE_DROP_TIME = MOVE_TIME + 1500;
  private final int MOVE_DROP_SHOOT_TIME = MOVE_DROP_TIME + 2500;
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
    if (isBlocked()) {
      return; // Don't allow changing position while blocked
    }
    switch (position) {
      case RESET:
        //indexerServo.setPosition(80); //Our servo can't do a full rotation, so we have to use 100/6 instead of 0.
        indexerServo.setPosition(0); // Full Rotation Servo Version
        break;
      case LEFT:
        // indexerServo.setPosition(0);
        indexerServo.setPosition(-60); // Full Rotation Servo Version
        leftBallColor = BallColor.NONE;
        break;
      case RIGHT:
        //indexerServo.setPosition(120);
        indexerServo.setPosition(60); // Full Rotation Servo Version
        rightBallColor = BallColor.NONE;
        break;
      case TOP:
        //indexerServo.setPosition(-120);
        // Full Rotation Servo Version:
        switch (currentPosition) {
          case TOP:
            break; // Already at top, do nothing
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
        topBallColor = BallColor.NONE;
        break;
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
  public void reset() {
    setPosition(Position.RESET);
  }

  private BallColor leftBallColor = BallColor.NONE;
  private BallColor rightBallColor = BallColor.NONE;
  private BallColor topBallColor = BallColor.NONE;

  private boolean loading = false;

  /**
   * Loads balls into the indexer and detects their colors.
   * @apiNote This method should be called when the indexer is NOT YET in the `RESET` position as this is how it knows to reset which balls are where
   * @return true if all positions are filled with balls, false otherwise
   */
  public boolean load() {
    // Reset the indexer to prepare for loading
    reset();
    // Wait a little bit for the indexer to reach the position
    if (!isBusy() && loading) {
      // Check the colors of the balls
      BallColor instantaneousLeftBallColor = detectColor(Position.LEFT);
      switch (instantaneousLeftBallColor) {
        case PURPLE:
        case GREEN:
          leftBallColor = instantaneousLeftBallColor;
          break;
        case UNKNOWN:
          if (leftBallColor != BallColor.GREEN && leftBallColor != BallColor.PURPLE) {
            leftBallColor = BallColor.UNKNOWN;
          }
          break;
        default:
          break;
      }
      BallColor instantaneousRightBallColor = detectColor(Position.RIGHT);
      switch (instantaneousRightBallColor) {
        case PURPLE:
        case GREEN:
          rightBallColor = instantaneousRightBallColor;
          break;
        case UNKNOWN:
          if (rightBallColor != BallColor.GREEN && rightBallColor != BallColor.PURPLE) {
            rightBallColor = BallColor.UNKNOWN;
          }
          break;
        default:
          break;
      }
      BallColor instantaneousTopBallColor = detectColor(Position.TOP);
      topBallColor = instantaneousTopBallColor != BallColor.NONE ? instantaneousTopBallColor : topBallColor;
      if (leftBallColor != BallColor.NONE && rightBallColor != BallColor.NONE && topBallColor != BallColor.NONE) {
        return true;
      }
    } else {
      loading = true;
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
    RevColorSensorV3 sensor;
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
    int green = sensor.green();
    int blue = sensor.blue();

    // If it's too dark overall, treat it as no ball
    if (sensor.getDistance(DistanceUnit.MM) > 50) {
      return BallColor.NONE;
    }

    if (blue > green) {
      return BallColor.PURPLE;
    }

    if (green > blue) {
      return BallColor.GREEN;
    }

    // Ambiguous or background
    return BallColor.UNKNOWN;
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
        } else if ((leftBallColor == BallColor.NONE
            || rightBallColor == BallColor.NONE /*Add this back in if we get a continuous rotation servo*/) &&
            (topBallColor == BallColor.PURPLE || (topBallColor == BallColor.UNKNOWN && allowUnknown))) {
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
        } else if ((leftBallColor == BallColor.NONE
            || rightBallColor == BallColor.NONE /*Add this back in if we get a continuous rotation servo*/) &&
            (topBallColor == BallColor.GREEN || (topBallColor == BallColor.UNKNOWN && allowUnknown))) {
          top();
        } else {
          return false;
        }
        return true;
      case UNKNOWN:
        if (rightBallColor != BallColor.NONE) {
          right();
        } else if (leftBallColor != BallColor.NONE) {
          left();
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
   * Checks if the indexer is currently in the shooting phase (i.e., has not yet had enough time to move, drop, and shoot a ball).  
   * @return true if the indexer is currently shooting, false otherwise
   */
  public boolean isShooting() {
    return positionTimer.milliseconds() < MOVE_DROP_SHOOT_TIME;
  }

  /**
   * Forces the indexer to assume it has preloaded balls of specific colors.
   * This should be called when initializing autonomous programs if the robot starts with preloaded balls.
   */
  public void forcePreload() {
    // indexerServo.setPosition(60); //Our servo can't do a full rotation, so we have to use 100/6 instead of 0.
    indexerServo.setPosition(0); // Full Rotation Servo Version
    leftBallColor = BallColor.PURPLE;
    rightBallColor = BallColor.GREEN;
    topBallColor = BallColor.PURPLE;
  }

  public BallColor getBallColor(Position position) {
    switch (position) {
      case LEFT:
        return leftBallColor;
      case RIGHT:
        return rightBallColor;
      case TOP:
        return topBallColor;
      default:
        return BallColor.NONE;
    }
  }
}