package org.firstinspires.ftc.teamcode.programs.diagnostics;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.hardware.Lift; // Using hardware Lift implementation
import org.firstinspires.ftc.teamcode.hardware.Motor;
import org.firstinspires.ftc.teamcode.hardware.DeviceNames;

@TeleOp(name = "Going Up", group = "Diagnostics")
public class GoingUp extends OpMode {

  private Lift lift;
  private Motor leftLift;
  private Motor rightLift;
  private static final double DEADZONE = 0.05;

  @Override
  public void init() {
    // Instantiate motors from Expansion Hub ports 2 & 3 (style matches Robot.java)
    leftLift = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_2.getDeviceName())); // 28 PPR typical
    rightLift = new Motor(hardwareMap.get(DcMotorEx.class, DeviceNames.EH_MOTOR_3.getDeviceName()));

    // Reverse one motor if mechanically mirrored so positive power raises lift
    rightLift.setDirection(DcMotorSimple.Direction.REVERSE);

    lift = new Lift(leftLift, rightLift);
    lift.setZeroPowerBehavior(com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE);

    telemetry.addLine("GoingUp initialized (Lift on CH_MOTOR_2 & CH_MOTOR_3)");
  }

  @Override
  public void loop() {
    double stick = -gamepad1.right_stick_y; // Invert so up on stick gives positive power.
    if (Math.abs(stick) < DEADZONE)
      stick = 0.0;

    // Send power to lift (clip to valid range if Lift does not already).
    double power = Math.max(-1.0, Math.min(1.0, stick));
    lift.setPower(power);

    telemetry.addData("Stick", stick);
    telemetry.addData("Power", power);
    telemetry.addData("Avg RPM", lift.getRPM());
    telemetry.addData("Avg Pos", lift.getCurrentPosition());
    telemetry.update();
  }

  @Override
  public void stop() {
    if (lift != null) {
      lift.setPower(0);
    }
  }
}
