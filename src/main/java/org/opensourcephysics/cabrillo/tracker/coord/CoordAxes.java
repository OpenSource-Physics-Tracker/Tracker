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
package org.opensourcephysics.cabrillo.tracker.coord;

import org.opensourcephysics.cabrillo.tracker.*;
import org.opensourcephysics.cabrillo.tracker.component.ReferenceFrame;
import org.opensourcephysics.cabrillo.tracker.component.TButton;
import org.opensourcephysics.cabrillo.tracker.component.TTrack;
import org.opensourcephysics.cabrillo.tracker.component.TTrackBar;
import org.opensourcephysics.cabrillo.tracker.footprint.Footprint;
import org.opensourcephysics.cabrillo.tracker.point.PointShapeFootprint;
import org.opensourcephysics.cabrillo.tracker.step.Step;
import org.opensourcephysics.cabrillo.tracker.tracker.Tracker;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerPanel;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerRes;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;
import org.opensourcephysics.display.DrawingPanel;
import org.opensourcephysics.display.Interactive;
import org.opensourcephysics.display.ResizableIcon;
import org.opensourcephysics.media.core.ImageCoordSystem;
import org.opensourcephysics.media.core.NumberField;
import org.opensourcephysics.media.core.TPoint;
import org.opensourcephysics.tools.FontSizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A CoordAxes displays and controls the image coordinate system of
 * a specified tracker panel.
 *
 * @author Douglas Brown
 */
public class CoordAxes extends TTrack {

    public static Icon gridOptionsIcon;
    public static String[] dataVariables;
    public static String[] formatVariables; // used by NumberFormatSetter
    public static Map<String, ArrayList<String>> formatMap;
    public static Map<String, String> formatDescriptionMap;

    static {
        dataVariables = new String[]{"x", "y", Tracker.THETA}; //$NON-NLS-2$ 
        formatVariables = new String[]{"pixel", Tracker.THETA};

        // assemble format map
        formatMap = new HashMap<>();
        ArrayList<String> list = new ArrayList<>();
        list.add(dataVariables[0]);
        list.add(dataVariables[1]);
        formatMap.put(formatVariables[0], list);

        list = new ArrayList<>();
        list.add(dataVariables[2]);
        formatMap.put(formatVariables[1], list);

        // assemble format description map
        formatDescriptionMap = new HashMap<>();
        formatDescriptionMap.put(formatVariables[0], TrackerRes.getString("CoordAxes.Origin.Label"));
        formatDescriptionMap.put(formatVariables[1], TrackerRes.getString("CoordAxes.Label.Angle"));

    }

    public boolean notyetShown = true;
    protected JLabel originLabel;
    protected WorldGrid grid;
    protected JCheckBox gridCheckbox;
    protected TButton gridButton;
    protected Component gridSeparator;
    protected boolean gridVisible;

    static {
        gridOptionsIcon = new ImageIcon(CoordAxes.class.getResource("/images/restore.gif"));
        gridOptionsIcon = new ResizableIcon(gridOptionsIcon);
    }

    /**
     * Constructs a CoordAxes.
     */
    public CoordAxes() {
        defaultColors = new Color[]{new Color(200, 0, 200)};
        setName(TrackerRes.getString("CoordAxes.New.Name"));
        // set up footprint choices and color
        setFootprints(new Footprint[]
                {PointShapeFootprint.getFootprint("Footprint.BoldSimpleAxes"),
                        PointShapeFootprint.getFootprint("Footprint.SimpleAxes")});
        defaultFootprint = getFootprint();
        setColor(defaultColors[0]);
        setViewable(false); // views ignore this track
        // set initial hint
        partName = TrackerRes.getString("TTrack.Selected.Hint");
        hint = TrackerRes.getString("CoordAxes.Hint");
        // initialize the step array
        // step 0 is the only step needed
        Step step = new CoordAxesStep(this, 0);
        step.setFootprint(getFootprint());
        steps.setStep(0, step);
        // configure angle field components
        angleField.addActionListener(e -> {
            if (trackerPanel == null) return;
            double theta = angleField.getValue();
            // get the origin and handle of the current step
            int n = trackerPanel.getFrameNumber();
            CoordAxesStep step1 = (CoordAxesStep) CoordAxes.this.getStep(n);
            TPoint origin = step1.getOrigin();
            TPoint handle = step1.getHandle();
            // move the handle to the new angle at same distance from origin
            double d = origin.distance(handle);
            double x = origin.getX() + d * Math.cos(theta);
            double y = origin.getY() - d * Math.sin(theta);
            handle.setXY(x, y);
            angleField.setValue(trackerPanel.getCoords().getAngle(n));
            angleField.requestFocusInWindow();
        });
        angleField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (trackerPanel == null) return;
                double theta = angleField.getValue();
                // get the origin and handle of the current step
                int n = trackerPanel.getFrameNumber();
                CoordAxesStep step = (CoordAxesStep) CoordAxes.this.getStep(n);
                TPoint origin = step.getOrigin();
                TPoint handle = step.getHandle();
                // move the handle to the new angle at same distance from origin
                double d = origin.distance(handle);
                double x = origin.getX() + d * Math.cos(theta);
                double y = origin.getY() - d * Math.sin(theta);
                handle.setXY(x, y);
                angleField.setValue(trackerPanel.getCoords().getAngle(n));
            }
        });
        originLabel = new JLabel();
        final Action setOriginAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (trackerPanel == null) return;
                double x = xField.getValue();
                double y = yField.getValue();
                ImageCoordSystem coords = trackerPanel.getCoords();
                int n = trackerPanel.getFrameNumber();
                coords.setOriginXY(n, x, y);
                xField.setValue(coords.getOriginX(n));
                yField.setValue(coords.getOriginY(n));
                CoordAxesStep step = (CoordAxesStep) CoordAxes.this.getStep(n);
                TPoint handle = step.getHandle();
                if (handle == trackerPanel.getSelectedPoint()) {
                    trackerPanel.setSelectedPoint(null);
                    trackerPanel.selectedSteps.clear();
                }
            }
        };

        xField.addActionListener(setOriginAction);
        yField.addActionListener(setOriginAction);
        xField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                setOriginAction.actionPerformed(null);
            }
        });
        yField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                setOriginAction.actionPerformed(null);
            }
        });

        // grid items
        grid = new WorldGrid();
        grid.setVisible(gridVisible);

        gridSeparator = Box.createRigidArea(new Dimension(4, 4));
        gridCheckbox = new JCheckBox();
        gridCheckbox.setBorder(BorderFactory.createEmptyBorder());
        gridCheckbox.setOpaque(false);
        gridCheckbox.addActionListener(e -> setGridVisible(gridCheckbox.isSelected()));

        gridButton = new TButton(gridOptionsIcon) {

            public Dimension getMaximumSize() {
                Dimension dim = super.getMaximumSize();
                dim.height = TTrackBar.getTrackbar(trackerPanel).toolbarComponentHeight;
                return dim;
            }

            @Override
            protected JPopupMenu getPopup() {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem colorItem = new JMenuItem(TrackerRes.getString("CoordAxes.MenuItem.GridColor"));
                colorItem.addActionListener(e -> {
                    // show the grid if not visible
                    if (!grid.isVisible()) {
                        gridCheckbox.doClick(0);
                    }

                    Color color = grid.getColor();
                    Color newColor = chooseColor(color, TrackerRes.getString("CoordAxes.Dialog.GridColor.Title"));
                    if (newColor != color) {
                        grid.setColor(newColor);
                        for (TrackerPanel next : panels) {
                            next.repaint();
                        }
                    }
                });
                popup.add(colorItem);
                JMenuItem transparencyItem = new JMenuItem(TrackerRes.getString("CoordAxes.MenuItem.GridOpacity"));
                transparencyItem.addActionListener(e -> {
                    // show the grid if not visible
                    if (!grid.isVisible()) {
                        gridCheckbox.doClick(0);
                    }

                    // show a dialog with a transparency slider
                    int alpha = grid.getAlpha();
                    final JSlider slider = new JSlider(0, 255, alpha);
                    slider.setMaximum(255);
                    slider.setMinimum(0);
                    slider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
                    slider.addChangeListener(e1 -> {
                        grid.setAlpha(slider.getValue());
                        for (TrackerPanel next : panels) {
                            next.repaint();
                        }
                    });

                    int response = JOptionPane.showConfirmDialog(trackerPanel, slider,
                            TrackerRes.getString("CoordAxes.Dialog.GridOpacity.Title"),
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (response == JOptionPane.CANCEL_OPTION) {
                        grid.setAlpha(alpha);
                        for (TrackerPanel next : panels) {
                            next.repaint();
                        }
                    }
                });
                popup.add(transparencyItem);
                FontSizer.setFonts(popup, FontSizer.getLevel());
                return popup;
            }

        };
    }

    /**
     * Gets the origin.
     *
     * @return the current origin
     */
    public TPoint getOrigin() {
        return ((CoordAxesStep) getStep(0)).getOrigin();
    }

    /**
     * Overrides TTrack isLocked method.
     *
     * @return <code>true</code> if this is locked
     */
    public boolean isLocked() {
        boolean locked = super.isLocked();
        if (trackerPanel != null) {
            locked = locked || trackerPanel.getCoords().isLocked();
        }
        return locked;
    }

    /**
     * Overrides TTrack setVisible method to change notyetShown flag.
     *
     * @param visible <code>true</code> to show this track
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            notyetShown = false;
            if (grid != null) grid.setVisible(gridVisible);
        } else if (grid != null) {
            grid.setVisible(false);
        }
    }

    /**
     * Sets the grid visibility.
     *
     * @param visible <code>true</code> to show the grid
     */
    public void setGridVisible(boolean visible) {
        if (gridVisible == visible) return;
        gridVisible = visible;
        grid.setVisible(gridVisible);
        gridCheckbox.setSelected(gridVisible);
        for (TrackerPanel next : panels) {
            next.repaint();
        }
    }

    /**
     * Overrides TTrack setTrailVisible method to keep trails hidden.
     *
     * @param visible ignored
     */
    public void setTrailVisible(boolean visible) {
    }

    /**
     * Mimics step creation by setting the origin position.
     *
     * @param n the frame number
     * @param x the x coordinate in image space
     * @param y the y coordinate in image space
     * @return the step
     */
    public Step createStep(int n, double x, double y) {
//    Step step = steps.getStep(n);
        Step step = getStep(0);
        if (trackerPanel.getSelectedPoint() instanceof CoordAxesStep.Handle) {
            ((CoordAxesStep) step).getHandle().setXY(x, y);

        } else ((CoordAxesStep) step).getOrigin().setXY(x, y);
        return step;
    }

    /**
     * Overrides TTrack deleteStep method to prevent deletion.
     *
     * @param n the frame number
     * @return the deleted step
     */
    public Step deleteStep(int n) {
        return null;
    }

    /**
     * Overrides TTrack getStep method. Always return step 0.
     *
     * @param n the frame number (ignored)
     * @return step 0
     */
    public Step getStep(int n) {
        Step step = steps.getStep(0);
        // always erase since step position/shape may change
        // without calls to TPoint setXY method
        step.erase();
        return step;
    }

    /**
     * Gets the length of the steps created by this track.
     *
     * @return the footprint length
     */
    public int getStepLength() {
        return CoordAxesStep.getLength();
    }

    /**
     * Determines if at least one point in this track is autotrackable.
     *
     * @return true if autotrackable
     */
    public boolean isAutoTrackable() {
        return true;
    }

    /**
     * Determines if the given point index is autotrackable.
     *
     * @param pointIndex the points[] index
     * @return true if autotrackable
     */
    public boolean isAutoTrackable(int pointIndex) {
        return true;
//  	return pointIndex==0; // origin only
    }

    /**
     * Returns a description of the point at a given index. Used by AutoTracker.
     *
     * @param pointIndex the points[] index
     * @return the description
     */
    public String getTargetDescription(int pointIndex) {
        if (pointIndex == 0) {
            return TrackerRes.getString("CoordAxes.Origin.Name");
        }
        return TrackerRes.getString("CoordAxes.Handle.Name");
//  	return null;
    }

    @Override
    public void setTrackerPanel(TrackerPanel panel) {
        super.setTrackerPanel(panel);
        if (trackerPanel != null) {
            trackerPanel.addDrawable(grid);
        }
    }

    /**
     * Used by autoTracker to mark a step at a match target position.
     *
     * @param n the frame number
     * @param x the x target coordinate in image space
     * @param y the y target coordinate in image space
     * @return the TPoint that was automarked
     */
    public TPoint autoMarkAt(int n, double x, double y) {
        ImageCoordSystem coords = trackerPanel.getCoords();
        if (getTargetIndex() == 0) { // origin
            if (coords.isFixedOrigin()) {
                coords.setFixedOrigin(false);
            }
            TPoint origin = getOrigin();
            origin.setXY(x, y);
        } else { // handle
            if (coords.isFixedAngle()) {
                coords.setFixedAngle(false);
            }
            TPoint handle = ((CoordAxesStep) getStep(0)).getHandle();
            handle.setXY(x, y);
        }
        firePropertyChange("step", null, n);
        return getMarkedPoint(n, getTargetIndex());
    }

    /**
     * Used by autoTracker to get the marked point for a given frame and index.
     * Overrides TTrack method.
     *
     * @param n     the frame number
     * @param index the index
     * @return a TPoint
     */
    public TPoint getMarkedPoint(final int n, int index) {
        if (index == 0) {
            return new OriginPoint(n);
        }
        TPoint handle = ((CoordAxesStep) getStep(0)).getHandle();
        return new AnglePoint(handle.getX(), handle.getY(), n);
    }

    /**
     * Gets the length of the footprints required by this track.
     *
     * @return the footprint length
     */
    public int getFootprintLength() {
        return 1;
    }

    /**
     * Implements findInteractive method.
     *
     * @param panel the drawing panel
     * @param xpix  the x pixel position on the panel
     * @param ypix  the y pixel position on the panel
     * @return the first step that is hit
     */
    public Interactive findInteractive(
            DrawingPanel panel, int xpix, int ypix) {
        if (!(panel instanceof TrackerPanel) ||
                !isVisible() ||
                !isEnabled()) return null;
        ImageCoordSystem coords = ((TrackerPanel) panel).getCoords();
        if (coords instanceof ReferenceFrame) return null;
        // only look at step 0 since getStep(n) returns 0 for every n
        Interactive ia = getStep(0).findInteractive(trackerPanel, xpix, ypix);
        if (ia == null) {
            partName = TrackerRes.getString("TTrack.Selected.Hint");
            hint = TrackerRes.getString("CoordAxes.Hint");
            return null;
        }
        if (ia instanceof CoordAxesStep.Handle) {
            partName = TrackerRes.getString("CoordAxes.Handle.Name");
            hint = TrackerRes.getString("CoordAxes.Handle.Hint");
        } else {
            partName = TrackerRes.getString("CoordAxes.Origin.Name");
            hint = TrackerRes.getString("CoordAxes.Origin.Hint");
        }
        return ia;
    }

    /**
     * Overrides TTrack getMenu method.
     *
     * @param trackerPanel the tracker panel
     * @return a menu
     */
    public JMenu getMenu(TrackerPanel trackerPanel) {
        JMenu menu = super.getMenu(trackerPanel);
        menu.remove(deleteTrackItem);
        if (menu.getItemCount() > 0)
            menu.remove(menu.getItemCount() - 1); // remove separator
        lockedItem.setEnabled(!trackerPanel.getCoords().isLocked());
        return menu;
    }

    /**
     * Overrides TTrack getToolbarTrackComponents method.
     *
     * @param trackerPanel the tracker panel
     * @return a list of components
     */
    public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
        ArrayList<Component> list = super.getToolbarTrackComponents(trackerPanel);
        int n = trackerPanel.getFrameNumber();
        ImageCoordSystem coords = trackerPanel.getCoords();
        originLabel.setText(TrackerRes.getString("CoordAxes.Origin.Label"));
        xField.setToolTipText(TrackerRes.getString("CoordAxes.Origin.Field.Tooltip"));
        yField.setToolTipText(TrackerRes.getString("CoordAxes.Origin.Field.Tooltip"));
        gridCheckbox.setText(TrackerRes.getString("CoordAxes.Checkbox.Grid"));
        gridCheckbox.setToolTipText(TrackerRes.getString("CoordAxes.Checkbox.Grid.Tooltip"));
        gridButton.setToolTipText(TrackerRes.getString("CoordAxes.Button.Grid.Tooltip"));
        list.add(gridSeparator);
        list.add(gridCheckbox);
        list.add(gridButton);
        list.add(magSeparator);
        list.add(originLabel);
        list.add(xSeparator);
        list.add(xLabel);
        list.add(xField);
        list.add(ySeparator);
        list.add(yLabel);
        list.add(yField);
        xField.setValue(coords.getOriginX(n));
        yField.setValue(coords.getOriginY(n));

        angleLabel.setText(TrackerRes.getString("CoordAxes.Label.Angle"));
        list.add(stepSeparator);
        list.add(angleLabel);
        list.add(angleField);
        // put coords angle into angle field
        angleField.setValue(coords.getAngle(n));

        xField.setEnabled(!isLocked());
        yField.setEnabled(!isLocked());
        angleField.setEnabled(!isLocked());
        return list;
    }

    /**
     * Responds to property change events. This listens for the following
     * events: "stepnumber" & "image" from TrackerPanel.
     *
     * @param e the property change event
     */
    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if (name.equals("stepnumber")) {
            int n = trackerPanel.getFrameNumber();
            ImageCoordSystem coords = trackerPanel.getCoords();
            angleField.setValue(coords.getAngle(n));
            xField.setValue(coords.getOriginX(n));
            yField.setValue(coords.getOriginY(n));
        } else super.propertyChange(e);
    }

    @Override
    public void setFontLevel(int level) {
        super.setFontLevel(level);
        Object[] objectsToSize = new Object[]
                {originLabel, gridCheckbox};
        FontSizer.setFonts(objectsToSize, level);
    }

    @Override
    public Map<String, NumberField[]> getNumberFields() {
        numberFields.clear();
        numberFields.put(dataVariables[0], new NumberField[]{xField});
        numberFields.put(dataVariables[1], new NumberField[]{yField});
        numberFields.put(dataVariables[2], new NumberField[]{angleField});
        return numberFields;
    }

    /**
     * A TPoint used by autotracker to check for manually marked angles.
     */
    public class AnglePoint extends TPoint {
        int frameNum;

        public AnglePoint(double x, double y, int n) {
            super(x, y);
            frameNum = n;
        }

        public double getAngle() {
            TPoint origin = getOrigin();
            return -origin.angle(this);
        }
    }

    /**
     * A TPoint used by autotracker to check for manually marked origins.
     */
    protected class OriginPoint extends TPoint {

        int frameNum;

        OriginPoint(int n) {
            frameNum = n;
        }

        public double getX() {
            ImageCoordSystem coords = trackerPanel.getCoords();
            return coords.getOriginX(frameNum);
        }

        public double getY() {
            ImageCoordSystem coords = trackerPanel.getCoords();
            return coords.getOriginY(frameNum);
        }
    }

    /**
     * Returns an ObjectLoader to save and load data for this class.
     *
     * @return the object loader
     */
    public static XML.ObjectLoader getLoader() {
        return new Loader();
    }

    /**
     * A class to save and load data for this class.
     */
    static class Loader implements XML.ObjectLoader {

        /**
         * Saves an object's data to an XMLControl.
         *
         * @param control the control to save to
         * @param obj     the object to save
         */
        public void saveObject(XMLControl control, Object obj) {
            // save track data
            XML.getLoader(TTrack.class).saveObject(control, obj);
            CoordAxes axes = (CoordAxes) obj;
            if (axes.gridVisible) {
                control.setValue("grid_visible", true);
            }
            if (axes.grid.isCustom()) {
                control.setValue("grid_alpha", axes.grid.getAlpha());
                control.setValue("grid_RGB", axes.grid.getColor().getRGB());
            }
        }

        /**
         * Creates a new object.
         *
         * @param control the XMLControl with the object data
         * @return the newly created object
         */
        public Object createObject(XMLControl control) {
            return new CoordAxes();
        }

        /**
         * Loads an object with data from an XMLControl.
         *
         * @param control the control
         * @param obj     the object
         * @return the loaded object
         */
        public Object loadObject(XMLControl control, Object obj) {
            // load track data
            XML.getLoader(TTrack.class).loadObject(control, obj);
            CoordAxes axes = (CoordAxes) obj;
            axes.notyetShown = false;

            if (control.getPropertyNames().contains("grid_visible")) {
                axes.setGridVisible(control.getBoolean("grid_visible"));
            }
            if (control.getPropertyNames().contains("grid_alpha")) {
                axes.grid.setAlpha(control.getInt("grid_alpha"));
            }
            if (control.getPropertyNames().contains("grid_RGB")) {
                Color color = new Color(control.getInt("grid_RGB"));
                axes.grid.setColor(color);
            }
            return obj;
        }
    }

}

