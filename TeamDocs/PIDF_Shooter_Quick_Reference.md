# PIDF Quick Reference - Shooter Motors

## Shooter Motor Specs
- **Operating RPM**: 3000 (constant)
- **Encoder PPR**: 28 ticks/revolution
- **Target Velocity**: 1400 ticks/second
- **Usage**: Frequent on/off, requires quick response to load

## Recommended Starting Values

### Primary (Balanced - Start Here!)
```java
Kp = 0.040   // High responsiveness
Ki = 0.0015  // Strong load correction
Kd = 0.0008  // Moderate damping
Kf = 0.0007  // Feedforward for 3000 RPM
```

### High Performance (Better Load Handling)
```java
Kp = 0.050   // More aggressive
Ki = 0.0020  // Stronger correction
Kd = 0.0010  // More damping
Kf = 0.0007  // Same baseline
```

### Smooth (Less Overshoot Risk)
```java
Kp = 0.030   // Gentler response
Ki = 0.0010  // Lighter correction
Kd = 0.0006  // Less damping
Kf = 0.0007  // Same baseline
```

## Quick Formulas

### Calculate F (Feedforward)
```
F = 1.0 / (RPM × PPR / 60)
F = 1.0 / (3000 × 28 / 60)
F = 1.0 / 1400 = 0.000714 ≈ 0.0007
```

### Calculate P (Proportional)
```
P = (30 to 80) × F
Shooter typical: P = 60 × F ≈ 0.040
```

### Calculate I (Integral)
```
I = P / (20 to 50)
Shooter typical: I = P / 27 ≈ 0.0015
```

### Calculate D (Derivative)
```
D = P / (40 to 100)
Shooter typical: D = P / 50 ≈ 0.0008
```

## Shooter vs Drive Motor PIDF

| Parameter | Drive Motors | Shooter Motors | Why Different |
|-----------|-------------|----------------|---------------|
| **P** | 0.015 | 0.040 | Shooter needs faster load response |
| **I** | 0.0003 | 0.0015 | Shooter fights continuous ball resistance |
| **D** | 0.0002 | 0.0008 | Shooter prevents startup overshoot |
| **F** | 0.0008 | 0.0007 | Different velocity (200 vs 3000 RPM) |

## Troubleshooting Table

| Problem | Cause | Solution |
|---------|-------|----------|
| Speed drops during shot | P or I too low | Increase P by 25% |
| Inconsistent shots | I too low | Increase I by 30% |
| Takes > 0.7s to spin up | P too low | Increase P by 30% |
| Overshoots and oscillates | P too high, D too low | Increase D by 50% |
| Different L/R speeds | Motor variance | Individual tuning per motor |
| Weaker shots late in match | I insufficient for voltage drop | Increase I by 40% |

## Tuning Order (Fast Track)
1. **F only** → Get close to 3000 RPM naturally
2. **Add P (0.040)** → Fast startup + load response
3. **Add I (0.0015)** → Consistent shot-to-shot
4. **Add D (0.0008)** → Eliminate overshoot

## Code Template
```java
// In Robot.java, after shooter initialization

leftShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
rightShooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

PIDFCoefficients shooterPIDF = new PIDFCoefficients(
    0.040,  // P - Quick load response
    0.0015, // I - Fight ball resistance
    0.0008, // D - Prevent overshoot
    0.0007  // F - 3000 RPM baseline
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
1. **Speed drops** 2900 RPM (100 RPM error)
2. **P responds**: 0.040 × 100 = 4 units boost
3. **I accumulates**: Adds sustained correction
4. **F maintains**: 70% baseline power continues
5. **D prevents**: Overshoot when recovering

**Result**: Fast recovery, consistent velocity shot after shot!

## When to Retune
- Shot inconsistency increases
- Spin-up time degrades
- New motors installed
- Mechanical changes to shooter
- Battery performance changes

## Full Documentation
See `PIDF_Shooter_Tuning_Guide.md` for complete theory and step-by-step instructions
