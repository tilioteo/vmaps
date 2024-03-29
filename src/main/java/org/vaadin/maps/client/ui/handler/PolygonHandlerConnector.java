package org.vaadin.maps.client.ui.handler;

import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.ui.VPolygonHandler;
import org.vaadin.maps.shared.ui.handler.PolygonHandlerState;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.handler.PolygonHandler.class)
public class PolygonHandlerConnector extends PathHandlerConnector {

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public VPolygonHandler getWidget() {
        return (VPolygonHandler) super.getWidget();
    }

    @Override
    public PolygonHandlerState getState() {
        return (PolygonHandlerState) super.getState();
    }

}
