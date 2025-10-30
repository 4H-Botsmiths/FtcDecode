# DECODE Robot Gamepad Controls

![Gamepad Diagram](Gamepad.png)

---

## 🎮 Gamepad 1 - Driver Controls

**Role**: Primary driver - responsible for robot movement, vision, and alignment

| Button | Function | Details | Tips |
|--------|----------|---------|------|
| **3** | **Drive Robot** | Left stick Y-axis: Forward/backward<br>Left stick X-axis: Strafe left/right | Holonomic (mecanum) drive - can move in any direction |
| **1** | **Rotate Robot** | Right stick X-axis: Rotate left/right | Use for fine-tuning orientation |
| **6** | **Slow Mode** | Left trigger: Reduces speed to 30% | Use for precise positioning near targets |
| **9** | **Turbo Mode** | Right trigger: Increases speed to 100% | Use for fast traversal across field |
| **10** | **Toggle Vision** | Right bumper: Turn AprilTag vision on/off | ⚠️ Only enable when needed - saves CPU! |
| **7** | **Auto-Align** | Left bumper: Auto-align to shooting position | Only works when vision is ON (button 10) |
| **A** | - | *Not assigned* | |
| **B** | - | *Not assigned* | |
| **X** | - | *Not assigned* | |
| **Y** | - | *Not assigned* | |
| **2** | - | *Not assigned* | |
| **4** | - | *Not assigned* | |
| **5** | - | *Not assigned* | |
| **8** | - | *Not assigned* | |

### 🚗 Driving Tips for Driver:
- **Normal driving**: Use left stick for movement, right stick for rotation (80% speed)
- **Precision mode**: Hold left trigger (6) for slow, controlled movements (30% speed)
- **Speed mode**: Hold right trigger (9) for maximum speed (100% speed)
- **Vision usage**: Only press right bumper (10) when approaching shooting position
- **Auto-alignment**: With vision ON, hold left bumper (7) to auto-align to AprilTag #1

---

## 🎯 Gamepad 2 - Operator Controls

**Role**: Scoring operator - responsible for shooting balls and managing indexer

| Button | Function | Details | Tips |
|--------|----------|---------|------|
| **A** | **Toggle Shooter** | Press to turn shooter ON/OFF | Flywheels spin up for shooting |
| **Y** | **Emergency Stop** | Immediately stops shooter | Use if something goes wrong! |
| **6** | **Indexer - Green Balls** | Left trigger: Rotate counter-clockwise | Positions GREEN balls for shooting |
| **9** | **Indexer - Purple Balls** | Right trigger: Rotate clockwise | Positions PURPLE balls for shooting |
| **10** | **Manual Feed Forward** | Right bumper: Run feeder forward | Use to feed balls into shooter |
| **7** | **Manual Feed Reverse** | Left bumper: Run feeder backward | Use to clear jams or reposition |
| **2** | **Manual Rotate LEFT** | D-pad LEFT: Override and rotate left | Manual override when color sensors fail |
| **4** | **Manual Rotate RIGHT** | D-pad RIGHT: Override and rotate right | Manual override when color sensors fail |
| **B** | - | *Not assigned* | |
| **X** | - | *Not assigned* | |
| **1** | - | *Not assigned* | |
| **3** | - | *Not assigned* | |
| **5** | - | *Not assigned* | |
| **8** | - | *Not assigned* | |

### 🎯 Shooting Sequence for Operator:

**During Autonomous** (watch the motif!):
1. Note the motif pattern on the obelisk (green/purple order)
2. Communicate ball order to alliance partner if needed

**During TeleOp** (scoring balls):
1. **Position balls**: Use triggers (6/9) to rotate indexer to correct color
   - Left trigger (6) = GREEN balls
   - Right trigger (9) = PURPLE balls
2. **Start shooter**: Press A button to spin up flywheels
3. **Feed balls**: Use right bumper (10) to feed balls into shooter
4. **Repeat**: Alternate colors as needed based on strategy
5. **Stop**: Press A again to toggle shooter off, or Y for emergency stop

### 🔧 Troubleshooting for Operator:
- **Ball jammed?** Use left bumper (7) to reverse feeder
- **Wrong color ready?** Use triggers (6/9) to rotate indexer
- **Color sensors not working?** Use D-pad LEFT (2) or RIGHT (4) to manually rotate indexer
- **Shooter not working?** Press Y to emergency stop, then restart with A
- **Unsure of ball color?** Check driver station telemetry for color sensor readings

---

## 🤝 Driver & Operator Coordination

### During Match Setup:
- **Driver**: Ensure robot is positioned correctly for autonomous start
- **Operator**: Watch for motif pattern, communicate to alliance if needed
- Both confirm autonomous program selected on driver station

### During Autonomous:
- **Hands off!** Robot runs programmed sequence
- **Operator**: Monitor ball positioning and shooter status
- **Driver**: Watch for any issues or collisions

### During TeleOp:
- **Driver**: Focus on positioning robot for optimal shooting angle
- **Operator**: Focus on ball management and shooting sequence
- **Communication**: Call out "Vision ON", "Aligned", "Shooting", "Reloading"

### Reload Sequence (at Human Player station):
1. **Driver**: Navigate to human player station
2. **Operator**: Prepare to receive balls
3. **Driver**: Position robot, call "Ready for reload"
4. **Human Player**: Loads balls into robot
5. **Operator**: Verifies balls loaded, calls "Ready to shoot"
6. **Driver**: Navigate back to shooting position

---

## 📊 Driver Station Telemetry

What you'll see on the screen (max 5 lines):

```
Mode: Manual / Auto-Align
Shooter: ON / OFF
Vision: ACTIVE / OFF
Target: FOUND (36.2 in) / NOT FOUND
Balls: GREEN ready / PURPLE ready / UNKNOWN
```

### Understanding the Display:
- **Mode**: Shows if auto-alignment is active
- **Shooter**: Current shooter motor status
- **Vision**: AprilTag camera status (turn off when not needed!)
- **Target**: Distance to AprilTag #1 (optimal: 36±3 inches)
- **Balls**: Which color ball is positioned in indexer

---

## ⚠️ Important Reminders

### For Driver:
- ⚡ **Vision uses CPU** - only enable (button 10) when approaching shooting position
- 🎯 Auto-align (button 7) only works when vision is ON
- 🏃 Use triggers for speed control - don't always drive at 80%
- 👀 Watch telemetry for auto-align distance feedback

### For Operator:
- 🎨 **Know the motif order** - green/purple sequence matters for bonus points!
- 🔄 Indexer color sensors detect ball colors - trust the telemetry
- ⏱️ Shooter takes ~2 seconds to spin up after pressing A
- 🚨 Y button is EMERGENCY STOP - use if anything seems wrong

### For Both:
- 🤝 **Communicate constantly** during match
- 📋 Have a pre-planned shooting strategy based on autonomous program
- 🔋 Monitor battery levels and robot performance
- 🏁 Remember parking bonus at end of match (3 points!)

---

## 🎓 Practice Drills

1. **Driver Practice**: Navigate figure-8 pattern, practice vision toggle and auto-align
2. **Operator Practice**: Load and shoot all balls, practice indexer color switching
3. **Team Practice**: Full reload cycle from human player station
4. **Alliance Practice**: Coordinate with partner robot for autonomous strategy
