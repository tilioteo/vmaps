package org.vaadin.maps.ui;

import com.vaadin.server.ComponentSizeValidator;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Component;

import java.util.Collections;
import java.util.Iterator;

/**
 * @author Kamil Morong
 */
public abstract class AbstractSingleComponentContainer<C extends Component> extends AbstractComponent
        implements SingleComponentContainer<C> {

    private C content;

    /**
     * Utility method for removing a component from its parent (if possible).
     *
     * @param content component to remove
     */
    // TODO move utility method elsewhere?
    public static <C extends Component> void removeFromParent(C content) throws IllegalArgumentException {
        com.vaadin.ui.HasComponents parent = content.getParent();
        if (parent instanceof ComponentContainer<?>) {
            // If the component already has a parent, try to remove it
            @SuppressWarnings("unchecked")
            ComponentContainer<C> oldParent = (ComponentContainer<C>) parent;
            oldParent.removeComponent(content);
        } else if (parent instanceof SingleComponentContainer) {
            SingleComponentContainer<?> oldParent = (SingleComponentContainer<?>) parent;
            if (oldParent.getContent() == content) {
                oldParent.setContent(null);
            }
        } else if (parent != null) {
            throw new IllegalArgumentException("Content is already attached to another parent.");
        }
    }

    @Override
    public int getComponentCount() {
        return (content != null) ? 1 : 0;
    }

    @Override
    public Iterator<Component> iterator() {
        if (content != null) {
            return Collections.<Component>singletonList(content).iterator();
        } else {
            return Collections.<Component>emptyList().iterator();
        }
    }

    @Override
    public Iterator<C> typedIterator() {
        if (content != null) {
            return Collections.<C>singletonList(content).iterator();
        } else {
            return Collections.<C>emptyList().iterator();
        }
    }

    @Override
    public void addComponentAttachListener(ComponentAttachListener listener) {
        addListener(ComponentAttachEvent.class, listener, ComponentAttachListener.attachMethod);

    }

    @Override
    public void removeComponentAttachListener(ComponentAttachListener listener) {
        removeListener(ComponentAttachEvent.class, listener, ComponentAttachListener.attachMethod);
    }

    @Override
    public void addComponentDetachListener(ComponentDetachListener listener) {
        addListener(ComponentDetachEvent.class, listener, ComponentDetachListener.detachMethod);
    }

    @Override
    public void removeComponentDetachListener(ComponentDetachListener listener) {
        removeListener(ComponentDetachEvent.class, listener, ComponentDetachListener.detachMethod);
    }

    /**
     * Fires the component attached event. This is called by the
     * {@link #setContent(C)} method after the component has been set as the
     * content.
     *
     * @param component the component that has been added to this container.
     */
    protected void fireComponentAttachEvent(C component) {
        fireEvent(new ComponentAttachEvent(this, component));
    }

    /**
     * Fires the component detached event. This is called by the
     * {@link #setContent(C)} method after the content component has been
     * replaced by other content.
     *
     * @param component the component that has been removed from this container.
     */
    protected void fireComponentDetachEvent(C component) {
        fireEvent(new ComponentDetachEvent(this, component));
    }

    @Override
    public C getContent() {
        return content;
    }

    /**
     * Sets the content of this container. The content is a component that
     * serves as the outermost item of the visual contents.
     * <p>
     * The content must always be set, either with a constructor parameter or by
     * calling this method.
     *
     * @param content a component (typically a layout) to use as content
     */
    @Override
    public void setContent(C content) {
        C oldContent = getContent();
        if (oldContent == content) {
            // do not set the same content twice
            return;
        }
        if (oldContent != null && oldContent.getParent() == this) {
            oldContent.setParent(null);
            fireComponentDetachEvent(oldContent);
        }
        this.content = content;
        if (content != null) {
            removeFromParent(content);

            content.setParent(this);
            fireComponentAttachEvent(content);
        }

        markAsDirty();
    }

    @Override
    public void setWidth(float width, Unit unit) {
        /*
         * child tree repaints may be needed, due to our fall back support for
         * invalid relative sizes
         */
        boolean dirtyChild = false;
        boolean childrenMayBecomeUndefined = false;
        if (getWidth() == SIZE_UNDEFINED && width != SIZE_UNDEFINED) {
            // children currently in invalid state may need repaint
            dirtyChild = getInvalidSizedChild(false);
        } else if ((width == SIZE_UNDEFINED && getWidth() != SIZE_UNDEFINED) || (unit == Unit.PERCENTAGE
                && getWidthUnits() != Unit.PERCENTAGE && !ComponentSizeValidator.parentCanDefineWidth(this))) {
            /*
             * relative width children may get to invalid state if width becomes
             * invalid. Width may also become invalid if units become percentage
             * due to the fallback support
             */
            childrenMayBecomeUndefined = true;
            dirtyChild = getInvalidSizedChild(false);
        }
        super.setWidth(width, unit);
        repaintChangedChildTree(dirtyChild, childrenMayBecomeUndefined, false);
    }

    private void repaintChangedChildTree(boolean invalidChild, boolean childrenMayBecomeUndefined, boolean vertical) {
        if (getContent() == null) {
            return;
        }
        boolean needRepaint = false;
        if (childrenMayBecomeUndefined) {
            // if became invalid now
            needRepaint = !invalidChild && getInvalidSizedChild(vertical);
        } else if (invalidChild) {
            // if not still invalid
            needRepaint = !getInvalidSizedChild(vertical);
        }
        if (needRepaint) {
            getContent().markAsDirtyRecursive();
        }
    }

    private boolean getInvalidSizedChild(final boolean vertical) {
        C content = getContent();
        if (content == null) {
            return false;
        }
        if (vertical) {
            return !ComponentSizeValidator.checkHeights(content);
        } else {
            return !ComponentSizeValidator.checkWidths(content);
        }
    }

    @Override
    public void setHeight(float height, Unit unit) {
        /*
         * child tree repaints may be needed, due to our fall back support for
         * invalid relative sizes
         */
        boolean dirtyChild = false;
        boolean childrenMayBecomeUndefined = false;
        if (getHeight() == SIZE_UNDEFINED && height != SIZE_UNDEFINED) {
            // children currently in invalid state may need repaint
            dirtyChild = getInvalidSizedChild(true);
        } else if ((height == SIZE_UNDEFINED && getHeight() != SIZE_UNDEFINED) || (unit == Unit.PERCENTAGE
                && getHeightUnits() != Unit.PERCENTAGE && !ComponentSizeValidator.parentCanDefineHeight(this))) {
            /*
             * relative height children may get to invalid state if height
             * becomes invalid. Height may also become invalid if units become
             * percentage due to the fallback support.
             */
            childrenMayBecomeUndefined = true;
            dirtyChild = getInvalidSizedChild(true);
        }
        super.setHeight(height, unit);
        repaintChangedChildTree(dirtyChild, childrenMayBecomeUndefined, true);
    }

}
