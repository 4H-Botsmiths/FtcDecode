package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.hardware.CRServo;

public class Robot {

  public final Motor leftShooter;
  public final Motor rightShooter;
  public final Shooter shooter;

  public final Motor frontLeft;
  public final Motor frontRight;
  public final Motor rearLeft;
  public final Motor rearRight;

  public final Motor intake;

  public final CRServo intakeServoLeft;
  public final CRServo intakeServoRight;

  public Robot(HardwareMap hardwareMap) {
    // Initialize hardware here
  
    this.leftShooter = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_4"),
        new Lights(leftShooterGreen, leftShooterRed));
    this.rightShooter = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_5"),
        new Lights(rightShooterGreen, rightShooterRed));
    this.rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

    this.intakeServoLeft = hardwareMap.get(CRServo.class, "SERVO_0");
    this.intakeServoRight = hardwareMap.get(CRServo.class, "SERVO_6");
    this.intakeServoRight.setDirection(CRServo.Direction.REVERSE);

    this.shooter = new Shooter(this.leftShooter, this.rightShooter, this.intakeServoLeft, this.intakeServoRight);

    this.frontLeft = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_1"));
    this.frontRight = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_0"));
    this.rearLeft = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_3"));
    this.rearRight = new Motor(hardwareMap.get(DcMotorEx.class, "MOTOR_2"));
  }

  public static final int DRIVE_MAX_RPM = 300;

  public void drive(double x, double y, double rotate) {
    double frontLeftPower = y + x + rotate;
    double frontRightPower = y - x - rotate;
    double rearLeftPower = y - x + rotate;
    double rearRightPower = y + x - rotate;

    frontLeft.setRPM(Range.clip(frontLeftPower, -1, 1) * Robot.DRIVE_MAX_RPM);
    frontRight.setRPM(Range.clip(frontRightPower, -1, 1) * Robot.DRIVE_MAX_RPM);
    rearLeft.setRPM(Range.clip(rearLeftPower, -1, 1) * Robot.DRIVE_MAX_RPM);
    rearRight.setRPM(Range.clip(rearRightPower, -1, 1) * Robot.DRIVE_MAX_RPM);
  }

/**
 * Drives the robot using field-centric inputs by compensating for the robot's current heading.
 *
 * <p>This method converts a desired motion vector provided in field coordinates (x, y)
 * into robot-relative coordinates by rotating the vector by -gyro (i.e. it applies a rotation
 * that compensates for the robot's current heading). The transformed robot-relative commands
 * are then passed to {@link #drive(double, double, double)} which computes individual wheel
 * powers and sets motor RPMs (clipped and scaled by {@code DRIVE_MAX_RPM}).</p>
 *
 * <p>Coordinate/convention notes:
 * <ul>
 *   <li>{@code x} is the lateral (strafe) command; positive values request motion to the right.</li>
 *   <li>{@code y} is the longitudinal (forward/backward) command; positive values request forward motion.</li>
 *   <li>{@code rotate} is the rotation command (a signed rotational rate); its sign follows the
 *       drivetrain's internal convention used by {@link #drive(double, double, double)}.</li>
 *   <li>{@code gyro} is the robot heading used to convert field-relative inputs to robot-relative;
 *       it is interpreted as an angle in radians.</li>
 * </ul>
 * </p>
 *
 * @param x lateral (strafe) input in field coordinates, typically in the range [-1, 1]
 * @param y forward/backward input in field coordinates, typically in the range [-1, 1]
 * @param rotate rotational input (signed), typically in the range [-1, 1]
 * @param gyro robot heading in radians used to transform field-centric inputs into robot-centric ones
 */

  public void drive(double x, double y, double rotate, double gyro) {
    double tempX = x * Math.cos(gyro) + y * Math.sin(gyro);
    double tempY = -x * Math.sin(gyro) + y * Math.cos(gyro);

    this.drive(tempX, tempY, rotate);
  }
}