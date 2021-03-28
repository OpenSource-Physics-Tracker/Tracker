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

import java.util.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.net.URL;

import javax.swing.*;

import org.opensourcephysics.cabrillo.tracker.vector.Vector;
import org.opensourcephysics.cabrillo.tracker.vector.VectorSum;
import org.opensourcephysics.cabrillo.tracker.vector.VectorSumInspector;
import org.opensourcephysics.controls.*;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.tools.FunctionTool;
import org.opensourcephysics.tools.Resource;
import org.opensourcephysics.tools.ResourceLoader;

/**
 * This creates a map of action name to action for many common tracker actions.
 *
 * @author Douglas Brown
 */
public class TActions {

  /**
   * Maps trackerPanel to actions map
   */
    static Map<TrackerPanel, Map<String, AbstractAction>> actionMaps = new HashMap<>();

    static String newline = System.getProperty("line.separator", "\n");

    /**
     * Private constructor.
     */
    private TActions() {
    }

    /**
     * Gets an action for a TrackerPanel.
     *
     * @param key          the name of the action
     * @param trackerPanel the TrackerPanel
     * @return the Action
     */
    public static Action getAction(String key, TrackerPanel trackerPanel) {
        return getActions(trackerPanel).get(key);
    }

    /**
     * Clears all actions. This forces creation of new ones using new locale.
     */
    public static void clear() {
        actionMaps.clear();
    }

    /**
     * Gets the action map for a TrackerPanel.
     *
     * @param trackerPanel the TrackerPanel
     * @return the Map
     */
    public static Map<String, AbstractAction> getActions(final TrackerPanel trackerPanel) {
        Map<String, AbstractAction> actions = actionMaps.get(trackerPanel);
        if (actions != null) return actions;
        // create new actionMap
        actions = new HashMap<>();
        actionMaps.put(trackerPanel, actions);
        // clear tracks
        final AbstractAction clearTracksAction = new AbstractAction(TrackerRes.getString("TActions.Action.ClearTracks"), null) {
            public void actionPerformed(ActionEvent e) {
                // check for locked tracks and get list of xml strings for undoableEdit
                ArrayList<String> xml = new ArrayList<>();
                boolean locked = false;
                ArrayList<org.opensourcephysics.display.Drawable> keepers = trackerPanel.getSystemDrawables();
                for (TTrack track : trackerPanel.getTracks()) {
                    if (keepers.contains(track)) continue;
                    xml.add(new XMLControlElement(track).toXML());
                    locked = locked || (track.isLocked() && !track.isDependent());
                }
                if (locked) {
                    int i = JOptionPane.showConfirmDialog(trackerPanel,
                            TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Message"),
                            TrackerRes.getString("TActions.Dialog.DeleteLockedTracks.Title"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (i != 0) return;
                }
                // post edit and clear tracks
                Undo.postTrackClear(trackerPanel, xml);
                trackerPanel.clearTracks();
            }
        };
        actions.put("clearTracks", clearTracksAction);
        // new tab
        AbstractAction newTabAction = new AbstractAction(TrackerRes.getString("TActions.Action.NewTab"), null) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    TrackerPanel newPanel = new TrackerPanel();
                    frame.addTab(newPanel);
                    frame.setSelectedTab(newPanel);
                    JSplitPane pane = frame.getSplitPane(newPanel, 0);
                    pane.setDividerLocation(frame.defaultRightDivider);
                    frame.refresh();
                }
            }
        };
        actions.put("newTab", newTabAction);
        // pastexml
        AbstractAction pasteAction = new AbstractAction(TrackerRes.getString("TActions.Action.Paste")) {
            public void actionPerformed(ActionEvent e) {
                if (!TrackerIO.pasteXML(trackerPanel)) {
                    // pasting XML failed, so try to paste data
                    String dataString = DataTool.paste();
                    trackerPanel.importData(dataString, null); // returns DataTrack if successful
                }
            }
        };
        actions.put("paste", pasteAction);
        // open
        Icon icon = new ResizableIcon(new ImageIcon(TActions.class.getResource("/images/open.gif")));
        final AbstractAction openAction = new AbstractAction(TrackerRes.getString("TActions.Action.Open"), icon) {
            public void actionPerformed(ActionEvent e) {
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    TrackerIO.open((File) null, frame);
                    frame.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        actions.put("open", openAction);
        // open url
        final AbstractAction openURLAction = new AbstractAction(TrackerRes.getString("TActions.Action.OpenURL")) {
            public void actionPerformed(ActionEvent e) {
                Object input = JOptionPane.showInputDialog(trackerPanel.getTFrame(),
                        TrackerRes.getString("TActions.Dialog.OpenURL.Message")
                                + ":                             ",
                        TrackerRes.getString("TActions.Dialog.OpenURL.Title"),
                        JOptionPane.PLAIN_MESSAGE, null, null, null);
                if (input == null || input.toString().trim().equals("")) {
                    return;
                }
                Resource res = ResourceLoader.getResource(input.toString().trim());
                if (res == null || res.getURL() == null) {
                    JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
                            TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Message")
                                    + "\n\"" + input.toString().trim() + "\"",
                            TrackerRes.getString("TActions.Dialog.URLResourceNotFound.Title"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                URL url = res.getURL();
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setMouseCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    TrackerIO.open(url, frame);
                    frame.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        actions.put("openURL", openURLAction);
        // openBrowser
        icon = new ResizableIcon(new ImageIcon(TActions.class.getResource("/images/open_catalog.gif")));
        final AbstractAction openBrowserAction = new AbstractAction(TrackerRes.getString("TActions.Action.OpenBrowser"), icon) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    frame.getLibraryBrowser().setVisible(true);
                }
            }
        };
        actions.put("openBrowser", openBrowserAction);
        // properties
        final AbstractAction propertiesAction = new AbstractAction(TrackerRes.getString("TActions.Action.Properties")) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    frame.getPropertiesDialog(trackerPanel).setVisible(true);
                }
            }
        };
        actions.put("properties", propertiesAction);
        // close tab
        AbstractAction closeAction = new AbstractAction(TrackerRes.getString("TActions.Action.Close")) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    frame.removeTab(trackerPanel);
                }
            }
        };
        actions.put("close", closeAction);
        // close all tabs
        AbstractAction closeAllAction = new AbstractAction(TrackerRes.getString("TActions.Action.CloseAll")) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    frame.removeAllTabs();
                }
            }
        };
        actions.put("closeAll", closeAllAction);
        // import file
        AbstractAction importAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) {
            public void actionPerformed(ActionEvent e) {
                TrackerIO.importFile(trackerPanel);
            }
        };
        actions.put("import", importAction);
        // import data
        AbstractAction importDataAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportData")) {
            public void actionPerformed(ActionEvent e) {
                getAction("dataTrack", trackerPanel).actionPerformed(e);
            }
        };
        actions.put("importData", importDataAction);
        // save current tab
        icon = new ResizableIcon(new ImageIcon(TActions.class.getResource("/images/save.gif")));
        AbstractAction saveAction = new AbstractAction(TrackerRes.getString("TActions.Action.Save"), icon) {
            public void actionPerformed(ActionEvent e) {
                TrackerIO.save(trackerPanel.getDataFile(), trackerPanel);
                trackerPanel.refreshNotesDialog();
            }
        };
        actions.put("save", saveAction);
        // save tab as
        AbstractAction saveAsAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveAs"), null) {
            public void actionPerformed(ActionEvent e) {
                TrackerIO.save(null, trackerPanel);
                trackerPanel.refreshNotesDialog();
            }
        };
        actions.put("saveAs", saveAsAction);
        // save zip resource
        icon = new ResizableIcon(new ImageIcon(TActions.class.getResource("/images/save_zip.gif")));
        AbstractAction saveZipAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveZip") + "...", icon) {
            public void actionPerformed(ActionEvent e) {
                ExportZipDialog zipDialog = ExportZipDialog.getDialog(trackerPanel);
                zipDialog.setVisible(true);
            }
        };
        actions.put("saveZip", saveZipAction);
        // save tabset as
        AbstractAction saveTabsetAsAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveFrame"), null) {
            public void actionPerformed(ActionEvent e) {
                TrackerIO.saveTabset(null, trackerPanel.getTFrame());
                trackerPanel.refreshNotesDialog();
            }
        };
        actions.put("saveTabsetAs", saveTabsetAsAction);
        // save video
        AbstractAction saveVideoAction = new AbstractAction(TrackerRes.getString("TActions.Action.SaveVideoAs")) {
            public void actionPerformed(ActionEvent e) {
                TrackerIO.saveVideo(null, trackerPanel);
            }
        };
        actions.put("saveVideo", saveVideoAction);
        // export file
        AbstractAction exportAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportTRK")) {
            public void actionPerformed(ActionEvent e) {
                TrackerIO.exportFile(trackerPanel);
            }
        };
        actions.put("export", exportAction);
        // delete track
        AbstractAction deleteTrackAction = new AbstractAction(TrackerRes.getString("TActions.Action.Delete"), null) {
            public void actionPerformed(ActionEvent e) {
                // action command is name of track to delete
                TTrack track = trackerPanel.getTrack(e.getActionCommand());
                if (track != null) track.delete();
            }
        };
        actions.put("deleteTrack", deleteTrackAction);
        AbstractAction configAction = new AbstractAction(TrackerRes.getString("TActions.Action.Config"), null) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                frame.showPrefsDialog();
            }
        };
        actions.put("config", configAction);
        // axesVisible
        icon = new ResizableIcon(new ImageIcon(TActions.class.getResource("/images/axes.gif")));
        AbstractAction axesVisibleAction = new AbstractAction(TrackerRes.getString("TActions.Action.AxesVisible"), icon) {
            public void actionPerformed(ActionEvent e) {
                CoordAxes axes = trackerPanel.getAxes();
                if (axes == null) return;
                boolean visible = !axes.isVisible();
                axes.setVisible(visible);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.hideMouseBox();
                if (visible && trackerPanel.getSelectedTrack() == null)
                    trackerPanel.setSelectedTrack(axes);
                else if (!visible && trackerPanel.getSelectedTrack() == axes)
                    trackerPanel.setSelectedTrack(null);
                trackerPanel.repaint();
            }
        };
        actions.put("axesVisible", axesVisibleAction);
        // videoFilter
        AbstractAction videoFilterAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Video video = trackerPanel.getVideo();
                if (video == null) return;
                FilterStack filterStack = video.getFilterStack();
                Filter filter = null;
                Map<String, Class<? extends Filter>> filterClasses = trackerPanel.getFilters();
                Class<? extends Filter> filterClass = filterClasses.get(e.getActionCommand());
                if (filterClass != null) {
                    try {
                        filter = filterClass.newInstance();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (filter != null) {
                        filterStack.addFilter(filter);
                        filter.setVideoPanel(trackerPanel);
                        JDialog inspector = filter.getInspector();
                        if (inspector != null) {
                            FontSizer.setFonts(inspector, FontSizer.getLevel());
                            inspector.pack();
                            inspector.setVisible(true);
                        }
                    }
                }
                trackerPanel.repaint();
            }
        };
        actions.put("videoFilter", videoFilterAction);
        // about video
        AbstractAction aboutVideoAction = new AbstractAction(
                TrackerRes.getString("TActions.AboutVideo"), null) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    PropertiesDialog dialog = frame.getPropertiesDialog(trackerPanel);
                    if (trackerPanel.getVideo() != null)
                        dialog.tabbedPane.setSelectedIndex(1);
                    dialog.setVisible(true);
                }
            }
        };
        actions.put("aboutVideo", aboutVideoAction);
        // print
        AbstractAction printAction = new AbstractAction(TrackerRes.getString("TActions.Action.Print"), null) {
            public void actionPerformed(ActionEvent e) {
                new TrackerIO.ComponentImage(trackerPanel).print();
            }
        };
        actions.put("print", printAction);
        // exit
        AbstractAction exitAction = new AbstractAction(TrackerRes.getString("TActions.Action.Exit"), null) {
            public void actionPerformed(ActionEvent e) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    for (int i = 0; i < frame.getTabCount(); i++) {
                        // save tabs in try/catch block so always closes
                        try {
                            if (!frame.getTrackerPanel(i).save()) {
                                return;
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                System.exit(0);
            }
        };
        actions.put("exit", exitAction);
        // new point mass
        AbstractAction pointMassAction = new AbstractAction(TrackerRes.getString("PointMass.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                PointMass pointMass = new PointMass();
                pointMass.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(pointMass);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(pointMass);

                if (!Tracker.markAtCurrentFrame) {
                    trackerPanel.getPlayer().setStepNumber(0);
                }
                // offer to add new mass if single cm exists
                ArrayList<CenterOfMass> list = trackerPanel.getDrawables(CenterOfMass.class);
                if (list.size() == 1) {
                    CenterOfMass cm = list.get(0);
                    int result = JOptionPane.showConfirmDialog(
                            trackerPanel,
                            "Add " + pointMass.getName() + " to center of mass \"" +
                                    cm.getName() + "\"?" + newline +
                                    "Note: \"" + cm.getName() + "\" will disappear until  " +
                                    pointMass.getName() + " is marked!",
                            TrackerRes.getString("TActions.Dialog.NewPointMass.Title"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        cm.addMass(pointMass);
                    }
                }
            }
        };
        actions.put("pointMass", pointMassAction);
        // new center of mass
        AbstractAction cmAction = new AbstractAction(TrackerRes.getString("CenterOfMass.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                CenterOfMass cm = new CenterOfMass();
                cm.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(cm);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(cm);
                CenterOfMassInspector cmInspector = cm.getInspector();
                cmInspector.setVisible(true);
            }
        };
        actions.put("cm", cmAction);
        // new vector
        AbstractAction vectorAction = new AbstractAction(
                TrackerRes.getString("Vector.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                org.opensourcephysics.cabrillo.tracker.vector.Vector vec = new Vector();
                vec.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(vec);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(vec);
                if (!Tracker.markAtCurrentFrame) {
                    trackerPanel.getPlayer().setStepNumber(0);
                }
            }
        };
        actions.put("vector", vectorAction);
        // new vector sum
        AbstractAction vectorSumAction = new AbstractAction(
                TrackerRes.getString("VectorSum.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                VectorSum sum = new VectorSum();
                sum.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(sum);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(sum);
                VectorSumInspector sumInspector = sum.getInspector();
                sumInspector.setVisible(true);
            }
        };
        actions.put("vectorSum", vectorSumAction);
        // new offset origin item
        AbstractAction offsetOriginAction = new AbstractAction(TrackerRes.getString("OffsetOrigin.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                OffsetOrigin offset = new OffsetOrigin();
                offset.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(offset);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(offset);
                trackerPanel.getAxes().setVisible(true);
            }
        };
        actions.put("offsetOrigin", offsetOriginAction);
        // new calibration item
        AbstractAction calibrationAction = new AbstractAction(TrackerRes.getString("Calibration.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                Calibration cal = new Calibration();
                cal.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(cal);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(cal);
                trackerPanel.getAxes().setVisible(true);
            }
        };
        actions.put("calibration", calibrationAction);
        // new line profile item
        AbstractAction lineProfileAction = new AbstractAction(TrackerRes.getString("LineProfile.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                TTrack lineProfile = new LineProfile();
                lineProfile.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(lineProfile);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(lineProfile);
            }
        };
        actions.put("lineProfile", lineProfileAction);
        // new RGBRegion item
        AbstractAction rgbRegionAction = new AbstractAction(TrackerRes.getString("RGBRegion.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                TTrack rgb = new RGBRegion();
                rgb.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(rgb);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(rgb);
                if (!Tracker.markAtCurrentFrame) {
                    trackerPanel.getPlayer().setStepNumber(0);
                }
            }
        };
        actions.put("rgbRegion", rgbRegionAction);
        // new analytic particle item
        AbstractAction analyticParticleAction = new AbstractAction(TrackerRes.getString("AnalyticParticle.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                final AnalyticParticle model = new AnalyticParticle();
                model.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(model);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(model);
                FunctionTool inspector = model.getModelBuilder();
                model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
                inspector.setVisible(true);
            }
        };
        actions.put("analyticParticle", analyticParticleAction);
        // new dynamic particle item
        AbstractAction dynamicParticleAction = new AbstractAction(TrackerRes.getString("DynamicParticle.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                DynamicParticle model = new DynamicParticle();
                model.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(model);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(model);
                FunctionTool inspector = model.getModelBuilder();
                model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
                inspector.setVisible(true);
            }
        };
        actions.put("dynamicParticle", dynamicParticleAction);
        // new dynamic particle polar item
        AbstractAction dynamicParticlePolarAction = new AbstractAction(TrackerRes.getString("DynamicParticlePolar.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                DynamicParticle model = new DynamicParticlePolar();
                model.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(model);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(model);
                FunctionTool inspector = model.getModelBuilder();
                model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
                inspector.setVisible(true);
            }
        };
        actions.put("dynamicParticlePolar", dynamicParticlePolarAction);
        // new dynamic system item
        AbstractAction dynamicSystemAction = new AbstractAction(TrackerRes.getString("DynamicSystem.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                DynamicSystem model = new DynamicSystem();
                model.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(model);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(model);
                FunctionTool inspector = model.getModelBuilder();
                model.setStartFrame(trackerPanel.getPlayer().getVideoClip().getStartFrameNumber());
                inspector.setVisible(true);
                DynamicSystemInspector systemInspector = model.getSystemInspector();
                systemInspector.setVisible(true);
            }
        };
        actions.put("dynamicSystem", dynamicSystemAction);
        // new DataTrack from text file item
        AbstractAction dataTrackAction = new AbstractAction(TrackerRes.getString("ParticleDataTrack.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                // choose file and import data
                File[] files = TrackerIO.getChooserFiles("open data");
                if (files == null) {
                    return;
                }
                String filePath = files[0].getAbsolutePath();
                trackerPanel.importData(filePath, null);
            }
        };
        actions.put("dataTrack", dataTrackAction);
        // new DataTrack from ejs item
        AbstractAction dataTrackfromEJSAction = new AbstractAction(TrackerRes.getString("ParticleDataTrack.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                // choose file and get its data
                File[] files = TrackerIO.getChooserFiles("open ejs");
                if (files == null) {
                    return;
                }
                String filePath = files[0].getAbsolutePath();
                String ext = XML.getExtension(filePath);
                if ("jar".equals(ext)) {
                    if (DataTrackTool.isDataSource(filePath)) {
                        String jarName = TrackerRes.getString("TActions.Action.DataTrack.Unsupported.JarFile")
                                + " \"" + XML.getName(filePath) + "\" ";
                        JOptionPane.showMessageDialog(trackerPanel.getTFrame(),
                                jarName + TrackerRes.getString("TActions.Action.DataTrack.Unsupported.Message") + ".",
                                TrackerRes.getString("TActions.Action.DataTrack.Unsupported.Title"),
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    DataTrackTool.launchDataSource(filePath, true);
                }
            }
        };
        actions.put("dataTrackFromEJS", dataTrackfromEJSAction);
        // new (read-only) tape measure
        String s = TrackerRes.getString("TapeMeasure.Name");
        AbstractAction tapeAction = new AbstractAction(s, null) {
            public void actionPerformed(ActionEvent e) {
                TapeMeasure tape = new TapeMeasure();
                tape.setReadOnly(true);
                tape.setDefaultNameAndColor(trackerPanel, " ");
                // place tape at center of viewport
                MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
                Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
                int xpix = rect.x + rect.width / 2;
                int ypix = rect.y + rect.height / 2;
                double x = trackerPanel.pixToX(xpix);
                double y = trackerPanel.pixToY(ypix);
                tape.createStep(0, x - 50, y, x + 50, y); // length 100 image units
                trackerPanel.addTrack(tape);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(tape);
            }
        };
        actions.put("tape", tapeAction);
        // new protractor
        AbstractAction protractorAction = new AbstractAction(
                TrackerRes.getString("Protractor.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                Protractor protractor = new Protractor();
                protractor.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(protractor);
                // place protractor at center of viewport
                MainTView mainView = trackerPanel.getTFrame().getMainView(trackerPanel);
                Rectangle rect = mainView.scrollPane.getViewport().getViewRect();
                int xpix = rect.x + rect.width / 2;
                int ypix = rect.y + rect.height / 2;
                double x = trackerPanel.pixToX(xpix);
                double y = trackerPanel.pixToY(ypix);
                ProtractorStep step = (ProtractorStep) protractor.getStep(0);
                double h = Math.abs(step.end1.y - step.end2.y);
                step.handle.setXY(x, y + h / 2);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(protractor);
            }
        };
        actions.put("protractor", protractorAction);
        // new circle track
        AbstractAction circleFitterAction = new AbstractAction(TrackerRes.getString("CircleFitter.Name"), null) {
            public void actionPerformed(ActionEvent e) {
                CircleFitter track = new CircleFitter();
                track.setDefaultNameAndColor(trackerPanel, " ");
                trackerPanel.addTrack(track);
                trackerPanel.setSelectedPoint(null);
                trackerPanel.selectedSteps.clear();
                trackerPanel.setSelectedTrack(track);
            }
        };
        actions.put("circleFitter", circleFitterAction);
        // clone track action
        AbstractAction cloneTrackAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String name = e.getActionCommand();
                TTrack track = trackerPanel.getTrack(name);
                if (track != null) {
                    // add digit to end of name
                    int n = 1;
                    try {
                        String number = name.substring(name.length() - 1);
                        n = Integer.parseInt(number) + 1;
                        name = name.substring(0, name.length() - 1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // increment digit if necessary
                    Set<String> names = new HashSet<>();
                    for (TTrack next : trackerPanel.getTracks()) {
                        names.add(next.getName());
                    }
                    try {
                        while (names.contains(name + n)) {
                            n++;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // create XMLControl of track, assign new name, and copy to clipboard
                    XMLControl control = new XMLControlElement(track);
                    control.setValue("name", name + n);
                    StringSelection data = new StringSelection(control.toXML());
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(data, data);
                    // now paste
                    TrackerIO.pasteXML(trackerPanel);
                }
            }
        };
        actions.put("cloneTrack", cloneTrackAction);
        // clear filters action
        final AbstractAction clearFiltersAction = new AbstractAction(TrackerRes.getString("TActions.Action.ClearFilters"), null) {
            public void actionPerformed(ActionEvent e) {
                Video video = trackerPanel.getVideo();
                if (video != null) {
                    ArrayList<String> xml = new ArrayList<>();
                    FilterStack stack = video.getFilterStack();
                    for (Filter filter : stack.getFilters()) {
                        xml.add(new XMLControlElement(filter).toXML());
                        PerspectiveTrack track = PerspectiveTrack.filterMap.get(filter);
                        if (track != null) {
                            trackerPanel.removeTrack(track);
                            track.dispose();
                        }
                    }
                    stack.clear();
                    if (e != null) {
                        Undo.postFilterClear(trackerPanel, xml);
                    }
                }
            }
        };
        actions.put("clearFilters", clearFiltersAction);
        // open video
        AbstractAction openVideoAction = new AbstractAction(TrackerRes.getString("TActions.Action.ImportVideo"), null) {
            public void actionPerformed(ActionEvent e) {
                TrackerIO.importVideo(trackerPanel);
            }
        };
        actions.put("openVideo", openVideoAction);
        // close video
        AbstractAction closeVideoAction = new AbstractAction(TrackerRes.getString("TActions.Action.CloseVideo"), null) {
            public void actionPerformed(ActionEvent e) {
                trackerPanel.setVideo(null);
                trackerPanel.repaint();
                trackerPanel.setImageSize(640, 480);
                TMenuBar.getMenuBar(trackerPanel).refresh();
            }
        };
        actions.put("closeVideo", closeVideoAction);
        // reference frame
        AbstractAction refFrameAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JMenuItem item = (JMenuItem) e.getSource();
                trackerPanel.setReferenceFrame(item.getActionCommand());
            }
        };
        actions.put("refFrame", refFrameAction);
        return actions;
    }
}