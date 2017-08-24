/**
 * Invoicex
 * Copyright (c) 2005-2016 Marco Ceccarelli, Tnx srl
 *
 * Questo software è soggetto, e deve essere distribuito con la licenza  
 * GNU General Public License, Version 2. La licenza accompagna il software
 * o potete trovarne una copia alla Free Software Foundation http://www.fsf.org .
 *
 * This software is subject to, and may be distributed under, the
 * GNU General Public License, Version 2. The license should have
 * accompanied the software or you may obtain a copy of the license
 * from the Free Software Foundation at http://www.fsf.org .
 * 
 * --
 * Marco Ceccarelli (m.ceccarelli@tnx.it)
 * Tnx snc (http://www.tnx.it)
 *
 */

package gestioneFatture.logic.documenti;


public class DettaglioIva {

    private String codice;
    private double percentuale;
    private String descrizione;
    private String descrizioneBreve;
    private double imponibile;
    public double imponibile_noarr;
    private double imposta;
    public double imposta_noarr;
    private double ivato;
    public double ivato_noarr;
    private double imponibile_deducibile;
    private double imponibile_indeducibile;
    private double ivato_deducibile;
    private double ivato_indeducibile;
    private double imposta_deducibile;
    private double imposta_indeducibile;
    private double perc_deducibilita;
    private String conto;
    
    public DettaglioIva() {
    }

    public DettaglioIva(String codice, double percentuale, String descrizione, String descrizioneBreve, double imponibile, double imposta) {
        this.codice = codice;
        this.percentuale = percentuale;
        this.descrizione = descrizione;
        this.descrizioneBreve = descrizioneBreve;
        this.imponibile = imponibile;
        this.imposta = imposta;
    }

    public DettaglioIva(String codice, double percentuale, String descrizione, String descrizioneBreve, double imponibile, double imposta, double ivato) {
        this.codice = codice;
        this.percentuale = percentuale;
        this.descrizione = descrizione;
        this.descrizioneBreve = descrizioneBreve;
        this.imponibile = imponibile;
        this.imposta = imposta;
        this.ivato = ivato;
    }

    public String getCodice() {
        return this.codice;
    }
    
    public int getCodiceInt() {
        return Integer.parseInt(codice);
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public double getPercentuale() {
        return this.percentuale;
    }

    public void setPercentuale(double percentuale) {
        this.percentuale = percentuale;
    }
    
    public String getDescrizione() {
        return this.descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizioneBreve() {
        return this.descrizioneBreve;
    }

    public void setDescrizioneBreve(String descrizioneBreve) {
        this.descrizioneBreve = descrizioneBreve;
    }

    public double getImponibile() {
        return this.imponibile;
    }

    public void setImponibile(double imponibile) {
        this.imponibile = imponibile;
    }

    public double getImposta() {
        return this.imposta;
    }

    public void setImposta(double imposta) {
        this.imposta = imposta;
    }

    @Override
    public String toString() {
        return codice + " perc:" + percentuale + " imponib:" + imponibile + " imposta:" + imposta + " ivato:" + ivato;
    }

    public double getIvato() {
        return ivato;
    }

    public void setIvato(double ivato) {
        this.ivato = ivato;
    }

    public double getImponibile_deducibile() {
        return imponibile_deducibile;
    }

    public void setImponibile_deducibile(double imponibile_deducibile) {
        this.imponibile_deducibile = imponibile_deducibile;
    }

    public double getImponibile_indeducibile() {
        return imponibile_indeducibile;
    }

    public void setImponibile_indeducibile(double imponibile_indeducibile) {
        this.imponibile_indeducibile = imponibile_indeducibile;
    }

    public double getIvato_deducibile() {
        return ivato_deducibile;
    }

    public void setIvato_deducibile(double ivato_deducibile) {
        this.ivato_deducibile = ivato_deducibile;
    }

    public double getIvato_indeducibile() {
        return ivato_indeducibile;
    }

    public void setIvato_indeducibile(double ivato_indeducibile) {
        this.ivato_indeducibile = ivato_indeducibile;
    }

    public double getImposta_deducibile() {
        return imposta_deducibile;
    }

    public void setImposta_deducibile(double imposta_deducibile) {
        this.imposta_deducibile = imposta_deducibile;
    }

    public double getImposta_indeducibile() {
        return imposta_indeducibile;
    }

    public void setImposta_indeducibile(double imposta_indeducibile) {
        this.imposta_indeducibile = imposta_indeducibile;
    }

    public double getPerc_deducibilita() {
        return perc_deducibilita;
    }

    public void setPerc_deducibilita(double perc_deducibilita) {
        this.perc_deducibilita = perc_deducibilita;
    }

    public String getConto() {
        return conto;
    }

    public void setConto(String conto) {
        this.conto = conto;
    }

}