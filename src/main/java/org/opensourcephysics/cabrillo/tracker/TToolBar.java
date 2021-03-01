/*
 * The tracker package defines a set of video/image analysis tools
 * built on the Open Source Physics framework by Wolfgang Christian.
 *
 * Copyright (c) 2018  Douglas Brown
 *
 * Tracker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tracker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tracker; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston MA 02111-1307 USA
 * or view the license online at <http://www.gnu.org/copyleft/gpl.html>
 *
 * For additional Tracker information and documentation, please see
 * <http://physlets.org/tracker/>.
 */
package org.opensourcephysics.cabrillo.tracker;

import org.opensourcephysics.cabrillo.tracker.PageTView.TabView;
import org.opensourcephysics.cabrillo.tracker.vector.Vector;
import org.opensourcephysics.controls.OSPLog;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.FontSizer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.*;

/**
 * This is the main toolbar for Tracker.
 *
 * @author Douglas Brown
 */
public class TToolBar extends JToolBar implements PropertyChangeListener {

    // static fields
    protected static Map<TrackerPanel, TToolBar> toolbars = new HashMap<>();

    protected static int[] trailLengths = {1, 4, 15, 0};
    protected static String[] trailLengthNames = {"none", "short", "long", "full"};
    protected ResizableIcon newTrackIcon = new ResizableIcon("/images/poof.gif");
    protected static Icon trackControlIcon, trackControlOnIcon, trackControlDisabledIcon;
    protected static Icon zoomIcon;
    protected static Icon clipOffIcon, clipOnIcon;
    protected static Icon axesOffIcon, axesOnIcon;
    protected static Icon calibrationToolsOffIcon, calibrationToolsOnIcon;
    protected static Icon calibrationToolsOffRolloverIcon, calibrationToolsOnRolloverIcon;
    protected static Icon pointsOffIcon, pointsOnIcon;
    protected static Icon velocOffIcon, velocOnIcon;
    protected static Icon accelOffIcon, accelOnIcon;
    protected static Icon traceOffIcon, traceOnIcon;
    protected static Icon labelsOffIcon, labelsOnIcon;
    protected static Icon stretchOffIcon, stretchOnIcon;
    protected static Icon xmassOffIcon, xmassOnIcon;
    protected static Icon fontSmallerIcon, fontBiggerIcon, fontSmallerDisabledIcon, fontBiggerDisabledIcon;
    protected static Icon autotrackerOffIcon, autotrackerOnIcon, autotrackerDisabledIcon;
    protected static Icon infoIcon, refreshIcon, htmlIcon, htmlDisabledIcon;
    protected static Icon[] trailIcons = new Icon[4];
    protected static int[] stretchValues = new int[]{1, 2, 3, 4, 6, 8, 12, 16, 24, 32};
    protected static Icon separatorIcon;
    protected static Icon checkboxOffIcon, checkboxOnIcon;
    protected ResizableIcon checkboxOnDisabledIcon = new ResizableIcon("/images/box_checked_disabled.gif");
    protected static Icon pencilOffIcon, pencilOnIcon, pencilOffRolloverIcon, pencilOnRolloverIcon;
    protected static NumberFormat zoomFormat = NumberFormat.getNumberInstance();

    // instance fields
    protected TrackerPanel trackerPanel; // manages & displays track data
    protected boolean refreshing; // true when refreshing toolbar
    protected WindowListener infoListener;
    protected int vStretch = 1, aStretch = 1;
    protected JButton openButton, openBrowserButton, saveButton, saveZipButton;
    protected TButton newTrackButton;
    protected JButton trackControlButton, clipSettingsButton;
    protected CalibrationButton calibrationButton;
    protected DrawingButton drawingButton;
    protected JButton axesButton, zoomButton, autotrackerButton;
    protected JButton traceVisButton, pVisButton, vVisButton, aVisButton;
    public JButton xMassButton, trailButton, labelsButton, stretchButton;
    protected JButton fontSmallerButton, fontBiggerButton;
    protected int trailLength = trailLengths[Tracker.trailLengthIndex];
    protected JPopupMenu newPopup = new JPopupMenu();
    protected JMenu vStretchMenu, aStretchMenu;
    protected ButtonGroup vGroup, aGroup;
    protected JMenuItem showTrackControlItem, selectNoneItem, stretchOffItem;
    protected JButton notesButton, refreshButton, desktopButton;
    protected Component toolbarFiller;
    protected int toolbarComponentHeight;
    protected JMenu cloneMenu;
    protected boolean notYetCalibrated = true;
    protected ComponentListener clipSettingsDialogListener;
    protected JPopupMenu zoomPopup = new JPopupMenu();
    protected ArrayList<PageTView.TabData> pageViewTabs = new ArrayList<>();

    static {
//        newTrackIcon = new ImageIcon("/resources/images/poof.gif");
        trackControlIcon = new ImageIcon("/resources/images/track_control.gif");
        trackControlOnIcon = new ImageIcon("/resources/images/track_control_on.gif");
        trackControlDisabledIcon = new ImageIcon("/resources/images/track_control_disabled.gif");
        zoomIcon = new ImageIcon("/resources/images/zoom.gif");
        clipOffIcon = new ImageIcon("/resources/images/clip_off.gif");
        clipOnIcon = new ImageIcon("/resources/images/clip_on.gif");
        axesOffIcon = new ImageIcon("/resources/images/axes.gif");
        axesOnIcon = new ImageIcon("/resources/images/axes_on.gif");
        calibrationToolsOffIcon = new ImageIcon("/resources/images/calibration_tool.gif");
        calibrationToolsOnIcon = new ImageIcon("/resources/images/calibration_tool_on.gif");
        calibrationToolsOffRolloverIcon = new ImageIcon("/resources/images/calibration_tool_rollover.gif");
        calibrationToolsOnRolloverIcon = new ImageIcon("/resources/images/calibration_tool_on_rollover.gif");
        pointsOffIcon = new ImageIcon("/resources/images/positions.gif");
        pointsOnIcon = new ImageIcon("/resources/images/positions_on.gif");
        velocOffIcon = new ImageIcon("/resources/images/velocities.gif");
        velocOnIcon = new ImageIcon("/resources/images/velocities_on.gif");
        accelOffIcon = new ImageIcon("/resources/images/accel.gif");
        accelOnIcon = new ImageIcon("/resources/images/accel_on.gif");
        traceOffIcon = new ImageIcon("/resources/images/trace.gif");
        traceOnIcon = new ImageIcon("/resources/images/trace_on.gif");
        labelsOffIcon = new ImageIcon("/resources/images/labels.gif");
        labelsOnIcon = new ImageIcon("/resources/images/labels_on.gif");
        stretchOffIcon = new ImageIcon("/resources/images/stretch.gif");
        stretchOnIcon = new ImageIcon("/resources/images/stretch_on.gif");
        xmassOffIcon = new ImageIcon("/resources/images/x_mass.gif");
        xmassOnIcon = new ImageIcon("/resources/images/x_mass_on.gif");
        fontSmallerDisabledIcon = new ImageIcon("/resources/images/font_smaller_disabled.gif");
        fontBiggerDisabledIcon = new ImageIcon("/resources/images/font_bigger_disabled.gif");
        autotrackerOffIcon = new ImageIcon("/resources/images/autotrack_off.gif");
        autotrackerOnIcon = new ImageIcon("/resources/images/autotrack_on.gif");
        autotrackerDisabledIcon = new ImageIcon("/resources/images/autotrack_disabled.gif");
        infoIcon = new ImageIcon("/resources/images/info.gif");
        refreshIcon = new ImageIcon("/resources/images/refresh.gif");
        refreshIcon = new ImageIcon("/resources/images/refresh.gif");
        htmlIcon = new ImageIcon("/resources/images/html.gif");
        htmlDisabledIcon = new ImageIcon("/resources/images/html_disabled.gif");
        trailIcons[0] = new ImageIcon("/resources/images/trails_off.gif");
        trailIcons[1] = new ImageIcon("/resources/images/trails_1.gif");
        trailIcons[2] = new ImageIcon("/resources/images/trails_2.gif");
        trailIcons[3] = new ImageIcon("/resources/images/trails_on.gif");
        separatorIcon = new ImageIcon("/resources/images/separator.gif");
        checkboxOffIcon = new ImageIcon("/resources/images/box_unchecked.gif");
        checkboxOnIcon = new ImageIcon("/resources/images/box_checked.gif");
//        checkboxOnDisabledIcon = new ResizableIcon("/resources/images/box_checked_disabled.gif");
        pencilOffIcon = new ImageIcon("/resources/images/pencil_off.gif");
        pencilOnIcon = new ImageIcon("/resources/images/pencil_on.gif");
        pencilOffRolloverIcon = new ImageIcon("/resources/images/pencil_off_rollover.gif");
        pencilOnRolloverIcon = new ImageIcon("/resources/images/pencil_on_rollover.gif");
        zoomFormat.setMaximumFractionDigits(0);
    }

    /**
     * TToolBar constructor.
     *
     * @param panel the tracker panel
     */
    private TToolBar(TrackerPanel panel) {
        trackerPanel = panel;
        trackerPanel.addPropertyChangeListener("track", this);
        trackerPanel.addPropertyChangeListener("clear", this);
        trackerPanel.addPropertyChangeListener("video", this);
        trackerPanel.addPropertyChangeListener("magnification", this);
        trackerPanel.addPropertyChangeListener("selectedtrack", this);
        trackerPanel.addPropertyChangeListener("selectedpoint", this);
        createGUI();
        refresh(true);
        validate();
    }

    /**
     * Creates the GUI.
     */
    protected void createGUI() {
        setFloatable(false);
        // create buttons
        Map<String, AbstractAction> actions = TActions.getActions(trackerPanel);
        // open and save buttons
        openButton = new TButton(actions.get("open"));
        openBrowserButton = new TButton(actions.get("openBrowser"));
        saveButton = new TButton(actions.get("save"));
        saveButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                String fileName = trackerPanel.getTitle();
                String extension = XML.getExtension(fileName);
                if (extension == null || !extension.equals("trk"))
                    fileName = XML.stripExtension(fileName) + ".trk";
                saveButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Save.Tooltip")
                        + " \"" + fileName + "\"");
            }
        });
        saveZipButton = new TButton(actions.get("saveZip"));
        // clip settings button
        clipSettingsDialogListener = new ComponentAdapter() {
            public void componentHidden(ComponentEvent e) {
                refresh(false);
            }
        };
        clipSettingsButton = new TButton(clipOffIcon, clipOnIcon);
        clipSettingsButton.addActionListener(e -> {
            VideoClip clip = trackerPanel.getPlayer().getVideoClip();
            ClipControl clipControl = trackerPanel.getPlayer().getClipControl();
            TFrame frame = trackerPanel.getTFrame();
            ClipInspector inspector = clip.getClipInspector(clipControl, frame);
            if (inspector.isVisible()) {
                inspector.setVisible(false);
                return;
            }
            FontSizer.setFonts(inspector, FontSizer.getLevel());
            inspector.pack();
            Point p0 = new Frame().getLocation();
            Point loc = inspector.getLocation();
            if ((loc.x == p0.x) && (loc.y == p0.y)) {
                // center inspector on the main view
                Rectangle rect = trackerPanel.getVisibleRect();
                Point p = frame.getMainView(trackerPanel).scrollPane.getLocationOnScreen();
                int x = p.x + (rect.width - inspector.getBounds().width) / 2;
                int y = p.y + (rect.height - inspector.getBounds().height) / 2;
                inspector.setLocation(x, y);
            }
            inspector.initialize();
            inspector.removeComponentListener(clipSettingsDialogListener);
            inspector.addComponentListener(clipSettingsDialogListener);
            inspector.setVisible(true);
            refresh(false);
        });
        // axes button
        axesButton = new TButton(axesOffIcon, axesOnIcon);
        axesButton.addActionListener(actions.get("axesVisible"));

        // calibration button
        calibrationButton = new CalibrationButton();

        // zoom button
        Action zoomAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // set zoom center to center of current viewport
                Rectangle rect = trackerPanel.scrollPane.getViewport().getViewRect();
                MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
                mainView.setZoomCenter(rect.x + rect.width / 2, rect.y + rect.height / 2);
                String name = e.getActionCommand();
                if (name.equals("auto")) {
                    trackerPanel.setMagnification(-1);
                } else {
                    double mag = Double.parseDouble(name);
                    trackerPanel.setMagnification(mag / 100);
                }
                refreshZoomButton();
            }
        };
        JMenuItem item = new JMenuItem(TrackerRes.getString("MainTView.Popup.MenuItem.ToFit"));
        item.setActionCommand("auto");
        item.addActionListener(zoomAction);
        zoomPopup.add(item);
        zoomPopup.addSeparator();
        for (int i = 0; i < TrackerPanel.ZOOM_LEVELS.length; i++) {
            int n = (int) (100 * TrackerPanel.ZOOM_LEVELS[i]);
            String m = String.valueOf(n);
            item = new JMenuItem(m + "%");
            item.setActionCommand(m);
            item.addActionListener(zoomAction);
            zoomPopup.add(item);
        }
        zoomButton = new TButton(zoomIcon) {
            protected JPopupMenu getPopup() {
                FontSizer.setFonts(zoomPopup, FontSizer.getLevel());
                return zoomPopup;
            }
        };
        zoomButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    trackerPanel.setMagnification(-1);
                    zoomPopup.setVisible(false);
                    refreshZoomButton();
                }
            }
        });

        // new track button
        newTrackButton = new TButton(newTrackIcon) {
            protected JPopupMenu getPopup() {
                return getNewTracksPopup();
            }
        };
        // track control button
        trackControlButton = new TButton(trackControlIcon, trackControlOnIcon);
        trackControlButton.setDisabledIcon(trackControlDisabledIcon);
        trackControlButton.addActionListener(e -> {
            TrackControl tc = TrackControl.getControl(trackerPanel);
            tc.setVisible(!tc.isVisible());
        });
        // AutoTracker button
        autotrackerButton = new TButton(autotrackerOffIcon, autotrackerOnIcon);
        autotrackerButton.setDisabledIcon(autotrackerDisabledIcon);
        autotrackerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                requestFocus(); // workaround--shouldn't need this...
            }
        });
        autotrackerButton.addActionListener(e -> {
            autotrackerButton.setSelected(!autotrackerButton.isSelected());
            AutoTracker autoTracker = trackerPanel.getAutoTracker();
            if (autoTracker.getTrack() == null) {
                TTrack track = trackerPanel.getSelectedTrack();
                if (track == null) {
                    for (TTrack next : trackerPanel.getTracks()) {
                        if (!next.isAutoTrackable()) continue;
                        track = next;
                        break;
                    }
                }
                autoTracker.setTrack(track);
            }
            autoTracker.getWizard().setVisible(autotrackerButton.isSelected());
            trackerPanel.repaint();
        });
        final Action refreshAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                button.setSelected(!button.isSelected());
                refresh(true);
            }
        };
        // p visible button
        pVisButton = new TButton(pointsOffIcon, pointsOnIcon);
        pVisButton.setSelected(true);
        pVisButton.addActionListener(refreshAction);
        // v visible button
        vVisButton = new TButton(velocOffIcon, velocOnIcon);
        vVisButton.addActionListener(refreshAction);
        // a visible button
        aVisButton = new TButton(accelOffIcon, accelOnIcon);
        aVisButton.addActionListener(refreshAction);
        // trace visible button
        traceVisButton = new TButton(traceOffIcon, traceOnIcon);
        traceVisButton.addActionListener(refreshAction);
        // trail button
        trailButton = new TButton() {
            protected JPopupMenu getPopup() {
                JPopupMenu popup = new JPopupMenu();
                ActionListener listener = e -> {
                    int n = Integer.parseInt(e.getActionCommand());
                    trailLength = trailLengths[n];
                    trailButton.setSelected(trailLength != 1);
                    refresh(true);
                    trackerPanel.repaint();
                };
                ButtonGroup group = new ButtonGroup();
                JMenuItem item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.NoTrail"));
                item.setSelected(trailLength == trailLengths[0]);
                item.setActionCommand(String.valueOf(0));
                item.addActionListener(listener);
                popup.add(item);
                group.add(item);
                item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.ShortTrail"));
                item.setSelected(trailLength == trailLengths[1]);
                item.setActionCommand(String.valueOf(1));
                item.addActionListener(listener);
                popup.add(item);
                group.add(item);
                item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.LongTrail"));
                item.setSelected(trailLength == trailLengths[2]);
                item.setActionCommand(String.valueOf(2));
                item.addActionListener(listener);
                popup.add(item);
                group.add(item);
                item = new JRadioButtonMenuItem(TrackerRes.getString("TrackControl.TrailMenu.FullTrail"));
                item.setSelected(trailLength == trailLengths[3]);
                item.setActionCommand(String.valueOf(3));
                item.addActionListener(listener);
                popup.add(item);
                group.add(item);
                FontSizer.setFonts(popup, FontSizer.getLevel());
                return popup;
            }
        };
        trailButton.setSelected(true);

        // labels visible button
        labelsButton = new TButton(labelsOffIcon, labelsOnIcon);
        labelsButton.setSelected(!Tracker.hideLabels);
        labelsButton.addActionListener(refreshAction);
        // x mass button
        xMassButton = new TButton(xmassOffIcon, xmassOnIcon);
        xMassButton.addActionListener(e -> {
            refreshAction.actionPerformed(e);
            TTrack track = trackerPanel.getSelectedTrack();
            if (track instanceof PointMass) {
                trackerPanel.getTFrame().getTrackBar(trackerPanel).refresh();
            }
        });
        // stretch button
        vStretchMenu = new JMenu();
        aStretchMenu = new JMenu();
        // velocity stretch menu
        ActionListener vListener = e -> {
            int n = Integer.parseInt(e.getActionCommand());
            trackerPanel.setSelectedPoint(null);
            trackerPanel.selectedSteps.clear();
            vStretch = n;
            refresh(true);
        };
        vGroup = new ButtonGroup();
        for (int i = 0; i < stretchValues.length; i++) {
            String s = String.valueOf(stretchValues[i]);
            item = new JRadioButtonMenuItem("x" + s);
            if (i == 0)
                item.setText(TrackerRes.getString("TrackControl.StretchVectors.None"));
            item.setActionCommand(s);
            item.setSelected(vStretch == stretchValues[i]);
            item.addActionListener(vListener);
            vStretchMenu.add(item);
            vGroup.add(item);
        }
        // acceleration stretch menu
        ActionListener aListener = e -> {
            int n = Integer.parseInt(e.getActionCommand());
            trackerPanel.setSelectedPoint(null);
            trackerPanel.selectedSteps.clear();
            aStretch = n;
            refresh(true);
        };
        aGroup = new ButtonGroup();
        for (int i = 0; i < stretchValues.length; i++) {
            String s = String.valueOf(stretchValues[i]);
            item = new JRadioButtonMenuItem("x" + s);
            if (i == 0)
                item.setText(TrackerRes.getString("TrackControl.StretchVectors.None"));
            item.setActionCommand(s);
            item.setSelected(aStretch == stretchValues[i]);
            item.addActionListener(aListener);
            aStretchMenu.add(item);
            aGroup.add(item);
        }
        stretchOffItem = new JMenuItem();
        stretchOffItem.addActionListener(e -> {
            vStretch = 1;
            aStretch = 1;
            refresh(true);
        });


        stretchButton = new TButton(stretchOffIcon, stretchOnIcon) {
            protected JPopupMenu getPopup() {
                JPopupMenu popup = new JPopupMenu();
                popup.add(vStretchMenu);
                popup.add(aStretchMenu);
                popup.addSeparator();
                popup.add(stretchOffItem);
                FontSizer.setFonts(popup, FontSizer.getLevel());
                return popup;
            }
        };

        // font buttons
        fontSmallerButton = new TButton(fontSmallerIcon);
        fontSmallerButton.setDisabledIcon(fontSmallerDisabledIcon);
        fontSmallerButton.addActionListener(e -> {
            int i = FontSizer.getLevel();
            FontSizer.setLevel(i - 1);
            fontSmallerButton.setEnabled(FontSizer.getLevel() > 0);
            fontBiggerButton.setEnabled(FontSizer.getLevel() < FontSizer.MAX_LEVEL);
        });
        fontBiggerButton = new TButton(fontBiggerIcon);
        fontBiggerButton.setDisabledIcon(fontBiggerDisabledIcon);
        fontBiggerButton.addActionListener(e -> {
            int i = FontSizer.getLevel();
            FontSizer.setLevel(i + 1);
            fontSmallerButton.setEnabled(FontSizer.getLevel() > 0);
            fontBiggerButton.setEnabled(FontSizer.getLevel() < FontSizer.MAX_LEVEL);
        });

        // horizontal glue for right end of toolbar
        toolbarFiller = Box.createHorizontalGlue();
        // info button
        infoListener = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                notesButton.setSelected(false);
            }
        };
        drawingButton = new DrawingButton();
        notesButton = new TButton(infoIcon);
        notesButton.addActionListener(e -> {
            TFrame frame = trackerPanel.getTFrame();
            if (frame != null && frame.getTrackerPanel(frame.getSelectedTab()) == trackerPanel) {
                frame.notesDialog.removeWindowListener(infoListener);
                frame.notesDialog.addWindowListener(infoListener);
                // position info dialog if first time shown
                // or if trackerPanel specifies location
                Point p0 = new Frame().getLocation();
                if (trackerPanel.infoX != Integer.MIN_VALUE ||
                        frame.notesDialog.getLocation().x == p0.x) {
                    int x, y;
                    Point p = frame.getLocationOnScreen();
                    if (trackerPanel.infoX != Integer.MIN_VALUE) {
                        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                        x = Math.max(p.x + trackerPanel.infoX, 0);
                        x = Math.min(x, dim.width - frame.notesDialog.getWidth());
                        y = Math.max(p.y + trackerPanel.infoY, 0);
                        y = Math.min(y, dim.height - frame.notesDialog.getHeight());
                        trackerPanel.infoX = Integer.MIN_VALUE;
                    } else {
                        Point pleft = TToolBar.this.getLocationOnScreen();
                        Dimension dim = frame.notesDialog.getSize();
                        Dimension wdim = TToolBar.this.getSize();
                        x = pleft.x + (int) (0.5 * (wdim.width - dim.width));
                        y = p.y + 16;
                    }
                    frame.notesDialog.setLocation(x, y);
                }
                notesButton.setSelected(!frame.notesDialog.isVisible());
                frame.notesDialog.setVisible(notesButton.isSelected());
                trackerPanel.refreshNotesDialog();
            }
        });
        refreshButton = new TButton(refreshIcon) {
            protected JPopupMenu getPopup() {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem item = new JMenuItem(TrackerRes.getString("TToolbar.Button.Refresh.Popup.RefreshNow"));
                item.addActionListener(e -> {
                    // offer to clear RGBRegion data that are valid and visible in a view
                    ArrayList<RGBRegion> regions = trackerPanel.getDrawables(RGBRegion.class);
                    ArrayList<RGBRegion> regionsToClear = new ArrayList<>();
                    if (!regions.isEmpty()) {
                        for (RGBRegion next : regions) {
                            if (trackerPanel.isTrackViewDisplayed(next) && next.dataValid) {
                                regionsToClear.add(next);
                            }
                        }
                    }
                    if (!regionsToClear.isEmpty()) {
                        // get user confirmation
                        StringBuilder list = new StringBuilder(" ");
                        for (RGBRegion next : regionsToClear) {
                            list.append(next.getName()).append(", ");
                        }
                        list = new StringBuilder(list.substring(0, list.length() - 2));
                        int i = JOptionPane.showConfirmDialog(trackerPanel.getTopLevelAncestor(),
                                TrackerRes.getString("TToolBar.Dialog.ClearRGB.Message1") + "\n" +
                                        TrackerRes.getString("TToolBar.Dialog.ClearRGB.Message2"),
                                TrackerRes.getString("TToolBar.Dialog.ClearRGB.Title") + list,
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (i == JOptionPane.YES_OPTION) {
                            for (RGBRegion next : regionsToClear) {
                                next.clearData();
                            }
                        }
                    }
                    trackerPanel.refreshTrackData();
                    trackerPanel.eraseAll();
                    trackerPanel.repaintDirtyRegion();
                });
                popup.add(item);
                popup.addSeparator();
                item = new JCheckBoxMenuItem(TrackerRes.getString("TToolbar.Button.Refresh.Popup.AutoRefresh"));
                item.setSelected(trackerPanel.isAutoRefresh);
                item.addActionListener(e -> {
                    JMenuItem item1 = (JMenuItem) e.getSource();
                    trackerPanel.isAutoRefresh = item1.isSelected();
                    if (trackerPanel.isAutoRefresh) {
                        trackerPanel.refreshTrackData();
                        trackerPanel.eraseAll();
                        trackerPanel.repaintDirtyRegion();
                    }
                });
                popup.add(item);
                FontSizer.setFonts(popup, FontSizer.getLevel());
                return popup;
            }

        };
        desktopButton = new TButton(htmlIcon) {
            protected JPopupMenu getPopup() {
                JPopupMenu popup = new JPopupMenu();
                if (!trackerPanel.supplementalFilePaths.isEmpty()) {
                    JMenu fileMenu = new JMenu(TrackerRes.getString("TToolbar.Button.Desktop.Menu.OpenFile"));
                    popup.add(fileMenu);
                    for (String next : trackerPanel.supplementalFilePaths) {
                        String title = XML.getName(next);
                        JMenuItem item = new JMenuItem(title);
                        item.setActionCommand(next);
                        item.setToolTipText(next);
                        item.addActionListener(e -> {
                            String path = e.getActionCommand();
                            OSPDesktop.displayURL(path);
                        });
                        fileMenu.add(item);
                    }
                }
                if (!pageViewTabs.isEmpty()) {
                    JMenu pageMenu = new JMenu(TrackerRes.getString("TToolbar.Button.Desktop.Menu.OpenPage"));
                    popup.add(pageMenu);
                    for (PageTView.TabData next : pageViewTabs) {
                        if (next.url == null) continue;
                        String title = next.title;
                        String path = trackerPanel.pageViewFilePaths.get(next.text);
                        if (path == null) {
                            path = next.url.toExternalForm();
                        }
                        JMenuItem item = new JMenuItem(title);
                        item.setActionCommand(path);
                        item.setToolTipText(path);
                        item.addActionListener(e -> {
                            String path1 = e.getActionCommand();
                            OSPDesktop.displayURL(path1);
                        });
                        pageMenu.add(item);
                    }
                }
                FontSizer.setFonts(popup, FontSizer.getLevel());
                return popup;
            }

        };
        desktopButton.setDisabledIcon(htmlDisabledIcon);

        // create menu items
        cloneMenu = new JMenu();
        showTrackControlItem = new JCheckBoxMenuItem();
        showTrackControlItem.addActionListener(e -> {
            TrackControl tc = TrackControl.getControl(trackerPanel);
            tc.setVisible(showTrackControlItem.isSelected());
        });
        selectNoneItem = new JMenuItem();
        selectNoneItem.addActionListener(e -> trackerPanel.setSelectedTrack(null));
    }

    protected void refreshZoomButton() {
        double zoom = trackerPanel.getMagnification() * 100;
        zoomButton.setText(zoomFormat.format(zoom) + "%");
    }

    /**
     * Refreshes the GUI.
     *
     * @param refreshTrackProperties true to refresh the track display properties
     */
    protected void refresh(final boolean refreshTrackProperties) {
        Tracker.logTime(getClass().getSimpleName() + hashCode() + " refresh");
        Runnable runner = () -> {
            refreshing = true; // signals listeners that items are being refreshed
            refreshZoomButton();
            calibrationButton.refresh();
            drawingButton.refresh();
            stretchButton.setSelected(vStretch > 1 || aStretch > 1);
            stretchOffItem.setText(TrackerRes.getString("TToolBar.MenuItem.StretchOff"));
            stretchOffItem.setEnabled(vStretch > 1 || aStretch > 1);
            // refresh stretch items
            Enumeration<AbstractButton> en = vGroup.getElements();
            while (en.hasMoreElements()) {
                AbstractButton next = en.nextElement();
                if (next.getActionCommand().equals(String.valueOf(vStretch))) {
                    next.setSelected(true);
                }
            }
            en = aGroup.getElements();
            while (en.hasMoreElements()) {
                AbstractButton next = en.nextElement();
                if (next.getActionCommand().equals(String.valueOf(aStretch))) {
                    next.setSelected(true);
                }
            }

            vStretchMenu.setText(TrackerRes.getString("PointMass.MenuItem.Velocity"));
            aStretchMenu.setText(TrackerRes.getString("PointMass.MenuItem.Acceleration"));
            openButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Open.Tooltip"));
            openBrowserButton.setToolTipText(TrackerRes.getString("TToolBar.Button.OpenBrowser.Tooltip"));
            saveZipButton.setToolTipText(TrackerRes.getString("TToolBar.Button.SaveZip.Tooltip"));
            clipSettingsButton.setToolTipText(MediaRes.getString("VideoPlayer.Button.ClipSettings.ToolTip"));
            axesButton.setToolTipText(TrackerRes.getString("TToolbar.Button.AxesVisible.Tooltip"));
            zoomButton.setToolTipText(TrackerRes.getString("TToolBar.Button.Zoom.Tooltip"));
            notesButton.setToolTipText(TrackerRes.getString("TActions.Action.Description"));
            refreshButton.setToolTipText(TrackerRes.getString("TToolbar.Button.Refresh.Tooltip"));
            desktopButton.setToolTipText(TrackerRes.getString("TToolbar.Button.Desktop.Tooltip"));
            pVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Positions.ToolTip"));
            vVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Velocities.ToolTip"));
            aVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Accelerations.ToolTip"));
            xMassButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Xmass.ToolTip"));
            trailButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Trails.ToolTip"));
            labelsButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Labels.ToolTip"));
            stretchButton.setToolTipText(TrackerRes.getString("TrackControl.Button.StretchVectors.ToolTip"));
            traceVisButton.setToolTipText(TrackerRes.getString("TrackControl.Button.Trace.ToolTip"));
            newTrackButton.setText(TrackerRes.getString("TrackControl.Button.NewTrack"));
            newTrackButton.setToolTipText(TrackerRes.getString("TrackControl.Button.NewTrack.ToolTip"));
            trackControlButton.setToolTipText(TrackerRes.getString("TToolBar.Button.TrackControl.Tooltip"));
            autotrackerButton.setToolTipText(TrackerRes.getString("TToolBar.Button.AutoTracker.Tooltip"));
            fontSmallerButton.setEnabled(FontSizer.getLevel() > 0);
            fontBiggerButton.setEnabled(FontSizer.getLevel() < FontSizer.MAX_LEVEL);
            if (trackerPanel.getPlayer() != null) {
                VideoClip clip = trackerPanel.getPlayer().getVideoClip();
                ClipInspector inspector = clip.getClipInspector();
                clipSettingsButton.setSelected(inspector != null && inspector.isVisible());
            }
            CoordAxes axes = trackerPanel.getAxes();
            if (axes != null) {
                axesButton.setSelected(axes.isVisible());
                axes.removePropertyChangeListener("visible", TToolBar.this);
                axes.addPropertyChangeListener("visible", TToolBar.this);
            }
            ArrayList<TTrack> tracks = trackerPanel.getUserTracks();
            trackControlButton.setEnabled(!tracks.isEmpty());
            autotrackerButton.setEnabled(trackerPanel.getVideo() != null);
            // count independent masses
            double totalMass = 0;
            int massCount = 0;
            for (TTrack track : tracks) {
                if (track instanceof PointMass
                        && !(track instanceof CenterOfMass)
                        && !(track instanceof DynamicSystem)) {
                    PointMass p = (PointMass) track;
                    totalMass += p.getMass();
                    massCount++;
                }
            }
            // refresh all tracks
            if (refreshTrackProperties) {
                for (TTrack track : trackerPanel.getTracks()) {
                    track.removePropertyChangeListener("locked", TToolBar.this);
                    track.addPropertyChangeListener("locked", TToolBar.this);
                    // refresh track display properties from current button states
                    track.setTrailLength(trailLength);
                    track.setTrailVisible(trailButton.isSelected());
                    if (track instanceof PointMass) {
                        PointMass p = (PointMass) track;
                        p.setTraceVisible(traceVisButton.isSelected());
                        p.setPositionVisible(trackerPanel, pVisButton.isSelected());
                        p.setVVisible(trackerPanel, vVisButton.isSelected());
                        p.setAVisible(trackerPanel, aVisButton.isSelected());
                        p.setLabelsVisible(trackerPanel, labelsButton.isSelected());
                        Footprint[] footprints = p.getVelocityFootprints();
                        for (Footprint footprint : footprints) {
                            if (footprint instanceof ArrowFootprint) {
                                ArrowFootprint arrow = (ArrowFootprint) footprint;
                                arrow.setSolidHead(false);
                                if (xMassButton.isSelected()) {
                                    arrow.setStretch(vStretch * massCount * p.getMass() / totalMass);
                                } else {
                                    arrow.setStretch(vStretch);
                                }
                            }
                        }
                        footprints = p.getAccelerationFootprints();
                        for (Footprint footprint : footprints) {
                            if (footprint instanceof ArrowFootprint) {
                                ArrowFootprint arrow = (ArrowFootprint) footprint;
                                arrow.setSolidHead(true);
                                if (xMassButton.isSelected()) {
                                    arrow.setStretch(aStretch * massCount * p.getMass() / totalMass);
                                } else {
                                    arrow.setStretch(aStretch);
                                }
                            }
                        }
                        p.repaint();
                    } else if (track instanceof Vector) {
                        Vector v = (Vector) track;
                        v.setLabelsVisible(labelsButton.isSelected());
                        Footprint[] footprints = v.getFootprints();
                        for (Footprint footprint : footprints) {
                            if (footprint instanceof ArrowFootprint) {
                                ArrowFootprint arrow = (ArrowFootprint) footprint;
                                arrow.setStretch(vStretch);
                            }
                        }
                        v.repaint();
                    }
                }
            }
            TPoint pt = trackerPanel.getSelectedPoint();
            if (pt != null) pt.showCoordinates(trackerPanel);

            // set trails icon
            for (int i = 0; i < trailLengths.length; i++) {
                if (trailLength == trailLengths[i]) {
                    trailButton.setIcon(trailIcons[i]);
                    FontSizer.setFonts(trailButton, FontSizer.getLevel());
                }
            }

            // refresh pageViewTabs list
            pageViewTabs.clear();
            TFrame frame = trackerPanel.getTFrame();
            if (frame != null) {
                TView[][] views = frame.getTViews(trackerPanel);
                for (TView[] next : views) {
                    if (next == null) continue;
                    for (TView view : next) {
                        if (view == null) continue;
                        if (view instanceof PageTView) {
                            PageTView page = (PageTView) view;
                            for (TabView tab : page.tabs) {
                                if (tab.data.url != null) {
                                    pageViewTabs.add(tab.data);
                                }
                            }
                        }
                    }
                }
                sortPageViewTabs();
            }

            // assemble buttons
            removeAll();
            if (org.opensourcephysics.display.OSPRuntime.applet == null) {
                if (trackerPanel.isEnabled("file.open")) {
                    add(openButton);
                }
                if (trackerPanel.isEnabled("file.save")) {
                    add(saveButton);
                }
                boolean showLib = trackerPanel.isEnabled("file.library")
                        && (trackerPanel.isEnabled("file.open") || trackerPanel.isEnabled("file.export"));
                if (showLib && getComponentCount() > 0)
                    add(getSeparator());
                if (trackerPanel.isEnabled("file.open") && trackerPanel.isEnabled("file.library")) {
                    add(openBrowserButton);
                }
                if (trackerPanel.isEnabled("file.export") && trackerPanel.isEnabled("file.library")) {
                    add(saveZipButton);
                }
            }
            if (getComponentCount() > 0)
                add(getSeparator()); // first separator
            boolean addSecondSeparator = false;
            if (trackerPanel.isEnabled("button.clipSettings")) {
                add(clipSettingsButton);
                addSecondSeparator = true;
            }
            if (trackerPanel.isEnabled("calibration.stick")
                    || trackerPanel.isEnabled("calibration.tape")
                    || trackerPanel.isEnabled("calibration.points")
                    || trackerPanel.isEnabled("calibration.offsetOrigin")) {
                add(calibrationButton);
                addSecondSeparator = true;
            }
            if (trackerPanel.isEnabled("button.axes")) {
                add(axesButton);
                addSecondSeparator = true;
            }
            if (addSecondSeparator)
                add(getSeparator());
            if (trackerPanel.isCreateTracksEnabled()) {
                add(newTrackButton);
            }
            add(trackControlButton);
            if (trackerPanel.isEnabled("track.autotrack"))
                add(autotrackerButton);
            add(getSeparator());
            add(zoomButton);
            add(getSeparator());
            if (trackerPanel.isEnabled("button.trails")
                    || trackerPanel.isEnabled("button.labels")) {
                if (trackerPanel.isEnabled("button.trails"))
                    add(trailButton);
                if (trackerPanel.isEnabled("button.labels"))
                    add(labelsButton);
                add(getSeparator());
            }
            if (trackerPanel.isEnabled("button.path")
                    || trackerPanel.isEnabled("button.x")
                    || trackerPanel.isEnabled("button.v")
                    || trackerPanel.isEnabled("button.a")) {
                if (trackerPanel.isEnabled("button.path"))
                    add(traceVisButton);
                if (trackerPanel.isEnabled("button.x"))
                    add(pVisButton);
                if (trackerPanel.isEnabled("button.v"))
                    add(vVisButton);
                if (trackerPanel.isEnabled("button.a"))
                    add(aVisButton);
                add(getSeparator());
            }
            if (trackerPanel.isEnabled("button.stretch")
                    || trackerPanel.isEnabled("button.xMass")) {
                if (trackerPanel.isEnabled("button.stretch"))
                    add(stretchButton);
                if (trackerPanel.isEnabled("button.xMass"))
                    add(xMassButton);
            }
            add(toolbarFiller);
            if (trackerPanel.isEnabled("button.drawing"))
                add(drawingButton);
            add(desktopButton);
            add(notesButton);
            boolean hasPageURLs = !pageViewTabs.isEmpty();
            desktopButton.setEnabled(hasPageURLs || !trackerPanel.supplementalFilePaths.isEmpty());
            add(refreshButton);

            FontSizer.setFonts(newTrackButton, FontSizer.getLevel());
            FontSizer.setFonts(zoomButton, FontSizer.getLevel());

            validate();
            repaint();
            refreshing = false;
        };
        if (SwingUtilities.isEventDispatchThread()) runner.run();
        else SwingUtilities.invokeLater(runner);
    }

    /**
     * Disposes of this toolbar
     */
    public void dispose() {
        toolbars.remove(trackerPanel);
        removeAll();
        trackerPanel.removePropertyChangeListener("track", this);
        trackerPanel.removePropertyChangeListener("clear", this);
        trackerPanel.removePropertyChangeListener("video", this);
        trackerPanel.removePropertyChangeListener("magnification", this);
        trackerPanel.removePropertyChangeListener("selectedtrack", this);
        trackerPanel.removePropertyChangeListener("selectedpoint", this);
        for (Integer n : TTrack.activeTracks.keySet()) {
            TTrack track = TTrack.activeTracks.get(n);
            track.removePropertyChangeListener("locked", this);
            track.removePropertyChangeListener("visible", this);
        }
        pageViewTabs.clear();
        trackerPanel = null;
    }

    @Override
    public void finalize() {
        OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector");
    }

    /**
     * Responds to the following events: "selectedtrack", "selectedpoint",
     * "track" from tracker panel, "locked" from tracks, "visible" from tape
     * and axes.
     *
     * @param e the property change event
     */
    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        switch (name) {
            case "video":   // video has changed
            case "locked":     // track has been locked or unlocked
            case "selectedpoint":   // selected point has changed
                refresh(false);
                break;
            case "selectedtrack":   // selected track has changed
                // refresh info dialog if visible
                trackerPanel.refreshNotesDialog();
                refresh(false);
                break;
            case "magnification":   // magnification has changed
                refreshZoomButton();
                break;
            case "visible":  // axes or calibration tool visibility
                if (e.getSource() == trackerPanel.getAxes()) {
                    axesButton.setSelected(trackerPanel.getAxes().isVisible());
                } else {
                    calibrationButton.refresh();
                }
                break;
            case "track":      // track has been added or removed
                if (e.getOldValue() != null) {      // track has been removed
                    TTrack track = (TTrack) e.getOldValue();
                    trackerPanel.calibrationTools.remove(track);
                    trackerPanel.visibleTools.remove(track);
                    track.removePropertyChangeListener("visible", this);
                    track.removePropertyChangeListener("locked", this);
                    if (trackerPanel.visibleTools.isEmpty()) {
                        calibrationButton.setSelected(false);
                    }
                }
                refresh(true);
                break;
            case "clear":      // tracks have been cleared
                for (Integer n : TTrack.activeTracks.keySet()) {
                    TTrack track = TTrack.activeTracks.get(n);
                    trackerPanel.calibrationTools.remove(track);
                    trackerPanel.visibleTools.remove(track);
                    track.removePropertyChangeListener("visible", this);
                    track.removePropertyChangeListener("locked", this);
                }
                calibrationButton.setSelected(false);
                refresh(true);
                break;
        }
    }

    /**
     * Refreshes and returns the "new tracks" popup menu.
     *
     * @return the popup
     */
    protected JPopupMenu getNewTracksPopup() {
        newPopup.removeAll();
        TMenuBar menubar = TMenuBar.getMenuBar(trackerPanel);
        menubar.refresh();
        for (Component c : menubar.newTrackItems) {
            newPopup.add(c);
            if (c == menubar.newDataTrackMenu) {
                // disable newDataTrackPasteItem unless pastable data is on the clipboard
                menubar.newDataTrackPasteItem.setEnabled(false);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable data = clipboard.getContents(null);
                if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    try {
                        String s = (String) data.getTransferData(DataFlavor.stringFlavor);
                        menubar.newDataTrackPasteItem.setEnabled(ParticleDataTrack.getImportableDataName(s) != null);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        if (menubar.cloneMenu.getItemCount() > 0 && trackerPanel.isEnabled("new.clone")) {
            newPopup.addSeparator();
            newPopup.add(menubar.cloneMenu);
        }
        return newPopup;
    }

    public static JButton getSeparator() {
        JButton b = new JButton(separatorIcon);
        b.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        return b;
    }

    private void sortPageViewTabs() {
        pageViewTabs.sort(Comparator.comparing(one -> one.title.toLowerCase()));
    }

    /**
     * Gets the toolbar for the specified tracker panel.
     *
     * @param panel the tracker panel
     * @return the toolbar
     */
    public static synchronized TToolBar getToolbar(TrackerPanel panel) {
        TToolBar toolbar = toolbars.get(panel);
        if (toolbar == null) {
            toolbar = new TToolBar(panel);
            toolbars.put(panel, toolbar);
        }
        return toolbar;
    }

    /**
     * Returns an XML.ObjectLoader to save and load object data.
     *
     * @return the XML.ObjectLoader
     */
    public static XML.ObjectLoader getLoader() {
        return new Loader();
    }

    /**
     * A class to save and load object data.
     */
    static class Loader implements XML.ObjectLoader {

        /**
         * Saves object data.
         *
         * @param control the control to save to
         * @param obj     the TrackerPanel object to save
         */
        public void saveObject(XMLControl control, Object obj) {
            TToolBar toolbar = (TToolBar) obj;
            control.setValue("trace", toolbar.traceVisButton.isSelected());
            control.setValue("position", toolbar.pVisButton.isSelected());
            control.setValue("velocity", toolbar.vVisButton.isSelected());
            control.setValue("acceleration", toolbar.aVisButton.isSelected());
            control.setValue("labels", toolbar.labelsButton.isSelected());
            control.setValue("multiply_by_mass", toolbar.xMassButton.isSelected());
            control.setValue("trail_length", toolbar.trailLength);
            control.setValue("stretch", toolbar.vStretch);
            control.setValue("stretch_acceleration", toolbar.aStretch);
        }

        /**
         * Creates an object.
         *
         * @param control the control
         * @return the newly created object
         */
        public Object createObject(XMLControl control) {
            return null;
        }

        /**
         * Loads an object with data from an XMLControl.
         *
         * @param control the control
         * @param obj     the object
         * @return the loaded object
         */
        public Object loadObject(XMLControl control, Object obj) {
            TToolBar toolbar = (TToolBar) obj;
            toolbar.traceVisButton.setSelected(control.getBoolean("trace"));
            toolbar.pVisButton.setSelected(control.getBoolean("position"));
            toolbar.vVisButton.setSelected(control.getBoolean("velocity"));
            toolbar.aVisButton.setSelected(control.getBoolean("acceleration"));
            toolbar.labelsButton.setSelected(control.getBoolean("labels"));
            toolbar.xMassButton.setSelected(control.getBoolean("multiply_by_mass"));
            toolbar.trailLength = control.getInt("trail_length");
            toolbar.vStretch = control.getInt("stretch");
            if (control.getPropertyNames().contains("stretch_acceleration")) {
                toolbar.aStretch = control.getInt("stretch_acceleration");
            } else toolbar.aStretch = toolbar.vStretch;
            return obj;
        }
    }

    /**
     * A button to manage the creation and visibility of calibration tools.
     */
    protected class CalibrationButton extends TButton
            implements ActionListener {

        boolean showPopup;
        JPopupMenu popup = new JPopupMenu();

        /**
         * Constructor.
         */
        private CalibrationButton() {
            setIcons(calibrationToolsOffIcon, calibrationToolsOnIcon);
            setRolloverIcon(calibrationToolsOffRolloverIcon);
            setRolloverSelectedIcon(calibrationToolsOnRolloverIcon);
            // mouse listener to distinguish between popup and tool visibility actions
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int w = calibrationToolsOffRolloverIcon.getIconWidth();
                    int dw = calibrationButton.getWidth() - w;
                    // show popup if right side of button clicked or if no tools selected
                    showPopup = e.getX() > (18 + dw / 2) || trackerPanel.visibleTools.isEmpty();
                }
            });
            addActionListener(this);
        }

        /**
         * Overrides TButton method.
         *
         * @return the popup, or null if the right side of this button was clicked
         */
        protected JPopupMenu getPopup() {
            if (!showPopup) return null;
            // rebuild popup menu
            popup.removeAll();
            JMenuItem item;
            for (TTrack track : trackerPanel.calibrationTools) {
                item = new JCheckBoxMenuItem(track.getName());
                item.setSelected(trackerPanel.visibleTools.contains(track));
                item.setActionCommand(track.getName());
                item.addActionListener(this);
                popup.add(item);
            }
            // new tools menu
            JMenu newToolsMenu = getCalibrationToolsMenu();
            if (newToolsMenu.getItemCount() > 0) {
                if (!trackerPanel.calibrationTools.isEmpty())
                    popup.addSeparator();
                popup.add(newToolsMenu);
            }
            FontSizer.setFonts(popup, FontSizer.getLevel());
            return popup;
        }

        protected JMenu getCalibrationToolsMenu() {
            // new tools menu
            JMenu newToolsMenu = new JMenu(TrackerRes.getString("TMenuBar.MenuItem.NewTrack"));
            JMenuItem item;
            if (trackerPanel.isEnabled("calibration.stick")) {
                item = new JMenuItem(TrackerRes.getString("Stick.Name"));
                item.addActionListener(e -> {
                    TapeMeasure track = new TapeMeasure();
                    track.setColor(Color.BLUE);
                    track.setStickMode(true);
                    // assign a default name
                    String name = TrackerRes.getString("CalibrationStick.New.Name");
                    int i = trackerPanel.getAlphabetIndex(name, " ");
                    String letter = TrackerPanel.alphabet.substring(i, i + 1);
                    track.setName(name + " " + letter);
                    trackerPanel.addTrack(track);
                    calibrationButton.setSelected(true);

                    // show all tools in visibleTools list
                    for (TTrack next : trackerPanel.visibleTools) {
                        showCalibrationTool(next);
                    }

                    // mark immediately if preferred
                    if (Tracker.centerCalibrationStick) {
                        // place at center of viewport
                        MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
                        Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
                        int xpix = rect.x + rect.width / 2;
                        int ypix = rect.y + rect.height / 2;
                        double x = trackerPanel.pixToX(xpix);
                        double y = trackerPanel.pixToY(ypix);
                        track.createStep(0, x - 50, y, x + 50, y); // length 100 image units
                    }

                    trackerPanel.setSelectedTrack(track);
                });
                newToolsMenu.add(item);
            }

            if (trackerPanel.isEnabled("calibration.tape")) {
                item = new JMenuItem(TrackerRes.getString("CalibrationTapeMeasure.Name"));
                item.addActionListener(e -> {
                    TapeMeasure track = new TapeMeasure();
                    track.setColor(Color.BLUE);
                    track.setReadOnly(false);
                    // assign a default name
                    String name = TrackerRes.getString("CalibrationTapeMeasure.New.Name");
                    int i = trackerPanel.getAlphabetIndex(name, " ");
                    String letter = TrackerPanel.alphabet.substring(i, i + 1);
                    track.setName(name + " " + letter);
                    trackerPanel.addTrack(track);
                    calibrationButton.setSelected(true);

                    // show all tools in visibleTools list
                    for (TTrack next : trackerPanel.visibleTools) {
                        showCalibrationTool(next);
                    }

                    // mark immediately if preferred
                    if (Tracker.centerCalibrationStick) {
                        // place at center of viewport
                        MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
                        Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
                        int xpix = rect.x + rect.width / 2;
                        int ypix = rect.y + rect.height / 2;
                        double x = trackerPanel.pixToX(xpix);
                        double y = trackerPanel.pixToY(ypix);
                        track.createStep(0, x - 50, y, x + 50, y); // length 100 image units
                    }

                    trackerPanel.setSelectedTrack(track);
                });
                newToolsMenu.add(item);
            }

            if (trackerPanel.isEnabled("calibration.points")) {
                item = new JMenuItem(TrackerRes.getString("Calibration.Name"));
                item.addActionListener(e -> {
                    Calibration track = new Calibration();
                    // assign a default name
                    String name = TrackerRes.getString("Calibration.New.Name");
                    int i = trackerPanel.getAlphabetIndex(name, " ");
                    String letter = TrackerPanel.alphabet.substring(i, i + 1);
                    track.setName(name + " " + letter);

                    trackerPanel.addTrack(track);
                    calibrationButton.setSelected(true);
                    // show all tools in visibleTools list
                    for (TTrack next : trackerPanel.visibleTools) {
                        showCalibrationTool(next);
                    }
                    trackerPanel.setSelectedTrack(track);
                    trackerPanel.getAxes().setVisible(true);
                });
                newToolsMenu.add(item);
            }

            if (trackerPanel.isEnabled("calibration.offsetOrigin")) {
                item = new JMenuItem(TrackerRes.getString("OffsetOrigin.Name"));
                item.addActionListener(e -> {
                    OffsetOrigin track = new OffsetOrigin();
                    // assign a default name
                    String name = TrackerRes.getString("OffsetOrigin.New.Name");
                    int i = trackerPanel.getAlphabetIndex(name, " ");
                    String letter = TrackerPanel.alphabet.substring(i, i + 1);
                    track.setName(name + " " + letter);

                    trackerPanel.addTrack(track);
                    calibrationButton.setSelected(true);
                    // show all tools in visibleTools list
                    for (TTrack next : trackerPanel.visibleTools) {
                        showCalibrationTool(next);
                    }
                    trackerPanel.setSelectedTrack(track);
                    trackerPanel.getAxes().setVisible(true);
                });
                newToolsMenu.add(item);
            }
            return newToolsMenu;
        }

        /**
         * Responds to action events from both this button and the popup items.
         *
         * @param e the action event
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == calibrationButton) { // button action: show/hide tools
                if (showPopup) return;
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.hideMouseBox();
                if (!calibrationButton.isSelected()) {
                    calibrationButton.setSelected(true);
                    // show tools in visibleTools list
                    for (TTrack track : trackerPanel.visibleTools) {
                        showCalibrationTool(track);
                    }
                } else {
                    calibrationButton.setSelected(false);
                    // hide all tools
                    for (TTrack track : trackerPanel.calibrationTools) {
                        hideCalibrationTool(track);
                    }
                }
                trackerPanel.repaint();
            } else { // menuItem action
                // see which item changed and show/hide corresponding tool
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                JMenuItem source = (JMenuItem) e.getSource();
                for (TTrack track : trackerPanel.calibrationTools) {
                    if (e.getActionCommand().equals(track.getName())) {
                        if (source.isSelected()) {
                            trackerPanel.visibleTools.add(track);
                            calibrationButton.setSelected(true);
                            // show only tools in visibleTools
                            for (TTrack next : trackerPanel.visibleTools) {
                                showCalibrationTool(next);
                            }
                        } else {
                            hideCalibrationTool(track);
                            trackerPanel.visibleTools.remove(track);
                            boolean toolsVisible = false;
                            for (TTrack next : trackerPanel.visibleTools) {
                                toolsVisible = toolsVisible || next.isVisible();
                            }
                            calibrationButton.setSelected(toolsVisible);
                        }
                    }
                }
                refresh();
            }
        }

        /**
         * Shows a calibration tool.
         *
         * @param track a calibration tool
         */
        void showCalibrationTool(TTrack track) {
            track.erase();
            track.setVisible(true);
            if (track instanceof Calibration) {
                int n = trackerPanel.getFrameNumber();
                Step step = track.getStep(n);
                if (step == null || step.getPoints()[1] == null) {
                    trackerPanel.setSelectedTrack(track);
                }
            } else if (track instanceof OffsetOrigin) {
                int n = trackerPanel.getFrameNumber();
                Step step = track.getStep(n);
                if (step == null) {
                    trackerPanel.setSelectedTrack(track);
                }
            }
        }

        /**
         * Hides a calibration tool.
         *
         * @param track a calibration tool
         */
        void hideCalibrationTool(TTrack track) {
            track.setVisible(false);
            if (trackerPanel.getSelectedTrack() == track) {
                trackerPanel.setSelectedTrack(null);
            }
        }

        /**
         * Refreshes this button.
         */
        void refresh() {
            setToolTipText(TrackerRes.getString("TToolbar.Button.TapeVisible.Tooltip"));
            // add "visible" property change listeners to calibration tools
            for (TTrack track : trackerPanel.calibrationTools) {
                track.removePropertyChangeListener("visible", TToolBar.this);
                track.addPropertyChangeListener("visible", TToolBar.this);
            }
            // check visibility of tools and state of menu items
            boolean toolsVisible = false;
            for (TTrack track : trackerPanel.calibrationTools) {
                toolsVisible = toolsVisible || track.isVisible();
            }
            if (notYetCalibrated && toolsVisible) {
                notYetCalibrated = false;
                setSelected(true);
            }
        }

    } // end calibration button

    /**
     * A button to manage the visibility of the pencil scenes and control dialog
     */
    protected class DrawingButton extends TButton
            implements ActionListener {

        boolean showPopup;
        JPopupMenu popup;
        JMenuItem drawingVisibleCheckbox;

        /**
         * Constructor.
         */
        private DrawingButton() {
            setIcons(pencilOffIcon, pencilOnIcon);
            setRolloverIcon(pencilOffRolloverIcon);
            setRolloverSelectedIcon(pencilOnRolloverIcon);
            addActionListener(this);

            // mouse listener to distinguish between popup and tool visibility actions
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    int w = getIcon().getIconWidth();
                    int dw = getWidth() - w;
                    // show popup if right side of button clicked
                    showPopup = e.getX() > (w * 18 / 28 + dw / 2);
                }
            });

            drawingVisibleCheckbox = new JMenuItem();
            drawingVisibleCheckbox.setSelected(true);
            drawingVisibleCheckbox.setDisabledIcon(checkboxOnDisabledIcon);
            drawingVisibleCheckbox.addActionListener(e -> {
                drawingVisibleCheckbox.setSelected(!drawingVisibleCheckbox.isSelected());
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
                drawer.setDrawingsVisible(drawingVisibleCheckbox.isSelected());
                trackerPanel.repaint();
            });
            popup = new JPopupMenu();
            popup.add(drawingVisibleCheckbox);
        }

        @Override
        protected JPopupMenu getPopup() {
            if (!showPopup) return null;
            refresh();
            FontSizer.setFonts(popup, FontSizer.getLevel());
            checkboxOnDisabledIcon.resize(FontSizer.getIntegerFactor());
            return popup;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (showPopup) return;
            trackerPanel.setSelectedPoint(null);
            trackerPanel.selectedSteps.clear();
            trackerPanel.hideMouseBox();
            setSelected(!isSelected());
            PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
            drawer.getDrawingControl().setVisible(isSelected());
            if (isSelected()) {
                if (drawer.scenes.isEmpty()) {
                    drawer.addNewScene();
                } else {
                    PencilScene scene = drawer.getSceneAtFrame(trackerPanel.getFrameNumber());
                    drawer.getDrawingControl().setSelectedScene(scene);
                }
                drawer.setDrawingsVisible(true);
            }
        }

        /**
         * Refreshes this button.
         */
        void refresh() {
            setToolTipText(TrackerRes.getString("TToolBar.Button.Drawings.Tooltip"));
            drawingVisibleCheckbox.setText(TrackerRes.getString("TToolBar.MenuItem.DrawingsVisible.Text"));
            PencilDrawer drawer = PencilDrawer.getDrawer(trackerPanel);
            drawingVisibleCheckbox.setSelected(drawer.areDrawingsVisible());
            drawingVisibleCheckbox.setIcon(drawer.areDrawingsVisible() ? checkboxOnIcon : checkboxOffIcon);
            drawingVisibleCheckbox.setEnabled(!PencilDrawer.isDrawing(trackerPanel));
        }

    }


}
