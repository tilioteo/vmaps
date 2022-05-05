package org.vaadin.maps.shared.ui.layer;

import org.vaadin.maps.shared.ui.AbstractLayerState;

/**
 * @author Kamil Morong
 */
public class WMSLayerState extends AbstractLayerState {
    public boolean singleTile = true;

    {
        primaryStyleName = "v-wmslayer";
    }
}
