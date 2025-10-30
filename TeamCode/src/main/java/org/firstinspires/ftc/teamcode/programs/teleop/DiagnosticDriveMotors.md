# DiagnosticDriveMotors

## Purpose

This diagnostic tool tests the **Robot.java configuration** for the four basic drive motors, not just the raw hardware. It verifies that the motor directions and encoder readings match what your competition programs (TeleOp and Autonomous) will actually use.

**Note:** This tests the individual drive motor objects from Robot.java, not the MechanumDrive subsystem itself. Use DiagnosticMechanumDrive.java (when created) to test the full subsystem functionality.

## What This Tests

This program tests the **exact motor objects** from `Robot.java`:
- `robot.frontRightDrive`
- `robot.frontLeftDrive`
- `robot.rearRightDrive`
- `robot.rearLeftDrive`

Each motor is commanded to spin "forward" using the configuration already set in `Robot.java`.

## Expected Behavior

**When this diagnostic runs correctly:**
- ✅ Each wheel should spin **FORWARD** (toward the front of the robot)
- ✅ Encoder counts should be **POSITIVE** and increasing
- ✅ Encoder counts should be **at least 500** after 2 seconds at 50% power

This is what we expect because `Robot.java` should be configured so that:
- Positive power = forward motion
- Motor directions are set correctly with `setDirection()`
- Encoders count positively when motors run forward

## If Something Goes Wrong

### Problem: Wheel spins BACKWARDS
**What it means:** The motor direction in `Robot.java` is reversed

**How to fix:**
1. Open `Robot.java`
2. Find the motor that failed (e.g., `frontRightDrive`)
3. Change its direction:
   ```java
   // If it was FORWARD, change to REVERSE
   frontRightDrive.setDirection(DcMotor.Direction.REVERSE);
   
   // Or if it was REVERSE, change to FORWARD
   frontRightDrive.setDirection(DcMotor.Direction.FORWARD);
   ```
4. Re-run this diagnostic until all wheels pass

### Problem: Encoder shows 0 or NOT MOVING
**What it means:** Encoder cable is disconnected or encoder is broken

**How to fix:**
1. Check encoder cable connections to the hub
2. Verify encoder is properly attached to motor
3. Try a different encoder cable
4. If still broken, motor may need replacement

### Problem: Encoder counts NEGATIVE
**What it means:** Motor direction is reversed in `Robot.java`

**How to fix:** Same as "Wheel spins BACKWARDS" above

### Problem: LOW COUNTS (less than 500)
**What it means:** Motor may be stalling or mechanically constrained

**How to fix:**
1. Check for mechanical obstructions
2. Verify wheels can spin freely when suspended
3. Check motor connections
4. Motor may be failing and need replacement

## Why Test Robot.java Configuration?

**This is NOT a raw hardware test.** This is a **configuration verification test.**

Your competition programs don't access motors directly - they use the motor objects from `Robot.java`. If those motor objects are configured incorrectly (wrong direction, wrong encoder polarity), your robot will drive incorrectly during competition.

By testing `robot.frontRightDrive` instead of raw hardware, you ensure:
- ✅ What works in this test will work in TeleOp
- ✅ What works in this test will work in Autonomous
- ✅ You're testing the **actual production configuration**
- ✅ No surprises on competition day

## Safety Requirements

⚠️ **ROBOT MUST BE SUSPENDED OFF THE GROUND**

This test spins wheels at 50% power. If the robot is on the ground:
- Robot may drive off the table
- Robot may collide with objects
- Team members may be injured

**Safe suspension methods:**
- Place robot on blocks with wheels hanging free
- Have team members hold robot securely in the air
- Use a proper robot suspension stand

## Running the Diagnostic

1. **Suspend the robot** safely off the ground
2. **Connect Driver Station** to robot
3. **Select "Diagnostic - Drive Motors"** from TeleOp programs
4. **Press INIT** - Read the safety warnings
5. **Press START** - You have 3 seconds to press STOP if needed
6. **Watch each wheel** as it spins:
   - Front Right (test 1 of 4)
   - Front Left (test 2 of 4)
   - Rear Right (test 3 of 4)
   - Rear Left (test 4 of 4)
7. **Check results** for each motor (PASSED / REVERSED / BROKEN)
8. **Fix any issues** in `Robot.java` and re-run until all pass

## Test Parameters

These can be tuned in the code if needed:

- **TEST_POWER**: `0.5` (50% power) - Safe for suspended testing
- **TEST_DURATION_MS**: `2000` (2 seconds) - Long enough to verify motion
- **PAUSE_BETWEEN_MS**: `1000` (1 second) - Time to observe results between tests

## Success Criteria

✅ **All motors PASSED** = Robot.java is correctly configured for competition
❌ **Any motors REVERSED** = Fix in Robot.java before competing
❌ **Any encoders BROKEN** = Fix hardware before competing

Once all four motors pass this diagnostic, your basic drive motor configuration is verified and competition-ready! 🎉

## What's Next?

After the drive motors pass this diagnostic:
- ✅ Basic motor wiring and direction verified
- 🔄 Test the **MechanumDrive subsystem** (when DiagnosticMechanumDrive.java is created)
- 🎯 Test other subsystems: **Shooter**, **Indexer**, **Vision**, etc.
