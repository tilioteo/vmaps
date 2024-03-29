package org.vaadin.maps.client.ui.feature;

import com.google.gwt.event.dom.client.*;
import com.vaadin.client.MouseEventDetailsBuilder;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.DateUtility;
import org.vaadin.maps.client.drawing.Utils;
import org.vaadin.maps.client.io.ParseException;
import org.vaadin.maps.client.ui.MapUtility;
import org.vaadin.maps.client.ui.VVectorFeature;
import org.vaadin.maps.shared.ui.Style;
import org.vaadin.maps.shared.ui.feature.FeatureServerRpc;
import org.vaadin.maps.shared.ui.feature.VectorFeatureState;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.feature.VectorFeature.class)
public class VectorFeatureConnector extends AbstractComponentConnector {

    @Override
    protected void init() {
        super.init();

        getWidget().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MouseEventDetails mouseDetails = MouseEventDetailsBuilder.buildMouseEventDetails(event.getNativeEvent(),
                        getWidget().getElement());
                getRpcProxy(FeatureServerRpc.class).click(DateUtility.getTimestamp(), mouseDetails);
            }
        });

        getWidget().addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                MouseEventDetails mouseDetails = MouseEventDetailsBuilder.buildMouseEventDetails(event.getNativeEvent(),
                        getWidget().getElement());
                getRpcProxy(FeatureServerRpc.class).doubleClick(DateUtility.getTimestamp(), mouseDetails);
            }
        });

        getWidget().addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                getRpcProxy(FeatureServerRpc.class).mouseOver(DateUtility.getTimestamp());
            }
        });

        getWidget().addMouseOutHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                getRpcProxy(FeatureServerRpc.class).mouseOut(DateUtility.getTimestamp());
            }
        });
    }

    @Override
    public VVectorFeature getWidget() {
        return (VVectorFeature) super.getWidget();
    }

    @Override
    public VectorFeatureState getState() {
        return (VectorFeatureState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (stateChangeEvent.hasPropertyChanged("style")) {
            getWidget().setStyle(MapUtility.getStyleFromMap(getState().style, Style.DEFAULT));
        }
        if (stateChangeEvent.hasPropertyChanged("hoverStyle")) {
            getWidget().setHoverStyle(MapUtility.getStyleFromMap(getState().hoverStyle, null));
        }
        if (stateChangeEvent.hasPropertyChanged("hidden")) {
            getWidget().setHidden(getState().hidden);
        }
        if (stateChangeEvent.hasPropertyChanged("wkb")) {
            try {
                getWidget().setGeometry(Utils.hexWKBToGeometry(getState().wkb));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (stateChangeEvent.hasPropertyChanged("centroidX") || stateChangeEvent.hasPropertyChanged("centroidY")) {
            getWidget().setCentroid(getState().centroidX, getState().centroidY);
        }
        if (stateChangeEvent.hasPropertyChanged("offsetX") || stateChangeEvent.hasPropertyChanged("offsetY")) {
            getWidget().setTextOffset(getState().offsetX, getState().offsetY);
        }
        if (stateChangeEvent.hasPropertyChanged("text")) {
            getWidget().setText(getState().text);
        }
    }

}
