package org.firstinspires.ftc.teamcode.hardware;

import android.util.Size;
import com.qualcomm.robotcore.hardware.HardwareMap;
import java.util.concurrent.CompletableFuture;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.VisionPortal.CameraState;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.List;
import java.util.stream.Collectors;

/*
 * This OpMode illustrates the basics of AprilTag recognition and pose estimation, using
 * two webcams.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
 */
public class Camera {

  /*
   * Variables used for switching cameras.
   */
  public Camera(HardwareMap hardwareMap) {
    this.webcam = hardwareMap.get(WebcamName.class, "Webcam 1");
  }

  private WebcamName webcam;
  /**
   * The variable to store our instance of the AprilTag processor.
   */
  private AprilTagProcessor aprilTag;

  /**
   * The variable to store our instance of the vision portal.
   */
  public VisionPortal visionPortal;

  /**
   * Initialize the AprilTag processor.
   */
  public void initAprilTag() throws CameraNotAttachedException {
    if (!webcam.isAttached()) {
      throw new CameraNotAttachedException();
    }

    // Create the AprilTag processor by using a builder.
    aprilTag = new AprilTagProcessor.Builder()
        //TODO: .setLensIntrinsics(615.0, 615.0, 320.0, 240.0) // Focal lengths fx, fy; Principal point cx, cy
        .build();
    // Create the vision portal by using a builder.
    visionPortal = new VisionPortal.Builder()
        .setCamera(this.webcam)
        .addProcessor(aprilTag)
        .setCameraResolution(new Size(640, 480)) // Refine further, lower is better
        .setStreamFormat(VisionPortal.StreamFormat.MJPEG)
        .build();

  } // end method initAprilTag()

  /**
   * Retrieves the list of AprilTags detected by the camera.
   *
   * @return         	A list of AprilTag objects representing the detected tags.
   * @throws CameraNotStreamingException  If the camera is not currently streaming.
   * @throws CameraNotAttachedException   If the camera is not attached.
   */
  public List<AprilTag> getAprilTags()
      throws CameraNotStreamingException, CameraNotAttachedException {
    if (!webcam.isAttached()) {
      throw new CameraNotAttachedException();
    } else if (visionPortal == null) {
      initAprilTag();
    }
    if (visionPortal.getCameraState() != CameraState.STREAMING) {
      throw new CameraNotStreamingException();
    }
    List<AprilTagDetection> currentDetections = aprilTag.getDetections();
    return currentDetections.stream().filter(aprilTag -> aprilTag.metadata != null)
        .map(aprilTag -> new AprilTag(aprilTag)).collect(Collectors.toList());
  }

  public AprilTag getAprilTag(AprilTagPosition position)
      throws CameraNotStreamingException, CameraNotAttachedException {
    List<AprilTag> detections = getAprilTags();
    for (AprilTag tag : detections) {
      if (tag.position == position) {
        return tag;
      }
    }
    return null;
  }

  public void pause() throws CameraNotAttachedException {
    if (!webcam.isAttached()) {
      throw new CameraNotAttachedException();
    } else if (visionPortal == null) {
      initAprilTag();
    }
    if (this.visionPortal.getCameraState() == CameraState.OPENING_CAMERA_DEVICE) {
      //You can't stop a camera stream before the device is opened
      CompletableFuture.runAsync(() -> {
        while (this.visionPortal.getCameraState() == CameraState.OPENING_CAMERA_DEVICE) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        visionPortal.stopStreaming();
      });
    } else {
      try {
        visionPortal.stopStreaming();
      } catch (Exception e) {
        //This function has called errors before
      }
    }
  }

  public void resume() throws CameraNotAttachedException {
    if (!webcam.isAttached()) {
      throw new CameraNotAttachedException();
    } else if (visionPortal == null) {
      initAprilTag();
    }
    visionPortal.resumeStreaming();
  }

  /**
   * Add telemetry about AprilTag detections.
   */
  public void telemetryAprilTag(Telemetry telemetry) {
    telemetry.addData("Camera State", visionPortal.getCameraState().toString());
    try {
      List<AprilTag> currentDetections = getAprilTags();
      telemetry.addData("# AprilTags Detected", currentDetections.size());

      // Step through the list of detections and display info for each one.
      for (AprilTag detection : currentDetections) {
        if (detection.metadata != null) {
          telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name));
          telemetry.addLine(String.format("Location: %s", detection.position.toString()));
          telemetry.addLine(String.format("XYZ >>%6.1f<< %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y,
              detection.ftcPose.z));
          telemetry
              .addLine(String.format("PRY %6.1f %6.1f >%6.1f<  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll,
                  detection.ftcPose.yaw));
          telemetry.addLine(String.format("RBE >>%6.1f<< %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range,
              detection.ftcPose.bearing, detection.ftcPose.elevation));
        } else {
          telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id));
          telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y));
        }
      } // end for() loop

      // Add "key" information to telemetry
      telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.");
      telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)");
      telemetry.addLine("RBE = Range, Bearing & Elevation");
    } catch (CameraNotAttachedException e) {
      telemetry.addLine("Camera is not attached");
    } catch (CameraNotStreamingException e) {
      telemetry.addLine("Camera is not streaming");
    }
  } // end method telemetryAprilTag()

  public class CameraNotStreamingException extends Exception {
    public CameraNotStreamingException() {
      super("The camera is not streaming");
    }
  }

  public class CameraNotAttachedException extends Exception {
    public CameraNotAttachedException() {
      super("The camera is not attached");
    }
  }

  public enum AprilTagPosition {
    GOAL, OBELISK, UNKNOWN
  }

  public class AprilTag extends AprilTagDetection {
    public AprilTagPosition position = AprilTagPosition.UNKNOWN;

    public AprilTag(AprilTagDetection detection) {
      super(detection.id, detection.hamming, detection.decisionMargin, detection.center, detection.corners,
          detection.metadata, detection.ftcPose, detection.rawPose, detection.robotPose,
          detection.frameAcquisitionNanoTime);
      if (detection.metadata == null) {
        throw new IllegalStateException(String.format("Detection with ID: %d has no metadata", detection.id));
      }
      if (detection.id == 20 || detection.id == 24) {
        position = AprilTagPosition.GOAL;
      }
      if (detection.id == 21 || detection.id == 22 || detection.id == 23) {
        position = AprilTagPosition.OBELISK;
      }
    }
  }
} // end class
