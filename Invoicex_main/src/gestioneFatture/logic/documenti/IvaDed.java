/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gestioneFatture.logic.documenti;

import it.tnx.commons.cu;

/**
 *
 * @author Marco
 */
public class IvaDed {

    String codice_iva;
    Double perc_deducibilita;

    public IvaDed(String codice_iva, Double perc_deducibilita) {
        if (perc_deducibilita == null) {
            perc_deducibilita = 100d;
        }
        this.perc_deducibilita = perc_deducibilita;
        this.codice_iva = cu.s(codice_iva).toUpperCase();
    }

    @Override
    public String toString() {
        return "IvaDed codice iva = " + cu.s(codice_iva) + " perc. dedducibilita = " + cu.s(perc_deducibilita);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IvaDed) {
            IvaDed ivaded2 = (IvaDed) obj;
            if (codice_iva != null && perc_deducibilita != null && ivaded2.codice_iva != null && ivaded2.perc_deducibilita != null
                    && codice_iva.equals(ivaded2.codice_iva) && perc_deducibilita.equals(ivaded2.perc_deducibilita)) {
                return true;
            }
            return false;
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return (String.valueOf(codice_iva) + String.valueOf(perc_deducibilita)).hashCode();
    }

}