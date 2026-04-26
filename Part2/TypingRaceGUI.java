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

    private Timer raceTimer;

    private static final int SLIDE_BACK_AMOUNT = 2;
    private static final int BURNOUT_DURATION = 3;

    private static final double CAFFEINE_SPEED_BOOST = 0.15;
    private static final double CAFFEINE_EXTRA_BURNOUT_RISK = 0.03;
    private static final double NIGHT_SHIFT_ACCURACY_PENALTY = 0.10;

    public TypingRaceGUI()
    {
        setTitle("Typing Race GUI");
        setSize(1200, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createTopPanel();
        createRacePanel();
        createBottomPanel();

        setVisible(true);
    }

    private void createTopPanel()
    {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(6, 1));

        titleLabel = new JLabel("Typing Race", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel instructionLabel = new JLabel(
            "Choose a passage, choose how many typists will compete, configure modifiers, then press Start Race.",
            SwingConstants.CENTER
        );

        JPanel passagePanel = new JPanel();
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
        });

        JPanel seatPanel = new JPanel();
        seatPanel.add(new JLabel("Number of typists:"));

        seatCountBox = new JComboBox<Integer>();
        seatCountBox.addItem(2);
        seatCountBox.addItem(3);
        seatCountBox.addItem(4);
        seatCountBox.addItem(5);
        seatCountBox.addItem(6);

        seatPanel.add(seatCountBox);

        JPanel modifierPanel = new JPanel();
        modifierPanel.add(new JLabel("Difficulty modifiers:"));

        autocorrectBox = new JCheckBox("Autocorrect");
        caffeineBox = new JCheckBox("Caffeine Mode");
        nightShiftBox = new JCheckBox("Night Shift");

        modifierPanel.add(autocorrectBox);
        modifierPanel.add(caffeineBox);
        modifierPanel.add(nightShiftBox);

        topPanel.add(titleLabel);
        topPanel.add(instructionLabel);
        topPanel.add(passagePanel);
        topPanel.add(customPanel);
        topPanel.add(seatPanel);
        topPanel.add(modifierPanel);

        add(topPanel, BorderLayout.NORTH);
    }

    private void createRacePanel()
    {
        racePanel = new JPanel();
        racePanel.setLayout(new GridLayout(1, 1));

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

    private void createBottomPanel()
    {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(2, 1));

        startButton = new JButton("Start Race");
        statusLabel = new JLabel("Press Start Race to begin.", SwingConstants.CENTER);

        startButton.addActionListener((ActionEvent e) -> startRace());

        bottomPanel.add(startButton);
        bottomPanel.add(statusLabel);

        add(bottomPanel, BorderLayout.SOUTH);
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
        }

        startButton.setEnabled(false);
        passageBox.setEnabled(false);
        seatCountBox.setEnabled(false);
        customPassageField.setEnabled(false);
        autocorrectBox.setEnabled(false);
        caffeineBox.setEnabled(false);
        nightShiftBox.setEnabled(false);

        statusLabel.setText("Race started!");

        updateGUI();

        raceTimer = new Timer(200, (ActionEvent e) -> raceStep());
        raceTimer.start();
    }

    private void createTypists(int numberOfTypists)
    {
        typists = new Typist[numberOfTypists];

        String[] names = {
            "TURBOFINGERS",
            "HUNT_N_PECK",
            "KEYBOARD_KING",
            "TYPE_MASTER",
            "FAST_HANDS",
            "SPACEBAR_PRO"
        };

        char[] symbols = {
            '1',
            '2',
            '3',
            '4',
            '5',
            '6'
        };

        double[] accuracies = {
            0.85,
            0.40,
            0.70,
            0.60,
            0.75,
            0.55
        };

        for (int i = 0; i < numberOfTypists; i++)
        {
            typists[i] = new Typist(symbols[i], names[i], accuracies[i]);
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
            advanceTypist(typists[i]);
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

            startButton.setEnabled(true);
            passageBox.setEnabled(true);
            seatCountBox.setEnabled(true);
            customPassageField.setEnabled(true);
            autocorrectBox.setEnabled(true);
            caffeineBox.setEnabled(true);
            nightShiftBox.setEnabled(true);

            statusLabel.setText(
                "Winner: " + winner.getName()
                + " | Final accuracy: " + roundAccuracy(winner.getAccuracy())
            );
        }
    }

    /**
     * Simulates one turn for a typist.
     *
     * In one turn, a typist makes one attempt:
     * - If the attempt succeeds, they type one character.
     * - If the attempt fails, it is treated as a mistype and they slide back.
     */
    private void advanceTypist(Typist theTypist)
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
            typingChance = typingChance + CAFFEINE_SPEED_BOOST;

            if (typingChance > 1.0)
            {
                typingChance = 1.0;
            }
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

        double burnoutRisk = 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy();

        if (caffeineBox.isSelected() && turnCounter > 10)
        {
            burnoutRisk = burnoutRisk + CAFFEINE_EXTRA_BURNOUT_RISK;
        }

        if (Math.random() < burnoutRisk)
        {
            theTypist.burnOut(BURNOUT_DURATION);
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
            laneLabels[i].setText(createLaneHTML(typists[i]));
        }
    }

    private String createLaneHTML(Typist typist)
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
            + "<span style='background-color:#b7f7b7;'>"
            + escapeHTML(completed)
            + "</span>"
            + "<span style='background-color:#ffd966;'>"
            + typist.getSymbol()
            + "</span>"
            + escapeHTML(remaining)
            + "</html>";
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