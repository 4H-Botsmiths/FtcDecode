package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.hardware.Indexer;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

@Autonomous(name = "Color Tester", group = "Diagnostics")
public class ColorTester extends LinearOpMode {

  private RevColorSensorV3 leftColorSensor;
  private RevColorSensorV3 rightColorSensor;

  @Override
  public void runOpMode() {
    // Adjust these names to match your configuration
    leftColorSensor = hardwareMap.get(RevColorSensorV3.class, DeviceNames.EH_I2C_0.getDeviceName());
    rightColorSensor = hardwareMap.get(RevColorSensorV3.class, DeviceNames.CH_I2C_0.getDeviceName());

    telemetry.addLine("Color Tester Initialized");
    telemetry.addLine("Press PLAY to start sampling.");
    telemetry.update();

    waitForStart();

    while (opModeIsActive()) {
      Indexer.BallColor leftColor = detectColor(leftColorSensor);
      Indexer.BallColor rightColor = detectColor(rightColorSensor);

      telemetry.addLine("=== LEFT SENSOR ===");
      logSensor("Left", leftColorSensor, leftColor);

      telemetry.addLine("=== RIGHT SENSOR ===");
      logSensor("Right", rightColorSensor, rightColor);

      telemetry.update();

      // Slow down updates a bit so output is readable
      sleep(100);
    }
  }

  /**
   * Mirrors Indexer.detectColor logic, but works directly on a sensor.
   */
  private Indexer.BallColor detectColor(RevColorSensorV3 sensor) {
    int green = sensor.green();
    int blue = sensor.blue();

    if (sensor.getDistance(DistanceUnit.MM) > 50) {
      return Indexer.BallColor.NONE;
    }

    if (blue > green) {
      return Indexer.BallColor.PURPLE;
    }

    if (green > blue) {
      return Indexer.BallColor.GREEN;
    }

    return Indexer.BallColor.UNKNOWN;
  }

  private void logSensor(String label, RevColorSensorV3 sensor, Indexer.BallColor interpreted) {
    int red = sensor.red();
    int green = sensor.green();
    int blue = sensor.blue();
    double distanceMm = sensor.getDistance(DistanceUnit.MM);

    telemetry.addData(label + " R", red);
    telemetry.addData(label + " G", green);
    telemetry.addData(label + " B", blue);
    telemetry.addData(label + " Dist (mm)", "%.1f", distanceMm);
    telemetry.addData(label + " Color", interpreted);
  }
}