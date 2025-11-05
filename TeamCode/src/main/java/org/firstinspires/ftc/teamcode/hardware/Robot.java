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

    // Shooter motors: Direct drive (no gearbox), 28 PPR encoders
    // Operating at ~3000 RPM constant speed for ball launching
    this.leftShooter = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_0.getDeviceName()), 28);
    this.rightShooter = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_1.getDeviceName()), 28);
    this.rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

    // ==================================================================================
    // PIDF TUNING SECTION FOR SHOOTER MOTORS
    // ==================================================================================
    // The shooter motors run at ~3000 RPM and need to:
    // 1. Handle resistance from balls being pushed into them
    // 2. Get up to speed quickly without overshooting
    // 3. Be consistent from shot to shot
    // 4. Correct for errors sooner (more aggressive than drive motors)
    //
    // SHOOTER-SPECIFIC PIDF REQUIREMENTS:
    // - Higher P: Responds faster to ball resistance (load disturbance)
    // - Higher I: Maintains consistency despite load and voltage drop
    // - Moderate D: Prevents overshoot during rapid spin-up
    // - Same F concept: Provides baseline power for 3000 RPM
    //
    // CALCULATING PIDF VALUES FOR SHOOTER MOTORS:
    //
    // Step 1: Calculate velocity in ticks/second
    //   Shooter velocity = (3000 RPM × 28 PPR) / 60 seconds = 1400 ticks/sec
    //
    // Step 2: Calculate F (Feedforward)
    //   F = maximum_power / shooter_velocity
    //   F = 1.0 / 1400 = 0.000714
    //   Recommended: Kf = 0.0007
    //
    // Step 3: Calculate P (Proportional) - HIGHER for shooters!
    //   Rule of thumb: Kp ≈ 30 to 80 times Kf (vs. 10-100 for drive)
    //   For quick load response: Kp = 60 × 0.0007 = 0.042
    //   Recommended starting value: Kp = 0.040
    //   Why higher? Responds faster when ball hits, gets to speed quicker
    //
    // Step 4: Calculate I (Integral) - HIGHER for shooters!
    //   Rule of thumb: Ki ≈ Kp / 20 to Kp / 50 (vs. Kp/10-100 for drive)
    //   For consistent shots: Ki = 0.040 / 27 = 0.00148
    //   Recommended starting value: Ki = 0.0015
    //   Why higher? Fights ball resistance better, maintains speed under load
    //
    // Step 5: Calculate D (Derivative) - MODERATE for shooters
    //   Rule of thumb: Kd ≈ Kp / 40 to Kp / 100
    //   For minimal overshoot: Kd = 0.040 / 50 = 0.0008
    //   Recommended starting value: Kd = 0.0008
    //   Why moderate? Prevents overshoot on startup without slowing response
    //
    // RECOMMENDED STARTING VALUES (for ~3000 RPM shooter):
    //   Kp = 0.040   // High responsiveness to ball load
    //   Ki = 0.0015  // Strong correction for consistency
    //   Kd = 0.0008  // Moderate overshoot prevention
    //   Kf = 0.0007  // Feedforward for 3000 RPM baseline
    //
    // TO APPLY THESE VALUES, uncomment and customize the code below:
    /*
    // Set shooter motors to use velocity control with encoders
    leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

    // Define PIDF coefficients optimized for shooter operation
    // These values prioritize:
    // - Fast response when ball creates resistance
    // - Quick spin-up without overshoot (0.3-0.5 seconds to 3000 RPM)
    // - Consistent velocity shot-to-shot (±30 RPM)
    PIDFCoefficients shooterPIDF = new PIDFCoefficients(
        0.040,  // P - High for quick load response
        0.0015, // I - Strong for consistent performance
        0.0008, // D - Moderate to prevent overshoot
        0.0007  // F - Feedforward for 3000 RPM baseline
    );

    // Apply PIDF to both shooter motors
    // Note: Requires REV Expansion Hub/Control Hub with firmware 1.8.2 or higher
    leftShooter.asDcMotorEx().setVelocityPIDFCoefficients(
        shooterPIDF.p, shooterPIDF.i, shooterPIDF.d, shooterPIDF.f);
    rightShooter.asDcMotorEx().setVelocityPIDFCoefficients(
        shooterPIDF.p, shooterPIDF.i, shooterPIDF.d, shooterPIDF.f);
    */
    
    // TUNING TIPS FOR SHOOTERS:
    // 1. Start with F only (P=I=D=0), tune until close to 3000 RPM
    // 2. Add P=0.040 to improve load response and startup speed
    // 3. Add I=0.0015 for shot-to-shot consistency
    // 4. Add D=0.0008 to eliminate any overshoot
    // 5. Test with actual ball shots under match conditions!
    //
    // PERFORMANCE TARGETS:
    // - Spin-up time: 0.3-0.5 seconds (0 to 3000 RPM)
    // - Overshoot: < 5% (< 150 RPM above 3000)
    // - Speed drop during shot: < 100 RPM
    // - Shot-to-shot consistency: ±30 RPM
    // - Recovery after shot: < 0.2 seconds back to 3000 RPM
    //
    // For detailed shooter tuning instructions, see: TeamDocs/PIDF_Shooter_Tuning_Guide.md
    // For quick reference, see: TeamDocs/PIDF_Shooter_Quick_Reference.md
    // ==================================================================================

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