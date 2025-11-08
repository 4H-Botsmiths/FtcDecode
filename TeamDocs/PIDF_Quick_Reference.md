# PIDF Quick Reference Card

## Your Motor Specs
- **Peak RPM**: 300
- **Typical RPM**: 200
- **Encoder PPR**: 383.748 ticks/revolution
- **Max Velocity**: 1918.74 ticks/second (at 300 RPM)
- **Typical Velocity**: 1279.16 ticks/second (at 200 RPM)

## Recommended Starting Values

### For Typical 200 RPM Operation
```java
Kp = 0.015   // Proportional
Ki = 0.0003  // Integral
Kd = 0.0002  // Derivative
Kf = 0.0008  // Feedforward (MOST IMPORTANT)
```

### Conservative Alternative (Less Oscillation Risk)
```java
Kp = 0.010
Ki = 0.0001
Kd = 0.0000
Kf = 0.0008
```

## Quick Formulas

### Calculate F (Feedforward)
```
F = 1.0 / (target_RPM × PPR / 60)

For 200 RPM: F = 1.0 / 1279.16 ≈ 0.000782
For 300 RPM: F = 1.0 / 1918.74 ≈ 0.000521
```

### Calculate P (Proportional)
```
P = (10 to 100) × F

Typical: P = 20 × F ≈ 0.015
```

### Calculate I (Integral)
```
I = P / (10 to 100)

Typical: I = P / 50 ≈ 0.0003
```

### Calculate D (Derivative)
```
D = P / (10 to 100)

Typical: D = P / 75 ≈ 0.0002
```

## Troubleshooting

| Problem | Likely Cause | Solution |
|---------|--------------|----------|
| Motor oscillates/vibrates | P or I too high | Reduce P by 30% or reduce I |
| Doesn't reach target speed | F too low | Increase F by 10-20% |
| Overshoots target | P too high or D too low | Reduce P or add small D |
| Responds too slowly | P or F too low | Increase P by 30% or F by 10% |
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
PIDFCoefficients pidf = new PIDFCoefficients(
    0.015,  // P
    0.0003, // I
    0.0002, // D
    0.0008  // F
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
