# PIDF Quick Reference - Shooter Motors

## Shooter Motor Specs
- **Operating RPM**: 3000 (constant)
- **Encoder PPR**: 28 ticks/revolution
- **Target Velocity**: 1400 ticks/second
- **Usage**: Frequent on/off, requires quick response to load

## Recommended Starting Values

### Primary (Balanced - Start Here!)
```java
Kp = 35.0   // High responsiveness (corrected scale)
Ki = 6.0    // Strong load correction (corrected scale)
Kd = 2.0    // Moderate damping (corrected scale)
Kf = 23.0   // Feedforward for 3000 RPM (32767/1400)
```

### High Performance (Better Load Handling)
```java
Kp = 45.0   // More aggressive
Ki = 8.0    // Stronger correction
Kd = 3.0    // More damping
Kf = 23.0   // Same baseline
```

### Smooth (Less Overshoot Risk)
```java
Kp = 28.0   // Gentler response
Ki = 4.0    // Lighter correction
Kd = 1.5    // Less damping
Kf = 23.0   // Same baseline
```

## Quick Formulas

### IMPORTANT: FTC Velocity PIDF Scale
FTC's velocity PIDF system uses a different scale than normalized (0-1) control systems.
The scale is 32767 = max motor power (not 1.0). This applies to shooters just like drive motors!

### Calculate F (Feedforward)
```
F = 32767 / velocity_ticks_per_sec

F = 32767 / (3000 × 28 / 60)
F = 32767 / 1400 = 23.4 ≈ 23.0
```

### Calculate P (Proportional)
```
Start with moderate value for shooters: P = 30-40
Recommended: P = 35.0

Adjust based on response:
  - Too slow or weak load response: increase to 40-45
  - Oscillates: decrease to 28-32
```

### Calculate I (Integral)
```
Start with moderate value for shooters: I = 5-8
Recommended: I = 6.0

Adjust based on consistency:
  - Doesn't maintain speed under load: increase to 7-9
  - Overshoots: decrease to 4-5
```

### Calculate D (Derivative)
```
Start with moderate value for shooters: D = 1-3
Recommended: D = 2.0

Adjust based on overshoot:
  - Too much overshoot: increase to 3-4
  - Response too sluggish: decrease to 1.0 or 0
```

## Shooter vs Drive Motor PIDF

| Parameter | Drive Motors | Shooter Motors | Why Different |
|-----------|-------------|----------------|---------------|
| **P** | 12.0 | 35.0 | Shooter needs faster load response |
| **I** | 3.0 | 6.0 | Shooter fights continuous ball resistance |
| **D** | 0.0 | 2.0 | Shooter prevents startup overshoot |
| **F** | 17.0 | 23.0 | Different velocity (1279 vs 1400 ticks/sec) |

Note: Previous docs had values ~1000x too small due to wrong formula (used normalized scale instead of FTC's 32767 scale).

## Troubleshooting Table

| Problem | Cause | Solution |
|---------|-------|----------|
| Speed drops during shot | P or I too low | Increase P by 5-10 (e.g., 35→40) |
| Inconsistent shots | I too low | Increase I by 1-2 (e.g., 6→7) |
| Takes > 0.7s to spin up | P too low | Increase P by 5-10 (e.g., 35→40) |
| Overshoots and oscillates | P too high, D too low | Increase D by 1 or decrease P by 5 |
| Different L/R speeds | Motor variance | Individual tuning per motor |
| Weaker shots late in match | I insufficient for voltage drop | Increase I by 2 (e.g., 6→8) |

## Tuning Order (Fast Track)
1. **F only** → Get close to 3000 RPM naturally (F=23)
2. **Add P (35)** → Fast startup + load response
3. **Add I (6)** → Consistent shot-to-shot
4. **Add D (2)** → Eliminate overshoot

## Code Template
```java
// In Robot.java, after shooter initialization

leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

// Note: FTC velocity PIDF uses larger scale (P=35, not 0.04)
PIDFCoefficients shooterPIDF = new PIDFCoefficients(
    35.0,  // P - Quick load response
    6.0,   // I - Fight ball resistance
    2.0,   // D - Prevent overshoot
    23.0   // F - 3000 RPM baseline (32767/1400)
);

leftShooter.asDcMotorEx().setVelocityPIDFCoefficients(
    shooterPIDF.p, shooterPIDF.i, shooterPIDF.d, shooterPIDF.f);
rightShooter.asDcMotorEx().setVelocityPIDFCoefficients(
    shooterPIDF.p, shooterPIDF.i, shooterPIDF.d, shooterPIDF.f);
```

## Performance Targets

### Excellent Performance
- Spin-up: 0.3-0.5 seconds
- Overshoot: < 5% (< 150 RPM)
- Load drop: < 100 RPM during shot
- Consistency: ±30 RPM between shots
- Recovery: < 0.2 seconds after shot

### Needs Tuning
- Spin-up: > 0.7 seconds
- Sustained oscillation
- Load drop: > 200 RPM
- Consistency: > ±100 RPM
- Inconsistent shot distances

## Testing Commands
```java
// Use ShooterPIDFTuningTest.java OpMode
// Tests:
// - Startup acceleration
// - Steady-state speed
// - Load resistance simulation
// - Shot-to-shot consistency
// - Left/right motor sync
```

## Key Differences from Drive Tuning

**Shooter motors need MORE aggressive tuning because:**
1. **Higher P** → Fight ball resistance faster
2. **Higher I** → Maintain consistency under load
3. **Moderate D** → Quick startup without overshoot
4. **Constant speed** → Can optimize for one velocity

**Drive motors need LESS aggressive tuning because:**
1. **Variable speeds** → Must work across range
2. **Smooth operation** → Gentler response preferred
3. **Less load variation** → Don't need as much I
4. **Lower speeds** → Different F calculation

## Why These Values Work

At 3000 RPM with a ball hitting:
1. **Speed drops** to 2900 RPM (100 RPM error, ~140 ticks/sec)
2. **P responds**: 35 × 140 = 4900 units boost (strong immediate correction)
3. **I accumulates**: Adds sustained correction for persistent load
4. **F maintains**: ~70% baseline power continues (23 × 1400 ≈ 32200)
5. **D prevents**: Overshoot when recovering (damps the response)

**Result**: Fast recovery, consistent velocity shot after shot!

Note: Previous values (P=0.04, I=0.0015, D=0.0008, F=0.0007) were ~1000x too small due to using wrong formula (normalized scale vs FTC's 32767 scale).

## When to Retune
- Shot inconsistency increases
- Spin-up time degrades
- New motors installed
- Mechanical changes to shooter
- Battery performance changes

## Full Documentation
See `PIDF_Shooter_Tuning_Guide.md` for complete theory and step-by-step instructions
