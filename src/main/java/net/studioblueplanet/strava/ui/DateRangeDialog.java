package net.studioblueplanet.strava.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Calendar;
import java.util.Date;

/**
 * Dialog for selecting date range for Strava sync
 * Allows user to choose start and end dates for activity synchronization
 */
public class DateRangeDialog extends JDialog {

    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JLabel activityCountLabel;
    private boolean confirmed = false;
    private Date startDate;
    private Date endDate;

    /**
     * Creates a new date range selection dialog
     *
     * @param parent the parent frame
     */
    public DateRangeDialog(Frame parent) {
        super(parent, "Select Date Range", true);
        initComponents();
        setDefaultDateRange();
    }

    /**
     * Initializes the dialog components
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 250);
        setLocationRelativeTo(getParent());

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Start date
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("Start Date:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        startDateSpinner = createDateSpinner();
        mainPanel.add(startDateSpinner, gbc);

        // End date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(new JLabel("End Date:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        endDateSpinner = createDateSpinner();
        mainPanel.add(endDateSpinner, gbc);

        // Activity count preview
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        activityCountLabel = new JLabel("Select date range to preview activity count");
        activityCountLabel.setForeground(Color.GRAY);
        mainPanel.add(activityCountLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JButton syncButton = new JButton("Sync");
        syncButton.addActionListener(e -> {
            if (validateDateRange()) {
                startDate = (Date) startDateSpinner.getValue();
                endDate = (Date) endDateSpinner.getValue();
                confirmed = true;
                dispose();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(syncButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates a date spinner with proper formatting
     */
    private JSpinner createDateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        return spinner;
    }

    /**
     * Sets default date range to last 30 days
     */
    private void setDefaultDateRange() {
        Calendar cal = Calendar.getInstance();
        endDateSpinner.setValue(cal.getTime());

        cal.add(Calendar.DAY_OF_MONTH, -30);
        startDateSpinner.setValue(cal.getTime());
    }

    /**
     * Validates that start date is before or equal to end date
     *
     * @return true if valid
     */
    private boolean validateDateRange() {
        Date start = (Date) startDateSpinner.getValue();
        Date end = (Date) endDateSpinner.getValue();

        if (start.after(end)) {
            JOptionPane.showMessageDialog(this,
                "Start date must be before or equal to end date",
                "Invalid Date Range",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Sets the activity count preview text
     *
     * @param count the number of activities found
     */
    public void setActivityCount(int count) {
        SwingUtilities.invokeLater(() -> {
            if (count == 0) {
                activityCountLabel.setText("No activities found in selected range");
                activityCountLabel.setForeground(Color.RED);
            } else {
                activityCountLabel.setText(count + " activities found in selected range");
                activityCountLabel.setForeground(new Color(0, 128, 0)); // Dark green
            }
        });
    }

    /**
     * Shows the dialog and waits for user input
     *
     * @return true if user clicked Sync, false if cancelled
     */
    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    /**
     * Gets the selected start date
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Gets the selected end date
     *
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }
}
