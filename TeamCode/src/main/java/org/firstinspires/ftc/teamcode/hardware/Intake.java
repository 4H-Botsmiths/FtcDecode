package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.util.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Intake {
  private final List<CRServo> servos;

  public Intake(CRServo... servos) {
    this.servos = Collections.unmodifiableList(Arrays.asList(servos));
  }

  public static Intake fromHardwareMap(HardwareMap hardwareMap, String... servoNames) {
    List<CRServo> list = new ArrayList<>(servoNames.length);
    for (String name : servoNames) {
      list.add(hardwareMap.get(CRServo.class, name));
    }
    return new Intake(list.toArray(new CRServo[0]));
  }

  // Batch power controls
  public void setPowerAll(double power) {
    double p = Range.clip(power, -1.0, 1.0);
    for (CRServo s : servos)
      s.setPower(p);
  }

  public void stopAll() {
    setPowerAll(0.0);
  }

  // Utilities
  public int size() {
    return servos.size();
  }

  public CRServo get(int index) {
    return servos.get(index);
  }

  public List<CRServo> asList() {
    return servos; // already unmodifiable
  }

  public double[] getPowers() {
    double[] out = new double[servos.size()];
    for (int i = 0; i < servos.size(); i++)
      out[i] = servos.get(i).getPower();
    return out;
  }
}