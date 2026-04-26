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

    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;

    private boolean finished;
    private Typist winner;

    private JLabel titleLabel;
    private JLabel passageLabel;
    private JLabel lane1Label;
    private JLabel lane2Label;
    private JLabel lane3Label;
    private JLabel statusLabel;

    private JComboBox<String> passageBox;
    private JTextField customPassageField;
    private JButton startButton;

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
        setSize(1100, 550);
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
        topPanel.setLayout(new GridLayout(4, 1));

        titleLabel = new JLabel("Typing Race", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel instructionLabel = new JLabel(
            "Choose a passage length or enter your own custom passage.",
            SwingConstants.CENTER
        );

        passageBox = new JComboBox<String>();
        passageBox.addItem("Short");
        passageBox.addItem("Medium");
        passageBox.addItem("Long");
        passageBox.addItem("Custom");

        customPassageField = new JTextField("Enter your custom passage here");

        topPanel.add(titleLabel);
        topPanel.add(instructionLabel);
        topPanel.add(passageBox);
        topPanel.add(customPassageField);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * Creates the race display area.
     */
    private void createRacePanel()
    {
        JPanel racePanel = new JPanel();
        racePanel.setLayout(new GridLayout(4, 1));

        passageLabel = new JLabel("", SwingConstants.CENTER);
        passageLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));

        lane1Label = createLaneLabel();
        lane2Label = createLaneLabel();
        lane3Label = createLaneLabel();

        racePanel.add(passageLabel);
        racePanel.add(lane1Label);
        racePanel.add(lane2Label);
        racePanel.add(lane3Label);

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
            return "Java is fun.";
        }
        else if (choice.equals("Medium"))
        {
            return "Object oriented programming helps organise code clearly.";
        }
        else if (choice.equals("Long"))
        {
            return "Typing race applications show progress by moving a cursor through the passage character by character.";
        }
        else
        {
            return customPassageField.getText();
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

        seat1Typist = new Typist('1', "TURBOFINGERS", 0.85);
        seat2Typist = new Typist('2', "HUNT_N_PECK", 0.40);
        seat3Typist = new Typist('3', "KEYBOARD_KING", 0.70);

        finished = false;
        winner = null;

        seat1Typist.resetToStart();
        seat2Typist.resetToStart();
        seat3Typist.resetToStart();

        startButton.setEnabled(false);
        statusLabel.setText("Race started!");

        updateGUI();

        raceTimer = new Timer(200, (ActionEvent e) -> raceStep());
        raceTimer.start();
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

        advanceTypist(seat1Typist);
        advanceTypist(seat2Typist);
        advanceTypist(seat3Typist);

        updateGUI();

        if (raceFinishedBy(seat1Typist))
        {
            finished = true;
            winner = seat1Typist;
        }
        else if (raceFinishedBy(seat2Typist))
        {
            finished = true;
            winner = seat2Typist;
        }
        else if (raceFinishedBy(seat3Typist))
        {
            finished = true;
            winner = seat3Typist;
        }

        if (finished)
        {
            raceTimer.stop();
            startButton.setEnabled(true);

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

        lane1Label.setText(createLaneHTML(seat1Typist));
        lane2Label.setText(createLaneHTML(seat2Typist));
        lane3Label.setText(createLaneHTML(seat3Typist));
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