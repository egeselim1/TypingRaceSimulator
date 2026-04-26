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

    private JLabel titleLabel;
    private JLabel passageLabel;
    private JLabel statusLabel;

    private JComboBox<String> passageBox;
    private JComboBox<Integer> seatCountBox;

    private JTextField customPassageField;
    private JButton startButton;

    private JPanel racePanel;
    private JPanel customPanel;

    private Timer raceTimer;

    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int SLIDE_BACK_AMOUNT = 2;
    private static final int BURNOUT_DURATION = 3;

    /**
     * Constructor for the GUI window.
     */
    public TypingRaceGUI()
    {
        setTitle("Typing Race GUI");
        setSize(1200, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createTopPanel();
        createRacePanel();
        createBottomPanel();

        setVisible(true);
    }

    /**
     * Creates the top part of the GUI.
     */
    private void createTopPanel()
    {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(5, 1));

        titleLabel = new JLabel("Typing Race", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel instructionLabel = new JLabel(
            "Choose a passage, choose how many typists will compete, then press Start Race.",
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

        topPanel.add(titleLabel);
        topPanel.add(instructionLabel);
        topPanel.add(passagePanel);
        topPanel.add(customPanel);
        topPanel.add(seatPanel);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * Creates the race display area.
     */
    private void createRacePanel()
    {
        racePanel = new JPanel();
        racePanel.setLayout(new GridLayout(1, 1));

        passageLabel = new JLabel("Passage will appear here.", SwingConstants.CENTER);
        passageLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));

        racePanel.add(passageLabel);

        add(racePanel, BorderLayout.CENTER);
    }

    /**
     * Creates one lane label for one typist.
     */
    private JLabel createLaneLabel()
    {
        JLabel label = new JLabel("", SwingConstants.LEFT);
        label.setFont(new Font("Monospaced", Font.PLAIN, 18));
        label.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        return label;
    }

    /**
     * Creates the bottom part of the GUI.
     */
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

    /**
     * Gets the passage selected by the user.
     */
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

    /**
     * Starts the race.
     */
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
        createLaneLabels(numberOfTypists);

        finished = false;
        winner = null;

        for (int i = 0; i < typists.length; i++)
        {
            typists[i].resetToStart();
        }

        startButton.setEnabled(false);
        passageBox.setEnabled(false);
        seatCountBox.setEnabled(false);
        customPassageField.setEnabled(false);

        statusLabel.setText("Race started!");

        updateGUI();

        raceTimer = new Timer(200, (ActionEvent e) -> raceStep());
        raceTimer.start();
    }

    /**
     * Creates the requested number of typists.
     */
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

    /**
     * Creates the correct number of visual lanes.
     */
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

    /**
     * Runs one turn of the race.
     *
     * This replaces the while loop from the terminal version.
     * Swing Timer calls this method every 200 milliseconds.
     */
    private void raceStep()
    {
        if (finished)
        {
            return;
        }

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

            statusLabel.setText(
                "Winner: " + winner.getName()
                + " | Final accuracy: " + roundAccuracy(winner.getAccuracy())
            );
        }
    }

    /**
     * Simulates one turn for a typist.
     *
     * This uses the same race behaviour as the original terminal version:
     * typing, mistyping, sliding backwards, and burnout.
     */
    private void advanceTypist(Typist theTypist)
    {
        if (theTypist.isBurntOut())
        {
            theTypist.recoverFromBurnout();
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.01);
            return;
        }

        if (Math.random() < theTypist.getAccuracy())
        {
            theTypist.typeCharacter();
            theTypist.setAccuracy(theTypist.getAccuracy() + 0.001);
        }

        if (Math.random() < (1 - theTypist.getAccuracy()) * MISTYPE_BASE_CHANCE)
        {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.005);
        }

        if (Math.random() < 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy())
        {
            theTypist.burnOut(BURNOUT_DURATION);
            theTypist.setAccuracy(theTypist.getAccuracy() - 0.01);
        }
    }

    /**
     * Checks whether a typist has completed the passage.
     */
    private boolean raceFinishedBy(Typist theTypist)
    {
        return theTypist.getProgress() >= passageLength;
    }

    /**
     * Updates the GUI display.
     *
     * This replaces printRace() from the terminal version.
     */
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

    /**
     * Creates the visual display for one typist.
     *
     * Completed characters are highlighted green.
     * The typist symbol acts like the cursor and is highlighted yellow.
     */
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

    /**
     * Rounds accuracy to make it easier to read on screen.
     */
    private double roundAccuracy(double accuracy)
    {
        return Math.round(accuracy * 1000.0) / 1000.0;
    }

    /**
     * Escapes special HTML characters so the passage displays correctly.
     */
    private String escapeHTML(String text)
    {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace(" ", "&nbsp;");
    }

    /**
     * Main method to run the GUI.
     */
    public static void main(String[] args)
    {
        new TypingRaceGUI();
    }
}