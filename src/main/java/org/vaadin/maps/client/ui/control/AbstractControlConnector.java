package org.vaadin.maps.client.ui.control;

import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ui.AbstractSingleComponentContainerConnector;
import org.vaadin.maps.client.ui.AbstractControl;
import org.vaadin.maps.shared.ui.control.AbstractControlState;
import org.vaadin.maps.shared.ui.control.ControlClientRpc;

/**
 * @author Kamil Morong
 */
public abstract class AbstractControlConnector extends AbstractSingleComponentContainerConnector {

    @Override
    protected void init() {
        super.init();

        registerRpc(ControlClientRpc.class, new ControlClientRpc() {
            @Override
            public void activate() {
                getWidget().activate();
            }

            @Override
            public void deactivate() {
                getWidget().deactivate();
            }
        });
    }

    @Override
    public AbstractControl getWidget() {
        return (AbstractControl) super.getWidget();
    }

    @Override
    public AbstractControlState getState() {
        return (AbstractControlState) super.getState();
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        // nop
    }

}
