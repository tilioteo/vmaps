/**
 * 
 */
package org.vaadin.maps.client.ui.tile;

import org.vaadin.maps.client.DateUtility;
import org.vaadin.maps.shared.ui.tile.ProxyTileServerRpc;

import com.google.gwt.dom.client.NativeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.ClickEventHandler;
import com.vaadin.shared.MouseEventDetails;

/**
 * @author Kamil Morong
 *
 */
@SuppressWarnings("serial")
public abstract class ProxyTileConnector extends AbstractComponentConnector {

	protected final ClickEventHandler clickEventHandler = new ClickEventHandler(this) {
		@Override
		protected void fireClick(NativeEvent event, MouseEventDetails mouseDetails) {
			getRpcProxy(ProxyTileServerRpc.class).click(DateUtility.getTimestamp(), mouseDetails);
		}

	};

	@Override
	public void onStateChanged(StateChangeEvent stateChangeEvent) {
		super.onStateChanged(stateChangeEvent);

		clickEventHandler.handleEventHandlerRegistration();
	}

}
