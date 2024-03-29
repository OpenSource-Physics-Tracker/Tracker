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

import org.opensourcephysics.cabrillo.tracker.tracker.TrackerPanel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * This is a view of a tracker panel that can be added to a TViewChooser.
 * Classes that implement TView must descend from JComponent.
 *
 * @author Douglas Brown
 */
public interface TView extends PropertyChangeListener {

    /**
     * Initializes the view
     */
    void init();

    /**
     * Refreshes the view
     */
    void refresh();

    /**
     * Cleans up the view
     */
    void cleanup();

    /**
     * Disposes of the view
     */
    void dispose();

    /**
     * Gets the TrackerPanel containing the track data
     *
     * @return the tracker panel containing the data to be viewed
     */
    TrackerPanel getTrackerPanel();

    /**
     * Gets the name of the view
     *
     * @return the name of the view
     */
    String getViewName();

    /**
     * Gets the icon for this view
     *
     * @return the icon for the view
     */
    Icon getViewIcon();

    /**
     * Gets the toolbar components for this view
     *
     * @return an ArrayList of components to be added to a toolbar
     */
    ArrayList<Component> getToolBarComponents();

    /**
     * Returns true if this view is in a custom state.
     *
     * @return true if in a custom state, false if in the default state
     */
    boolean isCustomState();
}