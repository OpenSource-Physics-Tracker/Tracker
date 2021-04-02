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

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * This Icon centers and fills the shape specified in its constructor.
 *
 * @author Douglas Brown
 */
@Getter
@Setter
public class ShapeIcon implements Icon {

    private final int iconWidth;
    private final int iconHeight;

    private final Shape shape;
    private final Shape decoration;

    private Color color = Color.black;
    private Color decoColor = Color.black;

    /**
     * Centers the shape horizontally
     */
    private final double offsetX;

    /**
     * Centers the shape vertically
     */
    private final double offsetY;

    /**
     * Constructs a ShapeIcon.
     *
     * @param shape      the shape to draw
     * @param decoration a decorating shape to draw
     * @param width      width of the icon
     * @param height     height of the icon
     */
    public ShapeIcon(Shape shape, Shape decoration, int width, int height) {
        iconWidth = width;
        iconHeight = height;
        this.shape = shape;
        this.decoration = decoration;
        Rectangle rect = shape == null ? new Rectangle() : shape.getBounds();
        if (decoration != null)
            rect = rect.union(decoration.getBounds());
        offsetX = iconWidth / 2 - rect.width / 2 - rect.x;
        offsetY = iconHeight / 2 - rect.height / 2 - rect.y;
    }

    /**
     * Constructs a ShapeIcon.
     *
     * @param shape  the shape to draw
     * @param width  width of the icon
     * @param height height of the icon
     */
    public ShapeIcon(Shape shape, int width, int height) {
        this(shape, null, width, height);
    }


    /**
     * Sets the colors.
     *
     * @param color           the desired color
     * @param decorationColor the desired decoration color
     */
    public void setColor(Color color, Color decorationColor) {
        this.color = color;
        decoColor = decorationColor;
    }

    /**
     * Paints the icon.
     *
     * @param c  the component on which it is painted
     * @param _g the graphics context
     * @param x  the x coordinate of the icon
     * @param y  the y coordinate of the icon
     */
    public void paintIcon(Component c, Graphics _g, int x, int y) {
        Graphics2D g = (Graphics2D) _g;
        AffineTransform at = AffineTransform.getTranslateInstance(
                x + offsetX, y + offsetY);

        // save current graphics paint and clip
        Paint gPaint = g.getPaint();
        Shape gClip = g.getClip();

        // render shape(s)
        g.setPaint(color);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.clipRect(x, y, iconWidth, iconHeight);

        // paint shape, if any
        if (shape != null) {
            g.fill(at.createTransformedShape(shape));
        }

        // paint decoration, if any
        if (decoration != null) {
            g.setPaint(decoColor);
            g.fill(at.createTransformedShape(decoration));
        }
        // restore graphics paint and clip
        g.setPaint(gPaint);
        g.setClip(gClip);
    }
}