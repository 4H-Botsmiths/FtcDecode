# DiagnosticIndexer

## Purpose

This diagnostic tool tests the **Indexer subsystem** from Robot.java to verify that color sensors and rotation mechanisms work correctly for competition. It validates the exact configuration that TeleOp and Autonomous programs will use.

**Note:** This tests the complete Indexer subsystem (color sensors + rotation servo), not just raw hardware components.

## What This Tests

This program tests the **Indexer subsystem** from `Robot.java`:
- `indexer` (GoBUILDA 5-turn servo 2000-0024-0502 on CH_SERVO_1 for position-based rotation)
- `colorSensorLeft` (detects balls on left side)
- `colorSensorRight` (detects balls on right side)

The test validates:
- ✅ Color sensors can distinguish GREEN vs PURPLE balls
- ✅ Color sensors can detect ball presence (present vs absent)
- ✅ Indexer rotates counter-clockwise correctly
- ✅ Balls advance through the indexer slots properly
- ✅ RGB values are being read correctly from sensors

## Test Requirements

**Before starting the test:**
1. Load the indexer with **EXACTLY 3 balls in ANY pattern**
2. Must have: **2 PURPLE balls** and **1 GREEN ball**
3. Any loading pattern is acceptable:
   - Purple-Purple-Green ✓
   - Green-Purple-Purple ✓
   - Purple-Green-Purple ✓
   - Any other arrangement ✓

**Why any pattern is OK:**
- The robot uses color sensors to intelligently sequence the balls
- It will **always dump in this order: PURPLE → GREEN → PURPLE**
- This tests the robot's ability to read sensors and make decisions (critical for DECODE!)
- Tests both colors to verify sensor calibration
- Verifies the robot can find and position the correct ball

## Expected Behavior

**During the test (INTELLIGENT SEQUENCING):**

**Step 1 - Dump First PURPLE:**
1. Read sensors to find a purple ball
2. Rotate to position purple ball on right (dump position)
3. Dump the purple ball

**Step 2 - Dump GREEN:**
1. Read sensors to find the green ball
2. Rotate to position green ball on right (dump position)
3. Dump the green ball

**Step 3 - Dump Second PURPLE:**
1. Read sensors to find remaining purple ball
2. Rotate to position purple ball on right (dump position)
3. Dump the purple ball

**Key Intelligence:**
- Robot **decides** which direction to rotate based on sensor readings
- If desired ball is on right → No rotation needed
- If desired ball is on left → Rotate once counter-clockwise
- If desired ball is not visible → Rotate to find it, then position it

**Expected Final Results:**
- ✅ Dump Sequence: **PURPLE → GREEN → PURPLE** (correct order)
- ✅ All 3 balls successfully dumped
- ✅ Robot made correct decisions based on sensor readings

## How the Indexer Works

The indexer is a **circular paddle with 3 slots** that rotates to position balls:
- **Left Sensor**: Detects ball at approximately 10 o'clock position
- **Right Sensor**: Detects ball at approximately 2 o'clock position (drop position)
- **Dump Hole**: Located where right sensor detects balls

**Position-based rotation using GoBUILDA 5-turn servo:**
- **HOME position** (0.0): Default position where paddle blocks the drop hole
- **DROP position** (0.1667): Ball aligned with drop hole, ready to feed into shooter
- **Rotation increment** (0.3333): Each rotation moves 120° to next slot (360°/3 slots)
- **Rotation delay**: 500ms for servo to complete movement between positions

**Rotation directions:**
- **rotateLeft()**: Moves counter-clockwise - brings balls from left sensor to right (drop position)
- **rotateRight()**: Moves clockwise - brings balls from right sensor toward left side

This position-based control allows precise ball positioning for feeding into the shooter.

## If Something Goes Wrong

### Problem: "Only dumped X balls" (less than 3)
**What it means:** Robot couldn't find or identify all the balls

**Possible causes:**
1. Color sensors can't detect one or more ball colors
2. Ball color is ambiguous (faded, wrong shade, or dirty)
3. Sensor threshold needs adjustment

**How to fix:**
1. Check RGB values in telemetry - are they clearly different for purple vs green?
2. Use fresh, vibrant purple and green balls
3. Clean color sensor lenses
4. Adjust `COLOR_THRESHOLD` in `Indexer.java` if needed
5. Verify you loaded exactly 2 purple + 1 green

### Problem: "Wrong ball sequence"
**What it means:** Robot dumped balls in wrong order (not PURPLE-GREEN-PURPLE)

**Possible causes:**
1. Color sensors misidentified ball colors
2. Sensor readings are inconsistent
3. Rotation mechanism not working correctly

**How to fix:**
1. Check if ball colors are clearly purple and green (not faded)
2. Verify sensor readings in telemetry match visual observation
3. Clean color sensors
4. Adjust `COLOR_THRESHOLD` in `Indexer.java`
5. Verify rotation direction is correct (counter-clockwise)

### Problem: Balls not dumping / staying in indexer
**What it means:** Rotation mechanism may not be working, or servo not reaching correct positions

**How to fix:**
1. Check servo connection to Control Hub servo port CH_SERVO_1
2. Verify servo is configured as "Full Range Servo" in Driver Station
3. Verify servo is receiving power and responding to position commands
4. Check if ROTATION_DELAY_MS (500ms) is sufficient for servo movement
5. Mechanical issue - indexer paddle may be jammed or obstructed
6. Verify HOME_POSITION (0.0) and DROP_POSITION (0.1667) are correctly calibrated

### Problem: "Ball present - color UNKNOWN"
**What it means:** Sensor detects something but can't determine color

**Possible causes:**
- Ball color is ambiguous (faded, wrong color, or intermediate shade)
- Lighting conditions are poor
- COLOR_THRESHOLD needs tuning

**How to fix:**
1. Use fresh, vibrant green and purple balls
2. Test in consistent lighting (avoid direct sunlight or shadows)
3. Adjust `COLOR_THRESHOLD` in `Indexer.java` if needed
4. Clean color sensor lenses

## Why Test Robot.java Configuration?

**This is NOT a raw hardware test.** This is a **subsystem intelligence test.**

Your competition programs use the `Indexer` subsystem to intelligently sequence balls based on the motif pattern. In DECODE, the robot must shoot balls in a specific order determined by the motif. This diagnostic tests the exact same color detection and decision-making logic your autonomous program will use.

By testing the `Indexer` object from `Robot.java`, you ensure:
- ✅ What works in this test will work in TeleOp
- ✅ What works in this test will work in Autonomous
- ✅ Color detection thresholds are properly tuned
- ✅ Robot can intelligently sequence balls (critical for DECODE!)
- ✅ Rotation direction matches expectations
- ✅ You're testing the **actual production subsystem**
- ✅ No surprises on competition day

**DECODE Connection:** This test mimics autonomous where your robot must:
1. Read the motif pattern (tells you: Purple-Green-Purple or Green-Purple-Green, etc.)
2. Use sensors to find and position the correct colored ball
3. Dump balls in the correct sequence for bonus points

## Running the Diagnostic

1. **Load 3 balls** into indexer (2 purple + 1 green)
2. **Connect Driver Station** to robot
3. **Select "Diagnostic - Indexer"** from TeleOp programs
4. **Press INIT** - Verify ball count on screen
5. **Press START** - Test begins after 3-second countdown
6. **Watch the indexer** rotate and dump each ball
7. **Check telemetry** for color sensor readings
8. **Review final results** (PASSED / FAILED)
9. **Fix any issues** in `Indexer.java` or `Robot.java` if needed

## Test Parameters

These can be tuned in the code if needed:

- **READ_PAUSE_MS**: `2000` (2 seconds) - Time to display color readings before positioning
- **DUMP_VERIFICATION_MS**: `1000` (1 second) - Time to verify ball dropped
- **ROTATION_DELAY_MS**: `500` (0.5 seconds) - Time for servo to complete rotation (defined in `Indexer.java`)

**Servo Position Constants** (defined in `Indexer.java`):
- **HOME_POSITION**: `0.0` - Default position, blocks drop hole
- **DROP_POSITION**: `0.1667` - Ball aligned with drop hole for feeding
- **SLOT_INCREMENT**: `0.3333` - Rotation amount per slot (120° for 3-slot paddle)

If balls aren't fully positioning before drop, increase `ROTATION_DELAY_MS` in `Indexer.java`.

## Telemetry During Test

**Step 1 - Looking for PURPLE:**
```
Step: 1 of 3: Need PURPLE
Left: PURPLE
Right: GREEN
Raw: L(R:120 G:45 B:98) R(R:42 G:150 B:50)
```

**Positioning Ball:**
```
Step: 1: PURPLE
Status: On LEFT - rotating left
        Moving to drop position
Position: 0.3333 → 0.0000
```

**Dumping Ball:**
```
Dumping: PURPLE (1/3)
Time: 1.2 sec
```

**Final Results (SUCCESS):**
```
✓ Test Complete

Target Sequence: PURPLE → GREEN → PURPLE
Balls Dumped: 3 of 3

Result: ✓ ALL TESTS PASSED
        Correct sequence achieved!

Verified: ✓ Color sensors working
          ✓ Intelligent sequencing working
          ✓ Rotation mechanism working

Status: Competition-ready!
```

## Success Criteria

✅ **Test PASSED** = Indexer subsystem is correctly configured for competition
- Robot successfully dumped balls in correct order: PURPLE → GREEN → PURPLE
- Color sensors accurately identified all ball colors
- Robot intelligently positioned each ball before dumping
- All 3 balls successfully dumped

❌ **Test FAILED** = Fix issues before competing
- Wrong dump sequence (not PURPLE → GREEN → PURPLE)
- Less than 3 balls dumped
- Color sensors misidentifying colors
- Robot can't find or position balls correctly

## What's Next?

After the indexer passes this diagnostic:
- ✅ Color sensors are calibrated and working
- ✅ Rotation mechanism verified
- 🔄 Test integration with **Shooter subsystem**
- 🎯 Test full scoring sequence in TeleOp

Once this diagnostic passes, your indexer is competition-ready! 🎉

## Tips for Success

**Ball Preparation:**
- Use clean balls with vibrant colors
- Avoid faded or worn balls
- Keep balls away from dirt and debris

**Testing Environment:**
- Test in similar lighting to competition venue
- Avoid direct sunlight on color sensors
- Consistent lighting gives consistent results

**Troubleshooting:**
- Watch the "Raw Values" line to see actual RGB readings
- Purple should show high Red and Blue values
- Green should show high Green value
- If readings are close to thresholds, consider tuning

**Remember:** This diagnostic verifies your Indexer subsystem is ready for autonomous ball sequencing and TeleOp scoring!
