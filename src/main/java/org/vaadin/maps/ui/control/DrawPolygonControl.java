package org.vaadin.maps.ui.control;

import org.vaadin.maps.shared.ui.Style;
import org.vaadin.maps.shared.ui.control.DrawPolygonControlState;
import org.vaadin.maps.ui.StyleUtility;
import org.vaadin.maps.ui.handler.PathHandler.FinishStrategy;
import org.vaadin.maps.ui.handler.PolygonHandler;
import org.vaadin.maps.ui.layer.VectorFeatureLayer;

/**
 * @author Kamil Morong
 */
public class DrawPolygonControl extends DrawFeatureControl<PolygonHandler> {

    private Style startPointStyle = null;
    private Style startPointHoverStyle = null;
    private Style lineStyle = null;
    private Style vertexStyle = null;

    public DrawPolygonControl(VectorFeatureLayer layer) {
        super(layer);

        setStartPointStyle(Style.DEFAULT_DRAW_START_POINT);
        setStartPointHoverStyle(Style.DEFAULT_HOVER_START_POINT);
        setLineStyle(Style.DEFAULT_DRAW_LINE);
        setVertexStyle(Style.DEFAULT_DRAW_VERTEX);
    }

    @Override
    protected DrawPolygonControlState getState() {
        return (DrawPolygonControlState) super.getState();
    }

    public Style getStartPointStyle() {
        return startPointStyle;
    }

    public void setStartPointStyle(Style style) {
        this.startPointStyle = style;
        getState().startPointStyle = StyleUtility.getStyleMap(style);
        markAsDirty();
    }

    public Style getStartPointHoverStyle() {
        return startPointHoverStyle;
    }

    public void setStartPointHoverStyle(Style style) {
        this.startPointHoverStyle = style;
        getState().startPointHoverStyle = StyleUtility.getStyleMap(style);
        markAsDirty();
    }

    public Style getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(Style style) {
        this.lineStyle = style;
        getState().lineStyle = StyleUtility.getStyleMap(style);
        markAsDirty();
    }

    public Style getVertexStyle() {
        return vertexStyle;
    }

    public void setVertexStyle(Style style) {
        this.vertexStyle = style;
        getState().vertexStyle = StyleUtility.getStyleMap(style);
        markAsDirty();
    }

    public FinishStrategy getStrategy() {
        return getHandler() != null ? getHandler().getStrategy() : null;
    }

    public void setStrategy(FinishStrategy finishStrategy) {
        if (getHandler() != null) {
            getHandler().setStrategy(finishStrategy);
        }
    }

}
