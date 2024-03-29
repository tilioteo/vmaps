package org.vaadin.maps.client.ui;

import com.google.gwt.core.client.Duration;
import com.google.gwt.dom.client.Element;
import com.vaadin.shared.MouseEventDetails;
import org.vaadin.maps.client.geometry.LinearRing;
import org.vaadin.maps.client.geometry.Polygon;

/**
 * @author Kamil Morong
 */
public class VPolygonHandler extends VPathHandler {

    public static final String CLASSNAME = "v-polygonhandler";

    public VPolygonHandler() {
        super();
        setStyleName(CLASSNAME);
    }

    @Override
    protected void syntheticClick(MouseEventDetails details, Element relativeElement) {
        cleanMouseState();

        if (!active || frozen) {
            return;
        }

        boolean finish = false;
        boolean isDoubleClick = false;

        if (null == clickDuration) {
            clickDuration = new Duration();

        } else {
            if (clickDuration.elapsedMillis() <= DOUBLE_CLICK_THRESHOLD) {
                isDoubleClick = true;
                clickDuration = null;
            } else {
                clickDuration = new Duration();
            }
        }

        if (isDoubleClick
                && !(FinishStrategy.DoubleClick.equals(strategy) || FinishStrategy.NearStartClick.equals(strategy))) {
            return;
        }

        /*
         * if (clickHandlerSlave != null) {
         * clickHandlerSlave.syntheticClick(details, relativeElement); }
         */

        int[] xy = getMouseEventXY(mouseEventDetails, relativeElement);

        // first click
        // add start point and begin line drawing
        // create and insert start point
        if (null == startPoint) {
            prepareDrawing(xy[0], xy[1]);
            prepareLineString(xy);
        } else {
            if (canCloseLineString()) {
                if ((details.isShiftKey() && (FinishStrategy.AltClick.equals(strategy)
                        || (isDoubleClick && FinishStrategy.DoubleClick.equals(strategy))))
                        || FinishStrategy.NearStartClick.equals(strategy)) {
                    // finish drawing with closing line if shift key has been
                    // pressed
                    // and the click is in start point's circle
                    if (isWithinCircle(startPoint.getX(), startPoint.getY(), startPoint.getRadius(), xy[0], xy[1])) {
                        finish = true;
                        if (isDoubleClick) {
                            // remove last vertex from everywhere
                            removeLastLineStringVertex();
                            removeLastVertex();
                        }
                    }
                } else if ((FinishStrategy.AltClick.equals(strategy) && details.isAltKey())) {
                    // finish drawing when strategy conditions pass
                    finish = true;
                    // append last vertex
                    addLineStringVertex(xy);

                } else if (FinishStrategy.DoubleClick.equals(strategy) && isDoubleClick) {
                    finish = true;
                }
            }

            if (finish) {
                // close line
                closeLineString();

                Polygon polygon = new Polygon(new LinearRing(lineString));

                fireEvent(new GeometryEvent(VPolygonHandler.this, polygon));

                cleanDrawing();
                cleanLineString();
            } else {
                addLineSegment(xy[0], xy[1]);
                // append vertex
                addLineStringVertex(xy);
            }
        }
    }

}
