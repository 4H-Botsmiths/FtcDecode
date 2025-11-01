package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.Telemetry;

@TeleOp(name = "Indexer Diagnostics", group = "Diagnostics")
public class IndexerDiagnostics extends LinearOpMode {

  // TODO: Declare your indexer hardware components here
  // Example: public DcMotor indexerMotor = null;
  // Example: public Servo indexerServo = null;

  @Override
  public void runOpMode() {
    telemetry.setAutoClear(false);
    Telemetry.Item statusItem = telemetry.addData("Status", "Initializing...");
    
    // TODO: Add telemetry items for your indexer components
    // Example: Telemetry.Item indexerItem = telemetry.addData("Indexer Motor", "Waiting...");
    
    telemetry.update();

    // TODO: Initialize your indexer hardware from hardwareMap
    // Example: indexerMotor = hardwareMap.get(DcMotor.class, DeviceNames.INDEXER_MOTOR.getDeviceName());

    statusItem.setValue("Initialized - Hit Start When Ready");
    telemetry.update();
    
    waitForStart();
    
    statusItem.setValue("Running Indexer Tests...");
    telemetry.update();

    // TODO: Add your test logic here
    // This is where you'll test your indexer functionality

    statusItem.setValue("Done, See Below For Test Results");
    telemetry.update();
    
    while (opModeIsActive()) {
      sleep(100);
    }
  }

  // TODO: Add helper methods for your indexer tests
  // Example:
  // private void testIndexerMotor() {
  //   // Your test code here
  // }
}
