/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.commons;

/**
 *
 * @author Marco
 */
public class NullValue {
    double id = Math.random();

    @Override
    public String toString() {
        return "NullValue " + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NullValue) {
            return id == ((NullValue)obj).id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
