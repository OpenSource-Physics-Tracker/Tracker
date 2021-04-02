package org.opensourcephysics.cabrillo.tracker;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeSupport;

/**
 * This class contains options, which are used for AutoTracker,
 * and some basic logic to work with them.
 * The main purpose of this class is to separate user-defined options of AutoTracker
 * which refer (in most cases) to whole track or its big part
 * from internal variables, which AutoTracker uses and varies widely.
 * These options may be attached, for example, to a track,
 * or saved/loaded to/from .trk file,
 * if someone implements appropriate loaders ;-)
 *
 * @author Nikolai Avdeev aka NickKolok
 */
@Getter
@Setter
public class AutoTrackerOptions implements Cloneable {

    private int goodMatch = 4;
    private int evolveAlpha = 63;
    private int autoSkipCount = 2;

    /**
     * Positive for 1D, negative for 2D tracking.
     */
    private int lineSpread = -1;

    /**
     * 0 for ellipse, 1 for rect.
     */
    private int maskShapeType = 0;

    private double maskWidth = 16.0, maskHeight = 16.0;
    private boolean lookAhead = true;

    private final int possibleMatch = 1;

    public static final int MAX_EVOLVE_RATE = 100;

    public final PropertyChangeSupport changes = new PropertyChangeSupport(this);


    public void setGoodMatch(int goodMatch) {
        int oldMatch = this.goodMatch;
        this.goodMatch = goodMatch;
        changes.firePropertyChange("goodMatch", oldMatch, goodMatch);
    }

    public boolean isMatchGood(double match) {
        return match > goodMatch;
    }


    public boolean isMatchPossible(double match) {
        return match > possibleMatch;
    }


    public void setEvolveAlpha(int evolveAlpha) {
        int oldAlpha = this.evolveAlpha;
        this.evolveAlpha = evolveAlpha;
        changes.firePropertyChange("evolveAlpha", oldAlpha, evolveAlpha);
    }

    protected void setEvolveAlphaFromRate(int evolveRate) {
        double max = MAX_EVOLVE_RATE;
        int alpha = (int) (1.0 * evolveRate * 255 / max);
        if (evolveRate >= max) alpha = 255;
        if (evolveRate <= 0) alpha = 0;
        setEvolveAlpha(alpha);
    }


    public void setMaskWidth(double maskWidth) {
        if (this.maskWidth == maskWidth) {
            return;
        }
        double old = this.maskWidth;
        this.maskWidth = maskWidth;
        changes.firePropertyChange("maskWidth", old, this.maskWidth);
    }


    public void setMaskHeight(double maskHeight) {
        if (this.maskHeight == maskHeight) {
            return;
        }
        double old = this.maskHeight;
        this.maskHeight = maskHeight;
        changes.firePropertyChange("maskHeight", old, this.maskHeight);
    }

    public void setLineSpread(int spread) {
        if (this.lineSpread == spread) {
            return;
        }
        int old = this.lineSpread;
        this.lineSpread = spread;
        changes.firePropertyChange("lineSpread", old, this.lineSpread);
    }

    public int getPredictionLookBack() {
        int predictionLookBack = 4;
        return predictionLookBack;
    }

    public void setMaskShapeType(int maskShapeType) {
        if (this.maskShapeType == maskShapeType) {
            return;
        }
        this.maskShapeType = maskShapeType;
        int old = this.maskShapeType;
        changes.firePropertyChange("maskShapeType", old, maskShapeType);
    }

    public Shape getMaskShape() {
        switch (maskShapeType) {
            case 0:
                return new Ellipse2D.Double(0, 0, maskWidth, maskHeight);
            case 1:
                return new Rectangle2D.Double(0, 0, maskWidth, maskHeight);
            default:
                return null;
        }
    }
    // TODO: fire messages for all properties
    // TODO: cloning without cloning `changes`
    // TODO: fire message only if the property has been changed indeed
}