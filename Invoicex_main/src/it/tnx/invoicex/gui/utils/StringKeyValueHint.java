/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.gui.utils;

/**
 *
 * @author mceccarelli
 */
public class StringKeyValueHint {
    public String key;
    public String value;

    public StringKeyValueHint() {
    }
    
    public StringKeyValueHint(String key) {
        this.key = key;
    }

    public StringKeyValueHint(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    @Override
    public String toString() {
        String ret = "";
        if (value == null) {
            ret = key;
        } else {
            ret = value + " [" + key + "]";
        }
        return ret;
    }
}
