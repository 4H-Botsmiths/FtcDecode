package org.firstinspires.ftc.teamcode.programs.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.teamcode.hardware.Robot;

/**
 * This file contains an example of an iterative (Non-Linear) "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop
 * period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When an selection is made from the menu, the corresponding OpMode
 * class Basic instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two
 * wheeled robot
 * It includes all the skeletal structure that all iterative OpModes contain.
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code
 * folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver
 * Station OpMode list
 */

@TeleOp(name = "Shooter Teleop", group = "A")
public class ShooterTeleop extends OpMode {
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
    // Tell the driver that initialization is complete.
    telemetry.addData("Left Shooter Ticks Per Rotation",
        robot.leftShooter.asDcMotorEx().getMotorType().getTicksPerRev());
    telemetry.addData("Right Shooter Ticks Per Rotation",
        robot.rightShooter.asDcMotorEx().getMotorType().getTicksPerRev());
    telemetry.addData("Left Shooter Max RPM",
        robot.leftShooter.asDcMotorEx().getMotorType().getMaxRPM());
    telemetry.addData("Right Shooter Max RPM",
        robot.rightShooter.asDcMotorEx().getMotorType().getMaxRPM());
    telemetry.addData("Status", "Initialized");
    telemetry.update();
  }

  /*
   * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
   */
  @Override
  public void init_loop() {
  }

  /*
   * Code to run ONCE when the driver hits PLAY
   */
  @Override
  public void start() {
  }

  /*
   * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
   */
  @Override
  public void loop() {
    // y=1.0, x=0.75, b=0.5, a=0.25
    if (gamepad1.a) {
      robot.leftShooter.setSpeed(1.0);
      robot.rightShooter.setSpeed(1.0);
    } else if (gamepad1.x) {
      robot.leftShooter.setSpeed(0.75);
      robot.rightShooter.setSpeed(0.75);
    } else if (gamepad1.y) {
      robot.leftShooter.setSpeed(0.5);
      robot.rightShooter.setSpeed(0.5);
    } else if (gamepad1.b) {
      robot.leftShooter.setSpeed(0.25);
      robot.rightShooter.setSpeed(0.25);
    } else {
      robot.leftShooter.setSpeed(0);
      robot.rightShooter.setSpeed(0);
    }

    robot.intake.setPower(gamepad1.right_trigger - gamepad1.left_trigger);
    telemetry.addData("Left Shooter Velocity", robot.leftShooter.getVelocity());
    telemetry.addData("Right Shooter Velocity", robot.rightShooter.getVelocity());
    telemetry.addData("Left Shooter RPM", robot.leftShooter.getRPM());
    telemetry.addData("Right Shooter RPM", robot.rightShooter.getRPM());
    telemetry.addData("Intake Power", robot.intake.getPower());
    telemetry.update();
  }

  /*
   * Code to run ONCE after the driver hits STOP
   */
  @Override
  public void stop() {
  }

}
