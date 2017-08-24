/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.tnx.commons;

/**
 *
 * @author mceccarelli
 */
public class KeyValuePair {
    public Object key;
    public Object value;

    public KeyValuePair(Object key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
