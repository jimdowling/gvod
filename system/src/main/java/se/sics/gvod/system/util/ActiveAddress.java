/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sics.gvod.system.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import se.sics.gvod.address.Address;

/**
 * This class manages the current peer address (defined in Usurp).
 *
 * @author jdowling
 */
public class ActiveAddress 
{

    /**
     * Singleton
     */
    private static ActiveAddress instance = null;

    /**
     * Used to inform swing components about updates
     */
    private final PropertyChangeSupport support;


    private ActiveAddress() {
        support = new PropertyChangeSupport(this);
    }

    private static ActiveAddress getInstance() {
        if (instance == null) {
            instance = new ActiveAddress();
        }
        return instance;
    }

    public synchronized static void addPropertyChangeListener(PropertyChangeListener listener) {
        getInstance().addPropertyChangeListenerP(listener);
    }

    private void addPropertyChangeListenerP(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public synchronized static void removePropertyChangeListener(PropertyChangeListener listener) {
        getInstance().removePropertyChangeListenerP(listener);
    }

    private void removePropertyChangeListenerP(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public synchronized static void addPropertyChangeListener(String property,
            PropertyChangeListener listener) {
        getInstance().addPropertyChangeListenerP(property, listener);
    }

    private void addPropertyChangeListenerP(String property,
            PropertyChangeListener listener) {
        support.addPropertyChangeListener(property, listener);
    }

    protected void firePropertyChange(String key, Object oldValue,
            Object newValue) {
        support.firePropertyChange(key, oldValue, newValue);
    }

    public synchronized static void updateParent(Address parent)
            throws ActiveAddressException {
        getInstance().updateParentP(parent);
    }

    private void updateParentP(Address parent) throws ActiveAddressException {
        if (parent == null) {
            throw new ActiveAddressException("Null parent");
        }
    }

}
