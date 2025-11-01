package org.firstinspires.ftc.teamcode.hardware;

/**
 * Enum containing all hardware device names as defined in the port mapping document.
 * Uses CH_ prefix for Control Hub devices and EH_ prefix for Expansion Hub devices.
 */
public enum DeviceNames {

  // === CONTROL HUB MOTORS ===
  CH_MOTOR_0("CH_MOTOR_0"),
  CH_MOTOR_1("CH_MOTOR_1"),
  CH_MOTOR_2("CH_MOTOR_2"),
  CH_MOTOR_3("CH_MOTOR_3"),

  // === EXPANSION HUB MOTORS ===
  EH_MOTOR_0("EH_MOTOR_0"),
  EH_MOTOR_1("EH_MOTOR_1"),
  EH_MOTOR_2("EH_MOTOR_2"),
  EH_MOTOR_3("EH_MOTOR_3"),

  // === CONTROL HUB SERVOS ===
  CH_SERVO_0("CH_SERVO_0"),
  CH_SERVO_1("CH_SERVO_1"),
  CH_SERVO_2("CH_SERVO_2"),
  CH_SERVO_3("CH_SERVO_3"),
  CH_SERVO_4("CH_SERVO_4"),

  // === EXPANSION HUB SERVOS ===
  EH_SERVO_0("EH_SERVO_0"),
  EH_SERVO_1("EH_SERVO_1"),
  EH_SERVO_2("EH_SERVO_2"),
  EH_SERVO_3("EH_SERVO_3"),
  EH_SERVO_4("EH_SERVO_4"),

  // === CONTROL HUB I2C ===
  CH_I2C_0("CH_I2C_0"),
  CH_I2C_1("CH_I2C_1"),
  CH_I2C_2("CH_I2C_2"),
  CH_I2C_3("CH_I2C_3"),

  // === EXPANSION HUB I2C ===
  EH_I2C_0("EH_I2C_0"),
  EH_I2C_1("EH_I2C_1"),
  EH_I2C_2("EH_I2C_2"),
  EH_I2C_3("EH_I2C_3"),

  // === CAMERAS ===
  WEBCAM_1("Webcam 1");

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