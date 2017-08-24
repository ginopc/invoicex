/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sdicons.json;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import com.sdicons.json.model.JSONArray;
import com.sdicons.json.model.JSONObject;
import com.sdicons.json.parser.JSONParser;
import java.io.StringReader;

/**
 *
 * @author mceccarelli
 */
public class Test {
    public static void main(String[] args) throws TokenStreamException, RecognitionException {
        String json = "{\"type\": \"menu\", \"value\": \"File\",\"items\": [{\"value\": \"New\", \"action\": \"CreateNewDoc\"}, {\"value\": \"Open\", \"action\": \"OpenDoc\"}, {\"value\": \"Close\", \"action\": \"CloseDoc\"}]}";
        JSONParser p = new JSONParser(new StringReader(json));
        JSONObject jo = (JSONObject) p.nextValue();
        JSONArray ja = (JSONArray)jo.get("items");
        System.out.println(ja);
    }
}
