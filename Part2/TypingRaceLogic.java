package Part2;

import java.util.ArrayList;

/**
 * Internal logic for the typing race.
 *
 * This class contains the race state, typist behaviour, scoring,
 * history, comparison, leaderboard and attribute impact text.
 *
 * It does not contain Swing GUI components.
 */
public class TypingRaceLogic
{
    public static final int MAX_TYPISTS = 6;

    public static final int TIMER_TICK = 50;
    private static final int BASE_TYPING_DELAY = 350;

    private static final int SLIDE_BACK_AMOUNT = 2;
    private static final int BURNOUT_DURATION = 3;

    private static final double CAFFEINE_ACCURACY_BOOST = 0.15;
    private static final double CAFFEINE_EXTRA_BURNOUT_RISK = 0.03;
    private static final double NIGHT_SHIFT_ACCURACY_PENALTY = 0.10;

    private static final double ENERGY_DRINK_FIRST_HALF_BOOST = 0.08;
    private static final double HEADPHONES_MISTYPE_REDUCTION = 0.07;

    private static final double WPM_BONUS_THRESHOLD = 20.0;
    private static final double HIGH_WPM_BONUS_THRESHOLD = 30.0;

    private String passage;
    private int passageLength;

    private Typist[] typists;
    private String[] typistColours;

    private boolean finished;
    private Typist winner;

    private int turnCounter;
    private long raceStartTime;
    private int raceNumber;

    private int[] typistDelays;
    private int[] typistCooldowns;
    private int[] typistBurnoutDurations;

    private double[] typistBurnoutMultipliers;

    private int[] correctKeystrokes;
    private int[] totalKeystrokes;
    private int[] burnoutCounts;
    private double[] startingAccuracies;

    private double[] personalBestWPM;
    private ArrayList<RaceResult>[] raceHistories;

    private int[] cumulativePoints;
    private int[] currentRacePoints;
    private int[] consecutiveWins;
    private int[] noBurnoutStreak;

    private String resultsText;
    private String historyText;
    private String leaderboardText;

    public TypingRaceLogic()
    {
        personalBestWPM = new double[MAX_TYPISTS];
        raceHistories = new ArrayList[MAX_TYPISTS];

        cumulativePoints = new int[MAX_TYPISTS];
        currentRacePoints = new int[MAX_TYPISTS];
        consecutiveWins = new int[MAX_TYPISTS];
        noBurnoutStreak = new int[MAX_TYPISTS];

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            personalBestWPM[i] = 0.0;
            raceHistories[i] = new ArrayList<RaceResult>();

            cumulativePoints[i] = 0;
            currentRacePoints[i] = 0;
            consecutiveWins[i] = 0;
            noBurnoutStreak[i] = 0;
        }

        raceNumber = 0;

        resultsText = "Race results will appear here after the first race.";
        historyText = "Historical data will appear here after races are completed.";
        leaderboardText = "Leaderboard will appear here after races are completed.";
    }

    public static class TypistSettings
    {
        String style;
        String keyboard;
        String symbol;
        String colour;
        boolean wristSupport;
        boolean energyDrink;
        boolean headphones;

        public TypistSettings(String style, String keyboard, String symbol, String colour,
                              boolean wristSupport, boolean energyDrink, boolean headphones)
        {
            this.style = style;
            this.keyboard = keyboard;
            this.symbol = symbol;
            this.colour = colour;
            this.wristSupport = wristSupport;
            this.energyDrink = energyDrink;
            this.headphones = headphones;
        }
    }

    private static class RaceResult
    {
        int raceNumber;
        int position;
        int progress;
        double wpm;
        double accuracyPercentage;
        int burnoutCount;
        double accuracyChange;
        int racePoints;
        int cumulativePoints;
        String title;

        RaceResult(int raceNumber, int position, int progress, double wpm,
                   double accuracyPercentage, int burnoutCount,
                   double accuracyChange, int racePoints,
                   int cumulativePoints, String title)
        {
            this.raceNumber = raceNumber;
            this.position = position;
            this.progress = progress;
            this.wpm = wpm;
            this.accuracyPercentage = accuracyPercentage;
            this.burnoutCount = burnoutCount;
            this.accuracyChange = accuracyChange;
            this.racePoints = racePoints;
            this.cumulativePoints = cumulativePoints;
            this.title = title;
        }
    }

    public void startRace(String passage, TypistSettings[] settings, boolean nightShiftOn)
    {
        this.passage = passage;
        passageLength = passage.length();

        createTypists(settings);
        applyStartingModifiers(nightShiftOn);
        initialisePerformanceTracking(settings.length);

        finished = false;
        winner = null;
        turnCounter = 0;
        raceStartTime = System.currentTimeMillis();

        for (int i = 0; i < typists.length; i++)
        {
            typists[i].resetToStart();
            typistCooldowns[i] = 0;
        }
    }

    private void createTypists(TypistSettings[] settings)
    {
        int numberOfTypists = settings.length;

        typists = new Typist[numberOfTypists];
        typistColours = new String[numberOfTypists];

        typistDelays = new int[numberOfTypists];
        typistCooldowns = new int[numberOfTypists];
        typistBurnoutMultipliers = new double[numberOfTypists];
        typistBurnoutDurations = new int[numberOfTypists];

        String[] names = {
            "TURBOFINGERS",
            "HUNT_N_PECK",
            "KEYBOARD_KING",
            "TYPE_MASTER",
            "FAST_HANDS",
            "SPACEBAR_PRO"
        };

        double baseAccuracy = 0.75;

        for (int i = 0; i < numberOfTypists; i++)
        {
            String symbol = settings[i].symbol.trim();

            if (symbol.length() == 0)
            {
                symbol = "" + (i + 1);
            }

            double accuracy = baseAccuracy;

            String style = settings[i].style;
            String keyboard = settings[i].keyboard;

            accuracy = accuracy + getStyleAccuracyModifier(style);
            accuracy = accuracy + getKeyboardAccuracyModifier(keyboard);

            accuracy = accuracy + getRankAccuracyImpact(i);

            int delay = BASE_TYPING_DELAY;
            delay = delay + getKeyboardDelayModifier(keyboard);

            if (delay < 120)
            {
                delay = 120;
            }

            typistDelays[i] = delay;
            typistCooldowns[i] = 0;

            typistBurnoutMultipliers[i] = getStyleBurnoutMultiplier(style)
                * getKeyboardBurnoutMultiplier(keyboard);

            typistBurnoutDurations[i] = BURNOUT_DURATION;

            if (settings[i].wristSupport)
            {
                typistBurnoutDurations[i] = typistBurnoutDurations[i] - 1;

                if (typistBurnoutDurations[i] < 1)
                {
                    typistBurnoutDurations[i] = 1;
                }
            }

            typistColours[i] = getColourHex(settings[i].colour);

            typists[i] = new Typist(symbol, names[i], accuracy);
        }
    }

    private void initialisePerformanceTracking(int numberOfTypists)
    {
        correctKeystrokes = new int[numberOfTypists];
        totalKeystrokes = new int[numberOfTypists];
        burnoutCounts = new int[numberOfTypists];
        startingAccuracies = new double[numberOfTypists];

        for (int i = 0; i < numberOfTypists; i++)
        {
            correctKeystrokes[i] = 0;
            totalKeystrokes[i] = 0;
            burnoutCounts[i] = 0;
            startingAccuracies[i] = typists[i].getAccuracy();
            currentRacePoints[i] = 0;
        }
    }

    public void raceStep(TypistSettings[] settings, boolean autocorrectOn, boolean caffeineOn)
    {
        if (finished)
        {
            return;
        }

        turnCounter = turnCounter + 1;

        for (int i = 0; i < typists.length; i++)
        {
            typistCooldowns[i] = typistCooldowns[i] - TIMER_TICK;

            if (typistCooldowns[i] <= 0)
            {
                advanceTypist(typists[i], i, settings[i], autocorrectOn, caffeineOn);
                typistCooldowns[i] = typistDelays[i];
            }
        }

        for (int i = 0; i < typists.length; i++)
        {
            if (raceFinishedBy(typists[i]))
            {
                finished = true;
                winner = typists[i];
                break;
            }
        }

        if (finished)
        {
            raceNumber = raceNumber + 1;
            calculateAndDisplayPerformanceMetrics();
        }
    }

    private void advanceTypist(Typist theTypist, int typistIndex,
                               TypistSettings settings, boolean autocorrectOn, boolean caffeineOn)
    {
        if (theTypist.isBurntOut())
        {
            theTypist.recoverFromBurnout();
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.01);
            return;
        }

        double typingChance = theTypist.getAccuracy();

        if (caffeineOn && turnCounter <= 10)
        {
            typingChance = typingChance + CAFFEINE_ACCURACY_BOOST;
        }

        if (settings.energyDrink)
        {
            if (theTypist.getProgress() < passageLength / 2)
            {
                typingChance = typingChance + ENERGY_DRINK_FIRST_HALF_BOOST;
            }
        }

        if (settings.headphones)
        {
            typingChance = typingChance + HEADPHONES_MISTYPE_REDUCTION;
        }

        if (typingChance > 1.0)
        {
            typingChance = 1.0;
        }

        if (typingChance < 0.0)
        {
            typingChance = 0.0;
        }

        int slideBackAmount = SLIDE_BACK_AMOUNT;

        if (autocorrectOn)
        {
            slideBackAmount = SLIDE_BACK_AMOUNT / 2;

            if (slideBackAmount < 1)
            {
                slideBackAmount = 1;
            }
        }

        totalKeystrokes[typistIndex] = totalKeystrokes[typistIndex] + 1;

        if (Math.random() < typingChance)
        {
            theTypist.typeCharacter();
            theTypist.setAccuracy(theTypist.getAccuracy() + 0.001);
            correctKeystrokes[typistIndex] = correctKeystrokes[typistIndex] + 1;
        }
        else
        {
            theTypist.slideBack(slideBackAmount);
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.005);
        }

        double burnoutRisk = 0.05
            * theTypist.getAccuracy()
            * theTypist.getAccuracy()
            * typistBurnoutMultipliers[typistIndex];

        if (caffeineOn && turnCounter > 10)
        {
            burnoutRisk = burnoutRisk + CAFFEINE_EXTRA_BURNOUT_RISK;
        }

        if (Math.random() < burnoutRisk)
        {
            theTypist.burnOut(typistBurnoutDurations[typistIndex]);
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.01);
            burnoutCounts[typistIndex] = burnoutCounts[typistIndex] + 1;
        }
    }

    private void calculateAndDisplayPerformanceMetrics()
    {
        long raceEndTime = System.currentTimeMillis();
        double elapsedMinutes = (raceEndTime - raceStartTime) / 60000.0;

        if (elapsedMinutes <= 0.0)
        {
            elapsedMinutes = 0.001;
        }

        String text = "";
        text = text + "Race " + raceNumber + " Results\n";
        text = text + "Elapsed time: " + roundTwo(elapsedMinutes * 60.0) + " seconds\n\n";

        for (int i = 0; i < typists.length; i++)
        {
            int progress = typists[i].getProgress();

            if (progress > passageLength)
            {
                progress = passageLength;
            }

            double wordsTyped = progress / 5.0;
            double wpm = wordsTyped / elapsedMinutes;

            double accuracyPercentage = 0.0;

            if (totalKeystrokes[i] > 0)
            {
                accuracyPercentage = (correctKeystrokes[i] * 100.0) / totalKeystrokes[i];
            }

            double accuracyChange = typists[i].getAccuracy() - startingAccuracies[i];

            int position = calculatePosition(i);

            if (wpm > personalBestWPM[i])
            {
                personalBestWPM[i] = wpm;
            }

            updateTitleCounters(i, position);

            int racePoints = calculateRacePoints(i, position, wpm);
            currentRacePoints[i] = racePoints;
            cumulativePoints[i] = cumulativePoints[i] + racePoints;

            String title = getTitleForTypist(i);

            RaceResult result = new RaceResult(
                raceNumber,
                position,
                progress,
                wpm,
                accuracyPercentage,
                burnoutCounts[i],
                accuracyChange,
                racePoints,
                cumulativePoints[i],
                title
            );

            raceHistories[i].add(result);

            text = text + typists[i].getName() + "\n";
            text = text + "- Position: " + position + "\n";
            text = text + "- Progress: " + progress + "/" + passageLength + " characters\n";
            text = text + "- WPM: " + roundTwo(wpm) + "\n";
            text = text + "- Accuracy Percentage: " + roundTwo(accuracyPercentage) + "%\n";
            text = text + "- Burnout Count: " + burnoutCounts[i] + "\n";
            text = text + "- Accuracy Change: " + signed(roundThree(accuracyChange)) + "\n";
            text = text + "- Personal Best WPM: " + roundTwo(personalBestWPM[i]) + "\n";
            text = text + "- Race Points: " + racePoints + "\n";
            text = text + "- Cumulative Points: " + cumulativePoints[i] + "\n";
            text = text + "- Title/Badge: " + title + "\n\n";
        }

        resultsText = text;

        updateHistoryArea();
        updateLeaderboardArea();
    }

    private int calculateRacePoints(int typistIndex, int position, double wpm)
    {
        int basePoints = getBasePointsForPosition(position);
        int wpmBonus = getWpmBonus(wpm);

        int total = basePoints + wpmBonus;

        return total;
    }

    private int getBasePointsForPosition(int position)
    {
        if (position == 1)
        {
            return 3;
        }
        else if (position == 2)
        {
            return 2;
        }
        else if (position == 3)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private int getWpmBonus(double wpm)
    {
        if (wpm >= HIGH_WPM_BONUS_THRESHOLD)
        {
            return 2;
        }
        else if (wpm >= WPM_BONUS_THRESHOLD)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private void updateTitleCounters(int typistIndex, int position)
    {
        if (position == 1)
        {
            consecutiveWins[typistIndex] = consecutiveWins[typistIndex] + 1;
        }
        else
        {
            consecutiveWins[typistIndex] = 0;
        }

        if (burnoutCounts[typistIndex] == 0)
        {
            noBurnoutStreak[typistIndex] = noBurnoutStreak[typistIndex] + 1;
        }
        else
        {
            noBurnoutStreak[typistIndex] = 0;
        }
    }

    private String getTitleForTypist(int typistIndex)
    {
        if (consecutiveWins[typistIndex] >= 3)
        {
            return "Speed Demon";
        }
        else if (noBurnoutStreak[typistIndex] >= 5)
        {
            return "Iron Fingers";
        }
        else if (cumulativePoints[typistIndex] >= 15)
        {
            return "League Contender";
        }
        else if (cumulativePoints[typistIndex] >= 8)
        {
            return "Rising Star";
        }
        else
        {
            return "Rookie";
        }
    }

    private int calculatePosition(int typistIndex)
    {
        int position = 1;
        int progress = typists[typistIndex].getProgress();

        for (int i = 0; i < typists.length; i++)
        {
            if (typists[i].getProgress() > progress)
            {
                position = position + 1;
            }
        }

        return position;
    }

    private int getCumulativeRank(int typistIndex)
    {
        int rank = 1;

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            if (cumulativePoints[i] > cumulativePoints[typistIndex])
            {
                rank = rank + 1;
            }
        }

        return rank;
    }

    private void updateHistoryArea()
    {
        String text = "";

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            if (raceHistories[i].size() > 0)
            {
                text = text + "Typist " + (i + 1) + " History\n";

                for (int j = 0; j < raceHistories[i].size(); j++)
                {
                    RaceResult result = raceHistories[i].get(j);

                    text = text
                        + "Race " + result.raceNumber
                        + " | Position: " + result.position
                        + " | Progress: " + result.progress
                        + " | WPM: " + roundTwo(result.wpm)
                        + " | Accuracy: " + roundTwo(result.accuracyPercentage) + "%"
                        + " | Burnouts: " + result.burnoutCount
                        + " | Accuracy Change: " + signed(roundThree(result.accuracyChange))
                        + " | Race Points: " + result.racePoints
                        + " | Total Points: " + result.cumulativePoints
                        + " | Title: " + result.title
                        + "\n";
                }

                text = text + "\n";
            }
        }

        historyText = text;
    }

    public String getComparisonText(String metric)
    {
        String text = "";

        text = text + "Comparison by " + metric + "\n\n";

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            if (raceHistories[i].size() > 0)
            {
                RaceResult latest = raceHistories[i].get(raceHistories[i].size() - 1);

                text = text + "Typist " + (i + 1) + ": ";

                if (metric.equals("WPM"))
                {
                    text = text + roundTwo(latest.wpm);
                }
                else if (metric.equals("Accuracy Percentage"))
                {
                    text = text + roundTwo(latest.accuracyPercentage) + "%";
                }
                else if (metric.equals("Burnout Count"))
                {
                    text = text + latest.burnoutCount;
                }
                else if (metric.equals("Accuracy Change"))
                {
                    text = text + signed(roundThree(latest.accuracyChange));
                }
                else if (metric.equals("Race Points"))
                {
                    text = text + latest.racePoints;
                }
                else
                {
                    text = text + cumulativePoints[i];
                }

                text = text + "\n";
            }
        }

        return text;
    }

    private void updateLeaderboardArea()
    {
        String text = "";

        text = text + "Global Leaderboard\n\n";

        int[] rankedIndexes = getRankedIndexes();

        for (int rank = 0; rank < rankedIndexes.length; rank++)
        {
            int i = rankedIndexes[rank];

            if (raceHistories[i].size() > 0)
            {
                text = text
                    + (rank + 1) + ". Typist " + (i + 1)
                    + " | Points: " + cumulativePoints[i]
                    + " | Title: " + getTitleForTypist(i)
                    + " | Consecutive wins: " + consecutiveWins[i]
                    + " | No-burnout streak: " + noBurnoutStreak[i]
                    + "\n";
            }
        }

        text = text + "\nPoints Algorithm:\n";
        text = text + "- 1st place: 3 base points\n";
        text = text + "- 2nd place: 2 base points\n";
        text = text + "- 3rd place: 1 base point\n";
        text = text + "- WPM >= 20: +1 bonus point\n";
        text = text + "- WPM >= 30: +2 bonus points\n";
        text = text + "- Burnouts are tracked but do not reduce points\n\n";

        text = text + "Titles & Badges:\n";
        text = text + "- Speed Demon: 3 consecutive wins\n";
        text = text + "- Iron Fingers: 5 consecutive races without burnout\n";
        text = text + "- Rising Star: 8 cumulative points\n";
        text = text + "- League Contender: 15 cumulative points\n\n";

        text = text + "Rank Impact:\n";
        text = text + "- Current rank 1 starts the next race with -0.03 accuracy.\n";
        text = text + "- Current rank 2 starts the next race with -0.01 accuracy.\n";

        leaderboardText = text;
    }

    private int[] getRankedIndexes()
    {
        int[] indexes = new int[MAX_TYPISTS];

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            indexes[i] = i;
        }

        for (int i = 0; i < MAX_TYPISTS - 1; i++)
        {
            for (int j = i + 1; j < MAX_TYPISTS; j++)
            {
                if (cumulativePoints[indexes[j]] > cumulativePoints[indexes[i]])
                {
                    int temp = indexes[i];
                    indexes[i] = indexes[j];
                    indexes[j] = temp;
                }
            }
        }

        return indexes;
    }

    public String getImpactText(TypistSettings[] settings,
                                boolean nightShiftOn,
                                boolean autocorrectOn,
                                boolean caffeineOn)
    {
        String text = "";

        text = text + "Global Modifiers:\n";

        if (nightShiftOn)
        {
            text = text + "- Night Shift: accuracy "
                + signed(-NIGHT_SHIFT_ACCURACY_PENALTY)
                + " for every typist.\n";
        }
        else
        {
            text = text + "- Night Shift: off.\n";
        }

        if (autocorrectOn)
        {
            text = text + "- Autocorrect: slide-back penalty is halved after a mistype.\n";
        }
        else
        {
            text = text + "- Autocorrect: off.\n";
        }

        if (caffeineOn)
        {
            text = text + "- Caffeine Mode: accuracy/typing chance +"
                + CAFFEINE_ACCURACY_BOOST
                + " for first 10 turns, then burnout risk +"
                + CAFFEINE_EXTRA_BURNOUT_RISK
                + ".\n";
        }
        else
        {
            text = text + "- Caffeine Mode: off.\n";
        }

        text = text + "\n";

        for (int i = 0; i < settings.length; i++)
        {
            String style = settings[i].style;
            String keyboard = settings[i].keyboard;

            int finalDelay = BASE_TYPING_DELAY + getKeyboardDelayModifier(keyboard);

            if (finalDelay < 120)
            {
                finalDelay = 120;
            }

            text = text + "Typist " + (i + 1) + ":\n";

            text = text + "- Base accuracy: 0.75 for all typists.\n";

            text = text + "- Style " + style
                + ": accuracy " + signed(getStyleAccuracyModifier(style))
                + ", speed no direct change"
                + ", burnout risk x" + getStyleBurnoutMultiplier(style)
                + ".\n";

            text = text + "- Keyboard " + keyboard
                + ": accuracy " + signed(getKeyboardAccuracyModifier(keyboard))
                + ", speed delay " + signed(getKeyboardDelayModifier(keyboard)) + "ms"
                + " (final delay " + finalDelay + "ms)"
                + ", burnout risk x" + getKeyboardBurnoutMultiplier(keyboard)
                + ".\n";

            if (settings[i].wristSupport)
            {
                text = text + "- Wrist Support: burnout duration reduced by 1 turn.\n";
            }

            if (settings[i].energyDrink)
            {
                text = text + "- Energy Drink: accuracy/typing chance +"
                    + ENERGY_DRINK_FIRST_HALF_BOOST
                    + " in the first half, then returns to normal.\n";
            }

            if (settings[i].headphones)
            {
                text = text + "- Noise-Cancelling Headphones: mistype chance reduced by "
                    + HEADPHONES_MISTYPE_REDUCTION
                    + ".\n";
            }

            if (!settings[i].wristSupport
                && !settings[i].energyDrink
                && !settings[i].headphones)
            {
                text = text + "- Accessories: none selected.\n";
            }

            text = text + "- Current cumulative points: " + cumulativePoints[i] + ".\n";

            if (raceNumber > 0)
            {
                text = text + "- Rank impact for next race: accuracy "
                    + signed(getRankAccuracyImpact(i))
                    + ".\n";
            }
            else
            {
                text = text + "- Rank impact: none before the first race.\n";
            }

            text = text + "\n";
        }

        return text;
    }

    private boolean raceFinishedBy(Typist theTypist)
    {
        return theTypist.getProgress() >= passageLength;
    }

    private double getRankAccuracyImpact(int typistIndex)
    {
        if (raceNumber == 0)
        {
            return 0.0;
        }

        int rank = getCumulativeRank(typistIndex);

        if (rank == 1)
        {
            return -0.03;
        }
        else if (rank == 2)
        {
            return -0.01;
        }
        else
        {
            return 0.0;
        }
    }

    private double getStyleAccuracyModifier(String style)
    {
        if (style.equals("Touch Typist"))
        {
            return 0.10;
        }
        else if (style.equals("Hunt & Peck"))
        {
            return -0.10;
        }
        else if (style.equals("Phone Thumbs"))
        {
            return -0.05;
        }
        else
        {
            return 0.05;
        }
    }

    private double getStyleBurnoutMultiplier(String style)
    {
        if (style.equals("Touch Typist"))
        {
            return 1.10;
        }
        else if (style.equals("Hunt & Peck"))
        {
            return 0.80;
        }
        else if (style.equals("Phone Thumbs"))
        {
            return 0.90;
        }
        else
        {
            return 1.30;
        }
    }

    private double getKeyboardAccuracyModifier(String keyboard)
    {
        if (keyboard.equals("Mechanical"))
        {
            return 0.05;
        }
        else if (keyboard.equals("Membrane"))
        {
            return 0.00;
        }
        else if (keyboard.equals("Touchscreen"))
        {
            return -0.08;
        }
        else
        {
            return -0.03;
        }
    }

    private int getKeyboardDelayModifier(String keyboard)
    {
        if (keyboard.equals("Mechanical"))
        {
            return -90;
        }
        else if (keyboard.equals("Membrane"))
        {
            return 40;
        }
        else if (keyboard.equals("Touchscreen"))
        {
            return 120;
        }
        else
        {
            return -180;
        }
    }

    private double getKeyboardBurnoutMultiplier(String keyboard)
    {
        if (keyboard.equals("Mechanical"))
        {
            return 1.00;
        }
        else if (keyboard.equals("Membrane"))
        {
            return 0.95;
        }
        else if (keyboard.equals("Touchscreen"))
        {
            return 0.90;
        }
        else
        {
            return 1.20;
        }
    }

    private String getColourHex(String colour)
    {
        if (colour.equals("Green"))
        {
            return "#b7f7b7";
        }
        else if (colour.equals("Blue"))
        {
            return "#b7d7ff";
        }
        else if (colour.equals("Orange"))
        {
            return "#ffd59e";
        }
        else if (colour.equals("Pink"))
        {
            return "#ffb7d5";
        }
        else if (colour.equals("Purple"))
        {
            return "#d7b7ff";
        }
        else
        {
            return "#b7f7f7";
        }
    }

    private void applyStartingModifiers(boolean nightShiftOn)
    {
        if (nightShiftOn)
        {
            for (int i = 0; i < typists.length; i++)
            {
                typists[i].setAccuracy(
                    typists[i].getAccuracy() - NIGHT_SHIFT_ACCURACY_PENALTY
                );
            }
        }
    }

    private String signed(double value)
    {
        if (value >= 0)
        {
            return "+" + value;
        }
        else
        {
            return "" + value;
        }
    }

    private String signed(int value)
    {
        if (value >= 0)
        {
            return "+" + value;
        }
        else
        {
            return "" + value;
        }
    }

    public double roundAccuracy(double accuracy)
    {
        return Math.round(accuracy * 1000.0) / 1000.0;
    }

    private double roundTwo(double value)
    {
        return Math.round(value * 100.0) / 100.0;
    }

    private double roundThree(double value)
    {
        return Math.round(value * 1000.0) / 1000.0;
    }

    public String getPassage()
    {
        return passage;
    }

    public int getPassageLength()
    {
        return passageLength;
    }

    public Typist[] getTypists()
    {
        return typists;
    }

    public String[] getTypistColours()
    {
        return typistColours;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public Typist getWinner()
    {
        return winner;
    }

    public String getResultsText()
    {
        return resultsText;
    }

    public String getHistoryText()
    {
        return historyText;
    }

    public String getLeaderboardText()
    {
        return leaderboardText;
    }
}