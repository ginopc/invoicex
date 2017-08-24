/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.utils;

import java.util.Map;

/**
 *
 * @author mceccarelli
 */
public abstract class Cache {
    public boolean initialized = false;
    public Map<String, Map> values;

    public void needInit() throws Exception {
        if (!initialized) {
            values = initValues();
            initialized = true;
        }
    }
    
    public void reset() {
        initialized = false;
    }

    public void reset(String id) {
        values.remove(id);
    }

    public void update(String id, Map rec) {
        values.put(id, rec);
    }

    abstract Map<String, Map> initValues();

    
}
