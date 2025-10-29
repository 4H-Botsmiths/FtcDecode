package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import com.qualcomm.robotcore.util.Range;

/**
 * Robot class that encapsulates all hardware components based on the port mapping document.
 * Uses CH_ prefix for Control Hub devices and EH_ prefix for Expansion Hub devices.
 * This class only initializes hardware and provides meaningful names - no behavior methods.
 */
public class Robot {

    // === CONTROL HUB MOTORS ===
    public final DcMotorEx frontRight;   // CH_MOTOR_0
    public final DcMotorEx frontLeft;    // CH_MOTOR_1
    public final DcMotorEx rearRight;    // CH_MOTOR_2
    public final DcMotorEx rearLeft;     // CH_MOTOR_3

    // === EXPANSION HUB MOTORS ===
    public final DcMotorEx leftShooter;  // EH_MOTOR_0
    public final DcMotorEx rightShooter; // EH_MOTOR_1

    // === CONTROL HUB SERVOS ===
    public final CRServo feederLeft;     // CH_SERVO_0
    public final CRServo indexer;        // CH_SERVO_1

    // === EXPANSION HUB SERVOS ===
    public final CRServo feederRight;    // EH_SERVO_0

    // === COLOR SENSORS ===
    public final ColorSensor colorSensorLeft;  // CH_I2C_0
    public final ColorSensor colorSensorRight; // EH_I2C_0
    
    // === CAMERAS ===
    public final WebcamName webcam;            // Webcam 1

    /**
     * Initialize all robot hardware from the hardware map
     * @param hardwareMap The hardware map from the OpMode
     */
    public Robot(HardwareMap hardwareMap) {
        // Initialize Control Hub Motors
        this.frontRight = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_0.getDeviceName());
        this.frontLeft = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_1.getDeviceName());
        this.rearRight = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_2.getDeviceName());
        this.rearLeft = hardwareMap.get(DcMotorEx.class, DeviceNames.CH_MOTOR_3.getDeviceName());

        // Initialize Expansion Hub Motors
        this.leftShooter = hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_0.getDeviceName());
        this.rightShooter = hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_1.getDeviceName());

        // Initialize Control Hub Servos
        this.feederLeft = hardwareMap.get(CRServo.class, DeviceNames.CH_SERVO_0.getDeviceName());
        this.indexer = hardwareMap.get(CRServo.class, DeviceNames.CH_SERVO_1.getDeviceName());

        // Initialize Expansion Hub Servos
        this.feederRight = hardwareMap.get(CRServo.class, DeviceNames.EH_SERVO_0.getDeviceName());

        // Initialize Color Sensors
        this.colorSensorLeft = hardwareMap.get(ColorSensor.class, DeviceNames.CH_I2C_0.getDeviceName());
        this.colorSensorRight = hardwareMap.get(ColorSensor.class, DeviceNames.EH_I2C_0.getDeviceName());
        
        // Initialize Camera
        this.webcam = hardwareMap.get(WebcamName.class, DeviceNames.WEBCAM_1.getDeviceName());
    }
}