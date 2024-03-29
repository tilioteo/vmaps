package org.vaadin.maps.ui.handler;

import com.tilioteo.common.event.MouseEvents;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Component;
import com.vaadin.util.ReflectTools;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import org.vaadin.maps.geometry.Utils;
import org.vaadin.maps.shared.ui.handler.PointHandlerServerRpc;
import org.vaadin.maps.shared.ui.handler.PointHandlerState;
import org.vaadin.maps.ui.control.Control;
import org.vaadin.maps.ui.feature.VectorFeature;
import org.vaadin.maps.ui.layer.VectorFeatureLayer;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Kamil Morong
 */
public class PointHandler extends FeatureHandler {

    /**
     * The last drawn feature
     */
    protected VectorFeature feature = null;
    /**
     * The drawing layer
     */
    protected VectorFeatureLayer layer = null;
    private final PointHandlerServerRpc rpc = new PointHandlerServerRpc() {

        @Override
        public void click(long timestamp, MouseEventDetails mouseDetails) {
            fireEvent(new ClickEvent(timestamp, PointHandler.this, mouseDetails));
        }

        @Override
        public void geometry(long timestamp, String wkb) {
            try {
                Geometry geometry = Utils.wkbHexToGeometry(wkb);
                if (layer != null && layer.getForLayer() != null) {
                    Utils.transformViewToWorld(geometry, layer.getForLayer().getViewWorldTransform());
                }
                VectorFeature feature = addNewFeature(geometry);
                fireEvent(new DrawFeatureEvent(timestamp, PointHandler.this, feature));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    };

    public PointHandler(Control control) {
        super(control);
        registerRpc(rpc);
    }

    protected VectorFeature addNewFeature(Geometry geometry) {
        if (geometry != null) {
            feature = new VectorFeature(geometry);
            feature.setStyle(featureStyle);
            layer.addComponent(feature);
            return feature;
        }
        return null;
    }

    @Override
    protected PointHandlerState getState() {
        return (PointHandlerState) super.getState();
    }

    @Override
    public void setLayer(VectorFeatureLayer layer) {
        this.layer = layer;
        getState().layer = layer;
    }

    @Override
    public boolean deactivate() {
        if (!super.deactivate())
            return false;

        cancel();

        return true;
    }

    /**
     * Adds the click listener.
     *
     * @param listener the Listener to be added.
     */
    public void addClickListener(ClickListener listener) {
        addListener(ClickEvent.class, listener, ClickListener.CLICK_METHOD);
    }

    /**
     * Removes the click listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removeClickListener(ClickListener listener) {
        removeListener(ClickEvent.class, listener, ClickListener.CLICK_METHOD);
    }

    /**
     * Interface for listening for a {@link ClickEvent} fired by a
     * {@link PointHandler}.
     */
    public interface ClickListener extends Serializable {

        public static final Method CLICK_METHOD = ReflectTools.findMethod(ClickListener.class, "click",
                ClickEvent.class);

        /**
         * Called when a drawing layer has been clicked.
         *
         * @param event An event containing information about the click.
         */
        public void click(ClickEvent event);

    }

    /**
     * Click event. This event is thrown, when the drawing layer is clicked.
     */
    public class ClickEvent extends MouseEvents.ClickEvent {

        private final Coordinate coordinate;

        /**
         * Constructor with details
         *
         * @param source The source where the click took place
         */
        public ClickEvent(long timestamp, Component source, MouseEventDetails details) {
            super(timestamp, source, details);
            this.coordinate = new Coordinate(details.getRelativeX(), details.getRelativeY());
        }

        /**
         * Returns the coordinate when the click took place. The position is in
         * coordinating system of layer.
         *
         * @return The position coordinate
         */
        public Coordinate getCoordinate() {
            return coordinate;
        }
    }

}
