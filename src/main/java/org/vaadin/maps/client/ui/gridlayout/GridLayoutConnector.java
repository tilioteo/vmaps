package org.vaadin.maps.client.ui.gridlayout;

import com.google.gwt.dom.client.Element;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.shared.ui.Connect;
import org.vaadin.maps.client.ui.AbstractLayoutConnector;
import org.vaadin.maps.client.ui.LayoutClickEventHandler;
import org.vaadin.maps.client.ui.Tile;
import org.vaadin.maps.client.ui.Tile.SizeChangeHandler;
import org.vaadin.maps.client.ui.Tile.TileLoadHandler;
import org.vaadin.maps.client.ui.VGridLayout;
import org.vaadin.maps.client.ui.tile.ImageTileConnector;
import org.vaadin.maps.shared.ui.LayoutClickRpc;
import org.vaadin.maps.shared.ui.gridlayout.GridLayoutServerRpc;
import org.vaadin.maps.shared.ui.gridlayout.GridLayoutState;
import org.vaadin.maps.shared.ui.gridlayout.GridLayoutState.ChildComponentData;

/**
 * @author Kamil Morong
 */
@Connect(org.vaadin.maps.ui.GridLayout.class)
public class GridLayoutConnector extends AbstractLayoutConnector {

    private final LayoutClickEventHandler clickEventHandler = new LayoutClickEventHandler(this) {

        @Override
        protected ComponentConnector getChildComponent(Element element) {
            return getWidget().getComponent(element);
        }

        @Override
        protected LayoutClickRpc getLayoutClickRPC() {
            return getRpcProxy(GridLayoutServerRpc.class);
        }

    };

    private final SizeChangeHandler sizeChangeHandler = new SizeChangeHandler() {
        @Override
        public void onSizeChange(Tile tile, int oldWidth, int oldHeight, int newWidth, int newHeight) {
            VGridLayout gridLayout = getWidget();

            if (gridLayout.getWidgetCount() == 1) {
                // center tile
                int dx = (getWidget().getMeasuredWidth() - newWidth) / 2;
                int dy = (getWidget().getMeasuredHeight() - newHeight) / 2;
                gridLayout.setWidgetPosition(tile.asWidget(), dx, dy);
            }
        }
    };

    private final TileLoadHandler loadHandler = new TileLoadHandler() {
        @Override
        public void onLoad(Tile tile) {
            VGridLayout gridLayout = getWidget();

            if (gridLayout.getWidgetCount() == 1) {
                // reset shift
                gridLayout.setShift(0, 0);
            }
        }
    };

    @Override
    public void init() {
        super.init();
        getWidget().client = getConnection();
    }

    @Override
    public void updateCaption(ComponentConnector connector) {
        // nop

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
        ChildComponentData childComponentData = getState().childData.get(child);
        getWidget().setWidgetPosition(child.getWidget(), childComponentData.left, childComponentData.top);
    }

    @Override
    public void onConnectorHierarchyChange(ConnectorHierarchyChangeEvent event) {
        for (ComponentConnector child : getChildComponents()) {
            if (!getWidget().contains(child.getWidget())) {
                if (child instanceof ImageTileConnector) {
                    ((ImageTileConnector) child).setSizeChangeHandler(sizeChangeHandler);
                    ((ImageTileConnector) child).setTileLoadHandler(loadHandler);
                }
                getWidget().add(child.getWidget());
                // child.addStateChangeHandler(childStateChangeHandler);
                setChildWidgetPosition(child);
            }
        }
        for (ComponentConnector oldChild : event.getOldChildren()) {
            if (oldChild.getParent() != this) {
                getWidget().remove(oldChild.getWidget());
                // oldChild.removeStateChangeHandler(childStateChangeHandler);
                if (oldChild instanceof ImageTileConnector) {
                    ((ImageTileConnector) oldChild).setSizeChangeHandler(null);
                    ((ImageTileConnector) oldChild).setTileLoadHandler(null);
                }
            }
        }

        getWidget().cleanupWrappers();
    }

    @Override
    public VGridLayout getWidget() {
        return (VGridLayout) super.getWidget();
    }

    @Override
    public GridLayoutState getState() {
        return (GridLayoutState) super.getState();
    }

}
