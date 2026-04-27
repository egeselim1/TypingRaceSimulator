package Part2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * GUI version of the typing race.
 *
 * This class controls the graphical interface and the overall race flow.
 * The Typist class stores the state and behaviour of each individual typist.
 */
public class TypingRaceGUI extends JFrame
{
    private String passage;
    private int passageLength;

    private Typist[] typists;
    private JLabel[] laneLabels;

    private boolean finished;
    private Typist winner;

    private int turnCounter;

    private JLabel titleLabel;
    private JLabel passageLabel;
    private JLabel statusLabel;

    private JComboBox<String> passageBox;
    private JComboBox<Integer> seatCountBox;

    private JTextField customPassageField;
    private JButton startButton;

    private JCheckBox autocorrectBox;
    private JCheckBox caffeineBox;
    private JCheckBox nightShiftBox;

    private JPanel racePanel;
    private JPanel customPanel;
    private JPanel typistConfigPanel;

    private JComboBox<String>[] styleBoxes;
    private JComboBox<String>[] keyboardBoxes;
    private JComboBox<String>[] colourBoxes;
    private JTextField[] symbolFields;
    private JPanel[] typistRows;

    private JCheckBox[] wristSupportBoxes;
    private JCheckBox[] energyDrinkBoxes;
    private JCheckBox[] headphonesBoxes;

    private JTextArea impactArea;

    private String[] typistColours;

    private int[] typistDelays;
    private int[] typistCooldowns;
    private int[] typistBurnoutDurations;

    private double[] typistBurnoutMultipliers;

    private Timer raceTimer;

    private static final int MAX_TYPISTS = 6;

    private static final int TIMER_TICK = 50;
    private static final int BASE_TYPING_DELAY = 350;

    private static final int SLIDE_BACK_AMOUNT = 2;
    private static final int BURNOUT_DURATION = 3;

    private static final double CAFFEINE_ACCURACY_BOOST = 0.15;
    private static final double CAFFEINE_EXTRA_BURNOUT_RISK = 0.03;
    private static final double NIGHT_SHIFT_ACCURACY_PENALTY = 0.10;

    private static final double ENERGY_DRINK_FIRST_HALF_BOOST = 0.08;
    private static final double ENERGY_DRINK_SECOND_HALF_PENALTY = 0.08;
    private static final double HEADPHONES_MISTYPE_REDUCTION = 0.07;

    public TypingRaceGUI()
    {
        setTitle("Typing Race GUI");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createTitlePanel();
        createSettingsPanel();
        createRacePanel();

        setVisible(true);
    }

    private void createTitlePanel()
    {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(2, 1));

        titleLabel = new JLabel("Typing Race", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel instructionLabel = new JLabel(
            "Choose passage, typist count, style, keyboard, symbol, colour, modifiers, and accessories.",
            SwingConstants.CENTER
        );

        titlePanel.add(titleLabel);
        titlePanel.add(instructionLabel);

        add(titlePanel, BorderLayout.NORTH);
    }

    private void createSettingsPanel()
    {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel passagePanel = new JPanel();
        passagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        passagePanel.add(new JLabel("Passage type:"));

        passageBox = new JComboBox<String>();
        passageBox.addItem("Short");
        passageBox.addItem("Medium");
        passageBox.addItem("Long");
        passageBox.addItem("Custom");

        passagePanel.add(passageBox);

        customPanel = new JPanel();
        customPanel.setLayout(new BorderLayout());
        customPanel.add(new JLabel("Custom passage: "), BorderLayout.WEST);

        customPassageField = new JTextField("");
        customPanel.add(customPassageField, BorderLayout.CENTER);
        customPanel.setVisible(false);

        passageBox.addActionListener((ActionEvent e) -> {
            String choice = (String) passageBox.getSelectedItem();

            if (choice.equals("Custom"))
            {
                customPanel.setVisible(true);
            }
            else
            {
                customPanel.setVisible(false);
            }

            customPanel.revalidate();
            customPanel.repaint();
        });

        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        seatPanel.add(new JLabel("Number of typists:"));

        seatCountBox = new JComboBox<Integer>();
        seatCountBox.addItem(2);
        seatCountBox.addItem(3);
        seatCountBox.addItem(4);
        seatCountBox.addItem(5);
        seatCountBox.addItem(6);

        seatCountBox.addActionListener((ActionEvent e) -> {
            updateTypistRowVisibility();
            updateImpactArea();
        });

        seatPanel.add(seatCountBox);

        JPanel modifierPanel = new JPanel();
        modifierPanel.setLayout(new GridLayout(3, 1));
        modifierPanel.setBorder(BorderFactory.createTitledBorder("Difficulty Modifiers"));

        autocorrectBox = new JCheckBox("Autocorrect");
        caffeineBox = new JCheckBox("Caffeine Mode");
        nightShiftBox = new JCheckBox("Night Shift");

        autocorrectBox.addActionListener((ActionEvent e) -> updateImpactArea());
        caffeineBox.addActionListener((ActionEvent e) -> updateImpactArea());
        nightShiftBox.addActionListener((ActionEvent e) -> updateImpactArea());

        modifierPanel.add(autocorrectBox);
        modifierPanel.add(caffeineBox);
        modifierPanel.add(nightShiftBox);

        createTypistConfigPanel();
        createImpactPanel();

        settingsPanel.add(passagePanel);
        settingsPanel.add(customPanel);
        settingsPanel.add(seatPanel);
        settingsPanel.add(modifierPanel);
        settingsPanel.add(typistConfigPanel);
        settingsPanel.add(impactArea);

        JScrollPane scrollPane = new JScrollPane(settingsPanel);
        scrollPane.setPreferredSize(new Dimension(560, 700));

        JPanel startPanel = new JPanel();
        startPanel.setLayout(new GridLayout(2, 1));
        startPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        startButton = new JButton("Start Race");
        startButton.addActionListener((ActionEvent e) -> startRace());

        statusLabel = new JLabel("Press Start Race to begin.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        startPanel.add(startButton);
        startPanel.add(statusLabel);

        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(startPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
    }

    private void createTypistConfigPanel()
    {
        typistConfigPanel = new JPanel();
        typistConfigPanel.setLayout(new BoxLayout(typistConfigPanel, BoxLayout.Y_AXIS));
        typistConfigPanel.setBorder(BorderFactory.createTitledBorder("Typist Settings"));

        styleBoxes = new JComboBox[MAX_TYPISTS];
        keyboardBoxes = new JComboBox[MAX_TYPISTS];
        colourBoxes = new JComboBox[MAX_TYPISTS];
        symbolFields = new JTextField[MAX_TYPISTS];
        typistRows = new JPanel[MAX_TYPISTS];

        wristSupportBoxes = new JCheckBox[MAX_TYPISTS];
        energyDrinkBoxes = new JCheckBox[MAX_TYPISTS];
        headphonesBoxes = new JCheckBox[MAX_TYPISTS];

        String[] defaultSymbols = {"⌨", "🐢", "👑", "⚡", "🚀", "🎯"};

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            JPanel row = new JPanel();
            row.setLayout(new GridLayout(7, 2));
            row.setBorder(BorderFactory.createTitledBorder("Typist " + (i + 1)));

            styleBoxes[i] = new JComboBox<String>();
            styleBoxes[i].addItem("Touch Typist");
            styleBoxes[i].addItem("Hunt & Peck");
            styleBoxes[i].addItem("Phone Thumbs");
            styleBoxes[i].addItem("Voice-to-Text");
            styleBoxes[i].addActionListener((ActionEvent e) -> updateImpactArea());

            keyboardBoxes[i] = new JComboBox<String>();
            keyboardBoxes[i].addItem("Mechanical");
            keyboardBoxes[i].addItem("Membrane");
            keyboardBoxes[i].addItem("Touchscreen");
            keyboardBoxes[i].addItem("Stenography");
            keyboardBoxes[i].addActionListener((ActionEvent e) -> updateImpactArea());

            symbolFields[i] = new JTextField(defaultSymbols[i], 4);

            colourBoxes[i] = new JComboBox<String>();
            colourBoxes[i].addItem("Green");
            colourBoxes[i].addItem("Blue");
            colourBoxes[i].addItem("Orange");
            colourBoxes[i].addItem("Pink");
            colourBoxes[i].addItem("Purple");
            colourBoxes[i].addItem("Cyan");

            wristSupportBoxes[i] = new JCheckBox("Wrist Support");
            energyDrinkBoxes[i] = new JCheckBox("Energy Drink");
            headphonesBoxes[i] = new JCheckBox("Noise-Cancelling Headphones");

            wristSupportBoxes[i].addActionListener((ActionEvent e) -> updateImpactArea());
            energyDrinkBoxes[i].addActionListener((ActionEvent e) -> updateImpactArea());
            headphonesBoxes[i].addActionListener((ActionEvent e) -> updateImpactArea());

            row.add(new JLabel("Style:"));
            row.add(styleBoxes[i]);

            row.add(new JLabel("Keyboard:"));
            row.add(keyboardBoxes[i]);

            row.add(new JLabel("Symbol/Emoji:"));
            row.add(symbolFields[i]);

            row.add(new JLabel("Colour:"));
            row.add(colourBoxes[i]);

            row.add(new JLabel("Accessory 1:"));
            row.add(wristSupportBoxes[i]);

            row.add(new JLabel("Accessory 2:"));
            row.add(energyDrinkBoxes[i]);

            row.add(new JLabel("Accessory 3:"));
            row.add(headphonesBoxes[i]);

            typistRows[i] = row;
            typistConfigPanel.add(row);
        }

        updateTypistRowVisibility();
    }

    private void createImpactPanel()
    {
        impactArea = new JTextArea(14, 35);
        impactArea.setEditable(false);
        impactArea.setLineWrap(true);
        impactArea.setWrapStyleWord(true);
        impactArea.setBorder(BorderFactory.createTitledBorder("Attribute Impact"));

        updateImpactArea();
    }

    private void updateImpactArea()
    {
        if (impactArea == null || seatCountBox == null || styleBoxes == null)
        {
            return;
        }

        int numberOfTypists = (Integer) seatCountBox.getSelectedItem();

        String text = "";

        text = text + "Global Modifiers:\n";

        if (nightShiftBox != null && nightShiftBox.isSelected())
        {
            text = text + "- Night Shift: accuracy "
                + signed(-NIGHT_SHIFT_ACCURACY_PENALTY)
                + " for every typist.\n";
        }
        else
        {
            text = text + "- Night Shift: off.\n";
        }

        if (autocorrectBox != null && autocorrectBox.isSelected())
        {
            text = text + "- Autocorrect: slide-back penalty is halved after a mistype.\n";
        }
        else
        {
            text = text + "- Autocorrect: off.\n";
        }

        if (caffeineBox != null && caffeineBox.isSelected())
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

        for (int i = 0; i < numberOfTypists; i++)
        {
            String style = (String) styleBoxes[i].getSelectedItem();
            String keyboard = (String) keyboardBoxes[i].getSelectedItem();

            int finalDelay = BASE_TYPING_DELAY + getKeyboardDelayModifier(keyboard);

            if (finalDelay < 120)
            {
                finalDelay = 120;
            }

            text = text + "Typist " + (i + 1) + ":\n";

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

            if (wristSupportBoxes[i].isSelected())
            {
                text = text + "- Wrist Support: burnout duration reduced by 1 turn.\n";
            }

            if (energyDrinkBoxes[i].isSelected())
            {
                text = text + "- Energy Drink: accuracy/typing chance +"
                    + ENERGY_DRINK_FIRST_HALF_BOOST
                    + " in first half, -"
                    + ENERGY_DRINK_SECOND_HALF_PENALTY
                    + " in second half.\n";
            }

            if (headphonesBoxes[i].isSelected())
            {
                text = text + "- Noise-Cancelling Headphones: mistype chance reduced by "
                    + HEADPHONES_MISTYPE_REDUCTION
                    + ".\n";
            }

            if (!wristSupportBoxes[i].isSelected()
                && !energyDrinkBoxes[i].isSelected()
                && !headphonesBoxes[i].isSelected())
            {
                text = text + "- Accessories: none selected.\n";
            }

            text = text + "\n";
        }

        impactArea.setText(text);
        impactArea.setCaretPosition(0);
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

    private void updateTypistRowVisibility()
    {
        if (seatCountBox == null || typistRows == null)
        {
            return;
        }

        int numberOfTypists = (Integer) seatCountBox.getSelectedItem();

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            typistRows[i].setVisible(i < numberOfTypists);
        }

        typistConfigPanel.revalidate();
        typistConfigPanel.repaint();
    }

    private void createRacePanel()
    {
        racePanel = new JPanel();
        racePanel.setLayout(new GridLayout(1, 1));
        racePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        passageLabel = new JLabel("Passage will appear here.", SwingConstants.CENTER);
        passageLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));

        racePanel.add(passageLabel);

        add(racePanel, BorderLayout.CENTER);
    }

    private JLabel createLaneLabel()
    {
        JLabel label = new JLabel("", SwingConstants.LEFT);
        label.setFont(new Font("Monospaced", Font.PLAIN, 18));
        label.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        return label;
    }

    private String getSelectedPassage()
    {
        String choice = (String) passageBox.getSelectedItem();

        if (choice.equals("Short"))
        {
            return "The quick fox jumps.";
        }
        else if (choice.equals("Medium"))
        {
            return "Typing quickly requires focus accuracy and steady rhythm.";
        }
        else if (choice.equals("Long"))
        {
            return "In a typing race each player moves through the passage character by character while mistakes can slow them down or push them backwards.";
        }
        else
        {
            return customPassageField.getText().trim();
        }
    }

    private void startRace()
    {
        passage = getSelectedPassage();

        if (passage == null || passage.length() == 0)
        {
            statusLabel.setText("Error: passage cannot be empty.");
            return;
        }

        passageLength = passage.length();

        int numberOfTypists = (Integer) seatCountBox.getSelectedItem();

        createTypists(numberOfTypists);
        applyStartingModifiers();
        createLaneLabels(numberOfTypists);

        finished = false;
        winner = null;
        turnCounter = 0;

        for (int i = 0; i < typists.length; i++)
        {
            typists[i].resetToStart();
            typistCooldowns[i] = 0;
        }

        setConfigurationEnabled(false);

        statusLabel.setText("Race started!");

        updateGUI();

        raceTimer = new Timer(TIMER_TICK, (ActionEvent e) -> raceStep());
        raceTimer.start();
    }

    private void createTypists(int numberOfTypists)
    {
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
            String symbol = symbolFields[i].getText().trim();

            if (symbol.length() == 0)
            {
                symbol = "" + (i + 1);
            }

            double accuracy = baseAccuracy;

            String style = (String) styleBoxes[i].getSelectedItem();
            String keyboard = (String) keyboardBoxes[i].getSelectedItem();

            accuracy = accuracy + getStyleAccuracyModifier(style);
            accuracy = accuracy + getKeyboardAccuracyModifier(keyboard);

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

            if (wristSupportBoxes[i].isSelected())
            {
                typistBurnoutDurations[i] = typistBurnoutDurations[i] - 1;

                if (typistBurnoutDurations[i] < 1)
                {
                    typistBurnoutDurations[i] = 1;
                }
            }

            typistColours[i] = getColourHex((String) colourBoxes[i].getSelectedItem());

            typists[i] = new Typist(symbol, names[i], accuracy);
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

    private void applyStartingModifiers()
    {
        if (nightShiftBox.isSelected())
        {
            for (int i = 0; i < typists.length; i++)
            {
                typists[i].setAccuracy(
                    typists[i].getAccuracy() - NIGHT_SHIFT_ACCURACY_PENALTY
                );
            }
        }
    }

    private void createLaneLabels(int numberOfTypists)
    {
        racePanel.removeAll();

        racePanel.setLayout(new GridLayout(numberOfTypists + 1, 1));

        passageLabel = new JLabel("", SwingConstants.CENTER);
        passageLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));

        racePanel.add(passageLabel);

        laneLabels = new JLabel[numberOfTypists];

        for (int i = 0; i < numberOfTypists; i++)
        {
            laneLabels[i] = createLaneLabel();
            racePanel.add(laneLabels[i]);
        }

        racePanel.revalidate();
        racePanel.repaint();
    }

    private void raceStep()
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
                advanceTypist(typists[i], i);
                typistCooldowns[i] = typistDelays[i];
            }
        }

        updateGUI();

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
            raceTimer.stop();

            setConfigurationEnabled(true);

            statusLabel.setText(
                "Winner: " + winner.getName()
                + " | Final accuracy: " + roundAccuracy(winner.getAccuracy())
            );
        }
    }

    private void advanceTypist(Typist theTypist, int typistIndex)
    {
        if (theTypist.isBurntOut())
        {
            theTypist.recoverFromBurnout();
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.01);
            return;
        }

        double typingChance = theTypist.getAccuracy();

        if (caffeineBox.isSelected() && turnCounter <= 10)
        {
            typingChance = typingChance + CAFFEINE_ACCURACY_BOOST;
        }

        if (energyDrinkBoxes[typistIndex].isSelected())
        {
            if (theTypist.getProgress() < passageLength / 2)
            {
                typingChance = typingChance + ENERGY_DRINK_FIRST_HALF_BOOST;
            }
            else
            {
                typingChance = typingChance - ENERGY_DRINK_SECOND_HALF_PENALTY;
            }
        }

        if (headphonesBoxes[typistIndex].isSelected())
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

        if (autocorrectBox.isSelected())
        {
            slideBackAmount = SLIDE_BACK_AMOUNT / 2;

            if (slideBackAmount < 1)
            {
                slideBackAmount = 1;
            }
        }

        if (Math.random() < typingChance)
        {
            theTypist.typeCharacter();
            theTypist.setAccuracy(theTypist.getAccuracy() + 0.001);
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

        if (caffeineBox.isSelected() && turnCounter > 10)
        {
            burnoutRisk = burnoutRisk + CAFFEINE_EXTRA_BURNOUT_RISK;
        }

        if (Math.random() < burnoutRisk)
        {
            theTypist.burnOut(typistBurnoutDurations[typistIndex]);
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.01);
        }
    }

    private boolean raceFinishedBy(Typist theTypist)
    {
        return theTypist.getProgress() >= passageLength;
    }

    private void updateGUI()
    {
        passageLabel.setText(
            "<html><b>Passage:</b> " + escapeHTML(passage) + "</html>"
        );

        for (int i = 0; i < typists.length; i++)
        {
            laneLabels[i].setText(createLaneHTML(typists[i], i));
        }
    }

    private String createLaneHTML(Typist typist, int typistIndex)
    {
        int progress = typist.getProgress();

        if (progress > passageLength)
        {
            progress = passageLength;
        }

        String completed = passage.substring(0, progress);
        String remaining = passage.substring(progress);

        String burnoutText = "";

        if (typist.isBurntOut())
        {
            burnoutText = " BURNT OUT (" + typist.getBurnoutTurnsRemaining() + " turns)";
        }

        return "<html>"
            + "<b>" + typist.getName() + "</b>"
            + " | Accuracy: " + roundAccuracy(typist.getAccuracy())
            + burnoutText
            + "<br>"
            + "<span style='background-color:" + typistColours[typistIndex] + ";'>"
            + escapeHTML(completed)
            + "</span>"
            + "<span style='background-color:#ffd966;'>"
            + escapeHTML(typist.getSymbol())
            + "</span>"
            + escapeHTML(remaining)
            + "</html>";
    }

    private void setConfigurationEnabled(boolean enabled)
    {
        startButton.setEnabled(enabled);
        passageBox.setEnabled(enabled);
        seatCountBox.setEnabled(enabled);
        customPassageField.setEnabled(enabled);
        autocorrectBox.setEnabled(enabled);
        caffeineBox.setEnabled(enabled);
        nightShiftBox.setEnabled(enabled);

        for (int i = 0; i < MAX_TYPISTS; i++)
        {
            styleBoxes[i].setEnabled(enabled);
            keyboardBoxes[i].setEnabled(enabled);
            symbolFields[i].setEnabled(enabled);
            colourBoxes[i].setEnabled(enabled);
            wristSupportBoxes[i].setEnabled(enabled);
            energyDrinkBoxes[i].setEnabled(enabled);
            headphonesBoxes[i].setEnabled(enabled);
        }
    }

    private double roundAccuracy(double accuracy)
    {
        return Math.round(accuracy * 1000.0) / 1000.0;
    }

    private String escapeHTML(String text)
    {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace(" ", "&nbsp;");
    }

    public static void main(String[] args)
    {
        new TypingRaceGUI();
    }
}