package org.opensourcephysics.cabrillo.tracker.dialog;

import org.opensourcephysics.cabrillo.tracker.component.TFrame;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerIO;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerRes;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.media.core.VideoIO;
import org.opensourcephysics.tools.FontSizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MonitorDialog extends JDialog {

    JProgressBar monitor;
    Timer timer;
    int frameCount = Integer.MIN_VALUE;

    public MonitorDialog(TFrame frame, String path) {
        super(frame, false);
        setName(path);
        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);
        monitor = new JProgressBar(0, 100);
        monitor.setValue(0);
        monitor.setStringPainted(true);
        // make timer to step progress forward slowly
        timer = new Timer(300, e -> {
            if (!isVisible()) return;
            int progress = monitor.getValue() + 1;
            if (progress <= 20)
                monitor.setValue(progress);
        });
        timer.setRepeats(true);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                VideoIO.setCanceled(true);
            }
        });
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(4, 30, 8, 30));
        progressPanel.add(monitor, BorderLayout.CENTER);
        progressPanel.setOpaque(false);
        JLabel label = new JLabel(TrackerRes.getString("Tracker.Splash.Loading") //$NON-NLS-1$
                + " \"" + XML.getName(path) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
        JPanel labelbar = new JPanel();
        labelbar.add(label);
        JButton cancelButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
        cancelButton.addActionListener(e -> {
            VideoIO.setCanceled(true);
            close();
        });
        JPanel buttonbar = new JPanel();
        buttonbar.add(cancelButton);
        contentPane.add(labelbar, BorderLayout.NORTH);
        contentPane.add(progressPanel, BorderLayout.CENTER);
        contentPane.add(buttonbar, BorderLayout.SOUTH);
        FontSizer.setFonts(contentPane, FontSizer.getLevel());
        pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - getBounds().width) / 2;
        int y = (dim.height - getBounds().height) / 2;
        setLocation(x, y);
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void setProgress(int progress) {
        monitor.setValue(progress);
    }

    public void setFrameCount(int count) {
        frameCount = count;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void close() {
        timer.stop();
        setVisible(false);
        TrackerIO.monitors.remove(this);
        dispose();
    }


}