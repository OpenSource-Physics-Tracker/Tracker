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

import lombok.Getter;
import lombok.Setter;
import org.opensourcephysics.tools.FontSizer;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.HashSet;

/**
 * A CircleFootprint returns a circle for a Point[] of length 1.
 */
@Getter
@Setter
public class CircleFootprint implements Footprint, Cloneable {

    /**
     * Constructs a CircleFootprint.
     *
     * @param name   the name
     * @param radius radius of the footprint
     */
    public CircleFootprint(String name, int radius) {
        this.name = name;
        setRadius(radius);
        setStroke(new BasicStroke(1.0f));
        center.setFrame(-1, -1, 2, 2);
    }

    protected static float plainStrokeSize = 1.0f;
    protected static float boldStrokeSize = 2.0f;

    protected String name;

    protected Ellipse2D circle = new Ellipse2D.Double();
    protected Ellipse2D center = new Ellipse2D.Double();

    protected Shape highlight;
    protected Shape outline;
    protected Shape spot;

    protected AffineTransform transform = new AffineTransform();

    protected int alpha = 0;

    protected Color color = new Color(0, 0, 0, alpha);
    protected Color highlightColor = Color.black;

    protected Shape[] hitShapes = new Shape[1];

    protected BasicStroke baseHighlightStroke, baseOutlineStroke;
    protected BasicStroke highlightStroke, outlineStroke;

    protected boolean outlined = true;
    protected boolean spotShown;

    protected CircleDialog dialog;

    protected int r;
    protected int prevRadius;

    protected float prevStrokeSize;

    protected boolean prevSpot;

    /**
     * Clones a CircleFootprint.
     *
     * @return the clone
     */
    protected Object clone() throws CloneNotSupportedException {
        CircleFootprint clone = (CircleFootprint) super.clone();
        clone.circle = new Ellipse2D.Double();
        return clone;
    }

    /**
     * Gets a named footprint.
     *
     * @param name the name of the footprint
     * @return the footprint
     */
    public static Footprint getFootprint(String name) {
        for (Footprint value : footprints) {
            CircleFootprint footprint = (CircleFootprint) value;
            if (name.equals(footprint.getName())) try {
                return (Footprint) footprint.clone();
            } catch (CloneNotSupportedException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Gets the display name of the footprint.
     *
     * @return the localized display name
     */
    public String getDisplayName() {
        return TrackerRes.getString(name);
    }

    /**
     * Gets the minimum point array length required by this footprint.
     *
     * @return the length
     */
    public int getLength() {
        return 1;
    }

    /**
     * Gets the icon.
     *
     * @param w width of the icon
     * @param h height of the icon
     * @return the icon
     */
    public Icon getIcon(int w, int h) {
        int scale = FontSizer.getIntegerFactor();
        w *= scale;
        h *= scale;
        int realRadius = r;
        setRadius(outlined ? 5 : 6);
        Shape shape = getShape(new Point[]{new Point()});
        Area area = null;
        if (spotShown) {
            area = new Area(spot);
        }
        if (outlined) {
            if (area == null)
                area = new Area(outline);
            else
                area.add(new Area(outline));
        }
        ShapeIcon icon = new ShapeIcon(shape, area, w, h);
        icon.setColor(color, highlightColor);
        setRadius(realRadius);
        return icon;
    }

    /**
     * Gets the footprint mark.
     *
     * @param points a Point array
     * @return the mark
     */
    public Mark getMark(Point[] points) {
        final Shape shape = getShape(points);
        final Shape outline = this.outline;
        final Shape highlight = this.highlight;
        final Shape spot = this.spot;
        return new Mark() {
            public void draw(Graphics2D g, boolean highlighted) {
                Paint gpaint = g.getPaint();
                g.setPaint(color);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.fill(shape);
                g.setPaint(highlightColor);
                if (spotShown) {
                    g.fill(spot);
                }
                if (outlined) {
                    g.fill(outline);
                }
                if (highlighted) {
                    g.fill(highlight);
                }
                g.setPaint(gpaint);
            }

            public Rectangle getBounds(boolean highlighted) {
                Rectangle bounds = shape.getBounds();
                if (highlighted) bounds.add(highlight.getBounds());
                return bounds;
            }
        };
    }


    /**
     * Sets the stroke.
     *
     * @param stroke the stroke
     */
    public void setStroke(BasicStroke stroke) {
        baseOutlineStroke = stroke;
        baseHighlightStroke = new BasicStroke(stroke.getLineWidth() + 1.0f);
    }

    /**
     * Gets the stroke. May return null;
     *
     * @return the stroke
     */
    public BasicStroke getStroke() {
        return null;
    }

    /**
     * Sets the color.
     *
     * @param color the desired color
     */
    public void setColor(Color color) {
        this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        highlightColor = new Color(color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Gets the color.
     *
     * @return the color
     */
    public Color getColor() {
        return highlightColor;
    }

    /**
     * Sets the radius.
     *
     * @param radius the radius
     */
    public void setRadius(int radius) {
        r = radius;
        circle.setFrame(-r, -r, 2 * r, 2 * r);
    }

    /**
     * Sets the alpha of the fill.
     *
     * @param alpha 0 for transparent, 255 for solid
     */
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        setColor(color);
    }

    /**
     * Gets the properties for saving.
     *
     * @return the properties "r outline spot bold"
     */
    public String getProperties() {
        String s = r + " "; //$NON-NLS-1$
        if (outlined)
            s += "outline "; //$NON-NLS-1$
        if (spotShown)
            s += "spot "; //$NON-NLS-1$
        if (baseOutlineStroke.getLineWidth() > plainStrokeSize)
            s += "bold "; //$NON-NLS-1$
        return s;
    }

    /**
     * Sets the properties when loading.
     *
     * @param props the properties "r outline spot bold"
     */
    public void setProperties(String props) {
        if (props == null) return;
        int n = props.indexOf(" "); //$NON-NLS-1$
        String radius = props.substring(0, n);
        try {
            setRadius(Integer.parseInt(radius));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        setOutlined(props.contains("outline")); //$NON-NLS-1$
        setSpotShown(props.contains("spot")); //$NON-NLS-1$
        float f = props.contains("bold") ? boldStrokeSize : plainStrokeSize; //$NON-NLS-1$
        setStroke(new BasicStroke(f));
    }

    /**
     * Shows the properties dialog.
     *
     * @param track the track using this footprint
     */
    public void showProperties(TTrack track) {
        if (dialog == null) {
            dialog = new CircleDialog(track);
            // center on screen
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (dim.width - dialog.getBounds().width) / 2;
            int y = (dim.height - dialog.getBounds().height) / 2;
            dialog.setLocation(x, y);
        }
        dialog.boldCheckbox.setSelected(baseOutlineStroke.getLineWidth() > plainStrokeSize);
        dialog.spotCheckbox.setSelected(spotShown);
        dialog.spinner.setValue(r);
        prevSpot = spotShown;
        prevStrokeSize = baseOutlineStroke.getLineWidth();
        prevRadius = r;
        dialog.setVisible(true);
    }

    /**
     * Shows the properties dialog.
     *
     * @param frame    a TFrame
     * @param listener an ActionListener
     */
    public void showProperties(TFrame frame, ActionListener listener) {
        if (dialog == null) {
            dialog = new CircleDialog(frame, listener);
            // center on screen
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (dim.width - dialog.getBounds().width) / 2;
            int y = (dim.height - dialog.getBounds().height) / 2;
            dialog.setLocation(x, y);
        }
        dialog.boldCheckbox.setSelected(baseOutlineStroke.getLineWidth() > plainStrokeSize);
        dialog.spotCheckbox.setSelected(spotShown);
        dialog.spinner.setValue(r);
        prevSpot = spotShown;
        prevStrokeSize = baseOutlineStroke.getLineWidth();
        prevRadius = r;
        dialog.setVisible(true);
    }

    /**
     * Gets the fill shape for a specified point.
     *
     * @param points an array of points
     * @return the fill shape
     */
    public Shape getShape(Point[] points) {
        Point p = points[0];
        transform.setToTranslation(p.x, p.y);
        int scale = FontSizer.getIntegerFactor();
        if (scale > 1) {
            transform.scale(scale, scale);
        }
        Shape c = transform.createTransformedShape(circle);
        if (outlineStroke == null || outlineStroke.getLineWidth() != scale * baseOutlineStroke.getLineWidth()) {
            outlineStroke = new BasicStroke(scale * baseOutlineStroke.getLineWidth());
            highlightStroke = new BasicStroke(scale * baseHighlightStroke.getLineWidth());
        }
        highlight = highlightStroke.createStrokedShape(c);
        outline = outlineStroke.createStrokedShape(c);
        spot = transform.createTransformedShape(center);
        hitShapes[0] = spot; // ignored by PointMass!
        return c;
    }

    private class CircleDialog extends JDialog {

        int trackID;
        TrackerPanel trackerPanel;
        JSpinner spinner;
        JLabel spinnerLabel;
        JButton okButton, cancelButton;
        JCheckBox boldCheckbox, spotCheckbox;
        ActionListener actionListener;

        /**
         * Constructs a CircleDialog for a specified track.
         *
         * @param track the track
         */
        public CircleDialog(TTrack track) {
            this(track.trackerPanel.getTFrame(), null);
            trackID = track.getID();
            trackerPanel = track.trackerPanel;
        }

        /**
         * Constructs a CircleDialog.
         *
         * @param frame a TFrame
         */
        public CircleDialog(TFrame frame, ActionListener listener) {
            super(frame, true);
            actionListener = listener;
            setTitle(TrackerRes.getString("CircleFootprint.Dialog.Title")); //$NON-NLS-1$
            setResizable(false);
            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            createGUI();
            pack();
            okButton.requestFocusInWindow();
        }

        /**
         * Creates the visible components.
         */
        void createGUI() {
            JPanel contentPane = new JPanel(new BorderLayout());
            setContentPane(contentPane);
            JPanel upper = new JPanel();
            upper.setBorder(BorderFactory.createEtchedBorder());
            contentPane.add(upper, BorderLayout.NORTH);
            // add spinner label and spinner
            spinnerLabel = new JLabel(TrackerRes.getString("CircleFootprint.Dialog.Label.Radius")); //$NON-NLS-1$
            spinnerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            upper.add(spinnerLabel);
            SpinnerModel model = new SpinnerNumberModel(3, 3, 100, 1);
            spinner = new JSpinner(model);
            JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            tf.setEnabled(false);
            tf.setDisabledTextColor(Color.BLACK);
            ChangeListener listener = e -> {
                int radius = (Integer) spinner.getValue();
                if (radius == r) return;
                setRadius(radius);
                if (trackerPanel != null) {
                    trackerPanel.changed = true;
                    TTrack track = TTrack.getTrack(trackID);
                    track.repaint();
                } else if (actionListener != null) {
                    actionListener.actionPerformed(null);
                }
            };
            spinner.addChangeListener(listener);
            upper.add(spinner);
            // add bold label and checkbox
            boldCheckbox = new JCheckBox(TrackerRes.getString("CircleFootprint.Dialog.Checkbox.Bold")); //$NON-NLS-1$
            boldCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            boldCheckbox.setOpaque(false);
            boldCheckbox.addActionListener(e -> {
                float f = boldCheckbox.isSelected() ? boldStrokeSize : plainStrokeSize;
                setStroke(new BasicStroke(f));
                if (trackerPanel != null) {
                    trackerPanel.changed = true;
                    TTrack track = TTrack.getTrack(trackID);
                    track.repaint();
                } else if (actionListener != null) {
                    actionListener.actionPerformed(null);
                }
            });
            upper.add(boldCheckbox);
            spotCheckbox = new JCheckBox(TrackerRes.getString("CircleFootprint.Dialog.Checkbox.CenterSpot")); //$NON-NLS-1$
            spotCheckbox.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            spotCheckbox.addActionListener(e -> {
                setSpotShown(spotCheckbox.isSelected());
                if (trackerPanel != null) {
                    trackerPanel.changed = true;
                    TTrack track = TTrack.getTrack(trackID);
                    track.repaint();
                } else if (actionListener != null) {
                    actionListener.actionPerformed(null);
                }
            });
            upper.add(spotCheckbox);
            // add close button
            JPanel lower = new JPanel();
            contentPane.add(lower, BorderLayout.SOUTH);
            okButton = new JButton(TrackerRes.getString("Dialog.Button.OK")); //$NON-NLS-1$
            okButton.addActionListener(e -> {
                setVisible(false);
                if (trackerPanel != null) {
                    TTrack track = TTrack.getTrack(trackID);
                    track.setFootprint(CircleFootprint.this.getName());
                }
            });
            lower.add(okButton);
            cancelButton = new JButton(TrackerRes.getString("Dialog.Button.Cancel")); //$NON-NLS-1$
            cancelButton.addActionListener(e -> {
                setSpotShown(prevSpot);
                setStroke(new BasicStroke(prevStrokeSize));
                setRadius(prevRadius);
                if (trackerPanel != null) {
                    TTrack track = TTrack.getTrack(trackID);
                    track.repaint();
                } else if (actionListener != null) {
                    actionListener.actionPerformed(null);
                }
                setVisible(false);
            });
            lower.add(cancelButton);
        }
    }

    // static fields
    private static final Collection<Footprint> footprints = new HashSet<>();

    // static constants
    private static final CircleFootprint CIRCLE;
    private static final CircleFootprint FILLED_CIRCLE;

    // static initializers
    static {
        CIRCLE = new CircleFootprint("CircleFootprint.Circle", 4); //$NON-NLS-1$
        footprints.add(CIRCLE);
        FILLED_CIRCLE = new CircleFootprint("CircleFootprint.FilledCircle", 8); //$NON-NLS-1$
        FILLED_CIRCLE.setSpotShown(true);
        FILLED_CIRCLE.setAlpha(102);
        footprints.add(FILLED_CIRCLE);
    }
}