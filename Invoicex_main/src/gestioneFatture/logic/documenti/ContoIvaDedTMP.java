/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture.logic.documenti;

import it.tnx.commons.cu;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Marco
 */
public class ContoIvaDedTMP {

    String conto;
    String codice_iva;
    Double perc_deducibilita;

    public ContoIvaDedTMP(String conto, String codice_iva, Double perc_deducibilita) {
        if (perc_deducibilita == null) {
            perc_deducibilita = 100d;
        }
        this.conto = cu.s(conto);
        this.perc_deducibilita = perc_deducibilita;
        this.codice_iva = cu.s(codice_iva).toUpperCase();
    }

    @Override
    public String toString() {
        return "ContoIvaDed conto = " + conto + " codice iva = " + cu.s(codice_iva) + " perc. dedducibilita = " + cu.s(perc_deducibilita);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ContoIvaDedTMP) {
            ContoIvaDedTMP ivaded2 = (ContoIvaDedTMP) obj;
            if (codice_iva != null && perc_deducibilita != null && ivaded2.codice_iva != null && ivaded2.perc_deducibilita != null
                    && conto.equals(ivaded2.conto)
                    && codice_iva.equals(ivaded2.codice_iva)
                    && perc_deducibilita.equals(ivaded2.perc_deducibilita)) {
                return true;
            }
            return false;
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return (cu.s(conto) + cu.s(codice_iva) + cu.s(perc_deducibilita)).hashCode();
    }

}