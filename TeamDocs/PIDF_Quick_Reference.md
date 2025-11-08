# PIDF Quick Reference Card

## Your Motor Specs
- **Peak RPM**: 300
- **Typical RPM**: 200
- **Encoder PPR**: 383.748 ticks/revolution
- **Max Velocity**: 1918.74 ticks/second (at 300 RPM)
- **Typical Velocity**: 1279.16 ticks/second (at 200 RPM)

## Recommended Starting Values

### For Typical 200-300 RPM Operation
```java
Kp = 12.0   // Proportional (improved from default 10)
Ki = 3.0    // Integral (default, works well)
Kd = 0.0    // Derivative (not needed for velocity)
Kf = 17.0   // Feedforward (CRITICAL - 32767/max_velocity)
```

### Conservative Alternative (Closer to Defaults)
```java
Kp = 10.0   // Default proportional gain
Ki = 3.0    // Default integral gain  
Kd = 0.0    // Default derivative gain
Kf = 17.0   // Feedforward (add to defaults)
```

## Quick Formulas

### IMPORTANT: FTC Velocity PIDF Scale
FTC's velocity PIDF system uses a different scale than normalized (0-1) control systems.
Typical values are P=10, I=3, D=0, F=17 (not 0.01, 0.003, etc.)

### Calculate F (Feedforward)
```
F = 32767 / max_velocity_ticks_per_sec

For 300 RPM: F = 32767 / 1918.74 ≈ 17.07
For 200 RPM: F = 32767 / 1279.16 ≈ 25.61
Recommend: F = 17.0 (based on peak velocity of 300 RPM)
```

### Calculate P (Proportional)
```
Start with default: P = 10.0
Adjust based on response:
  - Too slow: increase to 12-15
  - Oscillates: decrease to 7-9
```

### Calculate I (Integral)
```
Start with default: I = 3.0
Adjust based on steady-state error:
  - Doesn't reach target: increase to 4-5
  - Overshoots: decrease to 1-2
```

### Calculate D (Derivative)
```
Start with: D = 0.0 (usually not needed)
Add only if oscillation occurs: D = 0.5-2.0
```

## Troubleshooting

| Problem | Likely Cause | Solution |
|---------|--------------|----------|
| Motor oscillates/vibrates | P too high | Reduce P from 12 to 9-10 |
| Doesn't reach target speed | F too low or I too low | Increase F to 18-20 or I to 4-5 |
| Overshoots target | P too high | Reduce P to 8-10 |
| Responds too slowly | P or F too low | Increase P to 13-15 or F to 18-20 |
| Different motors behave differently | Motor-to-motor variation | May need individual tuning |

## Tuning Order
1. **F first** - Set P=I=D=0, tune F until close to target
2. **Add P** - Improve response time and accuracy
3. **Add I (if needed)** - Only if steady-state error exists
4. **Add D (if needed)** - Only if oscillation occurs

## Code Template
```java
// In Robot.java constructor, after motor initialization:

// Set to velocity control mode
frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

// Create PIDF coefficients
// Note: FTC velocity PIDF uses larger scale (P=10, not 0.01)
PIDFCoefficients pidf = new PIDFCoefficients(
    12.0,  // P - Improved from default 10
    3.0,   // I - Default, works well
    0.0,   // D - Not needed for velocity
    17.0   // F - Critical: 32767/max_velocity
);

// Apply to each motor
frontLeft.asDcMotorEx().setVelocityPIDFCoefficients(
    pidf.p, pidf.i, pidf.d, pidf.f);
frontRight.asDcMotorEx().setVelocityPIDFCoefficients(
    pidf.p, pidf.i, pidf.d, pidf.f);
rearLeft.asDcMotorEx().setVelocityPIDFCoefficients(
    pidf.p, pidf.i, pidf.d, pidf.f);
rearRight.asDcMotorEx().setVelocityPIDFCoefficients(
    pidf.p, pidf.i, pidf.d, pidf.f);
```

## Testing
Use the `PIDFTuningTest.java` OpMode to:
- Test motors at different speeds
- Measure response time and accuracy
- See real-time velocity tracking
- Verify no oscillation under load

## Key Points
- **F is most important** for velocity control
- Tune at your **typical operating speed** (200 RPM), not peak
- Always test under **actual robot load**
- Target < 5% velocity error for good control
- Motors should reach target within 0.5 seconds
- No vibration or oscillation at steady speeds

## When to Retune
- Robot weight changes significantly
- Different traction/friction conditions
- Motors get hot during long runs
- Competition field conditions differ from practice

## Full Documentation
See `PIDF_Tuning_Guide.md` for complete explanations and theory
