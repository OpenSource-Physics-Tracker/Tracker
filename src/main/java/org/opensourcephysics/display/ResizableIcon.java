package org.opensourcephysics.display;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ResizableIcon implements Icon {

    public ResizableIcon(Icon icon) {
        while (icon instanceof ResizableIcon) {
            icon = ((ResizableIcon) icon).icon;
        }
        this.icon = icon;
        baseWidth = width = icon.getIconWidth();
        baseHeight = height = icon.getIconHeight();
    }

    public ResizableIcon(String location) {
        this(new ImageIcon(location));
    }

    protected BufferedImage baseImage;

    protected Icon icon;

    protected int baseWidth;
    protected int baseHeight;

    protected int width;
    protected int height;


    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    /**
     * Gets the base icon which is resized by this ResizableIcon.
     *
     * @return the base icon
     */
    public Icon getBaseIcon() {
        return icon;
    }

    /**
     * Magnifies the icon by a specified integer factor.
     *
     * @param factor the factor
     */
    public void resize(int factor) {
        int n = Math.max(factor, 1);
        width = n * baseWidth;
        height = n * baseHeight;
    }

    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {

        if (baseImage == null || baseImage.getWidth() != baseWidth || baseImage.getHeight() != baseHeight) {

            baseImage = new BufferedImage(baseWidth, baseHeight, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2 = baseImage.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, baseWidth, baseHeight);
        g2.setComposite(AlphaComposite.SrcOver);
        icon.paintIcon(c, g2, 0, 0);
        g.drawImage(baseImage, x, y, width, height, c);
    }
}