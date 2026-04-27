# Typing Race Project

## Overview

This project contains a typing race simulation.

The project is split into two main parts:

- Part 1: Internal typing race logic and typist behaviour
- Part 2: GUI version of the typing race using Java Swing

The GUI version separates the internal race logic from the graphical interface:

- `TypingRaceLogic.java` contains the race rules, scoring, performance tracking, history, comparison data, and leaderboard logic.
- `TypingRaceGUI.java` contains the graphical user interface, buttons, labels, text areas, and display updates.
- `Typist.java` stores the state and behaviour of each individual typist.

---

## Dependencies

This project uses only standard Java libraries.

**Required:**

- Java Development Kit (JDK)
- Command line terminal, such as Command Prompt, PowerShell, Terminal, or VS Code terminal

No external libraries are required. The GUI uses Java Swing, which is included with standard Java.

---

## Project Structure

```text
TypingRaceSimulator/
 ├─ README.md
 └─ Part2/
     ├─ TypingRaceGUI.java
     ├─ TypingRaceLogic.java
     └─ Typist.java
```

All Java files inside the `Part2` folder use:

```java
package Part2;
```

Because of this, compile and run commands must be executed from the **root project folder**, not from inside the `Part2` folder.

---

## Setup Instructions

**1. Make sure Java is installed.**

Check Java version:

```
java -version
```

Check Java compiler version:

```
javac -version
```

**2. Open a terminal in the root project folder.**

For example, if the folder is called `TypingRaceSimulator`, navigate into it:

```
cd TypingRaceSimulator
```

**3. Compile the Java files.**

Use this command:

```
javac -encoding UTF-8 Part2/*.java
```

The `-encoding UTF-8` flag is included because the program may contain emoji or special symbols. This prevents encoding errors on Windows.

**4. Run the GUI program.**

```
java Part2.TypingRaceGUI
```

---

## Compile and Run Commands

From the root project folder:

```
javac -encoding UTF-8 Part2/*.java
java Part2.TypingRaceGUI
```

---

## Common Errors

### Encoding Issue

If you see an error like:

```
error: unmappable character for encoding windows-1252
```

Compile using UTF-8:

```
javac -encoding UTF-8 Part2/*.java
```

This error usually occurs on Windows when the source file contains emoji or special characters.

---

## Usage Guidelines

1. Run the program:
   ```
   java Part2.TypingRaceGUI
   ```

2. Choose a passage type:
   - Short
   - Medium
   - Long
   - Custom

3. Choose the number of typists.

4. Configure each typist by selecting:
   - Typing style
   - Keyboard type
   - Symbol or emoji
   - Colour
   - Accessories

5. Optionally, select race modifiers:
   - Autocorrect
   - Caffeine Mode
   - Night Shift

6. Press **Start Race**.

7. After the race finishes, the program displays:
   - Race results
   - Typist history
   - Comparison data
   - Leaderboard

---

## Scoring Guidelines

The scoring system works as follows:

| Placement | Base Points |
|-----------|-------------|
| 1st place | 3 points    |
| 2nd place | 2 points    |
| 3rd place | 1 point     |

**Bonus points:**

- WPM >= 20: +1 bonus point
- WPM >= 30: +2 bonus points

Burnouts are tracked in the results but do not reduce race points.

---

## Notes

- `TypingRaceGUI.java` should be run as the main program.
- `TypingRaceLogic.java` should not be run directly - it does not contain the `main` method.
- `Typist.java` is used by the logic class to store typist data and behaviour.