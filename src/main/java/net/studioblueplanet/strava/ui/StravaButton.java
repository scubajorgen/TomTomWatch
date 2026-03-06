package net.studioblueplanet.strava.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Custom button for triggering Strava sync operations
 * Shows loading state during sync and respects watch connection status
 */
public class StravaButton extends JButton {

    private static final String IDLE_TEXT = "Strava";
    private static final String SYNCING_TEXT = "Syncing...";
    private static final String TOOLTIP = "Sync activities to Strava";

    private boolean syncing = false;
    private ActionListener syncActionListener;

    /**
     * Creates a new Strava button
     */
    public StravaButton() {
        super(IDLE_TEXT);
        setToolTipText(TOOLTIP);
        setPreferredSize(new Dimension(100, 25));
        setEnabled(false); // Disabled until watch connected
    }

    /**
     * Sets the action to perform when button is clicked
     *
     * @param listener the action listener for sync operations
     */
    public void setSyncAction(ActionListener listener) {
        if (syncActionListener != null) {
            removeActionListener(syncActionListener);
        }
        syncActionListener = listener;
        addActionListener(listener);
    }

    /**
     * Sets whether sync is currently in progress
     * Changes button text and disabled state
     *
     * @param syncing true if sync in progress
     */
    public void setSyncing(boolean syncing) {
        this.syncing = syncing;
        setText(syncing ? SYNCING_TEXT : IDLE_TEXT);
        setEnabled(!syncing);
    }

    /**
     * Returns whether sync is currently in progress
     *
     * @return true if syncing
     */
    public boolean isSyncing() {
        return syncing;
    }

    /**
     * Enables or disables the button based on watch connection
     * Button is only enabled when not syncing and watch is connected
     *
     * @param watchConnected true if watch is connected
     */
    public void setWatchConnected(boolean watchConnected) {
        if (!syncing) {
            setEnabled(watchConnected);
        }
    }
}
