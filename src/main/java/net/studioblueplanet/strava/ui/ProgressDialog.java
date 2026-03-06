package net.studioblueplanet.strava.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog showing upload progress with progress bar and log messages
 * Allows cancellation after current upload completes
 */
public class ProgressDialog extends JDialog {

    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextArea logArea;
    private JButton cancelButton;
    private volatile boolean cancelled = false;

    /**
     * Creates a new progress dialog
     *
     * @param parent the parent frame
     */
    public ProgressDialog(Frame parent) {
        super(parent, "Strava Sync Progress", true);
        initComponents();
    }

    /**
     * Initializes the dialog components
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(600, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Initializing...");

        // Status label
        statusLabel = new JLabel("Preparing to sync activities...");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        // Top panel with progress bar and status
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(progressBar, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            cancelled = true;
            cancelButton.setEnabled(false);
            cancelButton.setText("Cancelling...");
            addLogMessage("Cancel requested - will stop after current upload completes");
        });

        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Updates the progress bar and status
     *
     * @param current the current item number
     * @param total the total number of items
     * @param activityName the current activity name
     */
    public void updateProgress(int current, int total, String activityName) {
        SwingUtilities.invokeLater(() -> {
            int percentage = (int) ((current * 100.0) / total);
            progressBar.setValue(percentage);
            progressBar.setString(percentage + "%");
            statusLabel.setText(String.format("Uploading activity %d of %d: %s",
                current, total, activityName));
        });
    }

    /**
     * Adds a log message to the text area
     *
     * @param message the message to add
     */
    public void addLogMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            logArea.append(String.format("[%s] %s%n", timestamp, message));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Returns whether user has cancelled the operation
     *
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the dialog to completed state
     *
     * @param success true if sync completed successfully
     */
    public void setCompleted(boolean success) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(100);
            progressBar.setString("Complete");
            statusLabel.setText(success ? "Sync completed successfully" : "Sync completed with errors");
            cancelButton.setText("Close");
            cancelButton.setEnabled(true);
            cancelButton.removeActionListener(cancelButton.getActionListeners()[0]);
            cancelButton.addActionListener(e -> dispose());
        });
    }

    /**
     * Shows the dialog (non-modal for progress updates)
     */
    public void showDialog() {
        setModal(false); // Non-modal so background thread can update it
        setVisible(true);
    }

    /**
     * Closes the dialog
     */
    public void closeDialog() {
        SwingUtilities.invokeLater(this::dispose);
    }
}
