package Part2;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * GUI version of the typing race.
 *
 * This class controls only the graphical interface.
 * The TypingRaceLogic class stores the internal race logic.
 * The Typist class stores the state and behaviour of each individual typist.
 */
public class TypingRaceGUI extends JFrame
{
    private TypingRaceLogic logic;

    private JLabel[] laneLabels;

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
    private JTextArea resultsArea;
    private JTextArea historyArea;
    private JTextArea comparisonArea;
    private JTextArea leaderboardArea;

    private JComboBox<String> comparisonMetricBox;

    private Timer raceTimer;

    public TypingRaceGUI()
    {
        logic = new TypingRaceLogic();

        setTitle("Typing Race GUI");
        setSize(1450, 900);
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
            "Choose passage, typist count, style, keyboard, symbol, colour, modifiers, accessories, and track rankings.",
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
        scrollPane.setPreferredSize(new Dimension(570, 700));

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

        styleBoxes = new JComboBox[TypingRaceLogic.MAX_TYPISTS];
        keyboardBoxes = new JComboBox[TypingRaceLogic.MAX_TYPISTS];
        colourBoxes = new JComboBox[TypingRaceLogic.MAX_TYPISTS];
        symbolFields = new JTextField[TypingRaceLogic.MAX_TYPISTS];
        typistRows = new JPanel[TypingRaceLogic.MAX_TYPISTS];

        wristSupportBoxes = new JCheckBox[TypingRaceLogic.MAX_TYPISTS];
        energyDrinkBoxes = new JCheckBox[TypingRaceLogic.MAX_TYPISTS];
        headphonesBoxes = new JCheckBox[TypingRaceLogic.MAX_TYPISTS];

        String[] defaultSymbols = {"⌨", "🐢", "👑", "⚡", "🚀", "🎯"};

        for (int i = 0; i < TypingRaceLogic.MAX_TYPISTS; i++)
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

    private void createRacePanel()
    {
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new BorderLayout());

        racePanel = new JPanel();
        racePanel.setLayout(new BoxLayout(racePanel, BoxLayout.Y_AXIS));
        racePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        passageLabel = new JLabel("Passage will appear here.", SwingConstants.LEFT);
        passageLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        passageLabel.setVerticalAlignment(SwingConstants.TOP);

        racePanel.add(passageLabel);

        JScrollPane raceScrollPane = new JScrollPane(racePanel);
        raceScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        raceScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTabbedPane tabs = new JTabbedPane();

        resultsArea = new JTextArea(10, 40);
        resultsArea.setEditable(false);
        resultsArea.setText("Race results will appear here after the first race.");

        historyArea = new JTextArea(10, 40);
        historyArea.setEditable(false);
        historyArea.setText("Historical data will appear here after races are completed.");

        JPanel comparisonPanel = new JPanel();
        comparisonPanel.setLayout(new BorderLayout());

        JPanel comparisonTopPanel = new JPanel();
        comparisonTopPanel.add(new JLabel("Compare metric:"));

        comparisonMetricBox = new JComboBox<String>();
        comparisonMetricBox.addItem("WPM");
        comparisonMetricBox.addItem("Accuracy Percentage");
        comparisonMetricBox.addItem("Burnout Count");
        comparisonMetricBox.addItem("Accuracy Change");
        comparisonMetricBox.addItem("Race Points");
        comparisonMetricBox.addItem("Cumulative Points");

        JButton updateComparisonButton = new JButton("Update Comparison");
        updateComparisonButton.addActionListener((ActionEvent e) -> updateComparisonArea());

        comparisonTopPanel.add(comparisonMetricBox);
        comparisonTopPanel.add(updateComparisonButton);

        comparisonArea = new JTextArea(10, 40);
        comparisonArea.setEditable(false);
        comparisonArea.setText("Comparison view will appear here after a race.");

        comparisonPanel.add(comparisonTopPanel, BorderLayout.NORTH);
        comparisonPanel.add(new JScrollPane(comparisonArea), BorderLayout.CENTER);

        leaderboardArea = new JTextArea(10, 40);
        leaderboardArea.setEditable(false);
        leaderboardArea.setText("Leaderboard will appear here after races are completed.");

        tabs.add("Results", new JScrollPane(resultsArea));
        tabs.add("History", new JScrollPane(historyArea));
        tabs.add("Comparison", comparisonPanel);
        tabs.add("Leaderboard", new JScrollPane(leaderboardArea));

        centrePanel.add(raceScrollPane, BorderLayout.CENTER);
        centrePanel.add(tabs, BorderLayout.SOUTH);

        add(centrePanel, BorderLayout.CENTER);
    }

    private JLabel createLaneLabel()
    {
        JLabel label = new JLabel("", SwingConstants.LEFT);
        label.setFont(new Font("Monospaced", Font.PLAIN, 13));
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 40));
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
        String passage = getSelectedPassage();

        if (passage == null || passage.length() == 0)
        {
            statusLabel.setText("Error: passage cannot be empty.");
            return;
        }

        int numberOfTypists = (Integer) seatCountBox.getSelectedItem();

        TypingRaceLogic.TypistSettings[] settings = getTypistSettings(numberOfTypists);

        logic.startRace(
            passage,
            settings,
            nightShiftBox.isSelected()
        );

        createLaneLabels(numberOfTypists);

        setConfigurationEnabled(false);

        statusLabel.setText("Race started!");

        updateGUI();

        raceTimer = new Timer(TypingRaceLogic.TIMER_TICK, (ActionEvent e) -> raceStep());
        raceTimer.start();
    }

    private TypingRaceLogic.TypistSettings[] getTypistSettings(int numberOfTypists)
    {
        TypingRaceLogic.TypistSettings[] settings =
            new TypingRaceLogic.TypistSettings[numberOfTypists];

        for (int i = 0; i < numberOfTypists; i++)
        {
            settings[i] = new TypingRaceLogic.TypistSettings(
                (String) styleBoxes[i].getSelectedItem(),
                (String) keyboardBoxes[i].getSelectedItem(),
                symbolFields[i].getText(),
                (String) colourBoxes[i].getSelectedItem(),
                wristSupportBoxes[i].isSelected(),
                energyDrinkBoxes[i].isSelected(),
                headphonesBoxes[i].isSelected()
            );
        }

        return settings;
    }

    private void raceStep()
    {
        int numberOfTypists = logic.getTypists().length;

        TypingRaceLogic.TypistSettings[] settings = getTypistSettings(numberOfTypists);

        logic.raceStep(
            settings,
            autocorrectBox.isSelected(),
            caffeineBox.isSelected()
        );

        updateGUI();

        if (logic.isFinished())
        {
            raceTimer.stop();

            resultsArea.setText(logic.getResultsText());
            resultsArea.setCaretPosition(0);

            historyArea.setText(logic.getHistoryText());
            historyArea.setCaretPosition(0);

            updateComparisonArea();

            leaderboardArea.setText(logic.getLeaderboardText());
            leaderboardArea.setCaretPosition(0);

            setConfigurationEnabled(true);

            statusLabel.setText(
                "Winner: " + logic.getWinner().getName()
                + " | Final accuracy: " + logic.roundAccuracy(logic.getWinner().getAccuracy())
            );
        }
    }

    private void updateComparisonArea()
    {
        if (comparisonArea == null || comparisonMetricBox == null)
        {
            return;
        }

        String metric = (String) comparisonMetricBox.getSelectedItem();

        comparisonArea.setText(logic.getComparisonText(metric));
        comparisonArea.setCaretPosition(0);
    }

    private void updateImpactArea()
    {
        if (impactArea == null || seatCountBox == null || styleBoxes == null)
        {
            return;
        }

        int numberOfTypists = (Integer) seatCountBox.getSelectedItem();

        TypingRaceLogic.TypistSettings[] settings = getTypistSettings(numberOfTypists);

        impactArea.setText(
            logic.getImpactText(
                settings,
                nightShiftBox != null && nightShiftBox.isSelected(),
                autocorrectBox != null && autocorrectBox.isSelected(),
                caffeineBox != null && caffeineBox.isSelected()
            )
        );

        impactArea.setCaretPosition(0);
    }

    private void updateTypistRowVisibility()
    {
        if (seatCountBox == null || typistRows == null)
        {
            return;
        }

        int numberOfTypists = (Integer) seatCountBox.getSelectedItem();

        for (int i = 0; i < TypingRaceLogic.MAX_TYPISTS; i++)
        {
            typistRows[i].setVisible(i < numberOfTypists);
        }

        typistConfigPanel.revalidate();
        typistConfigPanel.repaint();
    }

    private int getAdaptiveFontSize()
    {
        String passage = logic.getPassage();

        if (passage == null)
        {
            return 13;
        }

        int length = passage.length();

        if (length <= 40)
        {
            return 15;
        }
        else if (length <= 90)
        {
            return 13;
        }
        else if (length <= 160)
        {
            return 11;
        }
        else
        {
            return 10;
        }
    }

    private int getCharactersPerLine()
    {
        int fontSize = getAdaptiveFontSize();

        int availableWidth = racePanel.getWidth() - 180;

        if (availableWidth < 300)
        {
            availableWidth = 300;
        }

        int approximateCharacterWidth = fontSize;

        int charsPerLine = availableWidth / approximateCharacterWidth;

        if (charsPerLine < 35)
        {
            charsPerLine = 35;
        }

        if (charsPerLine > 65)
        {
            charsPerLine = 65;
        }

        return charsPerLine;
    }

    private String createWrappedPassageHTML(String text)
    {
        int charsPerLine = getCharactersPerLine();

        String html = "";

        for (int i = 0; i < text.length(); i++)
        {
            if (i > 0 && i % charsPerLine == 0)
            {
                html = html + "<br>";
            }

            html = html + escapeHTML("" + text.charAt(i));
        }

        return html;
    }

    private void updateGUI()
    {
        int fontSize = getAdaptiveFontSize();

        String passage = logic.getPassage();

        passageLabel.setText(
            "<html>"
            + "<span style='font-family:monospace; font-size:" + fontSize + "px;'>"
            + "<b>Passage:</b><br>"
            + createWrappedPassageHTML(passage)
            + "</span>"
            + "</html>"
        );

        Typist[] typists = logic.getTypists();

        for (int i = 0; i < typists.length; i++)
        {
            laneLabels[i].setText(createLaneHTML(typists[i], i));
        }
    }

    private String createLaneHTML(Typist typist, int typistIndex)
    {
        int passageLength = logic.getPassageLength();
        String passage = logic.getPassage();
        String[] typistColours = logic.getTypistColours();

        int progress = typist.getProgress();

        if (progress > passageLength)
        {
            progress = passageLength;
        }

        String burnoutText = "";

        if (typist.isBurntOut())
        {
            burnoutText = " BURNT OUT (" + typist.getBurnoutTurnsRemaining() + " turns)";
        }

        int fontSize = getAdaptiveFontSize();
        int charsPerLine = getCharactersPerLine();

        String html = "";

        html = html
            + "<html>"
            + "<span style='font-family:monospace; font-size:" + fontSize + "px;'>"
            + "<b>" + typist.getName() + "</b>"
            + " | Accuracy: " + logic.roundAccuracy(typist.getAccuracy())
            + burnoutText
            + "<br>";

        for (int i = 0; i < passage.length(); i++)
        {
            if (i > 0 && i % charsPerLine == 0)
            {
                html = html + "<br>";
            }

            if (i == progress)
            {
                html = html
                    + "<span style='background-color:#ffd966;'>"
                    + escapeHTML(typist.getSymbol())
                    + "</span>";
            }

            if (i < progress)
            {
                html = html
                    + "<span style='background-color:" + typistColours[typistIndex] + ";'>"
                    + escapeHTML("" + passage.charAt(i))
                    + "</span>";
            }
            else
            {
                html = html + escapeHTML("" + passage.charAt(i));
            }
        }

        if (progress >= passage.length())
        {
            html = html
                + "<span style='background-color:#ffd966;'>"
                + escapeHTML(typist.getSymbol())
                + "</span>";
        }

        html = html + "</span></html>";

        return html;
    }

    private void createLaneLabels(int numberOfTypists)
    {
        racePanel.removeAll();

        racePanel.setLayout(new BoxLayout(racePanel, BoxLayout.Y_AXIS));

        passageLabel = new JLabel("", SwingConstants.LEFT);
        passageLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        passageLabel.setVerticalAlignment(SwingConstants.TOP);
        passageLabel.setBorder(BorderFactory.createEmptyBorder(8, 40, 12, 40));

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

    private void setConfigurationEnabled(boolean enabled)
    {
        startButton.setEnabled(enabled);
        passageBox.setEnabled(enabled);
        seatCountBox.setEnabled(enabled);
        customPassageField.setEnabled(enabled);
        autocorrectBox.setEnabled(enabled);
        caffeineBox.setEnabled(enabled);
        nightShiftBox.setEnabled(enabled);

        for (int i = 0; i < TypingRaceLogic.MAX_TYPISTS; i++)
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

    private String escapeHTML(String text)
    {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    public static void main(String[] args)
    {
        new TypingRaceGUI();
    }
}