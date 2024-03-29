package org.vaadin.maps.ui.tile;

import com.tilioteo.common.event.MouseEvents.ClickEvent;
import com.tilioteo.common.event.MouseEvents.ClickListener;
import com.tilioteo.common.event.TimekeepingComponentEvent;
import com.vaadin.shared.EventId;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.util.ReflectTools;
import org.vaadin.maps.server.TileResource;
import org.vaadin.maps.shared.ui.tile.ProxyTileServerRpc;
import org.vaadin.maps.shared.ui.tile.ProxyTileState;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author Kamil Morong
 */
public abstract class AbstractProxyTile<T extends TileResource> extends AbstractTile {

    private int clippedWidth = 0;
    private int clippedHeight = 0;
    protected final ProxyTileServerRpc rpc = new ProxyTileServerRpc() {
        @Override
        public void load(long timestamp) {
            fireEvent(new LoadEvent(timestamp, AbstractProxyTile.this));
        }

        @Override
        public void error(long timestamp) {
            fireEvent(new ErrorEvent(timestamp, AbstractProxyTile.this));
        }

        @Override
        public void click(long timestamp, MouseEventDetails mouseDetails) {
            fireEvent(new ClickEvent(timestamp, AbstractProxyTile.this, mouseDetails));
        }

        @Override
        public void updateClippedSize(int width, int height) {
            int oldWidth = AbstractProxyTile.this.clippedWidth;
            int oldHeight = AbstractProxyTile.this.clippedHeight;
            AbstractProxyTile.this.clippedWidth = width;
            AbstractProxyTile.this.clippedHeight = height;
            clippedSizeChanged(oldWidth, oldHeight, width, height);
        }
    };

    /**
     * Creates a new empty Tile.
     */
    public AbstractProxyTile() {
        registerRpc(rpc);
    }

    /**
     * Creates a new Image whose contents are loaded from given resource. The
     * dimensions are assumed if possible. The type is guessed from resource.
     *
     * @param source the Source of the embedded object.
     */
    public AbstractProxyTile(T source) {
        this();
        setSource(source);
    }

    @Override
    protected ProxyTileState getState() {
        return (ProxyTileState) super.getState();
    }

    /**
     * Get the object source resource.
     *
     * @return the source
     */
    @SuppressWarnings("unchecked")
    public T getSource() {
        return (T) getTileResource();
    }

    /**
     * Sets the object source resource. The dimensions are assumed if possible.
     * The type is guessed from resource.
     *
     * @param source the source to set.
     */
    public void setSource(T source) {
        setTileResource(source);
    }

    protected int getClippedWidth() {
        return clippedWidth;
    }

    protected int getClippedHeight() {
        return clippedHeight;
    }

    protected abstract void clippedSizeChanged(int oldWidth, int oldHeight, int newWidth, int newHeight);

    /**
     * Adds the tile load listener.
     *
     * @param listener the Listener to be added.
     */
    public void addLoadListener(LoadListener listener) {
        addListener(LoadEvent.class, listener, LoadListener.TILE_LOAD_METHOD);
    }

    /**
     * Removes the tile load listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removeLoadListener(LoadListener listener) {
        removeListener(LoadEvent.class, listener, LoadListener.TILE_LOAD_METHOD);
    }

    /**
     * Adds the tile error listener.
     *
     * @param listener the Listener to be added.
     */
    public void addErrorListener(ErrorListener listener) {
        addListener(ErrorEvent.class, listener, ErrorListener.TILE_ERROR_METHOD);
    }

    /**
     * Removes the tile error listener.
     *
     * @param listener the Listener to be removed.
     */
    public void removeErrorListener(ErrorListener listener) {
        removeListener(ErrorEvent.class, listener, ErrorListener.TILE_ERROR_METHOD);
    }

    /**
     * Add a click listener to the component. The listener is called whenever
     * the user clicks inside the component. Depending on the content the event
     * may be blocked and in that case no event is fired.
     * <p>
     * Use {@link #removeClickListener(ClickListener)} to remove the listener.
     *
     * @param listener The listener to add
     */
    public void addClickListener(ClickListener listener) {
        addListener(EventId.CLICK_EVENT_IDENTIFIER, ClickEvent.class, listener, ClickListener.clickMethod);
    }

    /**
     * Remove a click listener from the component. The listener should earlier
     * have been added using {@link #addClickListener(ClickListener)}.
     *
     * @param listener The listener to remove
     */
    public void removeClickListener(ClickListener listener) {
        removeListener(EventId.CLICK_EVENT_IDENTIFIER, ClickEvent.class, listener);
    }

    /**
     * Interface for listening for a {@link LoadEvent} fired by a
     * {@link AbstractProxyTile}.
     */
    public interface LoadListener extends Serializable {

        Method TILE_LOAD_METHOD = ReflectTools.findMethod(LoadListener.class, "load",
                LoadEvent.class);

        /**
         * Called when a {@link AbstractTile} has been loaded. A reference to
         * the tile is given by {@link LoadEvent#getTile()}.
         *
         * @param event An event containing information about the click.
         */
        void load(LoadEvent event);

    }

    /**
     * Interface for listening for a {@link ErrorEvent} fired by a
     * {@link AbstractProxyTile}.
     */
    public interface ErrorListener extends Serializable {

        Method TILE_ERROR_METHOD = ReflectTools.findMethod(ErrorListener.class, "error",
                ErrorEvent.class);

        /**
         * Called when a {@link AbstractProxyTile} loading failed. A reference
         * to the tile is given by {@link ErrorEvent#getTile()}.
         *
         * @param event An event containing information about the error.
         */
        void error(ErrorEvent event);

    }

    /**
     * Load event. This event is thrown, when the tile is loaded.
     */
    public static class LoadEvent extends TimekeepingComponentEvent {

        /**
         * New instance of tile load event.
         *
         * @param source the Source of the event.
         */
        public LoadEvent(long timestamp, AbstractProxyTile<?> source) {
            super(timestamp, source);
        }

        /**
         * Gets the ProxyTile where the event occurred.
         *
         * @return the Source of the event.
         */
        public AbstractProxyTile<?> getTile() {
            return (AbstractProxyTile<?>) getSource();
        }

    }

    /**
     * Error event. This event is thrown, when the tile loading failed.
     */
    public static class ErrorEvent extends TimekeepingComponentEvent {

        /**
         * New instance of tile error event.
         *
         * @param source the Source of the event.
         */
        public ErrorEvent(long timestamp, AbstractProxyTile<?> source) {
            super(timestamp, source);
        }

        /**
         * Gets the ProxyTile where the event occurred.
         *
         * @return the Source of the event.
         */
        public AbstractProxyTile<?> getTile() {
            return (AbstractProxyTile<?>) getSource();
        }

    }

}
