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
package org.opensourcephysics.cabrillo.tracker.configuration;

import org.opensourcephysics.cabrillo.tracker.tracker.TrackerPanel;
import org.opensourcephysics.controls.XML;
import org.opensourcephysics.controls.XMLControl;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * This manages a set of enabled configuration properties.
 *
 * @author Douglas Brown
 * @version 1.3 Aug 2004
 */
public class Configuration {

    public Set<String> enabled;

    /**
     * Creates an empty Configuration.
     */
    public Configuration() {
        enabled = new TreeSet<>();
    }

    /**
     * Creates a Configuration and initializes it with a configuration.
     *
     * @param enabled a Set of enabled properties
     */
    public Configuration(Set<String> enabled) {
        this.enabled = enabled;
    }

    /**
     * Creates a Configuration and initializes it with the specified tracker panel
     * configuration.
     *
     * @param trackerPanel the tracker panel
     */
    public Configuration(TrackerPanel trackerPanel) {
        enabled = trackerPanel.getEnabled();
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
            Configuration config = (Configuration) obj;
            // save the configuration
            control.setValue("enabled", config.enabled); //$NON-NLS-1$
        }

        /**
         * Creates an object.
         *
         * @param control the control
         * @return the newly created object
         */
        public Object createObject(XMLControl control) {
            return new Configuration();
        }

        /**
         * Loads an object with data from an XMLControl.
         *
         * @param control the control
         * @param obj     the object
         * @return the loaded object
         */
        public Object loadObject(XMLControl control, Object obj) {
            Configuration config = (Configuration) obj;
            // load the configuration
            Object set = control.getObject("enabled"); //$NON-NLS-1$
            if (set != null) {
                TreeSet<String> enabled = new TreeSet<>();
                for (Object next : (Collection) set) {
                    enabled.add((String) next);
                }
                config.enabled = enabled;
            }
            return obj;
        }
    }
}
