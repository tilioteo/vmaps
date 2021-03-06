/**
 * 
 */
package org.vaadin.maps.shared.ui.tile;

import org.vaadin.maps.shared.ui.ClickRpc;
import org.vaadin.maps.shared.ui.LoadRpc;

/**
 * @author Kamil Morong
 *
 */
public interface ProxyTileServerRpc extends ClickRpc, LoadRpc {

	public void updateClippedSize(int width, int height);

}
