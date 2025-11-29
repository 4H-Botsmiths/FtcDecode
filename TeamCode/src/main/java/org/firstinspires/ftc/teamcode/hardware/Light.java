package org.firstinspires.ftc.teamcode.hardware;

import com.qualcomm.robotcore.hardware.LED;

public class Light {
  private final LED ledGreen;
  private final LED ledRed;

  public Light(LED ledRed, LED ledGreen) {
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
