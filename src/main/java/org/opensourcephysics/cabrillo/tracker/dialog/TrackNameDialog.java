package org.opensourcephysics.cabrillo.tracker.dialog;


import org.opensourcephysics.cabrillo.tracker.component.TTrack;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerPanel;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerRes;
import org.opensourcephysics.tools.FontSizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A dialog used to set the name of a track.
 */
public class TrackNameDialog extends JDialog {

    JLabel nameLabel;
    JTextField nameField;
    TTrack target;
    TrackerPanel trackerPanel;

    // constructor
    public TrackNameDialog(TrackerPanel panel) {
        super(panel.getTFrame(), null, true);
        trackerPanel = panel;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                String newName = nameField.getText();
                if (target != null)
                    trackerPanel.setTrackName(target, newName, true);
            }
        });
        nameField = new JTextField(20);
        nameField.addActionListener(e -> {
            String newName = nameField.getText();
            if (target != null)
                trackerPanel.setTrackName(target, newName, true);
        });
        nameLabel = new JLabel();
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.add(nameLabel);
        bar.add(nameField);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(bar, BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    /**
     * Sets the track.
     *
     * @param track the track
     */
    public void setTrack(TTrack track) {
        target = track;
        // initial text is current track name
        FontSizer.setFonts(this, FontSizer.getLevel());
        setTitle(TrackerRes.getString("TTrack.Dialog.Name.Title")); //$NON-NLS-1$
        nameLabel.setText(TrackerRes.getString("TTrack.Dialog.Name.Label")); //$NON-NLS-1$
        nameField.setText(track.getName());
        nameField.selectAll();
        pack();
    }
}
