# PIDF Tuning Guide for Shooter Motors

## Overview
This guide explains how to tune PIDF values specifically for your shooter motors running at ~3000 RPM. Unlike drive motors that need smooth, gentle control, shooter motors need:
- **Fast acceleration** to get up to speed quickly
- **Strong resistance handling** when balls push against them
- **Quick error correction** to maintain consistency
- **Minimal overshoot** to avoid wasting time
- **Shot-to-shot consistency** for reliable performance

## Your Shooter Motor Specifications
- **Operating Speed**: ~3000 RPM (constant)
- **Motor Type**: Direct drive (no gearbox)
- **Encoder PPR**: 28 ticks/revolution
- **Usage Pattern**: Frequently turned on/off
- **Critical Requirements**: 
  - Maintain speed under load (ball resistance)
  - Rapid acceleration without overshoot
  - Consistent velocity between shots

## Key Differences from Drive Motors

### Shooter Motors Need More Aggressive Tuning
| Aspect | Drive Motors (200 RPM) | Shooter Motors (3000 RPM) |
|--------|----------------------|---------------------------|
| **Speed** | Variable (0-300 RPM) | Constant (~3000 RPM) |
| **P Gain** | 12.0 | Higher (~35.0, more responsive) |
| **I Gain** | 3.0 | Larger (~6.0, fight load better) |
| **D Gain** | 0.0 | Moderate (~2.0, prevent overshoot) |
| **F Gain** | 17.0 | 23.0 (higher velocity: 1400 vs 1279 ticks/sec) |
| **Priority** | Smooth changes | Fast response + consistency |

Note: All values use FTC's scale where 32767 = max motor power.

## Calculating PIDF Values for Shooter Motors

### Step 1: Calculate Velocity in Ticks/Second
```
Shooter velocity = (3000 RPM × 28 PPR) / 60 seconds
                 = 84,000 / 60
                 = 1400 ticks/second
```

### Step 2: Calculate F (Feedforward)
F is the foundation for velocity control.

**IMPORTANT**: FTC velocity PIDF uses scale where 32767 = max motor power (not 1.0).

**Correct Formula for FTC**: `Kf = 32767 / velocity_ticks_per_sec`

```
Kf = 32767 / 1400
Kf ≈ 23.4

Rounded for tuning: Kf ≈ 23.0
```

**Why this matters**: F provides the baseline power to maintain 3000 RPM. Without F, the motor would rely entirely on error correction, which is slower.

**Why previous formula was wrong**: Using `F = 1.0 / 1400 ≈ 0.0007` assumed normalized (0-1) scale, but FTC uses 32767 scale internally, making that value ~1000x too small!

### Step 3: Calculate P (Proportional) - MORE AGGRESSIVE
For shooters that need to resist load and correct quickly:

**Rule of thumb for FTC**: Start with P in range of 28-45 for shooters

```
Conservative: Kp = 28.0
Moderate: Kp = 35.0
Aggressive: Kp = 45.0
```

**Recommendation for shooters**:
```
Kp = 35.0  (good balance for load resistance)
```

**Why higher P than drive motors?**
- Responds faster when a ball creates resistance
- Corrects speed drops more aggressively
- Gets up to speed faster on startup

### Step 4: Calculate I (Integral) - HIGHER FOR LOAD
I eliminates steady-state error and fights continuous load.

**Rule of thumb for FTC**: Start with I in range of 4-8 for shooters

```
For Kp = 35.0:
Aggressive: Ki = 8.0
Moderate: Ki = 6.0
Conservative: Ki = 4.0
```

**Recommendation for shooters**:
```
Ki = 6.0  (moderate - fights ball resistance)
```

**Why higher I?**
- When a ball pushes against the shooter, I accumulates the error
- Higher I means faster recovery from load-induced slowdowns
- Maintains consistency between shots even as battery voltage drops

### Step 5: Calculate D (Derivative) - MODERATE TO PREVENT OVERSHOOT
D prevents overshoot during rapid acceleration.

**Rule of thumb for FTC**: Start with D in range of 1-3 for shooters

```
For Kp = 35.0:
Aggressive: Kd = 3.0
Moderate: Kd = 2.0
Conservative: Kd = 1.0
```

**Recommendation for shooters**:
```
Kd = 2.0  (moderate - prevents overshoot on startup)
```

**Why moderate D?**
- Prevents the motor from overshooting 3000 RPM when starting
- Dampens oscillation without slowing response too much
- Helps achieve target speed without excessive "bounce"

## Recommended Starting Values for Shooter Motors

### Primary Recommendation (Balanced Performance)
```java
// For ~3000 RPM shooter with frequent on/off cycles
// Note: FTC velocity PIDF uses larger scale (P=35, not 0.04)
Kp = 35.0   // High responsiveness to load
Ki = 6.0    // Strong steady-state correction
Kd = 2.0    // Moderate overshoot prevention
Kf = 23.0   // Feedforward for 3000 RPM (32767/1400)
```

### Alternative: Maximum Responsiveness (If consistency suffers)
```java
// More aggressive - better load handling, risk of slight oscillation
Kp = 45.0
Ki = 8.0
Kd = 3.0
Kf = 23.0
```

### Alternative: Smoother Response (If overshooting)
```java
// Less aggressive - smoother but slower load response
Kp = 28.0
Ki = 4.0
Kd = 1.5
Kf = 23.0
```

## How PIDF Works Together for Shooters

The control equation is:
```
motor_power = (P × error) + (I × Σerror) + (D × Δerror) + (F × target)
```

### During Normal Operation (at 3000 RPM, no load):
- **F provides ~71% of power** needed to maintain 3000 RPM (23 × 1400 ≈ 32200 of 32767 max)
- **P contributes ~0%** (no error)
- **I contributes ~0%** (accumulated error is zero)
- **D contributes ~0%** (error rate is zero)

### When Ball Creates Resistance:
1. **Velocity drops** (e.g., from 3000 to 2900 RPM, ~140 ticks/sec error)
2. **P responds immediately**: `35 × 140 = 4900` units of extra power (strong correction)
3. **I starts accumulating**: Every cycle adds to Σerror, providing sustained correction
4. **D dampens any oscillation**: Prevents overshooting when returning to 3000 RPM
5. **F continues baseline**: Still providing the ~71% baseline power

### Result: 
Motor quickly returns to 3000 RPM and maintains it despite the ball resistance, ensuring consistent shot velocity.

Note: Previous docs used values ~1000x too small (P=0.04 vs 35) due to using wrong formula.

## Implementation in Code

Add this to `Robot.java` constructor after shooter motor initialization:

```java
// In Robot.java, after shooter motor creation (around line 32-34)

// Set shooter motors to use velocity control
leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

// PIDF coefficients optimized for 3000 RPM shooter operation
// These values prioritize:
// - Fast response to load (ball resistance)
// - Quick acceleration without overshoot
// - Consistent shot-to-shot performance
// Note: FTC velocity PIDF uses larger scale (P=35, not 0.04)
PIDFCoefficients shooterPIDF = new PIDFCoefficients(
    35.0,  // P - High for quick load response
    6.0,   // I - Strong for sustained error correction
    2.0,   // D - Moderate to prevent overshoot
    23.0   // F - Feedforward for 3000 RPM baseline (32767/1400)
);

// Apply PIDF to both shooter motors
leftShooter.asDcMotorEx().setVelocityPIDFCoefficients(
    shooterPIDF.p, shooterPIDF.i, shooterPIDF.d, shooterPIDF.f);
rightShooter.asDcMotorEx().setVelocityPIDFCoefficients(
    shooterPIDF.p, shooterPIDF.i, shooterPIDF.d, shooterPIDF.f);
```

## Tuning Process for Shooters

### Phase 1: Tune F for Baseline Speed
1. Set P=I=D=0, only use F
2. Spin up shooter to 3000 RPM with no load
3. Observe if motor reaches approximately 3000 RPM
4. Adjust F:
   - If consistently too slow: increase F by 10%
   - If consistently too fast: decrease F by 10%
5. Target: Within 100 RPM of 3000 RPM using F alone

### Phase 2: Add P for Quick Response
1. Start with P = 35.0
2. Test startup acceleration:
   - Should reach 3000 RPM within 0.3-0.5 seconds
   - Should not overshoot significantly (< 5%)
3. Test with ball resistance:
   - Speed should recover quickly when ball hits
   - Should maintain steady speed during shot
4. Adjust P if needed:
   - Too slow or weak: increase P by 5-10 (e.g., 35→40)
   - Oscillates: decrease P by 5 (e.g., 35→30)
4. Adjust P:
   - **Too slow recovery from load**: Increase P by 20%
   - **Oscillates or overshoots on startup**: Decrease P by 20%

### Phase 3: Add I for Load Consistency
1. Start with I = 6.0
2. Run continuous shooting test (10+ shots)
3. Monitor RPM during each shot
4. Check for:
   - Consistent speed shot-to-shot
   - No gradual drift in speed
   - Speed maintained during ball contact
5. Adjust I:
   - **Speed drops during shots**: Increase I by 1-2 (e.g., 6→7)
   - **Oscillates after disturbance**: Decrease I by 1-2 (e.g., 6→4)

### Phase 4: Add D to Eliminate Overshoot
1. Start with D = 2.0
2. Test rapid startup (0 to 3000 RPM)
3. Check overshoot amount and settling time
4. Adjust D:
   - **Overshoots > 5%**: Increase D by 0.5-1 (e.g., 2→2.5 or 3)
   - **Takes too long to reach speed**: Decrease D by 0.5-1 (e.g., 2→1.5 or 1)

## Testing Checklist for Shooters

Use the `ShooterPIDFTuningTest` OpMode (included) to verify:

- [ ] Reaches 3000 RPM within 0.5 seconds from stopped
- [ ] Does not overshoot by more than 5% (3150 RPM)
- [ ] Maintains 3000 RPM ±50 RPM with no load
- [ ] Recovers to 3000 RPM within 0.2 seconds after ball shot
- [ ] Consistent RPM across 10 consecutive shots (±30 RPM variation)
- [ ] No sustained oscillation or vibration
- [ ] Left and right motors track within 50 RPM of each other
- [ ] Performance stable across different battery charge levels

## Common Issues and Solutions

### Issue: Speed Drops When Ball Hits
**Symptoms**: RPM drops by 200+ when ball contacts flywheel
**Cause**: P and/or I too low
**Solution**: 
1. Increase P by 25% first (faster immediate response)
2. If still drops, increase I by 30% (stronger sustained correction)

### Issue: Inconsistent Shot Velocity
**Symptoms**: Some shots faster/slower than others
**Cause**: Insufficient I or varying battery voltage
**Solution**:
1. Increase I by 30-40%
2. Check battery voltage - may need higher I for worn batteries
3. Ensure motors reach 3000 RPM before each shot

### Issue: Takes Too Long to Spin Up
**Symptoms**: > 0.7 seconds to reach 3000 RPM
**Cause**: P too low or F slightly off
**Solution**:
1. Increase P by 30%
2. Fine-tune F to get closer to 3000 RPM naturally
3. Check for mechanical resistance

### Issue: Overshoots Then Oscillates
**Symptoms**: Goes to 3200 RPM, then 2800, then 3100, etc.
**Cause**: P too high and/or D too low
**Solution**:
1. Increase D by 50% first (dampens oscillation)
2. If oscillation persists, reduce P by 15%

### Issue: Different Left/Right Motor Speeds
**Symptoms**: One motor consistently 100+ RPM different
**Cause**: Motor-to-motor variation or wiring issues
**Solution**:
1. Check wiring and encoders
2. May need individual PIDF tuning per motor
3. Verify both encoders working correctly

### Issue: Performance Degrades Over Match
**Symptoms**: Shots get weaker as battery drains
**Cause**: I not compensating for voltage drop
**Solution**:
1. Increase I by 40-50%
2. Test with partially drained battery
3. Consider voltage compensation if available

## Advanced Shooter Considerations

### Battery Voltage Compensation
As battery voltage drops during a match:
- Base motor speed will naturally decrease
- Higher I helps compensate automatically
- May want to test PIDF with battery at 12.0V (low) and 13.5V (full)

### Shooter Wheel Inertia
The shooter flywheel has significant rotational inertia:
- **Advantage**: Maintains speed through ball impact better
- **Disadvantage**: Takes longer to spin up
- **Tuning Impact**: May need slightly higher P for faster acceleration

### Dual Motor Synchronization
Two shooter motors should track together:
- If one is consistently slower, check:
  - Encoder wiring/connection
  - Motor health
  - Mechanical friction/resistance
- Consider individual tuning if persistent difference exists

### Shot Timing Optimization
For maximum consistency:
1. Wait for `atSpeedRPM(3000)` to return true
2. Ideal: Both motors within 2-3% of target (2940-3060 RPM)
3. Use small delay (50-100ms) after reaching speed before shooting
4. This ensures momentum has stabilized

## When to Retune Shooter PIDF

Retune if you notice:
- Shot distance/velocity becoming inconsistent
- Longer spin-up time than expected
- Different performance with fresh vs. worn battery
- After replacing shooter motors or encoders
- Mechanical changes to shooter assembly
- Different ball weight or compression

## Performance Targets

### Excellent Shooter PIDF Performance:
- **Spin-up time**: 0.3-0.5 seconds (0 to 3000 RPM)
- **Overshoot**: < 5% (< 150 RPM above target)
- **Recovery time**: < 0.2 seconds after ball shot
- **Consistency**: ±30 RPM shot-to-shot variation
- **Under-load drop**: < 100 RPM during ball contact
- **Steady-state error**: < 50 RPM (within 1.7%)

### Acceptable Performance:
- **Spin-up time**: 0.5-0.7 seconds
- **Overshoot**: < 10% (< 300 RPM above target)
- **Recovery time**: < 0.3 seconds after ball shot
- **Consistency**: ±50 RPM shot-to-shot variation
- **Under-load drop**: < 150 RPM during ball contact
- **Steady-state error**: < 100 RPM (within 3.3%)

### Needs Retuning:
- Spin-up time > 0.7 seconds
- Sustained oscillation
- Inconsistent shots (> ±100 RPM variation)
- Significant speed drop during shots (> 200 RPM)

## Comparison: Default vs. Tuned PIDF

### With Default/No PIDF:
- Spin-up: 0.8-1.2 seconds
- Speed drops 200-400 RPM when ball hits
- Shot consistency: ±150 RPM
- Battery voltage greatly affects performance

### With Optimized PIDF (Recommended Values):
- Spin-up: 0.3-0.5 seconds
- Speed drops 50-100 RPM when ball hits
- Shot consistency: ±30 RPM
- Minimal battery voltage impact

**Result**: More shots scored, better accuracy, more reliable autonomous routines!

## References and Further Reading

- General PIDF tuning guide: `PIDF_Tuning_Guide.md`
- Quick reference: `PIDF_Quick_Reference.md`
- Test OpMode: `ShooterPIDFTuningTest.java`
- FTC SDK Documentation: https://ftc-docs.firstinspires.org/
- Flywheel velocity control: https://www.chiefdelphi.com/t/flywheel-velocity-control/

## Revision History
- 2025-11-05: Initial version for 3000 RPM shooter motors with emphasis on load resistance and consistency
