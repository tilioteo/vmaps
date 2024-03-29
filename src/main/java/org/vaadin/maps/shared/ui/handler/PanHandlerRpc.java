package org.vaadin.maps.shared.ui.handler;

import com.vaadin.shared.communication.ServerRpc;

/**
 * @author Kamil Morong
 */
public interface PanHandlerRpc extends ServerRpc {

    void panStart(long timestamp, int x, int y);

    void panEnd(long timestamp, int deltaX, int deltaY);

}
