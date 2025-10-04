package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.LED;

public class Lights {
  private final LED ledGreen;
  private final LED ledRed;

  public Lights(LED ledGreen, LED ledRed) {
    this.ledGreen = ledGreen;
    this.ledRed = ledRed;
  }

  public void setGreen(boolean state) {
    ledGreen.enable(state);
  }

  public void setRed(boolean state) {
    ledRed.enable(state);
  }

  public void on() {
    ledGreen.enable(true);
    ledRed.enable(true);
  }

  public void off() {
    ledGreen.enable(false);
    ledRed.enable(false);
  }

  public boolean getGreen() {
    return ledGreen.isLightOn();
  }

  public boolean getRed() {
    return ledRed.isLightOn();
  }
}
