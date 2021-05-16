package org.opensourcephysics.cabrillo.tracker.auto;

import org.opensourcephysics.cabrillo.tracker.component.TTrack;
import org.opensourcephysics.cabrillo.tracker.auto.AutoTrackerCore.KeyFrame;


/**
 * @author Nikolai Avdeev aka NickKolok
 */
public class AutoTrackerFeedback {

    public void setSelectedTrack(TTrack track) {
    }

    public void onBeforeAddKeyframe(double x, double y) {
    }

    public void onAfterAddKeyframe(KeyFrame keyFrame) {
    }

    public void onSetTrack() {
    }

    public void onTrackUnbind(TTrack track) {
    }

    public void onTrackBind(TTrack track) {
    }

}