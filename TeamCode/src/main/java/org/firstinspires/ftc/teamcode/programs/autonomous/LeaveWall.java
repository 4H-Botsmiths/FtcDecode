package org.firstinspires.ftc.teamcode.programs.autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.hardware.Robot;

@Autonomous(name = "Drive Forward", group = "A", preselectTeleOp = "Decode Camera TeleOp")
public class LeaveWall extends OpMode {
  public Robot robot;

  // 
  /*
   * Code to run ONCE when the driver hits INIT
   */
  @Override
  public void init() {
    telemetry.addData("Status", "Initializing");
    telemetry.update();
    this.robot = new Robot(hardwareMap);
    telemetry.addData("Status", "Initialized");
    telemetry.update();
  }

  /*
   * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
   */
  @Override
  public void init_loop() {
  }

  ElapsedTime timer = new ElapsedTime();

  /*
   * Code to run ONCE when the driver hits PLAY
   */
  @Override
  public void start() {
    timer.reset();
  }

  /*
   * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
   */
  @Override
  public void loop() {
    if (timer.milliseconds() < 2000) {
      robot.drive(0, 0.25, 0);
    } else {
      robot.drive(0, 0, 0);
    }
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
  }

}
