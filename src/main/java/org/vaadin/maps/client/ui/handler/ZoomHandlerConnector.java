package org.vaadin.maps.client.ui.handler;

import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.DateUtility;
import org.vaadin.maps.client.ui.VZoomHandler;
import org.vaadin.maps.client.ui.VZoomHandler.ZoomEvent;
import org.vaadin.maps.client.ui.VZoomHandler.ZoomEventHandler;
import org.vaadin.maps.shared.ui.handler.ZoomHandlerRpc;
import org.vaadin.maps.shared.ui.handler.ZoomHandlerState;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.handler.ZoomHandler.class)
public class ZoomHandlerConnector extends LayerLayoutHandlerConnector implements ZoomEventHandler {

    @Override
    protected void init() {
        super.init();

        getWidget().addZoomEventHandler(this);
    }

    @Override
    public VZoomHandler getWidget() {
        return (VZoomHandler) super.getWidget();
    }

    @Override
    public ZoomHandlerState getState() {
        return (ZoomHandlerState) super.getState();
    }

    @Override
    public void zoom(ZoomEvent event) {
        getRpcProxy(ZoomHandlerRpc.class).zoomChange(DateUtility.getTimestamp(), event.getZoom());
    }

}
