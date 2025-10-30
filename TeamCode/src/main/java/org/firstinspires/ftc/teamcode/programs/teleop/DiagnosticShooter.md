# DiagnosticShooter

## Purpose

This diagnostic tool tests the **Shooter subsystem** from Robot.java to verify that shooter motors and feeder servos work correctly and are configured with proper directions for ball launching.

**Note:** This tests the complete Shooter subsystem (shooter motors + feeder servos), not just raw hardware components.

## What This Tests

This program tests the **Shooter components** from `Robot.java`:
- `leftShooter` (DcMotorEx) - Left shooter motor with encoder
- `rightShooter` (DcMotorEx) - Right shooter motor with encoder
- `feederLeft` (CRServo) - Left feeder servo
- `feederRight` (CRServo) - Right feeder servo

The test validates:
- ✅ Shooter motors can reach target velocity (3000 RPM default)
- ✅ Shooter motors have working encoders
- ✅ Shooter motors spin **OPPOSITE** each other (creates launch tunnel)
- ✅ Feeder servos push **TOWARD** the shooter
- ✅ Feeder servos spin **OPPOSITE** each other (feed straight)

## Test Requirements

**Before starting the test:**
1. **Do NOT load balls** - Empty shooter completely
2. Robot can remain **on the ground** (no suspension needed)
3. **Keep hands clear** of shooter and feeders during test
4. Have a clear view of shooter motors and feeders to observe rotation direction

**Safety:** This test spins motors at high speed. Stay clear of moving parts!

## Expected Behavior

### Phase 1: Shooter Motors (Sequential Testing)

**Left Shooter Motor Test (5 seconds):**
- Motor spins up to 3000 RPM
- Encoder feedback shows increasing velocity
- **Watch the motor** - note which direction it spins
- Should reach target velocity within ~2-3 seconds

**Right Shooter Motor Test (5 seconds):**
- Motor spins up to 3000 RPM
- Encoder feedback shows increasing velocity
- **Critical: Must spin OPPOSITE direction** from left motor
- Together they create a "tunnel" that grips and launches balls

**Why opposite directions matter:**
- If both spin same direction → Ball will curve/spin out
- Opposite spins create friction tunnel → Ball launches straight
- Think of it like a pitching machine with two wheels

### Phase 2: Feeder Servos (Sequential Testing)

**Left Feeder Servo Test (5 seconds):**
- Servo spins at 50% power
- **Watch the servo** - it should push TOWARD the shooter
- After test, confirm direction with A (correct) or B (wrong) button

**Right Feeder Servo Test (5 seconds):**
- Servo spins at 50% power
- **Critical: Must spin OPPOSITE direction** from left feeder
- Should also push TOWARD shooter
- Together they feed ball straight into shooter tunnel

**Why opposite directions matter:**
- If both spin same direction → Ball pushed sideways, jams
- Opposite spins converge → Ball fed straight into tunnel
- Creates centered feed path for consistent shots

## How the Shooter Works

**The Shooter is a "Tunnel" Design:**
```
         [Left Motor ↻]
              ║ BALL ║  ← Tunnel grips and launches ball
         [Right Motor ↺]
                ↑
         [Feeders push in]
```

- **Left motor spins one way** (e.g., clockwise from front view)
- **Right motor spins opposite way** (e.g., counter-clockwise from front view)
- Together they create spinning tunnel that grips ball
- **Feeders push ball into tunnel** from below/behind
- High RPM causes ball to launch forward at high speed

## Running the Diagnostic

1. **Ensure shooter is empty** (no balls loaded)
2. **Connect Driver Station** to robot
3. **Select "Diagnostic - Shooter"** from TeleOp programs
4. **Press INIT** - Read safety warnings
5. **Press START** - You have 3 seconds to press STOP if needed
6. **Watch each component** as it spins:
   - Left Shooter (1 of 4) - 5 seconds
   - Right Shooter (2 of 4) - 5 seconds
   - Left Feeder (3 of 4) - 5 seconds, then press A (correct) or B (wrong)
   - Right Feeder (4 of 4) - 5 seconds, then press A (correct) or B (wrong)
7. **Review final results** (PASSED / FAILED for each component)
8. **Fix any issues** in `Robot.java` and re-run

## Test Parameters

These can be tuned in the code if needed:

- **TARGET_VELOCITY**: `3000.0` RPM - Target speed for shooter motors
- **TEST_DURATION_MS**: `5000` (5 seconds) - How long each component runs
- **FEEDER_POWER**: `0.5` (50% power) - Power level for feeder servos
- **VELOCITY_TOLERANCE**: `200.0` RPM - Acceptable variance from target

## If Something Goes Wrong

### Problem: Shooter motor doesn't reach target velocity

**What it means:** Motor not spinning fast enough or encoder not working

**Possible causes:**
1. Motor not receiving power
2. Encoder cable disconnected
3. Mechanical friction/binding
4. Motor direction reversed (fighting itself)

**How to fix:**
1. Check motor power connections to hub
2. Verify encoder cable is firmly connected
3. Check for mechanical obstructions
4. Verify motor isn't stalling under load

### Problem: Shooter motors spin same direction (not opposite)

**What it means:** One or both motors have wrong direction in `Robot.java`

**This is CRITICAL!** Same-direction spin won't launch balls correctly.

**How to fix:**
1. Open `Robot.java`
2. Find shooter motor initialization
3. Change motor direction:
   ```java
   // If they're spinning the same way, reverse ONE motor:
   leftShooter.setDirection(DcMotorSimple.Direction.REVERSE);
   // Or reverse the other one:
   rightShooter.setDirection(DcMotorSimple.Direction.REVERSE);
   ```
4. Re-run diagnostic until they spin opposite

### Problem: Feeder doesn't push toward shooter

**What it means:** Feeder servo direction is reversed in `Robot.java`

**How to fix:**
1. When prompted after feeder test, press **B button** (wrong direction)
2. Open `Robot.java`
3. Find feeder servo initialization
4. Add or remove reversal:
   ```java
   // If feeder pushing wrong way, reverse it:
   feederLeft.setDirection(DcMotorSimple.Direction.REVERSE);
   ```
5. Re-run diagnostic and press **A button** when correct

### Problem: Feeders spin same direction (not opposite)

**What it means:** Both feeders configured same way - need to be opposite

**How to fix:**
1. Open `Robot.java`
2. ONE feeder should be FORWARD, one should be REVERSE:
   ```java
   feederLeft.setDirection(DcMotorSimple.Direction.FORWARD);
   feederRight.setDirection(DcMotorSimple.Direction.REVERSE);
   // Or vice versa - depends on your mounting
   ```
3. Re-run diagnostic

### Problem: Motor stalls or vibrates

**What it means:** Mechanical issue or incorrect configuration

**Possible causes:**
- Something blocking motor rotation
- Motor mounted incorrectly
- Loose mounting causing vibration
- Motor damaged

**How to fix:**
1. Power off robot
2. Manually rotate shooter wheels - should spin freely
3. Check mounting bolts are tight
4. Verify nothing blocking rotation
5. Check motor shaft for damage

## Why Test Robot.java Configuration?

**This is NOT a raw hardware test.** This is a **subsystem integration test.**

Your competition programs use the shooter motors and feeder servos exactly as configured in `Robot.java`. If motor directions are wrong, velocities can't be reached, or feeders push the wrong way, your robot won't score during competition.

By testing from `Robot.java`, you ensure:
- ✅ What works in this test will work in TeleOp
- ✅ What works in this test will work in Autonomous
- ✅ Motor directions create proper launch tunnel
- ✅ Feeder directions feed balls correctly
- ✅ You're testing the **actual production subsystem**
- ✅ No surprises on competition day

## Telemetry During Test

**Shooter Motor Test:**
```
Testing: Left Shooter (1/4)
Velocity: 2847 RPM (95%)
Time: 3.2 / 5.0 sec
Status: ✓ At target velocity
```

**Feeder Servo Test:**
```
Testing: Left Feeder (3/4)
Time: 2.4 / 5.0 sec
Status: Spinning...
        Watch: Pushing TOWARD shooter?
```

**Manual Confirmation:**
```
Done: Left Feeder
     Did it push TOWARD shooter?
     Press A = YES, B = NO
```

**Final Results (SUCCESS):**
```
✓ Test Complete

Left Shooter: ✓ PASSED
Right Shooter: ✓ PASSED
Left Feeder: ✓ PASSED
Right Feeder: ✓ PASSED

Result: ✓ ALL TESTS PASSED

Verified: ✓ Shooter motors working
          ✓ Motors spin opposite (tunnel)
          ✓ Feeders push toward shooter
          ✓ Feeders spin opposite (straight)

Status: Shooter system ready!
```

## Success Criteria

✅ **Test PASSED** = Shooter subsystem correctly configured for competition
- Both shooter motors reach 3000 RPM
- Shooter motors spin opposite each other
- Both feeders push toward shooter (confirmed visually)
- Feeders spin opposite each other

❌ **Test FAILED** = Fix issues before competing
- Shooter motor(s) don't reach target velocity
- Shooter motors spin same direction
- Feeder(s) push away from shooter
- Feeders spin same direction

## What's Next?

After the shooter passes this diagnostic:
- ✅ Shooter motor directions verified
- ✅ Feeder directions verified
- 🔄 Test integration with **Indexer subsystem**
- 🎯 Test full scoring sequence with balls in TeleOp

Once this diagnostic passes, your shooter is competition-ready! 🎉

## Tips for Success

**Observing Motor Direction:**
- Stand at the side of the shooter for best view
- Watch rotation direction carefully
- Compare left vs right - are they opposite?
- If unsure, run test again and watch closely

**Observing Feeder Direction:**
- Look at feeder wheels from the side
- Toward shooter = pushing inward/forward
- Away from shooter = pushing outward/backward
- Both should converge toward shooter opening

**Troubleshooting:**
- If motors reach velocity but balls don't launch well → Check motor speeds (may need higher RPM)
- If balls jam → Check feeder alignment and direction
- If balls curve in flight → Shooter motors may not be spinning opposite
- If inconsistent launches → Check for mechanical wobble or loose mounting

**Remember:** This diagnostic verifies your Shooter subsystem directions and velocity control are ready for scoring balls during competition!
