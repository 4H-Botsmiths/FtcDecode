# GitHub Copilot Instructions for FTC DECODE Robot

## Competition Context
This is an FTC (FIRST Tech Challenge) robot for the 2025-2026 DECODE season.

**Competition Manual Reference**: [`TeamDocs/DECODE_Competition_Manual_TU07.pdf`](../TeamDocs/DECODE_Competition_Manual_TU07.pdf)

---

## Key DECODE Game Mechanics

### The Motif Challenge
- At the start of autonomous, a **motif pattern** appears on the obelisk
- The motif determines the **correct shooting order** for balls (green vs purple sequence)
- Reading the motif correctly = bonus points for shooting in the right order
- Only robots starting near the **motif/obelisk** can read the pattern
- Robots starting near **human players** cannot see the motif and need alliance communication

### Ball System
- **Two colors**: Green and Purple
- **Motif determines order**: Must shoot in the sequence shown by the motif
- **Indexer system**: Circular paddle with color sensors to identify balls
- **Shooter system**: Flywheel motors + feeder servos

### Starting Positions
1. **Near Motif/Obelisk** - Can read pattern, longer travel to human players for reload
2. **Near Human Players** - Cannot read pattern, shorter reload distance

### Time Constraints
- **Autonomous Period**: 30 seconds
- **Alliance Coordination**: First shooter (0-10s), Second shooter waits then shoots (10-20s)
- **Reload Timing**: ~8 seconds round trip for robots starting near human players

---

## Robot Architecture

### Hardware Configuration
- **Control Hub** (CH_) devices: 4 motors, servos, I2C sensors
- **Expansion Hub** (EH_) devices: Additional motors, servos, I2C sensors
- **Webcam**: USB camera for AprilTag detection and motif reading
- **DeviceNames enum**: Type-safe configuration for all hardware

### Subsystem Design Pattern
All subsystems follow this structure:
```java
public class SubsystemName {
    private Robot robot;  // Hardware abstraction
    
    public SubsystemName(Robot robot) {
        this.robot = robot;
    }
    
    // Public methods for actions
    // Private helper methods
}
```

### Core Subsystems
1. **MechanumDrive** - Holonomic drive with speed modes (NORMAL/SLOW/TURBO)
2. **Shooter** - Coordinated flywheel motors and feeder servos
3. **Indexer** - Circular paddle rotation with ball color detection
4. **AprilTagVision** - AprilTag detection for alignment (tag #1, optimal distance 36±3 inches)
5. **AutoAlign** - PID-like auto-alignment combining vision and drive

### Autonomous Program Structure
- **5 programs total**: DriveOnly, MotifFirstShoot, MotifSecondShoot, HumanFirstShoot, HumanSecondShoot
- Each uses LinearOpMode pattern with TODO comments for implementation
- Clear separation: initialization → sequence → cleanup

### TeleOp Structure
- **Gamepad 1** (Driver): Drive controls, vision toggle, auto-align
- **Gamepad 2** (Operator): Shooter, indexer, feeder controls
- **Performance optimized**: Vision only runs when toggled on

---

## Code Style Guidelines

### Target Audience
- Code must be readable by **teenage programmers** (ages 14-18)
- Extensive comments explaining **WHY**, not just WHAT
- Clear variable names, no cryptic abbreviations
- Break complex logic into smaller, named methods

### Comment Style
```java
// Good: Explains reasoning
// Wait 10 seconds for alliance partner to complete their shooting cycle
// This prevents collisions and allows coordinated scoring

// Bad: Just repeats code
// Wait 10 seconds
```

### Performance Considerations
⚠️ **Control Hub has limited CPU resources**
- Vision processing is expensive - only run when needed
- Use toggle pattern for vision (Right Bumper on GP1)
- Minimize telemetry updates (4-5 lines max, not every loop)
- Avoid blocking operations in main loop

### Telemetry Guidelines
- **Maximum 5 lines** - driver station screen is small
- Most important info first
- Use clear labels
- Update only when values change (for performance)

Example:
```java
telemetry.addData("Mode", "Auto-Align");
telemetry.addData("Distance", "%.1f in", vision.getDistance());
telemetry.addData("Angle", "%.1f deg", vision.getAngle());
telemetry.addData("Status", vision.getAlignmentStatus());
telemetry.update();
```

### Control Mapping Philosophy
- **Gamepad 1 (Driver)**: All driving, vision, alignment
  - Left stick: Strafe/Forward
  - Right stick: Rotation
  - Right trigger: Turbo speed
  - Left trigger: Slow speed
  - Right bumper: Toggle vision
  - Left bumper: Auto-align (when vision active)

- **Gamepad 2 (Operator)**: All scoring mechanisms
  - A button: Toggle shooter
  - Y button: Emergency stop
  - Triggers: Indexer rotation
  - Bumpers: Feeder manual control
  - D-pad: (Reserved for future use)

---

## Competition-Specific Requirements

### Autonomous Constraints
- Must cross starting line for points
- 30-second time limit
- Alliance coordination required for optimal scoring
- Parking in observation zone = 3 points

### Alliance Communication
When robots start at different positions:
- Robot at motif reads pattern
- Must communicate ball order to partner at human players
- Both can get motif bonus if order is correct

### Scoring Priorities
1. **Reliability over complexity** - points > risk
2. **Parking** - guaranteed 3 points
3. **Shooting** - major scoring opportunity
4. **Motif bonus** - extra points for correct order
5. **Reload cycles** - high risk/high reward

---

## Technical Standards

### File Organization
```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── hardware/
│   ├── Robot.java           # Hardware initialization
│   ├── DeviceNames.java     # Port mapping enum
│   ├── MechanumDrive.java   # Drive subsystem
│   ├── Shooter.java         # Shooter subsystem
│   ├── Indexer.java         # Indexer subsystem
│   ├── AprilTagVision.java  # Vision subsystem
│   └── AutoAlign.java       # Auto-alignment subsystem
└── programs/
    ├── autonomous/          # 5 autonomous programs
    └── teleop/              # TeleOp program
```

### Naming Conventions
- **Programs**: `Decode - [Description]` (short names for driver station)
- **Classes**: PascalCase
- **Methods**: camelCase
- **Constants**: UPPER_SNAKE_CASE
- **Private fields**: camelCase with descriptive names

### Error Handling
```java
try {
    // Initialization code
} catch (Exception e) {
    telemetry.addData("ERROR", e.getMessage());
    telemetry.update();
    // Degrade gracefully - don't crash!
}
```

---

## When Suggesting Code

### Always Consider:
1. ✅ Is this readable by a high school student?
2. ✅ Does this follow the subsystem pattern?
3. ✅ Is this performance-conscious for Control Hub?
4. ✅ Does this follow DECODE game rules?
5. ✅ Are there helpful comments explaining the logic?
6. ✅ Is telemetry limited and meaningful?

### Don't Suggest:
- ❌ Overly complex algorithms without explanation
- ❌ Blocking operations in main loop
- ❌ Excessive telemetry updates
- ❌ Direct hardware access (use subsystems)
- ❌ Magic numbers (use named constants)
- ❌ Code that violates FTC rules

### Prefer:
- ✅ Clear, simple solutions
- ✅ Named helper methods
- ✅ Constants for tunable values
- ✅ Inline comments for complex logic
- ✅ Subsystem abstraction
- ✅ Performance-conscious patterns

---

## Example Code Pattern

```java
/**
 * Description of what this does and WHY it's needed for DECODE
 */
@Autonomous(name = "Decode - Program Name", group = "Competition")
public class ProgramName extends LinearOpMode {
    
    // Hardware and subsystems
    private Robot robot;
    private SubsystemName subsystem;
    
    // Strategy parameters (tunable constants)
    private static final double TIMEOUT_SECONDS = 10.0;
    
    @Override
    public void runOpMode() {
        // Initialize
        try {
            robot = new Robot(hardwareMap);
            subsystem = new SubsystemName(robot);
        } catch (Exception e) {
            telemetry.addData("ERROR", e.getMessage());
        }
        
        telemetry.update();
        waitForStart();
        
        // Run sequence
        if (opModeIsActive()) {
            runSequence();
        }
        
        // Cleanup
        cleanup();
    }
    
    private void runSequence() {
        // Clear implementation with comments
    }
    
    private void cleanup() {
        // Stop all subsystems
    }
}
```

---

## Resources

- **Competition Manual**: [`TeamDocs/DECODE_Competition_Manual_TU07.pdf`](../TeamDocs/DECODE_Competition_Manual_TU07.pdf)
- **Autonomous Strategy Guide**: [`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/programs/autonomous/README.md`](../TeamCode/src/main/java/org/firstinspires/ftc/teamcode/programs/autonomous/README.md)
- **Port Mapping**: [`TeamDocs/PortMapping.md`](../TeamDocs/PortMapping.md)
- **Gamepad Controls**: [`TeamDocs/GamepadMapping.md`](../TeamDocs/GamepadMapping.md)

---

*Remember: We're building for a high school robotics team. Code quality, readability, and teachability are just as important as functionality!*
