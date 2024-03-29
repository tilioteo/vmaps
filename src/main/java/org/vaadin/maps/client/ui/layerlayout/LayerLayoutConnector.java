package org.vaadin.maps.client.ui.layerlayout;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.Util;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.communication.StateChangeEvent.StateChangeHandler;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.ui.AbstractLayoutConnector;
import org.vaadin.maps.client.ui.LayoutClickEventHandler;
import org.vaadin.maps.client.ui.SizeChangeHandler;
import org.vaadin.maps.client.ui.VLayerLayout;
import org.vaadin.maps.shared.ui.LayoutClickRpc;
import org.vaadin.maps.shared.ui.layerlayout.LayerLayoutServerRpc;
import org.vaadin.maps.shared.ui.layerlayout.LayerLayoutState;

import java.util.Iterator;
import java.util.List;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.LayerLayout.class)
public class LayerLayoutConnector extends AbstractLayoutConnector implements ElementResizeListener {

    private final LayoutClickEventHandler clickEventHandler = new LayoutClickEventHandler(this) {

        @Override
        protected ComponentConnector getChildComponent(Element element) {
            return getConnectorForElement(element);
        }

        @Override
        protected LayoutClickRpc getLayoutClickRPC() {
            return getRpcProxy(LayerLayoutServerRpc.class);
        }

        ;
    };

    private final StateChangeHandler childStateChangeHandler = new StateChangeHandler() {
        @Override
        public void onStateChanged(StateChangeEvent stateChangeEvent) {
            ComponentConnector child = (ComponentConnector) stateChangeEvent.getConnector();
            List<String> childStyles = child.getState().styles;
            if (childStyles == null) {
                getWidget().setWidgetWrapperStyleNames(child.getWidget(), (String[]) null);
            } else {
                getWidget().setWidgetWrapperStyleNames(child.getWidget(),
                        childStyles.toArray(new String[childStyles.size()]));
            }
        }
    };

    private int width = 0;
    private int height = 0;

    @Override
    protected void init() {
        super.init();
        getWidget().setLayoutManager(getLayoutManager());
        getLayoutManager().addElementResizeListener(getWidget().getElement(), this);
    }

    /**
     * Returns the deepest nested child component which contains "element". The
     * child component is also returned if "element" is part of its caption.
     *
     * @param element An element that is a nested sub element of the root element in
     *                this layout
     * @return The Paintable which the element is a part of. Null if the element
     * belongs to the layout and not to a child.
     */
    protected ComponentConnector getConnectorForElement(Element element) {
        return Util.getConnectorForElement(getConnection(), getWidget(), element);
    }

    @Override
    public void updateCaption(ComponentConnector component) {
        // nop
    }

    @Override
    public VLayerLayout getWidget() {
        return (VLayerLayout) super.getWidget();
    }

    @Override
    public LayerLayoutState getState() {
        return (LayerLayoutState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
        clickEventHandler.handleEventHandlerRegistration();

        for (ComponentConnector child : getChildComponents()) {
            setChildWidgetPosition(child);
        }
    }

    private void setChildWidgetPosition(ComponentConnector child) {
        getWidget().setWidgetOrder(child.getWidget(), getState().connectorToCssPosition.get(child.getConnectorId()));
    }

    ;

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        for (ComponentConnector child : getChildComponents()) {
            if (!getWidget().contains(child.getWidget())) {
                getWidget().add(child.getWidget());
                child.addStateChangeHandler(childStateChangeHandler);
                setChildWidgetPosition(child);
            }
        }
        for (ComponentConnector oldChild : event.getOldChildren()) {
            if (oldChild.getParent() != this) {
                getWidget().remove(oldChild.getWidget());
                oldChild.removeStateChangeHandler(childStateChangeHandler);
            }
        }

        getWidget().cleanupWrappers();
    }

    @Override
    public void onElementResize(ElementResizeEvent e) {
        LayoutManager layoutManager = getLayoutManager();
        int newWidth = layoutManager.getOuterWidth(e.getElement());
        int newHeight = layoutManager.getOuterHeight(e.getElement());

        // inform layers of size changed
        Iterator<Widget> iterator;
        for (iterator = getWidget().iterator(); iterator.hasNext(); ) {
            Widget child = iterator.next();
            if (child instanceof SizeChangeHandler) {
                ((SizeChangeHandler) child).onSizeChange(width, height, newWidth, newHeight);
            }
        }

        width = newWidth;
        height = newHeight;

        getRpcProxy(LayerLayoutServerRpc.class).updateMeasuredSize(newWidth, newHeight);
    }

}
