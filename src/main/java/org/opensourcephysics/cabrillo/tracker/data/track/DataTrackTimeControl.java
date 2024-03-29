package org.opensourcephysics.cabrillo.tracker.data.track;

import org.opensourcephysics.cabrillo.tracker.particle.ParticleDataTrack;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerRes;
import org.opensourcephysics.media.core.ClipControl;
import org.opensourcephysics.media.core.DataTrack;
import org.opensourcephysics.media.core.VideoPlayer;
import org.opensourcephysics.tools.FontSizer;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class DataTrackTimeControl extends JPanel implements PropertyChangeListener {

    protected DataTrack dataTrack;
    private JRadioButton videoButton, dataButton;

    /**
     * Constructor.
     *
     * @param track the DataTrack
     */
    public DataTrackTimeControl(DataTrack track) {
        super();
        dataTrack = track;
        track.addPropertyChangeListener(this);
        createGUI();
        refreshGUI();
    }

    /**
     * Creates the GUI.
     */
    protected void createGUI() {
        videoButton = new JRadioButton();
        videoButton.addActionListener(arg0 -> setTimeSourceToDataTrack(false));
        dataButton = new JRadioButton();
        dataButton.addActionListener(arg0 -> setTimeSourceToDataTrack(true));
        ButtonGroup group = new ButtonGroup();
        group.add(videoButton);
        group.add(dataButton);
        videoButton.setSelected(true);
        add(videoButton);
        add(dataButton);
    }

    /**
     * Refreshes the GUI.
     */
    protected void refreshGUI() {
        setBorder(BorderFactory.createTitledBorder(TrackerRes.getString("DataTrackTimeControl.Border.Title"))); //$NON-NLS-1$
        videoButton.setText(TrackerRes.getString("DataTrackTimeControl.Button.Video")); //$NON-NLS-1$
        dataButton.setText(TrackerRes.getString("DataTrackTimeControl.Button.Data")); //$NON-NLS-1$
        dataButton.setEnabled(dataTrack.isTimeDataAvailable());
        boolean dataSelected = ClipControl.isTimeSource(dataTrack);
        dataButton.setSelected(dataSelected);
        videoButton.setSelected(!dataSelected);
        // following line needed to display titled border correctly when a DataTrack is created
        FontSizer.setFonts(getBorder(), FontSizer.getLevel());
    }

    /**
     * Sets the DataTrack time source flag.
     *
     * @param isTrackTimeSource true to use DataTrack time, false to use video time
     */
    public void setTimeSourceToDataTrack(boolean isTrackTimeSource) {
        if (dataTrack.getVideoPanel() == null) return;
        VideoPlayer player = dataTrack.getVideoPanel().getPlayer();
        player.getClipControl().setTimeSource(isTrackTimeSource ? dataTrack : null);
        player.refresh();
        if (dataTrack instanceof ParticleDataTrack) {
            ((ParticleDataTrack) dataTrack).refreshInitialTime();
        }
    }

    /**
     * Gets the DataTrack time source flag.
     *
     * @return true if using DataTrack time
     */
    public boolean isTimeSourceDataTrack() {
        if (dataTrack.getVideoPanel() == null) return false;
        VideoPlayer player = dataTrack.getVideoPanel().getPlayer();
        return player.getClipControl().getTimeSource() == dataTrack;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension dim = super.getMaximumSize();
        dim.height = getPreferredSize().height;
        return dim;
    }

    public void propertyChange(PropertyChangeEvent e) {
        refreshGUI();
    }

}
