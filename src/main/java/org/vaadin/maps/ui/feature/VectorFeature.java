package org.vaadin.maps.ui.feature;

import com.tilioteo.common.event.MouseEvents;
import com.tilioteo.common.event.TimekeepingComponentEvent;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Component;
import com.vaadin.util.ReflectTools;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.vaadin.maps.geometry.Utils;
import org.vaadin.maps.server.ViewWorldTransform;
import org.vaadin.maps.server.ViewWorldTransform.TransformChangeEvent;
import org.vaadin.maps.server.ViewWorldTransform.TransformChangeListener;
import org.vaadin.maps.shared.ui.Style;
import org.vaadin.maps.shared.ui.feature.FeatureServerRpc;
import org.vaadin.maps.shared.ui.feature.VectorFeatureState;
import org.vaadin.maps.ui.StyleUtility;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Kamil Morong
 */
public class VectorFeature extends AbstractFeature implements TransformChangeListener {

    private final FeatureServerRpc rpc = new FeatureServerRpc() {
        @Override
        public void click(long timestamp, MouseEventDetails mouseDetails) {
            fireEvent(new ClickEvent(timestamp, VectorFeature.this, mouseDetails));
        }

        @Override
        public void doubleClick(long timestamp, MouseEventDetails mouseDetails) {
            fireEvent(new DoubleClickEvent(timestamp, VectorFeature.this, mouseDetails));
        }

        @Override
        public void mouseOver(long timestamp) {
            fireEvent(new MouseOverEvent(timestamp, VectorFeature.this));
        }

        @Override
        public void mouseOut(long timestamp) {
            fireEvent(new MouseOutEvent(timestamp, VectorFeature.this));
        }
    };

    private Geometry geometry = null;
    private Style style = null;
    private Style hoverStyle = null;

    private Style inheritedStyle = null;
    private Style inheritedHoverStyle = null;

    public VectorFeature() {
        super();
        registerRpc(rpc);

        setStyle(null);
    }

    public VectorFeature(Geometry geometry) {
        this();
        setGeometry(geometry);
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
        getState().wkb = Utils.geometryToWKBHex(geometry);

        setGeometryCentroid(this.geometry);
    }

    private void setGeometryCentroid(Geometry geometry) {
        if (geometry != null) {
            Point centroid = geometry.getCentroid();
            double x = centroid.getX();
            double y = centroid.getY();
            if (Double.isNaN(x) || Double.isInfinite(x)) {
                getState().centroidX = null;
            } else {
                getState().centroidX = x;
            }
            if (Double.isNaN(y) || Double.isInfinite(y)) {
                getState().centroidY = null;
            } else {
                getState().centroidY = y;
            }
        } else {
            getState().centroidX = null;
            getState().centroidY = null;
        }
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;

        getState().style = StyleUtility.getStyleMap(this.style != null ? this.style : inheritedStyle);
        markAsDirty();
    }

    public Style getHoverStyle() {
        return hoverStyle;
    }

    public void setHoverStyle(Style style) {
        hoverStyle = style;

        getState().hoverStyle = StyleUtility.getStyleMap(hoverStyle != null ? hoverStyle : style);
        markAsDirty();
    }

    public Style getInheritedStyle() {
        return inheritedStyle;
    }

    public void setInheritedStyle(Style style) {
        inheritedStyle = style;

        if (null == this.style) {
            getState().style = StyleUtility.getStyleMap(inheritedStyle);
        }
    }

    public Style getInheritedHoverStyle() {
        return inheritedHoverStyle;
    }

    public void setInheritedHoverStyle(Style style) {
        inheritedHoverStyle = style;

        if (null == hoverStyle) {
            getState().hoverStyle = StyleUtility.getStyleMap(inheritedHoverStyle);
        }
    }

    public String getText() {
        return getState().text;
    }

    public void setText(String text) {
        getState().text = text;
    }

    public void setTextOffset(double x, double y) {
        getState().offsetX = x;
        getState().offsetY = y;
    }

    public boolean isHidden() {
        return getState().hidden;
    }

    public void setHidden(boolean hidden) {
        getState().hidden = hidden;
    }

    @Override
    protected VectorFeatureState getState() {
        return (VectorFeatureState) super.getState();
    }

    /**
     * Adds the feature click listener.
     *
     * @param listener the Listener to be added.
     */
    public void addClickListener(ClickListener listener) {
        addListener(ClickEvent.class, listener, ClickListener.FEATURE_CLICK_METHOD);
    }

    /**
     * Removes the feature click listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removeClickListener(ClickListener listener) {
        removeListener(ClickEvent.class, listener, ClickListener.FEATURE_CLICK_METHOD);
    }

    /**
     * Adds the feature double-click listener.
     *
     * @param listener the Listener to be added.
     */
    public void addDoubleClickListener(DoubleClickListener listener) {
        addListener(DoubleClickEvent.class, listener, DoubleClickListener.FEATURE_DOUBLE_CLICK_METHOD);
    }

    /**
     * Removes the feature double-click listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removeDoubleClickListener(DoubleClickListener listener) {
        removeListener(DoubleClickEvent.class, listener, DoubleClickListener.FEATURE_DOUBLE_CLICK_METHOD);
    }

    /**
     * Adds the feature mouse over listener.
     *
     * @param listener the Listener to be added.
     */
    public void addMouseOverListener(MouseOverListener listener) {
        addListener(MouseOverEvent.class, listener, MouseOverListener.FEATURE_MOUSE_OVER_METHOD);
    }

    /**
     * Removes the feature mouse over listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removeMouseOverListener(MouseOverListener listener) {
        removeListener(MouseOverEvent.class, listener, MouseOverListener.FEATURE_MOUSE_OVER_METHOD);
    }

    /**
     * Adds the feature mouse out listener.
     *
     * @param listener the Listener to be added.
     */
    public void addMouseOutListener(MouseOutListener listener) {
        addListener(MouseOutEvent.class, listener, MouseOutListener.FEATURE_MOUSE_OUT_METHOD);
    }

    /**
     * Removes the feature mouse out listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removeMouseOutListener(MouseOutListener listener) {
        removeListener(MouseOutEvent.class, listener, MouseOutListener.FEATURE_MOUSE_OUT_METHOD);
    }

    @Override
    public void onTransformChange(TransformChangeEvent event) {
        transformToView(event.getViewWorldTransform());
    }

    private Geometry transformGeometryToView(Geometry worldGeometry, ViewWorldTransform viewWorldTransform) {
        if (worldGeometry != null && viewWorldTransform != null) {
            Geometry clone = (Geometry) worldGeometry.clone();

            Utils.transformWorldToView(clone, viewWorldTransform);
            return clone;
        }
        return null;
    }

    public void transformToView(ViewWorldTransform viewWorldTransform) {
        if (viewWorldTransform != null && viewWorldTransform.getViewWorldRatio() != 0) {
            Geometry viewGeometry = transformGeometryToView(this.geometry, viewWorldTransform);

            getState().wkb = Utils.geometryToWKBHex(viewGeometry);
            setGeometryCentroid(viewGeometry);

        } else {
            setGeometry(this.geometry);
        }
    }

    /**
     * Interface for listening for a {@link ClickEvent} fired by a
     * {@link VectorFeature}.
     */
    public interface ClickListener extends Serializable {

        Method FEATURE_CLICK_METHOD = ReflectTools.findMethod(ClickListener.class, "click",
                ClickEvent.class);

        /**
         * Called when a {@link VectorFeature} has been clicked. A reference to
         * the feature is given by {@link ClickEvent#getFeature()}.
         *
         * @param event An event containing information about the click.
         */
        void click(ClickEvent event);

    }

    /**
     * Interface for listening for a {@link DoubleClickEvent} fired by a
     * {@link VectorFeature}.
     */
    public interface DoubleClickListener extends Serializable {

        Method FEATURE_DOUBLE_CLICK_METHOD = ReflectTools.findMethod(DoubleClickListener.class,
                "doubleClick", DoubleClickEvent.class);

        /**
         * Called when a {@link VectorFeature} has been double-clicked. A
         * reference to the feature is given by
         * {@link DoubleClickEvent#getFeature()}.
         *
         * @param event An event containing information about the click.
         */
        void doubleClick(DoubleClickEvent event);

    }

    /**
     * Interface for listening for a {@link MouseOverEvent} fired by a
     * {@link VectorFeature}.
     */
    public interface MouseOverListener extends Serializable {

        Method FEATURE_MOUSE_OVER_METHOD = ReflectTools.findMethod(MouseOverListener.class,
                "mouseOver", MouseOverEvent.class);

        /**
         * Called when a mouse pointer enters {@link VectorFeature}. A
         * reference to the feature is given by
         * {@link MouseOverEvent#getSource()}.
         *
         * @param event An event containing information about the source.
         */
        void mouseOver(MouseOverEvent event);

    }

    /**
     * Interface for listening for a {@link MouseOutEvent} fired by a
     * {@link VectorFeature}.
     */
    public interface MouseOutListener extends Serializable {

        Method FEATURE_MOUSE_OUT_METHOD = ReflectTools.findMethod(MouseOutListener.class,
                "mouseOut", MouseOutEvent.class);

        /**
         * Called when mouse pointer leaves {@link VectorFeature}. A
         * reference to the feature is given by
         * {@link MouseOutEvent#getSource()}.
         *
         * @param event An event containing information about the source.
         */
        void mouseOut(MouseOutEvent event);

    }

    /**
     * Click event. This event is thrown, when the vector feature is clicked.
     */
    public static class ClickEvent extends MouseEvents.ClickEvent {

        public ClickEvent(long timestamp, Component source) {
            super(timestamp, source, null);
        }

        /**
         * Constructor with mouse details
         *
         * @param source  The source where the click took place
         * @param details Details about the mouse click
         */
        public ClickEvent(long timestamp, Component source, MouseEventDetails details) {
            super(timestamp, source, details);
        }

        /**
         * Gets the VectorFeature where the event occurred.
         *
         * @return the Source of the event.
         */
        public VectorFeature getFeature() {
            return (VectorFeature) getSource();
        }

    }

    public static class MouseOverEvent extends TimekeepingComponentEvent {

        public MouseOverEvent(long timestamp, Component source) {
            super(timestamp, source);
        }

        /**
         * Gets the VectorFeature where the event occurred.
         *
         * @return the Source of the event.
         */
        public VectorFeature getFeature() {
            return (VectorFeature) getSource();
        }

    }

    public static class MouseOutEvent extends TimekeepingComponentEvent {

        public MouseOutEvent(long timestamp, Component source) {
            super(timestamp, source);
        }

        /**
         * Gets the VectorFeature where the event occurred.
         *
         * @return the Source of the event.
         */
        public VectorFeature getFeature() {
            return (VectorFeature) getSource();
        }
    }

    public class DoubleClickEvent extends ClickEvent {

        public DoubleClickEvent(long timestamp, Component source) {
            super(timestamp, source);
        }

        public DoubleClickEvent(long timestamp, Component source, MouseEventDetails details) {
            super(timestamp, source, details);
        }

    }

}
