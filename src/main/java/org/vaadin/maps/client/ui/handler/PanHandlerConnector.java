package org.vaadin.maps.client.ui.handler;

import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.DateUtility;
import org.vaadin.maps.client.ui.VPanHandler;
import org.vaadin.maps.client.ui.VPanHandler.PanEndEvent;
import org.vaadin.maps.client.ui.VPanHandler.PanEndEventHandler;
import org.vaadin.maps.client.ui.VPanHandler.PanStartEvent;
import org.vaadin.maps.client.ui.VPanHandler.PanStartEventHandler;
import org.vaadin.maps.shared.ui.handler.PanHandlerRpc;
import org.vaadin.maps.shared.ui.handler.PanHandlerState;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.handler.PanHandler.class)
public class PanHandlerConnector extends LayerLayoutHandlerConnector
        implements PanStartEventHandler, PanEndEventHandler {

    @Override
    protected void init() {
        super.init();

        getWidget().addPanStartEventHandler(this);
        getWidget().addPanEndEventHandler(this);
    }

    @Override
    public VPanHandler getWidget() {
        return (VPanHandler) super.getWidget();
    }

    @Override
    public PanHandlerState getState() {
        return (PanHandlerState) super.getState();
    }

    @Override
    public void panStart(PanStartEvent event) {
        getRpcProxy(PanHandlerRpc.class).panStart(DateUtility.getTimestamp(), event.getX(), event.getY());
    }

    @Override
    public void panEnd(PanEndEvent event) {
        getRpcProxy(PanHandlerRpc.class).panEnd(DateUtility.getTimestamp(), event.getDeltaX(), event.getDeltaY());
    }

}
