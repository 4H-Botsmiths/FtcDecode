package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
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

  public final PositionServo indexerServo;
  public final ColorSensor leftColorSensor;
  public final ColorSensor rightColorSensor;
  public final Indexer indexer;

  public Robot(HardwareMap hardwareMap) {
    // Initialize hardware here

    // Shooter motors: Direct drive (no gearbox), 28 PPR encoders
    // Operating at ~3000 RPM constant speed for ball launching
    this.leftShooter = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_0.getDeviceName()), 28);
    this.rightShooter = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_1.getDeviceName()), 28);
    this.rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);

    PIDFCoefficients shooterPIDF = new PIDFCoefficients(
        45.0, // P - High for quick load response (corrected scale)
        8.0, // I - Strong for consistent performance (corrected scale)
        3.0, // D - Moderate to prevent overshoot (corrected scale)
        23.0 // F - Feedforward for 3000 RPM baseline (32767/1400, corrected scale)
    );
    // Apply PIDF to both shooter motors
    this.leftShooter.setPIDFCoefficients(shooterPIDF); // For detailed shooter tuning instructions, see: TeamDocs/PIDF_Shooter_Tuning_Guide.md
    this.rightShooter.setPIDFCoefficients(shooterPIDF); // For quick reference, see: TeamDocs/PIDF_Shooter_Quick_Reference.md

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
    // IMPORTANT: FTC velocity PIDF uses a different scale than normalized (0-1) control!
    // Default REV motor PIDF values are typically P=10, I=3, D=0, F=0 (not 0.01, 0.003, etc.)
    //
    // Step 1: Calculate maximum velocity in ticks/second
    //   Max velocity = (300 RPM × 383.748 PPR) / 60 seconds = 1918.74 ticks/sec
    //   Typical velocity = (200 RPM × 383.748 PPR) / 60 seconds = 1279.16 ticks/sec
    //
    // Step 2: Calculate F (Feedforward) - Start here!
    //   For FTC velocity control: F = 32767 / max_velocity_ticks_per_sec
    //   F = 32767 / 1918.74 ≈ 17.07
    //   Recommended starting value: Kf = 17.0
    //   (This provides baseline power proportional to target velocity)
    //
    // Step 3: Calculate P (Proportional)
    //   Start with default working value: Kp = 10.0
    //   Increase if response is too slow (try 12-15)
    //   Decrease if motor oscillates (try 7-9)
    //
    // Step 4: Calculate I (Integral)
    //   Start with default working value: Ki = 3.0
    //   Increase to 4-5 if motor doesn't reach target speed
    //   Decrease to 1-2 if motor overshoots or oscillates
    //
    // Step 5: Calculate D (Derivative)
    //   Start with: Kd = 0.0 (usually not needed for velocity control)
    //   Add small value (0.5-2.0) only if oscillation occurs
    //
    // RECOMMENDED STARTING VALUES (for 200-300 RPM operation):
    //   Kp = 12.0    // Proportional gain (improved from default 10)
    //   Ki = 3.0     // Integral gain (keep default, works well)
    //   Kd = 0.0     // Derivative gain (not needed)
    //   Kf = 17.0    // Feedforward gain (CRITICAL - was missing!)
    //
    // TO APPLY THESE VALUES, uncomment and customize the code below:
    /*
    // Set motors to use velocity control with encoders
    frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    
    // Define PIDF coefficients
    // These values are tuned for ~200-300 RPM operation
    // Note: FTC velocity PIDF uses larger scale than normalized control
    // Default REV values are P=10, I=3, D=0, F=0
    // We add F=17 for feedforward and increase P slightly for better response
    PIDFCoefficients drivePIDF = new PIDFCoefficients(
    12.0,  // P - Proportional gain (increased from default 10 for better response)
    3.0,   // I - Integral gain (default, works well)
    0.0,   // D - Derivative gain (not needed for velocity control)
    17.0   // F - Feedforward gain (CRITICAL for velocity control: 32767/1918.74)
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
    //    - For FTC velocity control, F should be around 32767 / max_ticks_per_sec
    //    - This gives F ≈ 17 for your motors (was 0 in defaults, causing poor tracking!)
    // 2. Add P to improve response time and accuracy (start with P=10, adjust 7-15 range)
    // 3. Add I only if motor doesn't quite reach target speed (default I=3 works well)
    // 4. Add D only if motor oscillates or overshoots (usually D=0 for velocity control)
    // 5. Test under actual robot load conditions!
    //
    // WHY PREVIOUS VALUES WERE TOO LOW:
    // The previous PIDF values (P=0.015, I=0.0003, D=0.0002, F=0.0008) were calculated
    // using formulas for normalized (0-1) control systems. FTC's velocity PIDF uses
    // a different internal scale where typical values are P=10, I=3, F=17, not 0.01!
    // This is roughly 1000x larger scale than assumed. The correct F formula is:
    //   F = 32767 / max_velocity_ticks_per_sec  (NOT 1.0 / max_velocity)
    //
    // For detailed tuning instructions, see: TeamDocs/PIDF_Tuning_Guide.md
    // ==================================================================================

    this.indexerServo = new PositionServo(hardwareMap.get(Servo.class, DeviceNames.CH_SERVO_1.getDeviceName()), 300,
        PositionServo.ServoMode.CENTERED);
    this.leftColorSensor = hardwareMap.get(ColorSensor.class, DeviceNames.CH_I2C_0.getDeviceName());
    this.rightColorSensor = hardwareMap.get(ColorSensor.class, DeviceNames.EH_I2C_0.getDeviceName());
    this.indexer = new Indexer(this.indexerServo, this.leftColorSensor, this.rightColorSensor);
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