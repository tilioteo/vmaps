package org.vaadin.maps.client.ui.control;

import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.ui.VZoomControl;
import org.vaadin.maps.shared.ui.control.ZoomControlState;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.control.ZoomControl.class)
public class ZoomControlConnector extends NavigateControlConnector {

    @Override
    public VZoomControl getWidget() {
        return (VZoomControl) super.getWidget();
    }

    @Override
    public ZoomControlState getState() {
        return (ZoomControlState) super.getState();
    }

}
