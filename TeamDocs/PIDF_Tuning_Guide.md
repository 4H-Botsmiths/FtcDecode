# PIDF Tuning Guide for Drive Motors

## Overview
This guide explains how to calculate and tune PIDF (Proportional-Integral-Derivative-Feedforward) values for your drive motors. PIDF control is essential for accurate velocity control when using `RUN_USING_ENCODER` or `RUN_TO_POSITION` modes on DC motors.

## Your Motor Specifications
- **Motor Type**: (Appears to be using goBILDA or similar motors based on gear ratio)
- **Gear Ratio**: ~20:1 (calculated from PPR formula in code)
- **Peak RPM**: 300 RPM
- **Typical Operating RPM**: 200 RPM
- **Encoder PPR (Pulses Per Rotation)**: Calculated as `((1 + (46/17)) * (1 + (46/11))) * 28` ≈ 383.748 ticks/revolution

## What is PIDF?

PIDF is a closed-loop control algorithm that helps motors maintain a target velocity or position accurately. Each component serves a specific purpose:

### P - Proportional
- **What it does**: Provides correction proportional to the error
- **Formula**: `P_output = Kp × error`
- **Effect**: Stronger P means faster response but can cause oscillation
- **When to increase**: Motor responds too slowly to speed changes
- **When to decrease**: Motor oscillates or overshoots target speed

### I - Integral  
- **What it does**: Eliminates steady-state error by accumulating past errors
- **Formula**: `I_output = Ki × Σ(error × dt)`
- **Effect**: Helps reach exact target speed over time
- **When to increase**: Motor settles below target speed permanently
- **When to decrease**: Motor has overshoot or oscillation that takes long to settle

### D - Derivative
- **What it does**: Dampens oscillation by responding to rate of error change
- **Formula**: `D_output = Kd × (d(error)/dt)`
- **Effect**: Reduces overshoot and stabilizes system
- **When to increase**: Too much overshoot or oscillation
- **When to decrease**: Motor responds too slowly or sluggishly

### F - Feedforward
- **What it does**: Anticipates required power based on target velocity
- **Formula**: `F_output = Kf × target_velocity`
- **Effect**: Improves response time by preemptively applying power
- **When to increase**: Motor lags behind target speed changes
- **When to decrease**: Motor overshoots when changing speeds

## How PIDF Works Together

The total motor power is calculated as:
```
power = (Kp × error) + (Ki × Σerror) + (Kd × Δerror) + (Kf × target)
```

Where:
- `error = target_velocity - current_velocity`
- `Σerror = accumulated error over time`
- `Δerror = rate of change of error`
- `target = target velocity in encoder ticks per second`

## Calculating Initial PIDF Values

### Step 1: Calculate Maximum Velocity
```
Max velocity (ticks/sec) = (Max RPM × PPR) / 60
                         = (300 × 383.748) / 60
                         = 1918.74 ticks/second

Typical velocity (ticks/sec) = (200 × 383.748) / 60
                              = 1279.16 ticks/second
```

### Step 2: Calculate F (Feedforward)
F is the most important coefficient for velocity control.

**Formula**: `Kf = (Max motor power) / (Max velocity)`

For FTC motors using normalized power (-1.0 to +1.0):
```
Kf = 1.0 / 1918.74
Kf ≈ 0.000521

For typical operations at 200 RPM:
Kf = 1.0 / 1279.16
Kf ≈ 0.000782
```

**Recommendation**: Start with F calculated for your typical operating speed (200 RPM):
```
Kf = 0.000782  (or approximately 0.0008)
```

### Step 3: Calculate P (Proportional)
P should be set to provide quick response without oscillation.

**Rule of thumb**: `Kp ≈ 10 × Kf` to `Kp ≈ 100 × Kf`

For your motors:
```
Kp ≈ 10 × 0.000782 = 0.00782  (conservative)
Kp ≈ 20 × 0.000782 = 0.01564  (moderate)
Kp ≈ 50 × 0.000782 = 0.03910  (aggressive)
```

**Recommendation**: Start with moderate value:
```
Kp = 0.015
```

### Step 4: Set I (Integral)
I helps eliminate steady-state error but can cause instability.

**Rule of thumb**: `Ki ≈ Kp / 10` to `Ki ≈ Kp / 100`

For your motors:
```
Ki ≈ 0.015 / 10 = 0.0015  (moderate)
Ki ≈ 0.015 / 100 = 0.00015  (conservative)
```

**Recommendation**: Start conservative:
```
Ki = 0.0001 to 0.0005
```

### Step 5: Set D (Derivative)
D dampens oscillation but can make system sluggish.

**Rule of thumb**: `Kd ≈ Kp / 10` to `Kp / 100`

For your motors:
```
Kd ≈ 0.015 / 10 = 0.0015  (moderate)
Kd ≈ 0.015 / 100 = 0.00015  (conservative)
```

**Recommendation**: Start with zero or very small:
```
Kd = 0.0 to 0.0005
```

## Recommended Starting Values

Based on your motor specifications (300 RPM peak, 200 RPM typical operation):

```java
// For velocity control at typical 200 RPM operations
Kp = 0.015
Ki = 0.0003
Kd = 0.0002
Kf = 0.0008
```

Alternative conservative starting point:
```java
// More conservative, less likely to oscillate
Kp = 0.010
Ki = 0.0001
Kd = 0.0000
Kf = 0.0008
```

## How to Set PIDF Values in Code

Add this to your `Robot.java` constructor after motor initialization:

```java
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.DcMotor;

// In Robot constructor, after drive motor initialization:

// Set motors to use velocity control with encoders
frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

// Define PIDF coefficients for velocity control
// Tuned for ~200 RPM typical operation (can peak at 300 RPM)
PIDFCoefficients pidfCoefficients = new PIDFCoefficients(
    0.015,  // P - Proportional gain
    0.0003, // I - Integral gain
    0.0002, // D - Derivative gain
    0.0008  // F - Feedforward gain
);

// Apply PIDF coefficients to each drive motor
// Note: Only works with REV Expansion Hub/Control Hub firmware 1.8.2+
frontLeft.asDcMotorEx().setVelocityPIDFCoefficients(
    pidfCoefficients.p,
    pidfCoefficients.i,
    pidfCoefficients.d,
    pidfCoefficients.f
);
frontRight.asDcMotorEx().setVelocityPIDFCoefficients(
    pidfCoefficients.p,
    pidfCoefficients.i,
    pidfCoefficients.d,
    pidfCoefficients.f
);
rearLeft.asDcMotorEx().setVelocityPIDFCoefficients(
    pidfCoefficients.p,
    pidfCoefficients.i,
    pidfCoefficients.d,
    pidfCoefficients.f
);
rearRight.asDcMotorEx().setVelocityPIDFCoefficients(
    pidfCoefficients.p,
    pidfCoefficients.i,
    pidfCoefficients.d,
    pidfCoefficients.f
);
```

## Tuning Process

### 1. Test F (Feedforward) First
F is the most important for velocity control.

1. Set P=I=D=0, only use F
2. Command a constant velocity (e.g., 150 RPM)
3. Observe if motor reaches approximately the right speed
4. Adjust F until motor gets close to target speed
5. If motor is consistently too slow, increase F
6. If motor is consistently too fast, decrease F

### 2. Add P (Proportional)
Once F is reasonable:

1. Add small P value (start with 0.01)
2. Command velocity changes
3. If response is too slow, increase P
4. If motor oscillates, decrease P
5. Goal: Motor quickly reaches target without oscillating

### 3. Add I (Integral) if Needed
Only if motor doesn't quite reach target:

1. Add small I value (start with 0.0001)
2. Watch for steady-state error elimination
3. If motor still doesn't reach target, slightly increase I
4. If motor has sustained oscillation, decrease I

### 4. Add D (Derivative) if Needed
Only if motor overshoots or oscillates:

1. Add small D value (start with 0.0001)
2. Watch for dampening of oscillation
3. Increase D if oscillation persists
4. Decrease D if motor becomes sluggish

## Testing Checklist

Create a test OpMode to verify your PIDF values:

- [ ] Motor smoothly accelerates to target speed
- [ ] Motor maintains target speed under no load
- [ ] Motor maintains target speed under typical load
- [ ] Motor doesn't oscillate or vibrate at constant speed
- [ ] Motor responds quickly to speed changes (within ~0.5 seconds)
- [ ] Motor doesn't overshoot when changing speeds
- [ ] All four drive motors behave similarly

## Common Issues and Solutions

### Motor oscillates/vibrates at constant speed
- **Cause**: P or I too high
- **Fix**: Reduce P by 20-30%, or reduce I to near zero

### Motor doesn't reach target speed
- **Cause**: F too low or I too low
- **Fix**: Increase F by 10-20%, or add small I value

### Motor overshoots when changing speeds
- **Cause**: P too high, D too low, or F too high
- **Fix**: Reduce P, add small D, or reduce F slightly

### Motor responds too slowly
- **Cause**: P too low or F too low
- **Fix**: Increase P by 20-30%, or increase F by 10%

### Different behavior under load vs. no load
- **Cause**: F not optimized, or I needed
- **Fix**: Tune F for loaded condition, add small I value

## Advanced Considerations

### Why F Changes with Operating Speed
The feedforward coefficient F is speed-dependent because:
- At 200 RPM: `F = 1.0 / 1279.16 ≈ 0.000782`
- At 300 RPM: `F = 1.0 / 1918.74 ≈ 0.000521`

If you operate at different speeds frequently, you might need to:
1. Use the F value for your most common operating speed
2. Increase P to compensate for F inaccuracy at other speeds
3. Accept slight performance variation at extreme speeds

### Motor Load Variation
If your robot weight or traction changes significantly:
- You may need to retune, especially F and I
- Higher load usually requires higher F
- Higher load may need higher I to maintain accuracy

### Temperature Effects
Motors perform differently when cold vs. hot:
- Hot motors may have slightly less torque
- This usually only affects F slightly
- Tune at normal operating temperature

## When NOT to Use PIDF

You may not need PIDF tuning if:
- Using `RUN_WITHOUT_ENCODER` mode (no closed-loop control)
- Using simple `setPower()` commands (open-loop control)
- Motors naturally reach good speeds without oscillation

## References and Further Reading

- FTC SDK Documentation: https://ftc-docs.firstinspires.org/
- REV Robotics Expansion Hub Firmware: Requires version 1.8.2 or higher for PIDF
- PID Control Theory: https://en.wikipedia.org/wiki/PID_controller
- Game Manual 1: Always check for legal requirements and restrictions

## Revision History

- 2025-11-05: Initial version for 300 RPM peak / 200 RPM typical drive motors
