# Team Documentation

This folder contains team-specific documentation for the FtcDecode robot.

## PIDF Motor Tuning Resources

### Drive Motors (300 RPM peak, 200 RPM typical)

#### 📘 [PIDF_Tuning_Guide.md](PIDF_Tuning_Guide.md)
**Comprehensive guide** for understanding and tuning PIDF values for drive motors.

**Contents:**
- What is PIDF and how it works
- Detailed explanation of P, I, D, and F components
- Step-by-step calculation formulas
- Motor specifications (300 RPM peak, 200 RPM typical)
- Recommended starting values
- Complete tuning process
- Troubleshooting common issues
- Testing checklist

**Use this when:** You need to understand the theory and calculations behind PIDF tuning for drive motors.

#### 📋 [PIDF_Quick_Reference.md](PIDF_Quick_Reference.md)
**Quick reference card** with formulas and values at a glance for drive motors.

**Contents:**
- Motor specifications summary
- Recommended PIDF starting values
- Quick calculation formulas
- Troubleshooting table
- Code template
- Key points reminder

**Use this when:** You need to quickly look up values or formulas during drive motor testing.

#### 💻 Test OpMode: `PIDFTuningTest.java`
Located at: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/programs/diagnostics/PIDFTuningTest.java`

**Interactive testing tool** to validate and tune your drive motor PIDF values.

**Features:**
- Real-time velocity tracking
- Multiple test speeds (50, 100, 150, 200 RPM)
- Gamepad controls for easy testing
- Error percentage display
- Individual motor monitoring
- Current PIDF coefficient display

**Use this when:** You're actively tuning and testing drive motor PIDF values on the robot.

### Shooter Motors (3000 RPM constant)

#### 📘 [PIDF_Shooter_Tuning_Guide.md](PIDF_Shooter_Tuning_Guide.md)
**Comprehensive guide** for understanding and tuning PIDF values specifically for shooter motors.

**Contents:**
- Why shooters need different PIDF values than drive motors
- Shooter-specific requirements (load resistance, quick spin-up, consistency)
- Detailed calculation formulas for 3000 RPM operation
- Higher P and I values explained
- Recommended starting values optimized for shooters
- Complete tuning process for shooter applications
- Troubleshooting shooter-specific issues
- Performance targets and testing checklist

**Use this when:** You need to understand how to tune PIDF for high-speed shooter motors that handle load.

#### 📋 [PIDF_Shooter_Quick_Reference.md](PIDF_Shooter_Quick_Reference.md)
**Quick reference card** with formulas and values specifically for shooter motors.

**Contents:**
- Shooter motor specifications (3000 RPM, 28 PPR)
- Recommended PIDF starting values (more aggressive than drive)
- Quick calculation formulas for shooters
- Shooter vs drive motor comparison table
- Troubleshooting table for shooter issues
- Code template for shooter PIDF setup

**Use this when:** You need quick lookup for shooter PIDF values or troubleshooting during testing.

#### 💻 Test OpMode: `ShooterPIDFTuningTest.java`
Located at: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/programs/diagnostics/ShooterPIDFTuningTest.java`

**Interactive testing tool** specifically designed for shooter motor tuning.

**Features:**
- Spin-up time measurement (targets < 0.5 seconds)
- Overshoot detection and percentage calculation
- Load simulation (simulates ball resistance)
- Recovery time tracking
- Shot-to-shot consistency monitoring
- Left/right motor synchronization check
- Real-time performance feedback

**Use this when:** You're actively tuning shooter PIDF values and testing load handling.

### 🔧 Code Implementation: `Robot.java`
Located at: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/hardware/Robot.java`

**Contains:**
- Detailed inline comments for BOTH drive and shooter motors
- Calculation formulas with specific motor specs
- Ready-to-uncomment code templates for both systems
- Motor PPR calculations explained
- Separate PIDF sections for drive vs shooter

**Use this when:** You're ready to implement PIDF values in your robot code.

## Getting Started with PIDF Tuning

### For Drive Motors:
1. **Read the guide**: Start with [PIDF_Tuning_Guide.md](PIDF_Tuning_Guide.md)
2. **Check the quick reference**: Use [PIDF_Quick_Reference.md](PIDF_Quick_Reference.md)
3. **Review the code**: Look at `Robot.java` drive motor comments
4. **Implement**: Uncomment the drive PIDF setup code in `Robot.java`
5. **Test**: Run `PIDFTuningTest` OpMode
6. **Fine-tune**: Adjust based on real-world performance

### For Shooter Motors:
1. **Read the shooter guide**: Start with [PIDF_Shooter_Tuning_Guide.md](PIDF_Shooter_Tuning_Guide.md)
2. **Check the shooter reference**: Use [PIDF_Shooter_Quick_Reference.md](PIDF_Shooter_Quick_Reference.md)
3. **Review the code**: Look at `Robot.java` shooter motor comments
4. **Implement**: Uncomment the shooter PIDF setup code in `Robot.java`
5. **Test**: Run `ShooterPIDFTuningTest` OpMode
6. **Fine-tune**: Test with actual ball shots under load

## Recommended Starting Values

### Drive Motors (300 RPM peak / 200 RPM typical):
```java
Kp = 0.015   // Proportional - smooth response
Ki = 0.0003  // Integral - light correction
Kd = 0.0002  // Derivative - minimal damping
Kf = 0.0008  // Feedforward (most important!)
```

### Shooter Motors (3000 RPM constant):
```java
Kp = 0.040   // Proportional - HIGH for load response
Ki = 0.0015  // Integral - HIGH for consistency
Kd = 0.0008  // Derivative - moderate damping
Kf = 0.0007  // Feedforward for 3000 RPM
```

**Key Difference**: Shooter motors need MORE aggressive tuning (higher P and I) to handle ball resistance and maintain shot-to-shot consistency!

## Other Team Documentation

- **[GamepadMapping.md](GamepadMapping.md)**: Controller button mappings
- **[PortMapping.md](PortMapping.md)**: Hardware port assignments
- **[BlankGamepadMapping.md](BlankGamepadMapping.md)**: Template for creating new gamepad mappings

## Questions?

If you have questions about PIDF tuning:
1. Review the appropriate comprehensive guide first (drive or shooter)
2. Check the troubleshooting sections
3. Test with the diagnostic OpMode (drive or shooter version)
4. Document your findings for the team

---

**Note**: These PIDF resources address:
- **Drive motors**: "What PIDF values for motors that peak at 300 RPM but typically run at 200 RPM?"
- **Shooter motors**: "What PIDF values for 3000 RPM shooters that need to handle ball resistance, spin up quickly, and be consistent shot-to-shot?"
