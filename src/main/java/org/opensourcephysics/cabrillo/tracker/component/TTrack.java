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
package org.opensourcephysics.cabrillo.tracker.component;

import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.event.*;

import org.opensourcephysics.cabrillo.tracker.step.Step;
import org.opensourcephysics.cabrillo.tracker.step.StepArray;
import org.opensourcephysics.cabrillo.tracker.Undo;
import org.opensourcephysics.cabrillo.tracker.auto.AutoTracker;
import org.opensourcephysics.cabrillo.tracker.calibration.Calibration;
import org.opensourcephysics.cabrillo.tracker.circle.CircleFitter;
import org.opensourcephysics.cabrillo.tracker.circle.CircleFootprint;
import org.opensourcephysics.cabrillo.tracker.coord.CoordAxes;
import org.opensourcephysics.cabrillo.tracker.dialog.NumberFormatDialog;
import org.opensourcephysics.cabrillo.tracker.dialog.TrackNameDialog;
import org.opensourcephysics.cabrillo.tracker.dialog.UnitsDialog;
import org.opensourcephysics.cabrillo.tracker.footprint.Footprint;
import org.opensourcephysics.cabrillo.tracker.line.LineProfile;
import org.opensourcephysics.cabrillo.tracker.offset.OffsetOrigin;
import org.opensourcephysics.cabrillo.tracker.perspective.PerspectiveTrack;
import org.opensourcephysics.cabrillo.tracker.point.PointMass;
import org.opensourcephysics.cabrillo.tracker.protractor.Protractor;
import org.opensourcephysics.cabrillo.tracker.rgb.RGBRegion;
import org.opensourcephysics.cabrillo.tracker.tape.TapeMeasure;
import org.opensourcephysics.cabrillo.tracker.track.TrackProperties;
import org.opensourcephysics.cabrillo.tracker.tracker.Tracker;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerPanel;
import org.opensourcephysics.cabrillo.tracker.tracker.TrackerRes;
import org.opensourcephysics.cabrillo.tracker.vector.Vector;
import org.opensourcephysics.display.*;
import org.opensourcephysics.media.core.*;
import org.opensourcephysics.tools.DataTool;
import org.opensourcephysics.tools.FontSizer;
import org.opensourcephysics.controls.*;

import org.opensourcephysics.cabrillo.tracker.auto.AutoTrackerCore.KeyFrame;


/**
 * A TTrack draws a series of visible Steps on a TrackerPanel.
 * This is an abstract class that cannot be instantiated directly.
 *
 * @author Douglas Brown
 */
public abstract class TTrack implements Interactive, Trackable, PropertyChangeListener {

    protected static String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; //$NON-NLS-1$
    protected static JDialog skippedStepWarningDialog;
    protected static JTextPane skippedStepWarningTextpane;
    protected static JCheckBox skippedStepWarningCheckbox;
    protected static JButton closeButton;
    protected static boolean skippedStepWarningOn = true;
    public static TrackNameDialog nameDialog;
    protected static int nextID = 1;
    public static HashMap<Integer, TTrack> activeTracks = new HashMap<>();
    protected static FontRenderContext frc
            = new FontRenderContext(null,   // no AffineTransform
            false,  // no antialiasing
            false); // no fractional metrics

    // instance fields
    protected String name = TrackerRes.getString("TTrack.Name.None"); //$NON-NLS-1$
    protected String description = ""; //$NON-NLS-1$
    protected boolean visible = true;
    protected boolean trailVisible = false;
    protected int trailLength; // controls trail length
    public boolean locked = false;
    protected boolean enabled = true;
    protected boolean viewable = true; // determines whether Views include this track
    protected Footprint[] footprints = new Footprint[0];
    protected Footprint footprint;
    protected Footprint defaultFootprint;
    protected Color[] defaultColors = new Color[]{Color.red};
    public StepArray steps = new StepArray();
    protected Collection<TrackerPanel> panels = new HashSet<>();
    public PropertyChangeSupport support;
    protected HashMap<String, Object> properties = new HashMap<>();
    public DatasetManager data;
    protected HashMap<TrackerPanel, double[]> worldBounds = new HashMap<>();
    protected Point2D point = new Point2D.Double();
    protected ArrayList<Component> toolbarTrackComponents = new ArrayList<>();
    protected ArrayList<Component> toolbarPointComponents = new ArrayList<>();
    protected TTrackTextLineLabel xLabel, yLabel, magLabel, angleLabel;
    protected JLabel tLabel, stepLabel, tValueLabel, stepValueLabel;
    public NumberField tField, xField, yField, magField;
    public DecimalField angleField;
    protected NumberField[] positionFields;
    protected Map<String, NumberField[]> numberFields = new TreeMap<>();
    protected Border fieldBorder;
    protected Component tSeparator, xSeparator, ySeparator, magSeparator,
            angleSeparator, stepSeparator;
    protected JMenu menu;
    protected boolean autoAdvance;
    public boolean markByDefault = false, isMarking = false;
    protected JCheckBoxMenuItem visibleItem;
    protected JCheckBoxMenuItem trailVisibleItem;
    protected JCheckBoxMenuItem markByDefaultItem;
    protected JCheckBoxMenuItem autoAdvanceItem;
    protected JCheckBoxMenuItem lockedItem;
    protected JMenuItem nameItem;
    protected JMenuItem colorItem;
    protected JMenu footprintMenu;
    protected ActionListener footprintListener, circleFootprintListener;
    protected JMenuItem deleteTrackItem, deleteStepItem, clearStepsItem;
    protected JMenuItem descriptionItem;
    protected JMenuItem dataBuilderItem;
    protected JSpinner xSpinner, ySpinner;
    public TrackerPanel trackerPanel;
    protected XMLProperty dataProp;
    protected Object[][] constantsLoadedFromXML;
    protected String[] dataDescriptions;
    public boolean dataValid; // true if data is valid
    protected boolean refreshDataLater;
    protected int[] preferredColumnOrder;
    protected ArrayList<Integer> dataFrames = new ArrayList<>();
    protected String partName, hint;
    protected int stepSizeWhenFirstMarked;
    public TreeSet<Integer> keyFrames = new TreeSet<>();
    // for autotracking
    public boolean autoTrackerMarking;
    protected int targetIndex;
    // attached tracks--used by AttachmentDialog with TapeMeasure, Protractor and CircleFitter tracks
    public TTrack[] attachments;
    protected String[] attachmentNames; // used when loading attachments
    // user-editable text columns shown in DataTable view
    public Map<String, String[]> textColumnEntries = new TreeMap<>();
    public ArrayList<String> textColumnNames = new ArrayList<>();
    // mouse listener for number fields
    protected MouseAdapter formatMouseListener, formatAngleMouseListener;
    public String[] customNumberFormats;
    private final int ID; // unique ID number

    // For autoskipping while autotracking
    public boolean skippedStepWarningSuppress = false;


    /**
     * Constructs a TTrack.
     */
    protected TTrack() {
        ID = nextID++;
        support = new SwingPropertyChangeSupport(this);
        // create toolbar components
        stepLabel = new JLabel();
        stepLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        stepValueLabel = new JLabel();
        stepValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        tLabel = new JLabel();
        tLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
        tValueLabel = new JLabel();
        tValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 2));
        tField = new TrackDecimalField(3) {
            public void setValue(double value) {
                super.setValue(value);
                tValueLabel.setText("(" + tField.getText() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        tField.setUnits("s"); //$NON-NLS-1$
        // create spinners
        SpinnerModel model = new SpinnerNumberModel(0, -100, 100, 0.1);
        xSpinner = new JSpinner(model);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(xSpinner, "0.00"); //$NON-NLS-1$
        editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
        xSpinner.setEditor(editor);
        model = new SpinnerNumberModel(0, -100, 100, 0.1);
        ySpinner = new JSpinner(model);
        editor = new JSpinner.NumberEditor(ySpinner, "0.00"); //$NON-NLS-1$
        editor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
        ySpinner.setEditor(editor);
        // create mouse listeners for fields
        formatMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (OSPRuntime.isPopupTrigger(e)) {
                    NumberField field = (NumberField) e.getSource();
                    String[] fieldName = null;
                    boolean hasUnits = false;
                    for (String name : getNumberFields().keySet()) {
                        if (numberFields.get(name)[0] == field) {
                            fieldName = new String[]{name};
                            String s = NumberFormatDialog.getVariableDimensions(TTrack.this.getClass(), name);
                            if (s != null) {
                                hasUnits = s.contains("L") || s.contains("M") || s.contains("T"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            }
                            break;
                        }
                    }
                    JPopupMenu popup = new JPopupMenu();
                    if (trackerPanel.isEnabled("number.formats") || trackerPanel.isEnabled("number.units")) { //$NON-NLS-1$ //$NON-NLS-2$
                        JMenu numberMenu = new JMenu(TrackerRes.getString("Popup.Menu.Numbers")); //$NON-NLS-1$
                        popup.add(numberMenu);
                        if (trackerPanel.isEnabled("number.formats")) { //$NON-NLS-1$
                            JMenuItem item = new JMenuItem();
                            final String[] selected = fieldName;
                            item.addActionListener(e14 -> {
                                NumberFormatDialog dialog = NumberFormatDialog.getNumberFormatDialog(trackerPanel, TTrack.this, selected);
                                dialog.setVisible(true);
                            });
                            item.setText(TrackerRes.getString("Popup.MenuItem.Formats") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
                            numberMenu.add(item);
                        }

                        if (hasUnits && trackerPanel.isEnabled("number.units")) { //$NON-NLS-1$
                            JMenuItem item = new JMenuItem();
                            item.addActionListener(e12 -> {
                                UnitsDialog dialog = trackerPanel.getUnitsDialog();
                                dialog.setVisible(true);
                            });
                            item.setText(TrackerRes.getString("Popup.MenuItem.Units") + "..."); //$NON-NLS-1$ //$NON-NLS-2$
                            numberMenu.add(item);
                        }
                    }
                    boolean hasLengthUnit = trackerPanel.lengthUnit != null;
                    boolean hasMassUnit = trackerPanel.massUnit != null;
                    if (hasLengthUnit && hasMassUnit) {
                        JMenuItem item = new JMenuItem();
                        final boolean vis = trackerPanel.isUnitsVisible();
                        item.addActionListener(e15 -> trackerPanel.setUnitsVisible(!vis));
                        item.setText(vis ? TrackerRes.getString("TTrack.MenuItem.HideUnits") : //$NON-NLS-1$
                                TrackerRes.getString("TTrack.MenuItem.ShowUnits")); //$NON-NLS-1$
                        if (popup.getComponentCount() > 0) popup.addSeparator();
                        popup.add(item);
                    }

                    FontSizer.setFonts(popup, FontSizer.getLevel());
                    popup.show(field, 0, field.getHeight());
                }
            }
        };
        formatAngleMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e == null || OSPRuntime.isPopupTrigger(e)) {
                    NumberField field = e == null ? angleField : (NumberField) e.getSource();
                    String fieldName = null;
                    for (String name : getNumberFields().keySet()) {
                        if (e != null && numberFields.get(name)[0] == e.getSource()) {
                            fieldName = name;
                            break;
                        }
                    }
                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem item = new JMenuItem();
                    final boolean radians = field.getConversionFactor() == 1;
                    item.addActionListener(e13 -> {
                        TFrame frame = trackerPanel.getTFrame();
                        frame.setAnglesInRadians(!radians);
                    });
                    item.setText(radians ?
                            TrackerRes.getString("TTrack.AngleField.Popup.Degrees") : //$NON-NLS-1$
                            TrackerRes.getString("TTrack.AngleField.Popup.Radians")); //$NON-NLS-1$
                    popup.add(item);
                    popup.addSeparator();

                    if (trackerPanel.isEnabled("number.formats")) { //$NON-NLS-1$
                        item = new JMenuItem();
                        final String[] selected = new String[]{fieldName};
                        item.addActionListener(e1 -> {
                            NumberFormatDialog dialog = NumberFormatDialog.getNumberFormatDialog(trackerPanel, TTrack.this, selected);
                            dialog.setVisible(true);
                        });
                        item.setText(TrackerRes.getString("TTrack.MenuItem.NumberFormat")); //$NON-NLS-1$
                        popup.add(item);
                    }

                    FontSizer.setFonts(popup, FontSizer.getLevel());
                    popup.show(field, 0, angleField.getHeight());
                }
            }
        };

        // create labels and fields
        xLabel = new TTrackTextLineLabel();
        xField = new TrackNumberField();
        yLabel = new TTrackTextLineLabel();
        yField = new TrackNumberField();
        magLabel = new TTrackTextLineLabel();
        magField = new TrackNumberField();
        magField.setMinValue(0);
        xField.addMouseListener(formatMouseListener);
        yField.addMouseListener(formatMouseListener);
        magField.addMouseListener(formatMouseListener);
        angleLabel = new TTrackTextLineLabel();
        angleField = new TrackDecimalField(1);
        angleField.addMouseListener(formatAngleMouseListener);
        Border empty = BorderFactory.createEmptyBorder(0, 3, 0, 3);
        Color grey = new Color(102, 102, 102);
        Border etch = BorderFactory.createEtchedBorder(Color.white, grey);
        fieldBorder = BorderFactory.createCompoundBorder(etch, empty);
        tField.setBorder(fieldBorder);
        xField.setBorder(fieldBorder);
        yField.setBorder(fieldBorder);
        magField.setBorder(fieldBorder);
        angleField.setBorder(fieldBorder);
        positionFields = new NumberField[]{xField, yField, magField, angleField};
        stepSeparator = Box.createRigidArea(new Dimension(4, 4));
        tSeparator = Box.createRigidArea(new Dimension(6, 4));
        xSeparator = Box.createRigidArea(new Dimension(6, 4));
        ySeparator = Box.createRigidArea(new Dimension(6, 4));
        magSeparator = Box.createRigidArea(new Dimension(6, 4));
        angleSeparator = Box.createRigidArea(new Dimension(6, 4));
        // create menu items
        visibleItem = new JCheckBoxMenuItem();
        trailVisibleItem = new JCheckBoxMenuItem();
        autoAdvanceItem = new JCheckBoxMenuItem();
        markByDefaultItem = new JCheckBoxMenuItem();
        lockedItem = new JCheckBoxMenuItem();
        deleteTrackItem = new JMenuItem();
        deleteStepItem = new JMenuItem();
        clearStepsItem = new JMenuItem();
        colorItem = new JMenuItem();
        colorItem.addActionListener(e -> {
            Color color = getColor();
            Color newColor = chooseColor(color, TrackerRes.getString("TTrack.Dialog.Color.Title")); //$NON-NLS-1$
            if (newColor != color) {
                XMLControl control = new XMLControlElement(new TrackProperties(TTrack.this));
                setColor(newColor);
                Undo.postTrackDisplayEdit(TTrack.this, control);
            }
        });

        nameItem = new JMenuItem();
        nameItem.addActionListener(e -> getNameDialog(TTrack.this).setVisible(true));
        footprintMenu = new JMenu();
        descriptionItem = new JMenuItem();
        descriptionItem.addActionListener(e -> {
            if (trackerPanel != null) {
                TFrame frame = trackerPanel.getTFrame();
                if (frame != null) {
                    if (frame.notesDialog.isVisible()) {
                        frame.notesDialog.setVisible(true);
                    } else frame.getToolBar(trackerPanel).notesButton.doClick();
                }
            }
        });
        dataBuilderItem = new JMenuItem();
        dataBuilderItem.addActionListener(e -> {
            if (trackerPanel != null) {
                trackerPanel.getDataBuilder().setSelectedPanel(getName());
                trackerPanel.getDataBuilder().setVisible(true);
            }
        });
        visibleItem.addItemListener(e -> {
            setVisible(visibleItem.isSelected());
            TTrack.this.repaint();
        });
        trailVisibleItem.addItemListener(e -> {
            setTrailVisible(trailVisibleItem.isSelected());
            if (!TTrack.this.isTrailVisible()) {
                // clear selected point on panels if nec
                Iterator<TrackerPanel> it = panels.iterator();
                TrackerPanel panel;
                while (it.hasNext()) {
                    panel = it.next();
                    Step step = panel.getSelectedStep();
                    if (step != null && step.getTrack() == TTrack.this) {
                        if (!(step.getFrameNumber() == panel.getFrameNumber())) {
                            panel.setSelectedPoint(null);
                            panel.selectedSteps.clear();
                        }
                    }
                }
            }
            TTrack.this.repaint();
        });
        markByDefaultItem.addActionListener(e -> setMarkByDefault(markByDefaultItem.isSelected()));
        autoAdvanceItem.addActionListener(e -> setAutoAdvance(autoAdvanceItem.isSelected()));
        lockedItem.addActionListener(e -> setLocked(lockedItem.isSelected()));
        deleteTrackItem.addActionListener(e -> delete());
        deleteStepItem.addActionListener(e -> trackerPanel.deletePoint(trackerPanel.getSelectedPoint()));
        clearStepsItem.addActionListener(e -> {
            if (isLocked()) return;
            XMLControl control = new XMLControlElement(TTrack.this);
            for (int n = 0; n < getSteps().length; n++) {
                steps.setStep(n, null);
            }
            for (String columnName : textColumnNames) {
                textColumnEntries.put(columnName, new String[0]);
            }
            Undo.postTrackEdit(TTrack.this, control);
            if (TTrack.this instanceof PointMass) {
                PointMass p = (PointMass) TTrack.this;
                p.updateDerivatives();
            }
            AutoTracker autoTracker = trackerPanel.getAutoTracker();
            if (autoTracker.getTrack() == TTrack.this)
                autoTracker.reset();
            autoTracker.getWizard().setVisible(false);
            firePropertyChange("steps", null, null); //$NON-NLS-1$
            trackerPanel.repaint();
        });
        footprintListener = e -> {
            String footprintName = e.getActionCommand();
            if (getFootprint().getName().equals(footprintName)) return;
            XMLControl control = new XMLControlElement(new TrackProperties(TTrack.this));
            setFootprint(footprintName);
            Undo.postTrackDisplayEdit(TTrack.this, control);
        };
        circleFootprintListener = e -> {
            footprintListener.actionPerformed(e);
            CircleFootprint cfp = (CircleFootprint) getFootprint();
            cfp.showProperties(TTrack.this);
        };

    }

    /**
     * Shows and hides this track.
     *
     * @param visible <code>true</code> to show this track
     */
    public void setVisible(boolean visible) {
        Boolean prev = this.visible;
        this.visible = visible;
        support.firePropertyChange("visible", prev, Boolean.valueOf(visible)); //$NON-NLS-1$
        if (trackerPanel != null) trackerPanel.repaint();
    }

    /**
     * Removes this track from all panels that draw it. If no other objects have
     * a reference to it, this should then be garbage-collected.
     */
    public void delete() {
        delete(true);
    }

    /**
     * Removes this track from all panels that draw it. If no other objects have
     * a reference to it, this should then be garbage-collected.
     *
     * @param postEdit true to post an undoable edit
     */
    public void delete(boolean postEdit) {
        if (isLocked() && !isDependent()) return;
        if (trackerPanel != null) {
            trackerPanel.setSelectedPoint(null);
            trackerPanel.selectedSteps.clear();
            // handle case when this is the origin of current reference frame
            ImageCoordSystem coords = trackerPanel.getCoords();
            if (coords instanceof ReferenceFrame &&
                    ((ReferenceFrame) coords).getOriginTrack() == this) {
                // set coords to underlying coords
                coords = ((ReferenceFrame) coords).getCoords();
                trackerPanel.setCoords(coords);
            }
        }
        if (postEdit) {
            Undo.postTrackDelete(this); // posts undoable edit
        }
        for (TrackerPanel panel : panels) {
            panel.removeTrack(this);
        }
        erase();
        dispose();
    }

    /**
     * Reports whether or not this is visible.
     *
     * @return <code>true</code> if this track is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Shows and hides the trail. If the trail is shown, all steps are
     * visible. If not, only the current step is visible.
     *
     * @param visible <code>true</code> to show trail
     */
    public void setTrailVisible(boolean visible) {
        trailVisible = visible;
    }

    /**
     * Gets the trail visibility.
     *
     * @return <code>true</code> if trail is visible
     */
    public boolean isTrailVisible() {
        return trailVisible;
    }

    /**
     * Sets the trail length.
     *
     * @param steps the trail length
     */
    public void setTrailLength(int steps) {
        trailLength = Math.max(0, steps);
    }

    /**
     * Gets the trail length.
     *
     * @return trail length
     */
    public int getTrailLength() {
        if (isMarking) return 1;
        return trailLength;
    }

    /**
     * Locks and unlocks this track. When locked, no changes are allowed.
     *
     * @param locked <code>true</code> to lock this
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        support.firePropertyChange("locked", null, locked); //$NON-NLS-1$
    }

    /**
     * Gets the locked property.
     *
     * @return <code>true</code> if this is locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets the autoAdvance property.
     *
     * @param auto <code>true</code> to request that the video autoadvance while marking.
     */
    public void setAutoAdvance(boolean auto) {
        autoAdvance = auto;
    }

    /**
     * Gets the autoAdvance property.
     *
     * @return <code>true</code> if this is autoadvance
     */
    public boolean isAutoAdvance() {
        return autoAdvance;
    }

    /**
     * Sets the markByDefault property. When true, the mouse handler should mark
     * a point whenever the active track reports itself incomplete.
     *
     * @param mark <code>true</code> to mark by default
     */
    public void setMarkByDefault(boolean mark) {
        markByDefault = mark;
    }

    /**
     * Gets the markByDefault property. When true, the mouse handler should mark
     * a point whenever the active track reports itself incomplete.
     *
     * @return <code>true</code> if this marks by default
     */
    public boolean isMarkByDefault() {
        return markByDefault;
    }

    /**
     * Gets the color.
     *
     * @return the current color
     */
    public Color getColor() {
        if (footprint == null)
            return defaultColors[0];
        return footprint.getColor();
    }

    /**
     * Sets the color.
     *
     * @param color the desired color
     */
    public void setColor(Color color) {
        if (color == null) color = defaultColors[0];
        for (Footprint value : footprints) value.setColor(color);
        erase();
        if (trackerPanel != null) {
            trackerPanel.changed = true;
            if (trackerPanel.modelBuilder != null) {
                trackerPanel.modelBuilder.refreshDropdown(null);
            }
            if (trackerPanel.dataBuilder != null) {
                org.opensourcephysics.tools.FunctionPanel panel =
                        trackerPanel.dataBuilder.getPanel(getName());
                if (panel != null) {
                    panel.setIcon(getIcon(21, 16, "track")); //$NON-NLS-1$
                    trackerPanel.dataBuilder.refreshDropdown(null);
                }
            }
        }
        support.firePropertyChange("color", null, color); //$NON-NLS-1$
    }

    /**
     * Sets the color to one of the default colors[].
     *
     * @param index the color index
     */
    public void setColorToDefault(int index) {
        int colorIndex = index % defaultColors.length;
        setColor(defaultColors[colorIndex]);
    }

    /**
     * Sets the default name and color for a specified tracker panel.
     *
     * @param trackerPanel the TrackerPanel
     * @param connector    the string connector between the name and letter suffix
     */
    public void setDefaultNameAndColor(TrackerPanel trackerPanel, String connector) {
        String name = getName();
        int i = trackerPanel.getAlphabetIndex(name, connector);
        String letter = TrackerPanel.alphabet.substring(i, i + 1);
        setName(name + connector + letter);
        setColorToDefault(i);
    }

    /**
     * Displays a JColorChooser and returns the selected color.
     *
     * @param color the initial color to select
     * @param title the title for the dialog
     * @return the newly selected color. or initial color if cancelled
     */
    public Color chooseColor(final Color color, String title) {
        final JColorChooser chooser = new JColorChooser();
        chooser.setColor(color);
        ActionListener cancelListener = e -> chooser.setColor(color);
        JDialog dialog = JColorChooser.createDialog(null, title, true,
                chooser, null, cancelListener);
        FontSizer.setFonts(dialog, FontSizer.getLevel());
        dialog.setVisible(true);
        return chooser.getColor();
    }

    /**
     * Gets the ID number of this track.
     *
     * @return the ID number
     */
    public int getID() {
        return ID;
    }

    /**
     * Gets the name of this track.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the name of this track.
     *
     * @param context ignored by default
     * @return the name
     */
    public String getName(String context) {
        return getName();
    }

    /**
     * Sets the name of this track.
     *
     * @param newName the new name of this track
     */
    public void setName(String newName) {
        if (newName != null && !newName.trim().equals("")) { //$NON-NLS-1$
            String prevName = name;
            name = newName;
            this.repaint();
            if (trackerPanel != null) {
                trackerPanel.changed = true;
                if (trackerPanel.dataBuilder != null) {
                    trackerPanel.dataBuilder.renamePanel(prevName, newName);
                }
                if (trackerPanel.modelBuilder != null) {
                    trackerPanel.modelBuilder.refreshBoosterDropdown();
                }
            }
            support.firePropertyChange("name", prevName, name); //$NON-NLS-1$
        }
    }

    /**
     * Gets the description of this track.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this track.
     *
     * @param desc a description
     */
    public void setDescription(String desc) {
        if (desc == null) desc = ""; //$NON-NLS-1$
        description = desc;
    }

    /**
     * Overrides Object toString method.
     *
     * @return a description of this object
     */
    public String toString() {
        return getClass().getSimpleName() + " " + name; //$NON-NLS-1$
    }

    @Override
    public void finalize() {
        OSPLog.finer(getClass().getSimpleName() + " recycled by garbage collector"); //$NON-NLS-1$
    }

    /**
     * Gets a message about this track to display in a message box.
     *
     * @return the message
     */
    public String getMessage() {
        String s = getName();
        if (partName != null)
            s += " " + partName; //$NON-NLS-1$
        if (isLocked() && !TrackerRes.getString("PointMass.Position.Locked.Hint").equals(hint)) { //$NON-NLS-1$
            hint = TrackerRes.getString("TTrack.Locked.Hint"); //$NON-NLS-1$
        }
        if (Tracker.showHints && hint != null)
            s += " (" + hint + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return s;
    }

    /**
     * Determines whether views and track menu include this track.
     *
     * @param viewable <code>true</code> to include this track in views
     */
    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

    /**
     * Reports whether or not this is viewable.
     *
     * @return <code>true</code> if this track is viewable
     */
    public boolean isViewable() {
        return viewable;
    }

    /**
     * Reports whether or not this is dependent. A dependent track gets some
     * or all of its data from other tracks. Dependent tracks should override
     * this method to return true.
     *
     * @return <code>true</code> if this track is dependent
     */
    public boolean isDependent() {
        return false;
    }

    /**
     * Sets the footprint choices. The footprint is set to the first choice.
     *
     * @param choices the array of Footprints available to this track
     */
    public void setFootprints(Footprint[] choices) {
        Collection<Footprint> valid = new ArrayList<>();
        for (Footprint choice : choices) {
            if (choice != null && choice.getLength() <= getFootprintLength()) {
                if (getFootprint() != null) choice.setColor(getColor());
                valid.add(choice);
            }
        }
        if (valid.size() > 0) {
            footprints = valid.toArray(new Footprint[0]);
            setFootprint(footprints[0].getName());
        }
    }

    /**
     * Sets the footprint choices. The footprint is set to the first choice.
     * The step parameter may be used to set the footprints of secondary step
     * arrays (veloc, accel, etc).
     *
     * @param choices the array of Footprints available to this track
     * @param step    the step that identifies the step array
     */
    public void setFootprints(Footprint[] choices, Step step) {
        setFootprints(choices);
    }

    /**
     * Gets the footprint choices.
     *
     * @return the array of Footprints available to this track
     */
    public Footprint[] getFootprints() {
        return footprints;
    }

    /**
     * Gets the footprint choices. The step parameter may be
     * used to get the footprints of secondary step arrays (veloc, accel, etc).
     *
     * @param step the step that identifies the step array
     * @return the array of Footprints available to this track
     */
    public Footprint[] getFootprints(Step step) {
        return footprints;
    }

    /**
     * Sets the footprint to the specified choice.
     *
     * @param name the name of the desired footprint
     */
    public void setFootprint(String name) {
        String props = null;
        int n = name.indexOf("#"); //$NON-NLS-1$
        if (n > -1) {
            props = name.substring(n + 1);
            name = name.substring(0, n);
        }
        for (Footprint value : footprints) {
            if (name.equals(value.getName())) {
                footprint = value;
                if (footprint instanceof CircleFootprint) {
                    ((CircleFootprint) footprint).setProperties(props);
                }
                Step[] stepArray = steps.array;
                for (Step step : stepArray)
                    if (step != null)
                        step.setFootprint(footprint);
                repaint();
                if (trackerPanel != null) {
                    trackerPanel.changed = true;
                    if (trackerPanel.modelBuilder != null) {
                        trackerPanel.modelBuilder.refreshDropdown(null);
                    }
                    if (trackerPanel.dataBuilder != null) {
                        org.opensourcephysics.tools.FunctionPanel panel =
                                trackerPanel.dataBuilder.getPanel(getName());
                        if (panel != null) {
                            panel.setIcon(getIcon(21, 16, "track")); //$NON-NLS-1$
                            trackerPanel.dataBuilder.refreshDropdown(null);
                        }
                    }
                }
                support.firePropertyChange("footprint", null, footprint); //$NON-NLS-1$
                return;
            }
        }
    }

    /**
     * Gets the full name of the current footprint, including properties if available
     *
     * @return the footprint name
     */
    public String getFootprintName() {
        Footprint fp = getFootprint();
        String s = fp.getName();
        if (fp instanceof CircleFootprint) {
            CircleFootprint cfp = (CircleFootprint) fp;
            s += "#" + cfp.getProperties(); //$NON-NLS-1$
        }
        return s;
    }

    /**
     * Gets the current footprint.
     *
     * @return the footprint
     */
    public Footprint getFootprint() {
        return footprint;
    }

    /**
     * Sets the footprint to the specified choice. The step parameter may be
     * used to set the footprints of secondary step arrays (veloc, accel, etc).
     *
     * @param name the name of the desired footprint
     * @param step the step that identifies the step array
     */
    public void setFootprint(String name, Step step) {
        setFootprint(name);
    }

    /**
     * Gets the current footprint. The step parameter may be
     * used to get the footprints of secondary step arrays (veloc, accel, etc).
     *
     * @param step the step that identifies the step array
     * @return the footprint
     */
    public Footprint getFootprint(Step step) {
        return getFootprint();
    }

    /**
     * Gets this track's current icon.
     *
     * @param w the icon width
     * @param h the icon height
     * @return the icon
     */
    public Icon getIcon(int w, int h, String context) {
        return getFootprint().getIcon(w, h);
    }

    /**
     * Gets the length of the steps created by this track.
     *
     * @return the footprint length
     */
    public abstract int getStepLength();

    /**
     * Gets the length of the footprints required by this track.
     *
     * @return the footprint length
     */
    public abstract int getFootprintLength();

    /**
     * Creates a new step.
     *
     * @param n the frame number
     * @param x the x coordinate in image space
     * @param y the y coordinate in image space
     * @return the new step
     */
    public abstract Step createStep(int n, double x, double y);

    /**
     * Deletes a step.
     *
     * @param n the frame number
     * @return the deleted step
     */
    public Step deleteStep(int n) {
        if (locked) return null;
        Step step = steps.getStep(n);
        if (step != null) {
            XMLControl control = new XMLControlElement(this);
            steps.setStep(n, null);
            for (String columnName : textColumnNames) {
                String[] entries = textColumnEntries.get(columnName);
                if (entries.length > n) {
                    entries[n] = null;
                }
            }
            Undo.postTrackEdit(this, control);
            support.firePropertyChange("step", null, n); //$NON-NLS-1$
        }
        return step;
    }

    /**
     * Gets a step specified by frame number. May return null.
     *
     * @param n the frame number
     * @return the step
     */
    public Step getStep(int n) {
        return steps.getStep(n);
    }

    /**
     * Gets next visible step after the specified step. May return null.
     *
     * @param step         the step
     * @param trackerPanel the tracker panel
     * @return the next visiblestep
     */
    public Step getNextVisibleStep(Step step, TrackerPanel trackerPanel) {
        Step[] steps = getSteps();
        boolean found = false;
        for (Step value : steps) {
            // return first step after found
            if (found && value != null &&
                    isStepVisible(value, trackerPanel)) return value;
            // find specified step
            if (value == step) found = true;
        }
        // cycle back to beginning if next step not yet identified
        if (found) {
            for (Step value : steps) {
                // return first visible step
                if (value != null && value != step &&
                        isStepVisible(value, trackerPanel))
                    return value;
            }
        }
        return null;
    }

    /**
     * Gets first visible step before the specified step. May return null.
     *
     * @param step         the step
     * @param trackerPanel the tracker panel
     * @return the previous visible step
     */
    public Step getPreviousVisibleStep(Step step, TrackerPanel trackerPanel) {
        Step[] steps = getSteps();
        boolean found = false;
        for (int i = steps.length - 1; i > -1; i--) {
            // return first step after found
            if (found && steps[i] != null &&
                    isStepVisible(steps[i], trackerPanel)) return steps[i];
            // find specified step
            if (steps[i] == step) found = true;
        }
        // cycle back to end if previous step not yet identified
        if (found) {
            for (int i = steps.length - 1; i > -1; i--) {
                // return first visible step
                if (steps[i] != null && steps[i] != step &&
                        isStepVisible(steps[i], trackerPanel))
                    return steps[i];
            }
        }
        return null;
    }

    /**
     * Gets a step containing a TPoint. May return null.
     *
     * @param point        a TPoint
     * @param trackerPanel the tracker panel holding the TPoint
     * @return the step containing the TPoint
     */
    public Step getStep(TPoint point, TrackerPanel trackerPanel) {
        if (point == null) return null;
        Step[] stepArray = steps.array;
        for (Step step : stepArray)
            if (step != null) {
                TPoint[] points = step.getPoints();
                for (TPoint tPoint : points) if (tPoint == point) return step;
            }
        return null;
    }

    /**
     * Gets the step array. Some or all elements may be null.
     *
     * @return the step array
     */
    public Step[] getSteps() {
        return steps.array;
    }

    /**
     * Returns true if the step at the specified frame number is complete.
     * Points may be created or remarked if false.
     *
     * @param n the frame number
     * @return <code>true</code> if the step is complete, otherwise false
     */
    public boolean isStepComplete(int n) {
        return false; // enables remarking
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
        createStep(n, x, y);
        return getMarkedPoint(n, getTargetIndex());
    }

    /**
     * Used by autoTracker to get the marked point for a given frame and index.
     *
     * @param n     the frame number
     * @param index the index
     * @return the step TPoint at the index
     */
    public TPoint getMarkedPoint(int n, int index) {
        Step step = getStep(n);
        if (step == null) return null;
        return step.getPoints()[index];
    }

    /**
     * Returns the target index for the autotracker.
     *
     * @return the point index
     */
    public int getTargetIndex() {
        return targetIndex;
    }

    /**
     * Sets the target index for the autotracker.
     *
     * @param index the point index
     */
    public void setTargetIndex(int index) {
        if (isAutoTrackable(index))
            targetIndex = index;
    }

    /**
     * Sets the target index for the autotracker.
     *
     * @param description the description of the target
     */
    public void setTargetIndex(String description) {
        for (int i = 0; i < getStepLength(); i++) {
            if (description.equals(getTargetDescription(i))) {
                setTargetIndex(i);
                break;
            }
        }
    }

    /**
     * Sets the target index for the autotracker.
     *
     * @param p a TPoint associated with a step in this track
     */
    protected void setTargetIndex(TPoint p) {
        Step step = getStep(p, trackerPanel);
        if (step != null)
            setTargetIndex(step.getPointIndex(p));
    }

    /**
     * Returns a description of a target point with a given index.
     *
     * @param pointIndex the index
     * @return the description
     */
    public String getTargetDescription(int pointIndex) {
        return null;
    }

    /**
     * Determines if the given point index is autotrackable.
     *
     * @param pointIndex the points[] index
     * @return true if autotrackable
     */
    public boolean isAutoTrackable(int pointIndex) {
        return true; // true by default--subclasses override
    }

    /**
     * Determines if at least one point in this track is autotrackable.
     *
     * @return true if autotrackable
     */
    public boolean isAutoTrackable() {
        return false; // false by default--subclasses override
    }

    /**
     * Returns true if this track contains no steps.
     *
     * @return <code>true</code> if this contains no steps
     */
    public boolean isEmpty() {
        Step[] array = steps.array;
        for (Step step : array) if (step != null) return false;
        return true;
    }

    /**
     * Returns an array of NumberFields {x, y, magnitude, angle} for a given step.
     *
     * @param step the step
     * @return the number fields
     */
    public NumberField[] getNumberFieldsForStep(Step step) {
        return positionFields;
    }

    /**
     * Sets the font level.
     *
     * @param level the desired font level
     */
    public void setFontLevel(int level) {
        Object[] objectsToSize = new Object[]
                {tLabel, xLabel, yLabel, magLabel, angleLabel, stepLabel, tValueLabel, stepValueLabel,
                        tField, xField, yField, magField, angleField};
        FontSizer.setFonts(objectsToSize, level);
    }

    /**
     * Returns the DatasetManager.
     *
     * @param trackerPanel the tracker panel
     * @return the DatasetManager
     */
    public DatasetManager getData(TrackerPanel trackerPanel) {
        if (data == null) {
            data = new DatasetManager(true);
            data.setSorted(true);
        }
        if (refreshDataLater)
            return data;
        if (!dataValid) {
            dataValid = true;
            // refresh track data
            refreshData(data, trackerPanel);
            // check for newly loaded dataFunctions
            if (dataProp != null) {
                XMLControl[] children = dataProp.getChildControls();
                outer:
                for (XMLControl child : children) {
                    // compare function name with existing datasets to avoid duplications
                    String name = child.getString("function_name"); //$NON-NLS-1$
                    for (Dataset next : data.getDatasets()) {
                        if (next instanceof DataFunction && next.getYColumnName().equals(name)) {
                            continue outer;
                        }
                    }
                    DataFunction f = new DataFunction(data);
                    child.loadObject(f);
                    f.setXColumnVisible(false);
                    data.addDataset(f);
                }
                dataProp = null;
            }
            if (constantsLoadedFromXML != null) {
                for (Object[] objects : constantsLoadedFromXML) {
                    String name = (String) objects[0];
                    double val = (Double) objects[1];
                    String expression = (String) objects[2];
                    String desc = objects.length < 4 ? null : (String) objects[3];
                    data.setConstant(name, val, expression, desc);
                }
                constantsLoadedFromXML = null;
            }
            // refresh dataFunctions
            ArrayList<Dataset> datasets = data.getDatasets();
            for (Dataset dataset : datasets) {
                if (dataset instanceof DataFunction) {
                    ((DataFunction) dataset).refreshFunctionData();
                }
            }
            DataTool tool = DataTool.getTool();
            if (trackerPanel != null && tool.isVisible()
                    && tool.getSelectedTab() != null && tool.getSelectedTab().isInterestedIn(data)) {
                tool.getSelectedTab().refreshData();
            }
        }
        return data;
    }

    /**
     * Refreshes the data in the specified DatasetManager. Subclasses should use this
     * method to refresh track-specific data sets.
     *
     * @param data         the DatasetManager
     * @param trackerPanel the tracker panel
     */
    protected void refreshData(DatasetManager data, TrackerPanel trackerPanel) {
    }

    /**
     * Refreshes the data for a specified frame range. This default implementation
     * ignores the range arguments.
     *
     * @param data         the DatasetManager
     * @param trackerPanel the tracker panel
     * @param startFrame   the start frame
     */
    protected void refreshData(DatasetManager data, TrackerPanel trackerPanel,
                               int startFrame) {
        refreshData(data, trackerPanel);
    }

    /**
     * Gets the name of a data variable. Index zero is the
     * shared x-variable, indices 1-n+1 are the y-variables.
     *
     * @param index the dataset index
     * @return a String data name
     */
    public String getDataName(int index) {
        if (index == 0) { // shared x-variable
            return data.getDataset(0).getXColumnName();
        }
        if (index < data.getDatasets().size() + 1) {
            return data.getDataset(index - 1).getYColumnName();
        }
        return null;
    }

    /**
     * Gets the description of a data variable. Index zero is the
     * shared x-variable, indices 1-n+1 are the y-variables.
     * Subclasses should override to provide correct descriptions.
     *
     * @param index the dataset index
     * @return a String data description
     */
    public String getDataDescription(int index) {
        if (dataDescriptions == null) return ""; //$NON-NLS-1$
        if (index >= dataDescriptions.length) {
            ArrayList<Dataset> datasets = data.getDatasets();
            index--;
            if (index < datasets.size() && datasets.get(index) instanceof DataFunction) {
                String desc = datasets.get(index).getYColumnDescription();
                if (desc == null) desc = ""; //$NON-NLS-1$
                return desc;
            }
            return ""; //$NON-NLS-1$
        }
        return dataDescriptions[index];
    }

    /**
     * Gets the preferred order of data table columns.
     *
     * @return a list of column indices in preferred order
     */
    public ArrayList<Integer> getPreferredDataOrder() {
        ArrayList<Integer> orderedData = new ArrayList<>();
        ArrayList<Dataset> datasets = data.getDatasets();
        if (preferredColumnOrder != null) {
            // first add preferred indices
            for (int j : preferredColumnOrder) {
                if (!orderedData.contains(j) // prevent duplicates
                        && j < datasets.size()) // prevent invalid indices
                    orderedData.add(j);
            }
        }
        // add indices not yet in array
        for (int i = 0; i < datasets.size(); i++) {
            if (!orderedData.contains(i)) {
                orderedData.add(i);
            }
        }
        return orderedData;
    }

    /**
     * Gets the frame number associated with specified variables and values.
     *
     * @param xVar     the x-variable name (required)
     * @param yVar     the y-variable name (may be null)
     * @param xyValues values array (length 1 or 2)
     * @return the frame number, or -1 if not found
     */
    public int getFrameForData(String xVar, String yVar, double[] xyValues) {
        if (dataFrames.isEmpty() || data.getDatasets().isEmpty())
            return -1;
        Dataset dataset = data.getDataset(0);
        if (xVar.equals(dataset.getXColumnName())) {
            // for independent variable, ignore yVar
            double[] vals = dataset.getXPoints();
            for (int i = 0; i < vals.length; i++) {
                if (xyValues[0] == vals[i]) {
                    return i < dataFrames.size() ? dataFrames.get(i) : -1;
                }
            }
        } else {
            // not independent variable, so find match in xVar dataset
            int n = data.getDatasetIndex(xVar);
            if (n > -1) {
                dataset = data.getDataset(n);
                double[] xVals = dataset.getYPoints();
                for (int i = 0; i < xVals.length; i++) {
                    if (xyValues[0] == xVals[i]) {
                        // found matching value
                        int frame = i < dataFrames.size() ? dataFrames.get(i) : -1;
                        // if yVar value is given, verify it matches as well
                        if (yVar != null && xyValues.length > 1) {
                            n = data.getDatasetIndex(yVar);
                            dataset = data.getDataset(n);
                            double[] yVals = dataset.getYPoints();
                            // if y value doesn't also match, reject and continue searching
                            if (xyValues[1] != yVals[i]) {
                                continue;
                            }
                        }
                        return frame;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Gets the data index for a specified frame.
     *
     * @param frameNumber the frame number
     * @return the data index, or -1 if not found
     */
    public int getDataIndex(int frameNumber) {
        if (!data.getDatasets().isEmpty()) {
            // find data index
            for (int i = 0; i < dataFrames.size(); i++) {
                if (frameNumber == dataFrames.get(i)) return i;
            }
        }
        return -1;
    }

    /**
     * Gets a map of number fields by name.
     *
     * @return a map of name to NumberField.
     */
    public Map<String, NumberField[]> getNumberFields() {
        return numberFields;
    }

    /**
     * Gets a list of data variables for a given track type.
     *
     * @return an ArrayList of names. May be empty.
     */
    public static ArrayList<String> getDataVariables(Class<? extends TTrack> trackType) {
        String[] vars = new String[0];
        if (PointMass.class.isAssignableFrom(trackType)) {
            vars = PointMass.dataVariables;
        }
        if (org.opensourcephysics.cabrillo.tracker.vector.Vector.class.isAssignableFrom(trackType)) {
            vars = org.opensourcephysics.cabrillo.tracker.vector.Vector.dataVariables;
        }
        if (LineProfile.class.isAssignableFrom(trackType)) {
            vars = LineProfile.dataVariables;
        }
        if (RGBRegion.class.isAssignableFrom(trackType)) {
            vars = RGBRegion.dataVariables;
        }
        if (TapeMeasure.class.isAssignableFrom(trackType)) {
            vars = TapeMeasure.dataVariables;
        }
        if (org.opensourcephysics.cabrillo.tracker.protractor.Protractor.class.isAssignableFrom(trackType)) {
            vars = org.opensourcephysics.cabrillo.tracker.protractor.Protractor.dataVariables;
        }
        if (CircleFitter.class.isAssignableFrom(trackType)) {
            vars = CircleFitter.dataVariables;
        }
        if (Calibration.class.isAssignableFrom(trackType)) {
            vars = Calibration.dataVariables;
        }
        if (OffsetOrigin.class.isAssignableFrom(trackType)) {
            vars = OffsetOrigin.dataVariables;
        }
        if (CoordAxes.class.isAssignableFrom(trackType)) {
            vars = CoordAxes.dataVariables;
        }
        return new ArrayList<>(Arrays.asList(vars));
    }

    /**
     * Gets a list of number field variables for a given track type.
     *
     * @return an ArrayList of names. May be empty.
     */
    protected static ArrayList<String> getNumberFieldVariables(Class<? extends TTrack> trackType) {
        String[] vars = new String[0];
        if (PointMass.class.isAssignableFrom(trackType)) {
            vars = PointMass.fieldVariables;
        }
        if (org.opensourcephysics.cabrillo.tracker.vector.Vector.class.isAssignableFrom(trackType)) {
            vars = org.opensourcephysics.cabrillo.tracker.vector.Vector.fieldVariables;
        }
        if (LineProfile.class.isAssignableFrom(trackType)) {
            vars = LineProfile.fieldVariables;
        }
        if (RGBRegion.class.isAssignableFrom(trackType)) {
            vars = RGBRegion.fieldVariables;
        }
        if (TapeMeasure.class.isAssignableFrom(trackType)) {
            vars = TapeMeasure.formatVariables; // not an error--same variables
        }
        if (org.opensourcephysics.cabrillo.tracker.protractor.Protractor.class.isAssignableFrom(trackType)) {
            vars = org.opensourcephysics.cabrillo.tracker.protractor.Protractor.fieldVariables;
        }
        if (CircleFitter.class.isAssignableFrom(trackType)) {
            vars = CircleFitter.fieldVariables;
        }
        if (Calibration.class.isAssignableFrom(trackType)) {
            vars = Calibration.dataVariables;    // not an error--same variables
        }
        if (OffsetOrigin.class.isAssignableFrom(trackType)) {
            vars = OffsetOrigin.dataVariables;    // not an error--same variables
        }
        if (CoordAxes.class.isAssignableFrom(trackType)) {
            vars = CoordAxes.dataVariables; // not an error--same variables
        }
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, vars);
        return list;
    }

    /**
     * Gets a list of all variable names for a given track type.
     *
     * @return an ArrayList of names. May be empty.
     */
    public static ArrayList<String> getAllVariables(Class<? extends TTrack> trackType) {
        ArrayList<String> list = new ArrayList<>(getDataVariables(trackType));
        for (String next : getNumberFieldVariables(trackType)) {
            if (!list.contains(next)) {
                list.add(next);
            }
        }
        return list;
    }

    /**
     * Gets a list of formatter display names for a given track type.
     *
     * @return an array of names. May be empty.
     */
    public static String[] getFormatterDisplayNames(Class<? extends TTrack> trackType) {
        if (PointMass.class.isAssignableFrom(trackType)) {
            return PointMass.formatVariables;
        }
        if (org.opensourcephysics.cabrillo.tracker.vector.Vector.class.isAssignableFrom(trackType)) {
            return org.opensourcephysics.cabrillo.tracker.vector.Vector.formatVariables;
        }
        if (LineProfile.class.isAssignableFrom(trackType)) {
            return LineProfile.formatVariables;
        }
        if (RGBRegion.class.isAssignableFrom(trackType)) {
            return RGBRegion.formatVariables;
        }
        if (TapeMeasure.class.isAssignableFrom(trackType)) {
            return TapeMeasure.formatVariables;
        }
        if (org.opensourcephysics.cabrillo.tracker.protractor.Protractor.class.isAssignableFrom(trackType)) {
            return org.opensourcephysics.cabrillo.tracker.protractor.Protractor.formatVariables;
        }
        if (CircleFitter.class.isAssignableFrom(trackType)) {
            return CircleFitter.formatVariables;
        }
        if (Calibration.class.isAssignableFrom(trackType)) {
            return Calibration.formatVariables;
        }
        if (OffsetOrigin.class.isAssignableFrom(trackType)) {
            return OffsetOrigin.formatVariables;
        }
        if (CoordAxes.class.isAssignableFrom(trackType)) {
            return CoordAxes.formatVariables;
        }
        return new String[0];
    }

    /**
     * Gets a map of formatter names to variables for a given track type.
     *
     * @return a Map<String, ArrayList<String>>. May be empty.
     */
    public static Map<String, ArrayList<String>> getFormatterMap(Class<? extends TTrack> trackType) {
        if (PointMass.class.isAssignableFrom(trackType)) {
            return PointMass.formatMap;
        }
        if (org.opensourcephysics.cabrillo.tracker.vector.Vector.class.isAssignableFrom(trackType)) {
            return org.opensourcephysics.cabrillo.tracker.vector.Vector.formatMap;
        }
        if (LineProfile.class.isAssignableFrom(trackType)) {
            return LineProfile.formatMap;
        }
        if (RGBRegion.class.isAssignableFrom(trackType)) {
            return RGBRegion.formatMap;
        }
        if (TapeMeasure.class.isAssignableFrom(trackType)) {
            return TapeMeasure.formatMap;
        }
        if (org.opensourcephysics.cabrillo.tracker.protractor.Protractor.class.isAssignableFrom(trackType)) {
            return org.opensourcephysics.cabrillo.tracker.protractor.Protractor.formatMap;
        }
        if (CircleFitter.class.isAssignableFrom(trackType)) {
            return CircleFitter.formatMap;
        }
        if (Calibration.class.isAssignableFrom(trackType)) {
            return Calibration.formatMap;
        }
        if (OffsetOrigin.class.isAssignableFrom(trackType)) {
            return OffsetOrigin.formatMap;
        }
        if (CoordAxes.class.isAssignableFrom(trackType)) {
            return CoordAxes.formatMap;
        }
        return new HashMap<>();
    }

    /**
     * Gets an array of variables to format for a given track type and formatter display name.
     *
     * @return an array of variables. May be null.
     */
    public static ArrayList<String> getVariablesFromFormatterDisplayName(Class<? extends TTrack> trackType, String formatterDisplayName) {
        return getFormatterMap(trackType).get(formatterDisplayName);
    }

    /**
     * Gets a map of formatter names to descriptions for a given track type.
     *
     * @return a Map<String, String>. May be empty.
     */
    public static Map<String, String> getFormatterDescriptionMap(Class<? extends TTrack> trackType) {
        if (PointMass.class.isAssignableFrom(trackType)) {
            return PointMass.formatDescriptionMap;
        }
        if (org.opensourcephysics.cabrillo.tracker.vector.Vector.class.isAssignableFrom(trackType)) {
            return Vector.formatDescriptionMap;
        }
        if (LineProfile.class.isAssignableFrom(trackType)) {
            return LineProfile.formatDescriptionMap;
        }
        if (RGBRegion.class.isAssignableFrom(trackType)) {
            return RGBRegion.formatDescriptionMap;
        }
        if (TapeMeasure.class.isAssignableFrom(trackType)) {
            return TapeMeasure.formatDescriptionMap;
        }
        if (org.opensourcephysics.cabrillo.tracker.protractor.Protractor.class.isAssignableFrom(trackType)) {
            return org.opensourcephysics.cabrillo.tracker.protractor.Protractor.formatDescriptionMap;
        }
        if (CircleFitter.class.isAssignableFrom(trackType)) {
            return CircleFitter.formatDescriptionMap;
        }
        if (Calibration.class.isAssignableFrom(trackType)) {
            return Calibration.formatDescriptionMap;
        }
        if (OffsetOrigin.class.isAssignableFrom(trackType)) {
            return OffsetOrigin.formatDescriptionMap;
        }
        if (CoordAxes.class.isAssignableFrom(trackType)) {
            return CoordAxes.formatDescriptionMap;
        }
        return new HashMap<>();
    }

    /**
     * Gets the text column names.
     *
     * @return list of column names.
     */
    public ArrayList<String> getTextColumnNames() {
        return textColumnNames;
    }

    /**
     * Adds a new text column.
     *
     * @param name the name
     */
    public void addTextColumn(String name) {
        // only add new, non-null names
        if (name == null || name.trim().equals("")) return; //$NON-NLS-1$
        name = name.trim();
        for (String next : textColumnNames) {
            if (next.equals(name)) return;
        }
        XMLControl control = new XMLControlElement(this);
        textColumnNames.add(name);
        textColumnEntries.put(name, new String[0]);
        Undo.postTrackEdit(this, control);
        trackerPanel.changed = true;
        this.firePropertyChange("text_column", null, name); //$NON-NLS-1$
    }

    /**
     * Removes a named text column.
     *
     * @param name the name
     * @return true if the column was removed
     */
    public boolean removeTextColumn(String name) {
        if (name == null) return false;
        name = name.trim();
        for (String next : textColumnNames) {
            if (next.equals(name)) {
                XMLControl control = new XMLControlElement(this);
                textColumnEntries.remove(name);
                textColumnNames.remove(name);
                Undo.postTrackEdit(this, control);
                trackerPanel.changed = true;
                firePropertyChange("text_column", name, null); //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }

    /**
     * Renames a text column.
     *
     * @param name    the existing name
     * @param newName the new name
     * @return true if renamed
     */
    public boolean renameTextColumn(String name, String newName) {
        if (name == null) return false;
        name = name.trim();
        if (newName == null || newName.trim().equals("")) return false; //$NON-NLS-1$
        newName = newName.trim();
        for (String next : textColumnNames) {
            if (next.equals(newName)) return false;
        }
        for (int i = 0; i < textColumnNames.size(); i++) {
            String next = textColumnNames.get(i);
            if (name.equals(next)) {
                // found column to change
                XMLControl control = new XMLControlElement(this);
                textColumnNames.remove(name);
                textColumnNames.add(i, newName);
                String[] entries = textColumnEntries.remove(name);
                textColumnEntries.put(newName, entries);
                Undo.postTrackEdit(this, control);
            }
        }
        trackerPanel.changed = true;
        this.firePropertyChange("text_column", name, newName); //$NON-NLS-1$
        return true;
    }

    /**
     * Gets the entry in a text column for a specified frame.
     *
     * @param columnName  the column name
     * @param frameNumber the frame number
     * @return the text entry (may be null)
     */
    public String getTextColumnEntry(String columnName, int frameNumber) {
        // return null if frame number out of bounds
        if (frameNumber < 0) return null;
        String[] entries = textColumnEntries.get(columnName);
        // return null if text column or entry index not defined
        if (entries == null) return null;
        if (frameNumber > entries.length - 1) return null;
        return entries[frameNumber];
    }

    /**
     * Sets the text in a text column for a specified frame.
     *
     * @param columnName  the column name
     * @param frameNumber the frame number
     * @param text        the text (may be null)
     * @return true if the text was changed
     */
    public boolean setTextColumnEntry(String columnName, int frameNumber, String text) {
        if (isLocked()) return false;
        // return if frame number out of bounds
        if (frameNumber < 0) return false;
        String[] entries = textColumnEntries.get(columnName);
        // return if text column not defined
        if (entries == null) return false;

        if (text.trim().equals("")) text = null;  //$NON-NLS-1$
        else text = text.trim();

        XMLControl control = new XMLControlElement(this);
        if (frameNumber > entries.length - 1) {
            // increase size of entries array
            String[] newEntries = new String[frameNumber + 1];
            System.arraycopy(entries, 0, newEntries, 0, entries.length);
            entries = newEntries;
            textColumnEntries.put(columnName, entries);
        }

        String prev = entries[frameNumber];
        if (prev.equals(text)) return false;
        // change text entry and fire property change
        entries[frameNumber] = text;
        Undo.postTrackEdit(this, control);
        trackerPanel.changed = true;
        firePropertyChange("text_column", null, null); //$NON-NLS-1$
        return true;
    }

    /**
     * Returns the array of attachments for this track. May return null.
     *
     * @return the attachments array
     */
    public TTrack[] getAttachments() {
        return attachments;
    }

    /**
     * Returns the description of a particular attachment point.
     *
     * @param n the attachment point index
     * @return the description
     */
    public String getAttachmentDescription(int n) {
        return TrackerRes.getString("AttachmentInspector.Label.End") + " " + (n + 1); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Loads the attachments for this track based on attachmentNames array, if any.
     *
     * @param refresh true to refresh attachments after loading
     * @return true if attachments were loaded
     */
    protected boolean loadAttachmentsFromNames(boolean refresh) {
        // if track attachmentNames is not null then find tracks and populate attachments
        if (attachmentNames == null) return false;

        boolean foundAll = true;
        TTrack[] temp = new TTrack[attachmentNames.length];
        for (int i = 0; i < attachmentNames.length; i++) {
            TTrack track = trackerPanel.getTrack(attachmentNames[i]);
            if (track != null) {
                temp[i] = track;
            } else if (attachmentNames[i] != null) {
                foundAll = false;
            }
        }
        if (foundAll) {
            attachments = temp;
            attachmentNames = null;
            if (refresh)
                refreshAttachmentsLater();
        }
        return foundAll;
    }

    /**
     * Refreshes the attachments for this track after a delay.
     * This should be used only when loading attachments from Names during loading
     */
    protected void refreshAttachmentsLater() {
        // use timer with 2 second delay
        Timer timer = new Timer(2000, e -> {
            // save changed state
            boolean changed = trackerPanel != null && trackerPanel.changed;
            refreshAttachments();
            if (trackerPanel != null) {
                // restore changed state
                trackerPanel.changed = changed;
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Refreshes the attachments for this track.
     */
    public void refreshAttachments() {
        if (attachments == null || attachments.length == 0) return;

        // unfix the track if it has attachments
        boolean hasAttachments = false;
        for (TTrack attachment : attachments) {
            hasAttachments = hasAttachments || attachment != null;
        }
        if (hasAttachments) {
            if (this instanceof TapeMeasure) {
                ((TapeMeasure) this).setFixedPosition(false);
            } else if (this instanceof org.opensourcephysics.cabrillo.tracker.protractor.Protractor) {
                ((org.opensourcephysics.cabrillo.tracker.protractor.Protractor) this).setFixed(false);
            }
        }

        VideoClip clip = trackerPanel.getPlayer().getVideoClip();
        for (int i = 0; i < attachments.length; i++) {
            TTrack targetTrack = attachments[i];
            if (targetTrack != null) {
                targetTrack.removePropertyChangeListener("step", this); //$NON-NLS-1$
                targetTrack.removePropertyChangeListener("steps", this); //$NON-NLS-1$
                targetTrack.addPropertyChangeListener("step", this); //$NON-NLS-1$
                targetTrack.addPropertyChangeListener("steps", this); //$NON-NLS-1$
                // attach/detach points
                for (int n = clip.getStartFrameNumber(); n <= clip.getEndFrameNumber(); n++) {
                    Step targetStep = targetTrack.getStep(n);
                    Step step = getStep(n);
                    if (step == null) continue;
                    TPoint p = step.getPoints()[i]; // not for CircleFitter--see overridden method
                    if (targetStep == null || !targetStep.valid) {
                        if (p != null) {
                            p.detach();
                        }
                    } else if (p != null) {
                        TPoint target = targetStep.getPoints()[0];
                        p.attachTo(target);
                    }
                }
            } else { // target track is null
                for (int n = clip.getStartFrameNumber(); n <= clip.getEndFrameNumber(); n++) {
                    Step step = getStep(n);
                    if (step == null) continue;
                    TPoint p = step.getPoints()[i];
                    if (p != null) {
                        p.detach();
                    }
                }
            }
        }

        TTrackBar.getTrackbar(trackerPanel).refresh();
//	refreshFields(trackerPanel.getFrameNumber());
    }

    /**
     * Prepares menu items and returns a new menu.
     * Subclasses should override this method and add track-specific menu items.
     *
     * @param trackerPanel the tracker panel
     * @return a menu
     */
    public JMenu getMenu(TrackerPanel trackerPanel) {
        // prepare menu items
        visibleItem.setText(TrackerRes.getString("TTrack.MenuItem.Visible")); //$NON-NLS-1$
        trailVisibleItem.setText(TrackerRes.getString("TTrack.MenuItem.TrailVisible")); //$NON-NLS-1$
        autoAdvanceItem.setText(TrackerRes.getString("TTrack.MenuItem.Autostep")); //$NON-NLS-1$
        markByDefaultItem.setText(TrackerRes.getString("TTrack.MenuItem.MarkByDefault")); //$NON-NLS-1$
        lockedItem.setText(TrackerRes.getString("TTrack.MenuItem.Locked")); //$NON-NLS-1$
        deleteTrackItem.setText(TrackerRes.getString("TTrack.MenuItem.Delete")); //$NON-NLS-1$
        deleteStepItem.setText(TrackerRes.getString("TTrack.MenuItem.DeletePoint")); //$NON-NLS-1$
        clearStepsItem.setText(TrackerRes.getString("TTrack.MenuItem.ClearSteps")); //$NON-NLS-1$
        colorItem.setText(TrackerRes.getString("TTrack.MenuItem.Color")); //$NON-NLS-1$
        nameItem.setText(TrackerRes.getString("TTrack.MenuItem.Name")); //$NON-NLS-1$
        footprintMenu.setText(TrackerRes.getString("TTrack.MenuItem.Footprint")); //$NON-NLS-1$
        descriptionItem.setText(TrackerRes.getString("TTrack.MenuItem.Description")); //$NON-NLS-1$
        dataBuilderItem.setText(TrackerRes.getString("TView.Menuitem.Define")); //$NON-NLS-1$
        visibleItem.setSelected(isVisible());
        lockedItem.setSelected(isLocked());
        trailVisibleItem.setSelected(isTrailVisible());
        markByDefaultItem.setSelected(isMarkByDefault());
        autoAdvanceItem.setSelected(isAutoAdvance());
        lockedItem.setEnabled(true);
        boolean cantDeleteSteps = isLocked() || isDependent();
        TPoint p = trackerPanel.getSelectedPoint();
        Step step = getStep(p, trackerPanel);

        deleteStepItem.setEnabled(!cantDeleteSteps && step != null);
        clearStepsItem.setEnabled(!cantDeleteSteps);
        deleteTrackItem.setEnabled(!(isLocked() && !isDependent()));
        nameItem.setEnabled(!(isLocked() && !isDependent()));
        footprintMenu.removeAll();
        Footprint[] fp = getFootprints();
        JMenuItem item;
        for (Footprint value : fp) {
            item = new JMenuItem(value.getDisplayName(), value.getIcon(21, 16));
            item.setActionCommand(value.getName());
            if (value instanceof CircleFootprint) {
                item.setText(value.getDisplayName() + "..."); //$NON-NLS-1$
                item.addActionListener(circleFootprintListener);
            } else {
                item.addActionListener(footprintListener);
            }
            if (value == footprint) {
                item.setBorder(BorderFactory.createLineBorder(item.getBackground().darker()));
            }
            footprintMenu.add(item);
        }
        // return a new menu every time
        menu = new JMenu(getName("track")); //$NON-NLS-1$
        menu.setIcon(getFootprint().getIcon(21, 16));
        // add name and description items
        if (trackerPanel.isEnabled("track.name") || //$NON-NLS-1$
                trackerPanel.isEnabled("track.description")) { //$NON-NLS-1$
            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) != null)
                menu.addSeparator();
            if (trackerPanel.isEnabled("track.name")) //$NON-NLS-1$
                menu.add(nameItem);
            if (trackerPanel.isEnabled("track.description")) //$NON-NLS-1$
                menu.add(descriptionItem);
        }
        // add color and footprint items
        if (trackerPanel.isEnabled("track.color") || //$NON-NLS-1$
                trackerPanel.isEnabled("track.footprint")) { //$NON-NLS-1$
            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) != null)
                menu.addSeparator();
            if (trackerPanel.isEnabled("track.color")) //$NON-NLS-1$
                menu.add(colorItem);
            if (trackerPanel.isEnabled("track.footprint")) //$NON-NLS-1$
                menu.add(footprintMenu);
        }
        // add visible, trail and locked items
        if (trackerPanel.isEnabled("track.visible") || //$NON-NLS-1$
                trackerPanel.isEnabled("track.locked")) { //$NON-NLS-1$
            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) != null)
                menu.addSeparator();
            if (trackerPanel.isEnabled("track.visible")) //$NON-NLS-1$
                menu.add(visibleItem);
            if (trackerPanel.isEnabled("track.locked")) //$NON-NLS-1$
                menu.add(lockedItem);
        }
        // add dataBuilder item if viewable and enabled
        if (this.isViewable() && trackerPanel.isEnabled("data.builder")) { //$NON-NLS-1$
            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) != null)
                menu.addSeparator();
            menu.add(dataBuilderItem);

        }
        // add clear steps and delete items
        if (trackerPanel.isEnabled("track.delete")) { //$NON-NLS-1$
            if (menu.getItemCount() > 0 && menu.getItem(menu.getItemCount() - 1) != null)
                menu.addSeparator();
            menu.add(deleteTrackItem);
        }
        return menu;
    }

    /**
     * Returns an empty list of track-related toolbar components. Subclasses
     * should override this method and add track-specific components.
     *
     * @param trackerPanel the tracker panel
     * @return a collection of components
     */
    public ArrayList<Component> getToolbarTrackComponents(TrackerPanel trackerPanel) {
        String tooltip = TrackerRes.getString("TTrack.NumberField.Format.Tooltip"); //$NON-NLS-1$
        if (OSPRuntime.isMac()) {
            tooltip = TrackerRes.getString("TTrack.NumberField.Format.Tooltip.OSX"); //$NON-NLS-1$
        }
        for (NumberField[] fields : getNumberFields().values()) {
            for (NumberField field : fields) {
                field.setToolTipText(tooltip);
            }
        }
        tField.setUnits(trackerPanel.getUnits(this, "t")); //$NON-NLS-1$
        toolbarTrackComponents.clear();
        return toolbarTrackComponents;
    }

    /**
     * Returns an empty list of point-related toolbar components. Subclasses
     * should override this method and add point-specific components.
     *
     * @param trackerPanel the tracker panel
     * @param point        the TPoint
     * @return a list of components
     */
    public ArrayList<Component> getToolbarPointComponents(TrackerPanel trackerPanel,
                                                          TPoint point) {
        toolbarPointComponents.clear();
        stepLabel.setText(TrackerRes.getString("TTrack.Label.Step")); //$NON-NLS-1$
        // put step time into tField
        Step step = getStep(point, trackerPanel);
        VideoClip clip = trackerPanel.getPlayer().getVideoClip();
        if (step != null && clip.includesFrame(step.getFrameNumber())) {
            int n = clip.frameToStep(step.getFrameNumber());
            stepValueLabel.setText(n + ":"); //$NON-NLS-1$
            double t = trackerPanel.getPlayer().getStepTime(n) / 1000;
            if (t >= 0) {
                tField.setValue(t);
            }
        }
        // set tooltip for angle field
        angleField.setToolTipText(angleField.getConversionFactor() == 1 ?
                TrackerRes.getString("TTrack.AngleField.Radians.Tooltip") : //$NON-NLS-1$
                TrackerRes.getString("TTrack.AngleField.Degrees.Tooltip")); //$NON-NLS-1$
        return toolbarPointComponents;
    }

    /**
     * Erases all steps on all panels.
     */
    public void erase() {
        Step[] stepArray = steps.array;
        for (Step step : stepArray) if (step != null) step.erase();
        if (trackerPanel != null && trackerPanel.autoTracker != null) {
            AutoTracker autoTracker = trackerPanel.getAutoTracker();
            if (autoTracker.getWizard().isVisible()
                    && autoTracker.getTrack() == this) {
                autoTracker.erase();
            }
        }
    }

    /**
     * Remarks all steps on all panels.
     */
    public void remark() {
        Step[] stepArray = steps.array;
        for (Step step : stepArray) if (step != null) step.remark();
    }

    /**
     * Repaints all steps on all panels.
     */
    public void repaint() {
        remark();
        for (TrackerPanel next : panels) {
            next.repaintDirtyRegion();
        }
    }

    /**
     * Erases all steps on the specified panel.
     *
     * @param trackerPanel the tracker panel
     */
    public void erase(TrackerPanel trackerPanel) {
        Step[] stepArray = steps.array;
        for (Step step : stepArray) if (step != null) step.erase(trackerPanel);
        if (trackerPanel.autoTracker != null) {
            AutoTracker autoTracker = trackerPanel.getAutoTracker();
            if (autoTracker.getWizard().isVisible()
                    && autoTracker.getTrack() == this) {
                autoTracker.erase();
            }
        }
    }

    /**
     * Remarks all steps on the specified panel.
     *
     * @param trackerPanel the tracker panel
     */
    public void remark(TrackerPanel trackerPanel) {
        Step[] stepArray = steps.array;
        for (Step step : stepArray) if (step != null) step.remark(trackerPanel);
    }

    /**
     * Repaints all steps on the specified panel.
     *
     * @param trackerPanel the tracker panel
     */
    public void repaint(TrackerPanel trackerPanel) {
        remark(trackerPanel);
        trackerPanel.repaintDirtyRegion();
    }

    /**
     * Repaints the specified step on all panels. This should be used
     * instead of the Step.repaint() method to paint a new step on all
     * panels for the first time, since a new step does not know what
     * panels it is drawn on whereas the track does.
     *
     * @param step the step
     */
    public void repaint(Step step) {
        for (TrackerPanel panel : panels) step.repaint(panel);
    }

    /**
     * Draws the steps on the tracker panel.
     *
     * @param panel the drawing panel requesting the drawing
     * @param _g    the graphics context on which to draw
     */
    public void draw(DrawingPanel panel, Graphics _g) {
        loadAttachmentsFromNames(true);
        if (!(panel instanceof TrackerPanel) || !visible) return;
        TrackerPanel trackerPanel = (TrackerPanel) panel;
        panels.add(trackerPanel);   // keep a list of tracker panels
        Graphics2D g = (Graphics2D) _g;
        int n = trackerPanel.getFrameNumber();
        int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
        if (trailVisible) {
            boolean shortTrail = getTrailLength() > 0;
            Step[] stepArray = steps.array;
            for (int frame = 0; frame < stepArray.length; frame++) {
                if (shortTrail && (n - frame > (getTrailLength() - 1) * stepSize || frame > n))
                    continue;
                if (stepArray[frame] != null &&
                        trackerPanel.getPlayer().getVideoClip().includesFrame(frame))
                    stepArray[frame].draw(trackerPanel, g);
            }
        } else {
            Step step = getStep(n);
            if (step != null)
                step.draw(trackerPanel, g);
        }
    }

    /**
     * Finds the interactive drawable object located at the specified
     * pixel position.
     *
     * @param panel the drawing panel
     * @param xpix  the x pixel position on the panel
     * @param ypix  the y pixel position on the panel
     * @return the first step TPoint that is hit
     */
    public Interactive findInteractive(
            DrawingPanel panel, int xpix, int ypix) {
        if (!(panel instanceof TrackerPanel) || !visible) return null;
        TrackerPanel trackerPanel = (TrackerPanel) panel;
        Interactive iad;
        int n = trackerPanel.getFrameNumber();
        int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
        if (trailVisible) {
            boolean shortTrail = getTrailLength() > 0;
            Step[] stepArray = steps.array;
            for (int frame = 0; frame < stepArray.length; frame++) {
                if (shortTrail && (n - frame > (getTrailLength() - 1) * stepSize || frame > n))
                    continue;
                if (stepArray[frame] != null &&
                        trackerPanel.getPlayer().getVideoClip().includesFrame(frame)) {
                    iad = stepArray[frame].findInteractive(trackerPanel, xpix, ypix);
                    if (iad != null) return iad;
                }
            }
        } else {
            Step step = getStep(n);
            if (step != null &&
                    trackerPanel.getPlayer().getVideoClip().includesFrame(n)) {
                iad = step.findInteractive(trackerPanel, xpix, ypix);
                return iad;
            }
        }
        return null;
    }


    /**
     * Gets x. Tracks have no meaningful position, so returns 0.
     *
     * @return 0
     */
    public double getX() {
        return 0;
    }

    /**
     * Gets y. Tracks have no meaningful position, so returns 0.
     *
     * @return 0
     */
    public double getY() {
        return 0;
    }

    /**
     * Empty setX method.
     *
     * @param x the x position
     */
    public void setX(double x) {
    }

    /**
     * Empty setY method.
     *
     * @param y the y position
     */
    public void setY(double y) {
    }

    /**
     * Empty setXY method.
     *
     * @param x the x position
     * @param y the y position
     */
    public void setXY(double x, double y) {
    }

    /**
     * Sets whether this responds to mouse hits.
     *
     * @param enabled <code>true</code> if this responds to mouse hits.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets whether this responds to mouse hits.
     *
     * @return <code>true</code> if this responds to mouse hits.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Reports whether information is available to set min/max values.
     *
     * @return <code>false</code> since TTrack knows only its image coordinates
     */
    public boolean isMeasured() {
        return !isEmpty();
    }

    /**
     * Gets the minimum x needed to draw this object.
     *
     * @return 0
     */
    public double getXMin() {
        return getX();
    }

    /**
     * Gets the maximum x needed to draw this object.
     *
     * @return 0
     */
    public double getXMax() {
        return getX();
    }

    /**
     * Gets the minimum y needed to draw this object.
     *
     * @return 0
     */
    public double getYMin() {
        return getY();
    }

    /**
     * Gets the maximum y needed to draw this object.
     *
     * @return 0
     */
    public double getYMax() {
        return getY();
    }

    /**
     * Gets the minimum world x needed to draw this object on the specified TrackerPanel.
     *
     * @param panel the TrackerPanel drawing this track
     * @return the minimum world x
     */
    public double getXMin(TrackerPanel panel) {
        double[] bounds = getWorldBounds(panel);
        return bounds[2];
    }

    /**
     * Gets the maximum world x needed to draw this object on the specified TrackerPanel.
     *
     * @param panel the TrackerPanel drawing this track
     * @return the maximum x of any step's footprint
     */
    public double getXMax(TrackerPanel panel) {
        double[] bounds = getWorldBounds(panel);
        return bounds[0];
    }

    /**
     * Gets the minimum world y needed to draw this object on the specified TrackerPanel.
     *
     * @param panel the TrackerPanel drawing this track
     * @return the minimum y of any step's footprint
     */
    public double getYMin(TrackerPanel panel) {
        double[] bounds = getWorldBounds(panel);
        return bounds[3];
    }

    /**
     * Gets the maximum world y needed to draw this object on the specified TrackerPanel.
     *
     * @param panel the TrackerPanel drawing this track
     * @return the maximum y of any step's footprint
     */
    public double getYMax(TrackerPanel panel) {
        double[] bounds = getWorldBounds(panel);
        return bounds[1];
    }

    /**
     * Sets a user property of the track.
     *
     * @param name  the name of the property
     * @param value the value of the property
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Gets a user property of the track. May return null.
     *
     * @param name the name of the property
     * @return the value of the property
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Gets a collection of user property names for the track.
     *
     * @return a collection of property names
     */
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    /**
     * Responds to property change events.
     *
     * @param e the property change event
     */
    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if (e.getSource() instanceof TrackerPanel) {
            TrackerPanel trackerPanel = (TrackerPanel) e.getSource();
            switch (name) {
                case "transform":
                case "coords":  //$NON-NLS-1$
                    if (!(this instanceof PointMass)) {
                        dataValid = false;
                    }
                    erase();
                    trackerPanel.repaint();
                    break;
                case "magnification":  //$NON-NLS-1$
                    erase();
                    trackerPanel.repaint();
                    break;
                case "imagespace":
//$NON-NLS-1$
                    erase(trackerPanel);
                    break;
                case "data":  //$NON-NLS-1$
                    dataValid = false;
                    break;
                case "radian_angles":  // angle format has changed //$NON-NLS-1$
                    setAnglesInRadians((Boolean) e.getNewValue());
                    break;
            }
        }
    }

    /**
     * Adds a PropertyChangeListener.
     *
     * @param listener the object requesting property change notification
     */
    public void addPropertyChangeListener(
            PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Fires a property change event.
     *
     * @param name   the name of the property
     * @param oldVal the old value of the property
     * @param newVal the new value of the property
     */
    public void firePropertyChange(String name, Object oldVal, Object newVal) {
        support.firePropertyChange(name, oldVal, newVal);
    }

    /**
     * Adds a PropertyChangeListener for a specified property.
     *
     * @param property the name of the property of interest to the listener
     * @param listener the object requesting property change notification
     */
    public void addPropertyChangeListener(
            String property, PropertyChangeListener listener) {
        support.addPropertyChangeListener(property, listener);
    }

    /**
     * Removes a PropertyChangeListener.
     *
     * @param listener the listener requesting removal
     */
    public void removePropertyChangeListener(
            PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener for a specified property.
     *
     * @param property the name of the property
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(
            String property, PropertyChangeListener listener) {
        support.removePropertyChangeListener(property, listener);
    }

    /**
     * Reports whether or not the specified step is visible.
     *
     * @param step         the step
     * @param trackerPanel the tracker panel
     * @return <code>true</code> if the step is visible
     */
    public boolean isStepVisible(Step step, TrackerPanel trackerPanel) {
        if (!isVisible()) return false;
        int n = step.getFrameNumber();
        if (!trackerPanel.getPlayer().getVideoClip().includesFrame(n)) return false;
        int frame = trackerPanel.getFrameNumber();
        if (n == frame) return true;
        if (!trailVisible) return false;
        if (getTrailLength() == 0) return true;
        int stepSize = trackerPanel.getPlayer().getVideoClip().getStepSize();
        return (frame - n) > -1 && (frame - n) < getTrailLength() * stepSize;
    }

    //___________________________ protected methods ____________________________

    /**
     * Identifies the controlling TrackerPanel for this track (by default,
     * the first TrackerPanel that adds this track to its drawables).
     *
     * @param panel the TrackerPanel
     */
    public void setTrackerPanel(TrackerPanel panel) {
        trackerPanel = panel;
    }

    /**
     * Gets the world bounds of this track on the specified TrackerPanel.
     *
     * @param panel the TrackerPanel
     * @return a double[] containing xMax, yMax, xMin, yMin
     */
    protected double[] getWorldBounds(TrackerPanel panel) {
        double[] bounds = worldBounds.get(panel);
        //if (bounds != null) return bounds;
        // make a rectangle containing the world positions of the TPoints in this track
        // then convert it into world units
        bounds = new double[4];
        Rectangle2D rect = new Rectangle2D.Double();
        Step[] array = steps.array;
        for (Step step : array) {
            if (step != null) {
                TPoint[] points = step.getPoints();
                for (TPoint tPoint : points) {
                    if (tPoint == null) continue;
                    rect.add(tPoint.getWorldPosition(panel));
                }
            }
        }
        // increase bounds to make room for footprint shapes
        bounds[0] = rect.getX() + 1.05 * rect.getWidth();  // xMax
        bounds[1] = rect.getY() + 1.05 * rect.getHeight(); // yMax
        bounds[2] = rect.getX() - 0.05 * rect.getWidth();  // xMin
        bounds[3] = rect.getY() - 0.05 * rect.getHeight(); // yMin
        worldBounds.put(panel, bounds);
        return bounds;
    }

    /**
     * Sets the display format for angles.
     *
     * @param radians <code>true</code> for radians, false for degrees
     */
    public void setAnglesInRadians(boolean radians) {
        angleField.setUnits(radians ? null : Tracker.DEGREES);
        angleField.setDecimalPlaces(radians ? 3 : 1);
        angleField.setConversionFactor(radians ? 1.0 : 180 / Math.PI);
        angleField.setToolTipText(radians ?
                TrackerRes.getString("TTrack.AngleField.Radians.Tooltip") : //$NON-NLS-1$
                TrackerRes.getString("TTrack.AngleField.Degrees.Tooltip")); //$NON-NLS-1$
    }

    /**
     * Disposes of resources when this track is deleted or cleared.
     */
    public void dispose() {
        panels.clear();
        properties.clear();
        worldBounds.clear();
        data = null;
        if (attachments != null) {
            for (int i = 0; i < attachments.length; i++) {
                TTrack targetTrack = attachments[i];
                if (targetTrack != null) {
                    targetTrack.removePropertyChangeListener("step", this); //$NON-NLS-1$
                    targetTrack.removePropertyChangeListener("steps", this); //$NON-NLS-1$
                }
                attachments[i] = null;
            }
            refreshAttachments();
        }
        attachments = null;
        attachmentNames = null;
        for (Step step : steps.array) {
            if (step != null) {
                step.dispose();
            }
        }
        steps = null;
        setTrackerPanel(null);
    }

    /**
     * Sets the marking flag. Flag should be true when ready to be marked by user.
     *
     * @param marking true when marking
     */
    public void setMarking(boolean marking) {
        isMarking = marking;
    }

    /**
     * Gets the cursor used for marking new steps.
     *
     * @param e the input event triggering this call (may be null)
     * @return the marking cursor
     */
    public Cursor getMarkingCursor(InputEvent e) {
        if (e != null && TMouseHandler.isAutoTrackTrigger(e) && trackerPanel.getVideo() != null && isAutoTrackable(getTargetIndex())) {
            Step step = getStep(trackerPanel.getFrameNumber());
            if (step == null || step.getPoints()[step.getPoints().length - 1] == null) {
                return TMouseHandler.autoTrackMarkCursor;
            }

            if (this instanceof CoordAxes
                    || this instanceof PerspectiveTrack
                    || this instanceof TapeMeasure
                    || this instanceof Protractor) {
                AutoTracker autoTracker = trackerPanel.getAutoTracker();
                if (autoTracker.getTrack() == null || autoTracker.getTrack() == this) {
                    int n = trackerPanel.getFrameNumber();
                    KeyFrame key = autoTracker.getFrame(n).getKeyFrame();
                    if (key == null)
                        return TMouseHandler.autoTrackMarkCursor;
                }
            }

            return TMouseHandler.autoTrackCursor;
        }

        return TMouseHandler.markPointCursor;
    }

    protected void createWarningDialog() {
        if (skippedStepWarningDialog == null
                && trackerPanel != null
                && trackerPanel.getTFrame() != null) {
            skippedStepWarningDialog = new JDialog(trackerPanel.getTFrame(), true);
            skippedStepWarningDialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    skippedStepWarningOn = !skippedStepWarningCheckbox.isSelected();
                }
            });
            JPanel contentPane = new JPanel(new BorderLayout());
            skippedStepWarningDialog.setContentPane(contentPane);
            skippedStepWarningTextpane = new JTextPane();
            skippedStepWarningTextpane.setEditable(false);
            skippedStepWarningTextpane.setOpaque(false);
            skippedStepWarningTextpane.setPreferredSize(new Dimension(400, 120));
            skippedStepWarningTextpane.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));
            skippedStepWarningTextpane.setContentType("text"); //$NON-NLS-1$
            skippedStepWarningTextpane.setFont(new JLabel().getFont());
            contentPane.add(skippedStepWarningTextpane, BorderLayout.CENTER);
            skippedStepWarningCheckbox = new JCheckBox();
            skippedStepWarningCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30));
            closeButton = new JButton();
            closeButton.addActionListener(e -> {
                skippedStepWarningOn = !skippedStepWarningCheckbox.isSelected();
                skippedStepWarningDialog.setVisible(false);
            });
            JPanel buttonbar = new JPanel();
            buttonbar.add(skippedStepWarningCheckbox);
            buttonbar.add(closeButton);
            contentPane.add(buttonbar, BorderLayout.SOUTH);
        }
    }

    protected JDialog getStepSizeWarningDialog() {
        createWarningDialog();
        if (skippedStepWarningDialog == null)
            return null;

        skippedStepWarningDialog.setTitle(TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Title")); //$NON-NLS-1$
        String m1 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message1"); //$NON-NLS-1$
        String m2 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message2"); //$NON-NLS-1$
        String m3 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message3"); //$NON-NLS-1$
        skippedStepWarningTextpane.setText(m1 + "  " + m2 + "  " + m3); //$NON-NLS-1$ //$NON-NLS-2$
        skippedStepWarningCheckbox.setText(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Checkbox")); //$NON-NLS-1$
        closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
        skippedStepWarningDialog.pack();
        // center on screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - skippedStepWarningDialog.getBounds().width) / 2;
        int y = (dim.height - skippedStepWarningDialog.getBounds().height) / 2;
        skippedStepWarningDialog.setLocation(x, y);

        return skippedStepWarningDialog;
    }

    protected JDialog getSkippedStepWarningDialog() {
        createWarningDialog();
        if (skippedStepWarningDialog == null)
            return null;

        skippedStepWarningDialog.setTitle(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Title")); //$NON-NLS-1$
        String m1 = TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Message1"); //$NON-NLS-1$
        String m3 = TrackerRes.getString("TTrack.Dialog.StepSizeWarning.Message3"); //$NON-NLS-1$
        skippedStepWarningTextpane.setText(m1 + "  " + m3); //$NON-NLS-1$
        skippedStepWarningCheckbox.setText(TrackerRes.getString("TTrack.Dialog.SkippedStepWarning.Checkbox")); //$NON-NLS-1$
        closeButton.setText(TrackerRes.getString("Dialog.Button.Close")); //$NON-NLS-1$
        FontSizer.setFonts(skippedStepWarningDialog, FontSizer.getLevel());
        skippedStepWarningDialog.pack();
        // center on screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - skippedStepWarningDialog.getBounds().width) / 2;
        int y = (dim.height - skippedStepWarningDialog.getBounds().height) / 2;
        skippedStepWarningDialog.setLocation(x, y);

        return skippedStepWarningDialog;
    }

    public Dataset convertTextToDataColumn(String textColumnName) {
        if (textColumnName == null || trackerPanel == null) return null;
        // find named text column
        String[] entries = this.textColumnEntries.get(textColumnName);
        if (entries != null && entries.length > 0) {
            DatasetManager data = getData(trackerPanel);
            double[] x = data.getXPoints(0);
            double[] values = new double[x.length];
            for (int i = 0; i < values.length; i++) {
                if (entries.length > i) {
                    if (entries[i] == null) {
                        values[i] = Double.NaN;
                    } else try {
                        values[i] = Double.parseDouble(entries[i]);
                    } catch (Exception ex) {
                        return null;
                    }
                } else values[i] = Double.NaN;
            }
            Dataset dataset = new Dataset();
            dataset.append(x, values);
            dataset.setXYColumnNames(data.getDataset(0).getXColumnName(), textColumnName, getName());
            dataset.setMarkerColor(getColor());
            return dataset;
        }
        return null;
    }

    /**
     * A NumberField that resizes itself for display on a TTrackBar.
     */
    protected class TrackNumberField extends NumberField {

        public TrackNumberField() {
            super(0);
        }

        @Override
        public void setText(String t) {
            super.setText(t);
            if (trackerPanel != null) {
                TTrackBar.getTrackbar(trackerPanel).resizeField(this);
            }
        }

    }

    /**
     * A DecimalField that resizes itself for display on a TTrackBar.
     */
    public class TrackDecimalField extends DecimalField {

        public TrackDecimalField(int places) {
            super(0, places);
        }

        @Override
        public void setText(String t) {
            super.setText(t);
            if (trackerPanel != null) {
                TTrackBar.getTrackbar(trackerPanel).resizeField(this);
            }
        }

    }

    public static class TTrackTextLineLabel extends TextLineLabel {
        @Override
        public void processParent(Container c) {
            while (c != null) {
                if (c instanceof TTrackBar) {
                    ((TTrackBar) c).refresh();
                    break;
                }
            }
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
            TTrack track = (TTrack) obj;
            // name
            control.setValue("name", track.getName()); //$NON-NLS-1$
            // description
            if (!track.description.equals(""))  //$NON-NLS-1$
                control.setValue("description", track.description); //$NON-NLS-1$
            // color
            control.setValue("color", track.getColor()); //$NON-NLS-1$
            // footprint name
            control.setValue("footprint", track.getFootprintName()); //$NON-NLS-1$
            // visible
            control.setValue("visible", track.isVisible()); //$NON-NLS-1$
            // trail
            control.setValue("trail", track.isTrailVisible()); //$NON-NLS-1$
            // locked
            if (track.isLocked()) control.setValue("locked", track.isLocked()); //$NON-NLS-1$
            // number formats
            String[] customPatterns = NumberFormatDialog.getCustomFormatPatterns(track);
            if (customPatterns.length > 0) {
                control.setValue("number_formats", customPatterns); //$NON-NLS-1$
            }
            // text columns
            if (!track.getTextColumnNames().isEmpty()) {
                String[] names = track.getTextColumnNames().toArray(new String[0]);
                control.setValue("text_column_names", names); //$NON-NLS-1$
                String[][] entries = new String[names.length][];
                for (int i = 0; i < names.length; i++) {
                    entries[i] = track.textColumnEntries.get(names[i]);
                }
                control.setValue("text_column_entries", entries); //$NON-NLS-1$
            }
            // data functions
            if (track.trackerPanel != null) {
                DatasetManager data = track.getData(track.trackerPanel);
                Iterator<Dataset> it = data.getDatasets().iterator();
                ArrayList<Dataset> list = new ArrayList<>();
                while (it.hasNext()) {
                    Dataset dataset = it.next();
                    if (dataset instanceof DataFunction) {
                        list.add(dataset);
                    }
                }
                if (!list.isEmpty()) {
                    String[] names = data.getConstantNames();
                    if (names.length > 0) {
                        Object[][] paramArray = new Object[names.length][4];
                        int i = 0;
                        for (String key : names) {
                            paramArray[i][0] = key;
                            paramArray[i][1] = data.getConstantValue(key);
                            paramArray[i][2] = data.getConstantExpression(key);
                            paramArray[i][3] = data.getConstantDescription(key);
                            i++;
                        }
                        control.setValue("constants", paramArray); //$NON-NLS-1$
                    }
                    DataFunction[] f = (DataFunction[]) list.toArray(new Dataset[0]);
                    control.setValue("data_functions", f); //$NON-NLS-1$
                }
            }
            // attachments
            if (track.attachments != null && track.attachments.length > 0) {
                String[] names = new String[track.attachments.length];
                boolean notNull = false;
                for (int i = 0; i < track.attachments.length; i++) {
                    TTrack next = track.attachments[i];
                    names[i] = next == null ? null : next.getName();
                    notNull = notNull || names[i] != null;
                }
                if (notNull) {
                    control.setValue("attachments", names); //$NON-NLS-1$
                }
            }
        }

        /**
         * Creates a new object.
         *
         * @param control the XMLControl with the object data
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
            TTrack track = (TTrack) obj;
            boolean locked = track.isLocked();
            track.setLocked(false);
            // name
            track.setName(control.getString("name")); //$NON-NLS-1$
            // description
            track.setDescription(control.getString("description")); //$NON-NLS-1$
            // color
            track.setColor((Color) control.getObject("color")); //$NON-NLS-1$
            // footprint
            String s = control.getString("footprint"); //$NON-NLS-1$
            if (s != null) track.setFootprint(s.trim());
            // visible and trail
            track.setVisible(control.getBoolean("visible")); //$NON-NLS-1$
            track.setTrailVisible(control.getBoolean("trail")); //$NON-NLS-1$
            // number formats
            track.customNumberFormats = (String[]) control.getObject("number_formats"); //$NON-NLS-1$
            // text columns
            track.textColumnNames.clear();
            track.textColumnEntries.clear();
            String[] columnNames = (String[]) control.getObject("text_column_names"); //$NON-NLS-1$
            if (columnNames != null) {
                String[][] columnEntries = (String[][]) control.getObject("text_column_entries"); //$NON-NLS-1$
                if (columnEntries != null) {
                    for (int i = 0; i < columnNames.length; i++) {
                        track.textColumnNames.add(columnNames[i]);
                        track.textColumnEntries.put(columnNames[i], columnEntries[i]);
                    }
                }
            }
            // data functions and constants
            track.constantsLoadedFromXML = (Object[][]) control.getObject("constants"); //$NON-NLS-1$
            for (Object o : control.getPropertyContent()) {
                XMLProperty prop = (XMLProperty) o;
                if (prop.getPropertyName().equals("data_functions")) { //$NON-NLS-1$
                    track.dataProp = prop;
                }
            }
            // attachments
            String[] names = (String[]) control.getObject("attachments"); //$NON-NLS-1$
            if (names != null) {
                track.attachmentNames = names;
            }
            // locked
            track.setLocked(locked || control.getBoolean("locked")); //$NON-NLS-1$
            return obj;
        }
    }

    public static TrackNameDialog getNameDialog(TTrack track) {
        if (nameDialog == null && track.trackerPanel != null) {
            nameDialog = new TrackNameDialog(track.trackerPanel);
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (dim.width - nameDialog.getBounds().width) / 2;
            int y = (dim.height - nameDialog.getBounds().height) / 2;
            nameDialog.setLocation(x, y);
        }
        // prepare dialog
        if (nameDialog != null) {
            nameDialog.setTrack(track);
        }
        return nameDialog;
    }

    public static TTrack getTrack(int ID) {
        return activeTracks.get(ID);
    }

}

