/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.invoicex.utils;

import it.tnx.Db;
import it.tnx.commons.DbUtils;
import it.tnx.commons.cu;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mceccarelli
 */
public class IvaCache extends Cache {

    public static IvaCache i = new IvaCache();

    public static String getDescrizione(String id) throws Exception {
        i.needInit();
        return (String) i.values.get(id).get("descrizione");
    }

    public static Double getPercentuale(String id) throws Exception {
        i.needInit();
        return cu.d0(i.values.get(id).get("percentuale"));
    }

    Map<String, Map> initValues() {
        try {
            return DbUtils.getListMapMap(Db.getConn(), "select codice, descrizione, percentuale from codici_iva", "codice");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
