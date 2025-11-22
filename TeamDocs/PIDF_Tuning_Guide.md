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

### IMPORTANT: Understanding FTC's PIDF Scale
FTC's velocity PIDF system uses a different internal scale than normalized (0-1) control systems.
The default REV motor PIDF values are typically P=10, I=3, D=0, F=0 (not 0.01, 0.003, etc.).
This is roughly 1000x larger than what you'd expect from normalized formulas.

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

**Correct Formula for FTC**: `Kf = 32767 / max_velocity_ticks_per_sec`

This uses FTC's internal scale where 32767 represents maximum motor power.

```
Kf = 32767 / 1918.74
Kf ≈ 17.07

For typical operations at 200 RPM:
Kf = 32767 / 1279.16  
Kf ≈ 25.61
```

**Recommendation**: Use F based on your peak velocity (300 RPM) so it works across the full range:
```
Kf = 17.0
```

**Note**: The default F=0 in REV motors is why your current setup "works ok but not great" - 
adding proper feedforward will dramatically improve velocity tracking!

### Step 3: Calculate P (Proportional)
P should be set to provide quick response without oscillation.

**Rule of thumb for FTC**: Start with default P=10, adjust in range of 7-15

For your motors:
```
Kp = 10.0  (conservative, default)
Kp = 12.0  (moderate, recommended for improved response)
Kp = 15.0  (aggressive, may oscillate)
```

**Recommendation**: Start with slightly improved value:
```
Kp = 12.0
```

### Step 4: Set I (Integral)
I helps eliminate steady-state error but can cause instability.

**Rule of thumb for FTC**: Start with default I=3, adjust in range of 1-5

For your motors:
```
Ki = 3.0  (default, works well)
Ki = 4.0  (if motor doesn't quite reach target)
Ki = 2.0  (if motor overshoots)
```

**Recommendation**: Start with default:
```
Ki = 3.0
```

### Step 5: Set D (Derivative)
D dampens oscillation but can make system sluggish. Usually not needed for velocity control.

**Rule of thumb for FTC**: Start with D=0, add only if needed (0.5-2.0 range)

**Recommendation**: Start with zero:
```
Kd = 0.0
```

## Recommended Starting Values

Based on your motor specifications (300 RPM peak, 200 RPM typical operation):

```java
// Recommended improved values (adds feedforward to defaults)
Kp = 12.0   // Proportional (improved from default 10)
Ki = 3.0    // Integral (default, works well)
Kd = 0.0    // Derivative (not needed)
Kf = 17.0   // Feedforward (32767/1918.74, CRITICAL!)
```

Alternative conservative starting point (default + feedforward):
```java
// Conservative (just add F to default REV values)
Kp = 10.0   // Default proportional
Ki = 3.0    // Default integral  
Kd = 0.0    // Default derivative
Kf = 17.0   // Add feedforward (was missing!)
```

**Why F=17 is Critical**: Your current defaults (P=10, I=3, D=0, F=0) work "ok but not great"
because F=0 means no feedforward. Adding F=17 will dramatically improve velocity tracking
by providing baseline power proportional to target velocity.

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
// Tuned for ~200-300 RPM operation
// Note: FTC velocity PIDF uses larger scale (P=10, not 0.01)
PIDFCoefficients pidfCoefficients = new PIDFCoefficients(
    12.0,  // P - Proportional gain (improved from default 10)
    3.0,   // I - Integral gain (default, works well)
    0.0,   // D - Derivative gain (not needed)
    17.0   // F - Feedforward gain (32767/1918.74, CRITICAL!)
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
- **Cause**: P too high
- **Fix**: Reduce P from 12 to 9-10

### Motor doesn't reach target speed
- **Cause**: F too low or I too low
- **Fix**: Increase F from 17 to 18-20, or increase I from 3 to 4-5

### Motor overshoots when changing speeds
- **Cause**: P too high
- **Fix**: Reduce P from 12 to 10 or lower

### Motor responds too slowly
- **Cause**: P or F too low
- **Fix**: Increase P from 12 to 13-15, or increase F from 17 to 18-20

### Different behavior under load vs. no load
- **Cause**: F not optimized, or I needed
- **Fix**: Fine-tune F for loaded condition, ensure I=3 or increase to 4

## Advanced Considerations

### Why F Changes with Operating Speed
The feedforward coefficient F is speed-dependent because:
- At 300 RPM (peak): `F = 32767 / 1918.74 ≈ 17.07`
- At 200 RPM (typical): `F = 32767 / 1279.16 ≈ 25.61`

Use F based on your **maximum** velocity (300 RPM) so it works across the full range.
The P and I terms will compensate for the slight F inaccuracy at lower speeds.

### Understanding the Scale Difference
**Critical**: FTC's velocity PIDF uses internal scale where 32767 = max motor power.
This is why default REV values are P=10, I=3 (not 0.01, 0.003).
The formula `F = 1.0 / max_velocity` is for normalized systems and gives values 
~1000x too small for FTC!

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
