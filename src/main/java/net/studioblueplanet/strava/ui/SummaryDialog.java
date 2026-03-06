package net.studioblueplanet.strava.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog showing sync summary with statistics and failed activities
 * Provides options to view error log and retry failed uploads
 */
public class SummaryDialog extends JDialog {

    private int totalCount;
    private int uploadedCount;
    private int skippedCount;
    private int failedCount;
    private List<String> failedActivities;

    /**
     * Creates a new summary dialog
     *
     * @param parent the parent frame
     * @param total total activities processed
     * @param uploaded successfully uploaded count
     * @param skipped skipped duplicates count
     * @param failed failed uploads count
     * @param failedActivities list of failed activity names
     */
    public SummaryDialog(Frame parent, int total, int uploaded, int skipped, int failed,
                        List<String> failedActivities) {
        super(parent, "Sync Summary", true);
        this.totalCount = total;
        this.uploadedCount = uploaded;
        this.skippedCount = skipped;
        this.failedCount = failed;
        this.failedActivities = failedActivities;
        initComponents();
    }

    /**
     * Initializes the dialog components
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(500, 400);
        setLocationRelativeTo(getParent());

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Statistics panel
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.NORTH);

        // Failed activities list (if any)
        if (failedCount > 0) {
            JPanel failedPanel = createFailedActivitiesPanel();
            mainPanel.add(failedPanel, BorderLayout.CENTER);
        } else {
            JLabel successLabel = new JLabel("All activities synced successfully!");
            successLabel.setHorizontalAlignment(SwingConstants.CENTER);
            successLabel.setFont(successLabel.getFont().deriveFont(Font.BOLD, 14f));
            successLabel.setForeground(new Color(0, 128, 0));
            mainPanel.add(successLabel, BorderLayout.CENTER);
        }

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the statistics panel
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));

        panel.add(new JLabel("Total Activities:"));
        panel.add(new JLabel(String.valueOf(totalCount)));

        panel.add(new JLabel("Uploaded:"));
        JLabel uploadedLabel = new JLabel(String.valueOf(uploadedCount));
        uploadedLabel.setForeground(new Color(0, 128, 0));
        panel.add(uploadedLabel);

        panel.add(new JLabel("Skipped (Duplicates):"));
        JLabel skippedLabel = new JLabel(String.valueOf(skippedCount));
        skippedLabel.setForeground(Color.ORANGE);
        panel.add(skippedLabel);

        panel.add(new JLabel("Failed:"));
        JLabel failedLabel = new JLabel(String.valueOf(failedCount));
        failedLabel.setForeground(failedCount > 0 ? Color.RED : Color.BLACK);
        panel.add(failedLabel);

        return panel;
    }

    /**
     * Creates the failed activities panel
     */
    private JPanel createFailedActivitiesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Failed Activities"));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String activity : failedActivities) {
            listModel.addElement(activity);
        }

        JList<String> failedList = new JList<>(listModel);
        failedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(failedList);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // View Error Log button (only if errors exist)
        if (failedCount > 0) {
            JButton viewLogButton = new JButton("View Error Log");
            viewLogButton.addActionListener(e -> viewErrorLog());
            panel.add(viewLogButton);

            // Retry Failed button
            JButton retryButton = new JButton("Retry Failed");
            retryButton.addActionListener(e -> {
                // TODO: Implement retry logic
                JOptionPane.showMessageDialog(this,
                    "Retry functionality will be implemented in the next phase",
                    "Coming Soon",
                    JOptionPane.INFORMATION_MESSAGE);
            });
            panel.add(retryButton);
        }

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);

        return panel;
    }

    /**
     * Opens the error log file
     */
    private void viewErrorLog() {
        File logFile = new File("strava-sync-errors.log");

        if (!logFile.exists()) {
            JOptionPane.showMessageDialog(this,
                "Error log file not found",
                "File Not Found",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.open(logFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Could not open error log: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows the dialog
     */
    public void showDialog() {
        setVisible(true);
    }
}
