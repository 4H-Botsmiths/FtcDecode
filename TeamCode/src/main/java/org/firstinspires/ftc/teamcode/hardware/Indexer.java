package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Indexer {
  private final Servo indexerServo;
  private final ColorSensor leftColorSensor;
  private final ColorSensor rightColorSensor;

  /**
   * Creates a new Indexer.
   * @param indexerServo the servo that controls the indexer
   * @param leftColorSensor the color sensor on the left side
   * @param rightColorSensor the color sensor on the right side
   */
  public Indexer(Servo indexerServo, ColorSensor leftColorSensor, ColorSensor rightColorSensor) {
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
  private final int MOVE_TIME = 500;
  /** How long it takes to move between positions and get the ball into the intake in milliseconds */
  private final int MOVE_DROP_TIME = MOVE_TIME + 500;
  ElapsedTime positionTimer = new ElapsedTime();

  /**
   * Sets the position of the indexer.
   * @param position desired position
   */
  public void setPosition(Position position) {
    switch (position) {
      case RESET:
        indexerServo.setPosition(convertPosition(0));
        break;
      case LEFT:
        indexerServo.setPosition(convertPosition(-0.6));
        if (positionTimer.milliseconds() > MOVE_DROP_TIME) {
          leftBallColor = BallColor.NONE;
        }
        break;
      case RIGHT:
        indexerServo.setPosition(convertPosition(0.6));
        if (positionTimer.milliseconds() > MOVE_DROP_TIME) {
          rightBallColor = BallColor.NONE;
        }
        break;
      case TOP:
        if (currentPosition == Position.LEFT) {
          indexerServo.setPosition(convertPosition(-1));
        } else if (currentPosition == Position.RIGHT) {
          indexerServo.setPosition(convertPosition(1));
        } else if (rightBallColor == BallColor.NONE) {
          indexerServo.setPosition(convertPosition(1));
        } else if (leftBallColor == BallColor.NONE) {
          indexerServo.setPosition(convertPosition(-1));
        } else {
          // Both sides are full, don't do anything
          return;
        }
        if (positionTimer.milliseconds() > MOVE_DROP_TIME) {
          topBallColor = BallColor.NONE;
        }
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
   * Converts a -1 to 1 range to a 0 to 1 range for servo positioning.
   * @param position -1 to 1 range
   * @return 0 to 1 range
   */
  private double convertPosition(double position) {
    return (position + 1) / 2;
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

  BallColor leftBallColor = BallColor.NONE;
  BallColor rightBallColor = BallColor.NONE;
  BallColor topBallColor = BallColor.NONE;

  /**
   * Loads balls into the indexer and detects their colors.
   * @apiNote This method should be called when the indexer is NOT YET in the `RESET` position as this is how it knows to reset which balls are where
   * @return true if all positions are filled with balls, false otherwise
   */
  public boolean load() {
    // Reset the indexer to prepare for loading
    reset();
    // Wait a little bit for the indexer to reach the position
    if (positionTimer.milliseconds() > MOVE_TIME) {
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
      default:
        return detectColor(Position.LEFT) != BallColor.NONE || detectColor(Position.RIGHT) != BallColor.NONE
            ? BallColor.UNKNOWN
            : BallColor.NONE;
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
  public boolean indexerBusy() {
    return positionTimer.milliseconds() < MOVE_TIME;
  }

  /**
   * Checks if the indexer is currently blocked (i.e., has had enough time to move and drop a ball).
   * @return true if the indexer is blocked, false otherwise
   */
  public boolean indexerBlocked() {
    return positionTimer.milliseconds() > MOVE_DROP_TIME;
  }
}