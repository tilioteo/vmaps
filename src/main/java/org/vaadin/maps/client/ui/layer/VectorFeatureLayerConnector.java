package org.vaadin.maps.client.ui.layer;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.ui.AbstractLayerConnector;
import org.vaadin.maps.client.ui.VVectorFeatureLayer;
import org.vaadin.maps.shared.ui.layer.VectorFeatureLayerState;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.layer.VectorFeatureLayer.class)
public class VectorFeatureLayerConnector extends AbstractLayerConnector {

    @Override
    public VVectorFeatureLayer getWidget() {
        return (VVectorFeatureLayer) super.getWidget();
    }

    @Override
    public VectorFeatureLayerState getState() {
        return (VectorFeatureLayerState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("fixed")) {
            getWidget().setFixed(getState().fixed);
        }
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent connectorHierarchyChangeEvent) {
        // We always have 1 child, unless the child is hidden
        Widget content = getContentWidget();
        if (content != null) {
            getWidget().setWidget(content);
        }
    }

}
