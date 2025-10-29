# DECODE Autonomous Strategy Guide

## � Understanding DECODE Game Mechanics

**The Motif Challenge:**
- At the start of autonomous, a **motif pattern** appears on the obelisk
- The motif determines the **correct shooting order** (green vs purple balls)
- Reading the motif correctly = bonus points for shooting in the right order
- Getting it wrong = no bonus (but still score points for making baskets)

**Two Starting Positions:**
1. **Near Motif/Obelisk** - Can read the pattern and shoot optimally
2. **Near Human Players** - Cannot read motif, must use pre-loaded ball order

**Alliance Coordination:**
- Two robots per alliance = two shooting opportunities
- Decide who shoots **FIRST** and who shoots **SECOND** before the match
- Second shooter waits ~10 seconds for first shooter to complete their cycle

---

## 📋 Available Autonomous Programs (5 Total)

### 1. Decode Auto - Drive Only ⚡
**Strategy:** Simple & Reliable - Just cross the starting line

**Starting Position:** Works from EITHER starting position

**Sequence:**
1. Drive forward to cross starting line
2. Stop and hold position

**Points:** Guaranteed points for leaving starting area

**Best for:**
- Learning autonomous basics
- When you want zero risk
- When autonomous period is chaotic and you just want guaranteed points
- First competitions while still developing complex strategies

---

### 2. Decode Auto - Motif First Shoot 🎯🥇
**Strategy:** Start near motif, read pattern, shoot FIRST, reload, shoot again

**Starting Position:** NEAR MOTIF/OBELISK (can read pattern)

**Alliance Role:** YOU SHOOT FIRST

**Sequence:**
1. Read motif pattern from obelisk
2. Navigate to shooting position
3. Shoot balls in correct order (based on motif)
4. Return to human player station
5. Reload balls
6. Navigate back to shooting position
7. Shoot second set of balls
8. Park in observation zone

**Best for:**
- When you have strong, fast autonomous
- When partner agrees to wait and shoot second
- Maximizing scoring (two shooting cycles + motif bonus)
- When starting near the motif/obelisk

---

### 3. Decode Auto - Motif Second Shoot 🎯🥈
**Strategy:** Start near motif, WAIT for partner, read pattern, shoot once

**Starting Position:** NEAR MOTIF/OBELISK (can read pattern)

**Alliance Role:** YOU SHOOT SECOND (partner shoots first)

**Wait Time:** 10 seconds for partner to complete their cycle

**Sequence:**
1. **WAIT** 10 seconds for alliance partner
2. Read motif pattern from obelisk
3. Navigate to shooting position
4. Shoot balls in correct order (based on motif)
5. Park in observation zone

**Best for:**
- When partner has aggressive autonomous and wants to shoot first
- Coordinated alliance strategy to avoid collisions
- Still get motif bonus but with safer timing
- When starting near the motif/obelisk

---

### 4. Decode Auto - Human First Shoot 🤝🥇
**Strategy:** Start near human players, shoot FIRST, reload, shoot again

**Starting Position:** NEAR HUMAN PLAYERS (cannot read motif from this position)

**Alliance Role:** YOU SHOOT FIRST

**Ball Order:** Must be communicated by alliance partner who can see the motif!

**Sequence:**
1. Navigate to shooting position
2. Shoot balls in correct order (based on motif read by partner)
3. Return to human player station (short distance!)
4. Reload balls
5. Navigate back to shooting position
6. Shoot second set of balls
7. Park in observation zone

**Best for:**
- When starting near human players
- When you want to shoot first and have fast reload capability
- Shorter travel distance for reloading
- Two scoring cycles with motif bonus if partner communicates order

**⚠️ CRITICAL:** Alliance partner starting at motif MUST communicate the ball order!

---

### 5. Decode Auto - Human Second Shoot 🤝🥈
**Strategy:** Start near human players, WAIT for partner, shoot once

**Starting Position:** NEAR HUMAN PLAYERS (cannot read motif from this position)

**Alliance Role:** YOU SHOOT SECOND (partner shoots first)

**Wait Time:** 10 seconds for partner to complete their cycle

**Ball Order:** Must be communicated by alliance partner who can see the motif!

**Sequence:**
1. **WAIT** 10 seconds for alliance partner
2. Navigate to shooting position
3. Shoot balls in correct order (based on motif read by partner)
4. Park in observation zone

**Best for:**
- When starting near human players
- When partner shoots first from motif position
- Coordinated alliance strategy
- Safer timing, less collision risk

**⚠️ CRITICAL:** Alliance partner starting at motif MUST communicate the ball order!

---

## 🤝 Pre-Match Alliance Discussion Checklist

Before each match, discuss with your alliance partner:

1. ☐ **Starting positions:** Who starts near motif? Who starts near human players?
2. ☐ **Shooting order:** Who shoots FIRST? Who shoots SECOND (waits)?
3. ☐ **Motif communication:** If one robot starts near human players, how will the motif order be communicated?
4. ☐ **Motif reading:** Confirm robot at motif position will read and communicate the pattern
5. ☐ **Parking zones:** Which observation zone will each robot use?
6. ☐ **Autonomous programs:** Confirm which program each robot will run

---

## 💡 Strategic Decision Making

### Choosing Your Starting Position

**Start Near Motif/Obelisk:**
- ✅ Can read motif for shooting bonus points
- ✅ Optimal for teams with good vision/pattern recognition
- ⚠️ Longer travel to human players for reload
- **Use:** Motif First Shoot OR Motif Second Shoot

**Start Near Human Players:**
- ✅ Very short reload distance
- ✅ Easier to execute two shooting cycles
- ⚠️ Cannot read motif - alliance partner must communicate ball order
- ✅ Can still get motif bonus if partner communicates order correctly
- **Use:** Human First Shoot OR Human Second Shoot

### Choosing First vs. Second Shooter

**Shoot First:**
- ✅ Maximum scoring potential (can attempt two cycles)
- ✅ No waiting - immediate action
- ⚠️ Must be fast and reliable
- ⚠️ Partner must wait for you
- **Use:** Motif First Shoot OR Human First Shoot

**Shoot Second (Wait 10 seconds):**
- ✅ Safer - partner clears field first
- ✅ Less collision risk
- ✅ More time for precise alignment
- ⚠️ Only one shooting cycle (no time for reload)
- ⚠️ Depends on partner finishing on time
- **Use:** Motif Second Shoot OR Human Second Shoot

---

## 📊 Timing Breakdown (30-second autonomous period)

### First Shooter Strategies (Motif or Human):
- **0-8 sec:** Read motif (if at motif position) and shoot first set
- **8-16 sec:** Return to human players and reload
- **16-24 sec:** Navigate back and shoot second set
- **24-30 sec:** Park in observation zone

### Second Shooter Strategies (Motif or Human):
- **0-10 sec:** **WAIT** for alliance partner to clear field
- **10-20 sec:** Navigate and shoot (using motif or pre-loaded order)
- **20-30 sec:** Park in observation zone

### Drive Only Strategy:
- **0-3 sec:** Drive forward across starting line
- **3-30 sec:** Hold position (already scored!)

---

## � Strategy Recommendations by Experience Level

### Beginner Team (First Season):
- Start with **Drive Only** - guaranteed points, build confidence
- Move to **Human Second Shoot** - simpler, no motif reading needed
- Always use "Second Shooter" role - safer coordination

### Intermediate Team (Learning Advanced Features):
- Try **Motif Second Shoot** - learn vision/motif reading
- Experiment with **Human First Shoot** - practice reload cycles
- Coordinate with strong alliance partners

### Advanced Team (Competition Ready):
- Master **Motif First Shoot** - maximum points potential
- Perfect your reload timing for two-cycle strategies
- Be flexible - select program based on alliance partner strength

---

## 🏆 Competition Day Notes

### Before the Match:
- ✅ Verify starting position matches your program selection
- ✅ If using Human start, **confirm how partner will communicate motif order!**
- ✅ Test motif reading in practice if using Motif programs
- ✅ Confirm wait time (10 sec) with alliance partner
- ✅ Select correct autonomous program on Driver Station
- ✅ If at motif position, be ready to communicate ball order to partner

### During Alliance Selection:
- Communicate your strongest autonomous strategy to potential partners
- Ask alliance partners: "Do you prefer to shoot first or second?"
- Verify which starting positions each robot will use
- Have backup strategies ready

### If Something Goes Wrong:
- **Motif reading fails?** → Shoot anyway, still get basket points
- **Partner doesn't wait?** → Adapt and continue your program
- **Collision risk?** → Stop and park safely
- **Reload takes too long?** → Skip second cycle, just park

---

## 📈 Points Optimization Guide

**Maximum Possible Points (Motif First Shoot):**
- Cross starting line: +X points
- First shooting cycle (4-6 balls in correct order): +XX points + motif bonus
- Second shooting cycle (4-6 balls): +XX points
- Park in observation zone: +3 points
- **Total: Best possible autonomous score**

**Reliable Points (Drive Only):**
- Cross starting line: +X points
- **Total: Guaranteed minimum score**

**Balanced Points (Motif/Human Second Shoot):**
- Cross starting line: +X points
- One shooting cycle: +XX points (+ motif bonus if using Motif program)
- Park in observation zone: +3 points
- **Total: Solid mid-range score with low risk**

---

## 🔧 Technical Implementation Status

**Current Status:** All 5 programs created with TODO comments for implementation

**Next Steps:**
1. Implement encoder-based drive movements
2. Implement motif reading vision code
3. Implement shooting and indexing sequences
4. Test and tune timing for each strategy
5. Practice alliance coordination

**Remember:** These programs are frameworks - the actual movement code needs to be implemented based on your specific robot dimensions, motor speeds, and field measurements!
