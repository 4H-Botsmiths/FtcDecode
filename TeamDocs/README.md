# Team Documentation

This folder contains team-specific documentation for the FtcDecode robot.

## PIDF Motor Tuning Resources

### 📘 [PIDF_Tuning_Guide.md](PIDF_Tuning_Guide.md)
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

**Use this when:** You need to understand the theory and calculations behind PIDF tuning.

### 📋 [PIDF_Quick_Reference.md](PIDF_Quick_Reference.md)
**Quick reference card** with formulas and values at a glance.

**Contents:**
- Motor specifications summary
- Recommended PIDF starting values
- Quick calculation formulas
- Troubleshooting table
- Code template
- Key points reminder

**Use this when:** You need to quickly look up values or formulas during testing.

### 💻 Test OpMode: `PIDFTuningTest.java`
Located at: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/programs/diagnostics/PIDFTuningTest.java`

**Interactive testing tool** to validate and tune your PIDF values.

**Features:**
- Real-time velocity tracking
- Multiple test speeds (50, 100, 150, 200 RPM)
- Gamepad controls for easy testing
- Error percentage display
- Individual motor monitoring
- Current PIDF coefficient display

**Use this when:** You're actively tuning and testing PIDF values on the robot.

### 🔧 Code Implementation: `Robot.java`
Located at: `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/hardware/Robot.java`

**Contains:**
- Detailed inline comments explaining PIDF
- Calculation formulas in comments
- Ready-to-uncomment code template
- Motor PPR calculations explained

**Use this when:** You're ready to implement PIDF values in your robot code.

## Getting Started with PIDF Tuning

1. **Read the guide**: Start with [PIDF_Tuning_Guide.md](PIDF_Tuning_Guide.md) to understand the concepts
2. **Check the quick reference**: Use [PIDF_Quick_Reference.md](PIDF_Quick_Reference.md) for formulas and starting values
3. **Review the code**: Look at `Robot.java` comments for implementation details
4. **Implement in code**: Uncomment the PIDF setup code in `Robot.java` constructor
5. **Test with OpMode**: Run `PIDFTuningTest` to validate and tune your values
6. **Fine-tune**: Adjust values based on real-world performance

## Recommended Starting Values

For drive motors with **300 RPM peak / 200 RPM typical** operation:

```java
Kp = 0.015   // Proportional gain
Ki = 0.0003  // Integral gain
Kd = 0.0002  // Derivative gain
Kf = 0.0008  // Feedforward gain (most important!)
```

## Other Team Documentation

- **[GamepadMapping.md](GamepadMapping.md)**: Controller button mappings
- **[PortMapping.md](PortMapping.md)**: Hardware port assignments
- **[BlankGamepadMapping.md](BlankGamepadMapping.md)**: Template for creating new gamepad mappings

## Questions?

If you have questions about PIDF tuning:
1. Review the comprehensive guide first
2. Check the troubleshooting sections
3. Test with the diagnostic OpMode
4. Document your findings for the team

---

**Note**: These PIDF resources were created to address the specific question: "What should I set the PIDF values to for drive motors that peak at 300 RPM but typically run at 200 RPM?"
