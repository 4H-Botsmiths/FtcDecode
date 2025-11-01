package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.hardware.Robot;

@TeleOp(name = "Indexer Diagnostics", group = "Diagnostics")
public class IndexerDiagnostics extends LinearOpMode {

  private Robot robot;

  @Override
  public void runOpMode() {
    telemetry.setAutoClear(true);
    telemetry.addData("Status", "Initializing...");
    telemetry.update();

    robot = new Robot(hardwareMap);

    telemetry.addData("Status", "Ready - Press Start");
    telemetry.update();
    
    waitForStart();
    
    while (opModeIsActive()) {
      String leftColor = detectColor(robot.colorSensorLeft);
      String rightColor = detectColor(robot.colorSensorRight);
      
      telemetry.addData("Left", leftColor);
      telemetry.addData("Right", rightColor);
      telemetry.update();
      
      sleep(100);
    }
  }

  private String detectColor(com.qualcomm.robotcore.hardware.ColorSensor sensor) {
    int red = sensor.red();
    int green = sensor.green();
    int blue = sensor.blue();
    
    // Detect purple (high red and blue, low green)
    if (red > green && blue > green && red > 100 && blue > 100) {
      return "Purple";
    }
    
    // Detect green (high green, lower red and blue)
    if (green > red && green > blue && green > 100) {
      return "Green";
    }
    
    return "Empty";
  }
}
