package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
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

  public final CRServo intakeServoLeft;
  public final CRServo intakeServoRight;
  public final Intake intake;

  /** Positive power is counter-clockwise. Negative power is clockwise */
  public final CRServo indexer;

  public Robot(HardwareMap hardwareMap) {
    // Initialize hardware here

    this.leftShooter = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_0.getDeviceName()), 28);
    this.rightShooter = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_1.getDeviceName()), 28);
    this.rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

    this.intakeServoLeft = hardwareMap.get(CRServo.class, DeviceNames.CH_SERVO_0.getDeviceName());
    this.intakeServoRight = hardwareMap.get(CRServo.class, DeviceNames.EH_SERVO_0.getDeviceName());
    this.intakeServoRight.setDirection(CRServo.Direction.REVERSE);
    this.intake = new Intake(this.intakeServoLeft, this.intakeServoRight);

    this.shooter = new Shooter(this.leftShooter, this.rightShooter);

    // Calculate drive motor PPR (Pulses Per Rotation) based on gear ratio
    // Formula: ((1 + (stage1_ratio)) * (1 + (stage2_ratio))) * base_motor_PPR
    // Gear ratios: 46:17 (stage 1) and 46:11 (stage 2)
    // Base motor: 28 PPR (likely a bare motor encoder count)
    // Result: ~383.748 ticks per wheel revolution
    double drivePPR = ((((1 + (46.0 / 17.0))) * (1 + (46.0 / 11.0))) * 28.0);
    
    this.frontLeft = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_2.getDeviceName()),
        drivePPR);
    this.frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    this.frontRight = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_3.getDeviceName()),
        drivePPR);
    this.rearLeft = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_0.getDeviceName()),
        drivePPR);
    this.rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    this.rearRight = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_1.getDeviceName()),
        drivePPR);

    // ==================================================================================
    // PIDF TUNING SECTION FOR DRIVE MOTORS
    // ==================================================================================
    // The drive motors peak at 300 RPM but typically operate around 200 RPM.
    // When using velocity control (setRPM/setVelocity), PIDF coefficients help the
    // motors accurately maintain target speeds.
    //
    // PIDF COMPONENTS EXPLAINED:
    // - P (Proportional): Responds to current error (target - actual speed)
    //   * Higher P = faster response but may cause oscillation
    //   * Formula contribution: Kp × error
    //
    // - I (Integral): Eliminates steady-state error by accumulating past errors
    //   * Higher I = reaches exact target but may cause overshoot
    //   * Formula contribution: Ki × Σ(error)
    //
    // - D (Derivative): Dampens oscillation by responding to error rate of change
    //   * Higher D = less overshoot but may slow response
    //   * Formula contribution: Kd × Δ(error)
    //
    // - F (Feedforward): Anticipates needed power based on target velocity
    //   * This is the MOST IMPORTANT coefficient for velocity control
    //   * Formula contribution: Kf × target_velocity
    //
    // CALCULATING PIDF VALUES FOR YOUR MOTORS:
    //
    // Step 1: Calculate maximum velocity in ticks/second
    //   Max velocity = (300 RPM × 383.748 PPR) / 60 seconds = 1918.74 ticks/sec
    //   Typical velocity = (200 RPM × 383.748 PPR) / 60 seconds = 1279.16 ticks/sec
    //
    // Step 2: Calculate F (Feedforward) - Start here!
    //   F = maximum_power / maximum_velocity
    //   For typical 200 RPM operations: F = 1.0 / 1279.16 ≈ 0.000782
    //   Recommended starting value: Kf = 0.0008
    //
    // Step 3: Calculate P (Proportional)
    //   Rule of thumb: Kp ≈ 10 to 100 times Kf
    //   Recommended starting value: Kp = 0.015 (about 20 × Kf)
    //
    // Step 4: Calculate I (Integral)
    //   Rule of thumb: Ki ≈ Kp / 10 to Kp / 100
    //   Recommended starting value: Ki = 0.0003 (conservative)
    //
    // Step 5: Calculate D (Derivative)
    //   Rule of thumb: Kd ≈ Kp / 10 to Kp / 100
    //   Recommended starting value: Kd = 0.0002 (small, or start at 0)
    //
    // RECOMMENDED STARTING VALUES (for 200 RPM typical operation):
    //   Kp = 0.015   // Proportional gain
    //   Ki = 0.0003  // Integral gain
    //   Kd = 0.0002  // Derivative gain
    //   Kf = 0.0008  // Feedforward gain (most important!)
    //
    // TO APPLY THESE VALUES, uncomment and customize the code below:
    /*
    // Set motors to use velocity control with encoders
    frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    // Define PIDF coefficients
    // These values are tuned for ~200 RPM typical operation (can peak at 300 RPM)
    PIDFCoefficients drivePIDF = new PIDFCoefficients(
        0.015,  // P - Proportional gain
        0.0003, // I - Integral gain
        0.0002, // D - Derivative gain
        0.0008  // F - Feedforward gain
    );

    // Apply PIDF to each drive motor
    // Note: Requires REV Expansion Hub/Control Hub with firmware 1.8.2 or higher
    frontLeft.asDcMotorEx().setVelocityPIDFCoefficients(
        drivePIDF.p, drivePIDF.i, drivePIDF.d, drivePIDF.f);
    frontRight.asDcMotorEx().setVelocityPIDFCoefficients(
        drivePIDF.p, drivePIDF.i, drivePIDF.d, drivePIDF.f);
    rearLeft.asDcMotorEx().setVelocityPIDFCoefficients(
        drivePIDF.p, drivePIDF.i, drivePIDF.d, drivePIDF.f);
    rearRight.asDcMotorEx().setVelocityPIDFCoefficients(
        drivePIDF.p, drivePIDF.i, drivePIDF.d, drivePIDF.f);
    */
    
    // TUNING TIPS:
    // 1. Start with F only (set P=I=D=0), tune until motor approximately reaches target
    // 2. Add P to improve response time and accuracy
    // 3. Add I only if motor doesn't quite reach target speed
    // 4. Add D only if motor oscillates or overshoots
    // 5. Test under actual robot load conditions!
    //
    // For detailed tuning instructions, see: TeamDocs/PIDF_Tuning_Guide.md
    // ==================================================================================

    this.indexer = hardwareMap.get(CRServo.class, DeviceNames.CH_SERVO_1.getDeviceName());
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