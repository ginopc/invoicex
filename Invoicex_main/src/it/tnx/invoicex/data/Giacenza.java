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



/*
 * Giacenza.java
 *
 * Created on 24 maggio 2007, 14.27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package it.tnx.invoicex.data;

/**
 *
 * @author mceccarelli
 */
public class Giacenza {
    private String codice_articolo;
    private String descrizione_articolo;
    private Integer categoria_id;
    private Integer sottocategoria_id;
    private String categoria;
    private String sottocategoria;
    private String matricola;
    private String lotto;
    private double giacenza;
    private double prezzo;
    private String deposito;
    private Integer deposito_id;
    
    /** Creates a new instance of Giacenza */
    public Giacenza() {
    }

    public String getCodice_articolo() {
        return codice_articolo;
    }

    public void setCodice_articolo(String codice_articolo) {
        this.codice_articolo = codice_articolo;
    }

    public String getDescrizione_articolo() {
        return descrizione_articolo;
    }

    public void setDescrizione_articolo(String descrizione_articolo) {
        this.descrizione_articolo = descrizione_articolo;
    }

    public String getMatricola() {
        return matricola;
    }

    public void setMatricola(String matricola) {
        this.matricola = matricola;
    }

    public double getGiacenza() {
        return giacenza;
    }

    public void setGiacenza(double giacenza) {
        this.giacenza = giacenza;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(double prezzo) {
        this.prezzo = prezzo;
    }
    public String toString() {
        if (matricola != null && lotto != null) {
            return "S/N: " + matricola + " Lotto: " + lotto;
        } else if (matricola != null) {
            return matricola;
        } else if (lotto != null) {
            return lotto;
        } else {
            return getCodice_articolo() + " " + getGiacenza();
        }
    }

    public String getLotto() {
        return lotto;
    }

    public void setLotto(String lotto) {
        this.lotto = lotto;
    }
    
    public String getDeposito() {
        return deposito;
    }
    public void setDeposito(String deposito) {
        this.deposito = deposito;
    }

    public Integer getDeposito_id() {
        return deposito_id;
    }

    public void setDeposito_id(Integer deposito_id) {
        this.deposito_id = deposito_id;
    }

    public Integer getCategoria_id() {
        return categoria_id;
    }

    public void setCategoria_id(Integer categoria_id) {
        this.categoria_id = categoria_id;
    }

    public Integer getSottocategoria_id() {
        return sottocategoria_id;
    }

    public void setSottocategoria_id(Integer sottocategoria_id) {
        this.sottocategoria_id = sottocategoria_id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getSottocategoria() {
        return sottocategoria;
    }

    public void setSottocategoria(String sottocategoria) {
        this.sottocategoria = sottocategoria;
    }
}
