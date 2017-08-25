/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.tnx.common;

import java.util.ArrayList;

/**
 *
 * @author MaurizioAru
 */
public class MainTest {
    public static void main(String[] args){
        ArrayList<String> parole = new ArrayList();
        
        parole.add("Ciao");
        parole.add("Mondo");
        
        // metodo 1
        String out1 = parole.toString();
        showValue("out1", out1);
        
        // metodo2
        String out2 = "";
        for(String parola : parole){
            out2 += parola + " UNION ";
        }
        showValue("out2", out2);
        
        // metodo3
        String out3 = "";
        for (int i=0; i<parole.size(); i++){
            out3 += parole.get(i);
            if ( (parole.size() - i) > 1){
                out3 += " UNION ";
            }
        }
        showValue("out3", out3);
    }
    
    public static void showValue(String valueName, String value){
        
        System.out.println(String.format("%s: %s", valueName, value));
    }
}
