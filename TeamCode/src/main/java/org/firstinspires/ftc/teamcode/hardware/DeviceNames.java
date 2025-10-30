package org.firstinspires.ftc.teamcode.hardware;

/**
 * Enum containing all hardware device names as defined in the port mapping document.
 * Uses CH_ prefix for Control Hub devices and EH_ prefix for Expansion Hub devices.
 */
public enum DeviceNames {
    
    // === CONTROL HUB MOTORS ===
    CH_MOTOR_0("CH_MOTOR_0"),  // Front Right
    CH_MOTOR_1("CH_MOTOR_1"),  // Front Left
    CH_MOTOR_2("CH_MOTOR_2"),  // Rear Right
    CH_MOTOR_3("CH_MOTOR_3"),  // Rear Left
    
    // === EXPANSION HUB MOTORS ===
    EH_MOTOR_0("EH_MOTOR_0"),  // Left Shooter
    EH_MOTOR_1("EH_MOTOR_1"),  // Right Shooter
    
    // === CONTROL HUB SERVOS ===
    CH_SERVO_0("CH_SERVO_0"),  // Feeder Left
    CH_SERVO_1("CH_SERVO_1"),  // Indexer (GoBUILDA 5-turn servo 2000-0024-0502)
    
    // === EXPANSION HUB SERVOS ===
    EH_SERVO_0("EH_SERVO_0"),  // Feeder Right
    
    // === CONTROL HUB I2C ===
    CH_I2C_0("CH_I2C_0"),      // Color Sensor Left
    
    // === EXPANSION HUB I2C ===
    EH_I2C_0("EH_I2C_0"),      // Color Sensor Right
    
    // === CAMERAS ===
    WEBCAM_1("Webcam 1");      // USB Camera for AprilTag detection
    
    private final String deviceName;
    
    DeviceNames(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    @Override
    public String toString() {
        return deviceName;
    }
}