/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.api;

import javax.json.*;

/**
 *
 * @author projects
 */
public class EventObject {    
    
    private String name;
    private JsonArray payload = null;
    
    public EventObject(String n) {
        name = n;
    }
    
    public String getName() {
        return (name);
    }
    
    public JsonArray getPayload() {
        return (payload);
    }
    public void setPayload(JsonArray array) {
        payload = array;
    }
    public void setPayload(JsonObject obj) {
        if (null == payload)
            payload = Json.createArrayBuilder().build();
        payload.add(obj);
    }

    @Override
    public String toString() {
        return ("name: " + name + ", payload: " + payload);
    }        
    
    public static EventObject valueOf(String s) {
        JsonObject obj = Frame.parseJson(s);
        EventObject evt = new EventObject(obj.getString("name"));    
        evt.setPayload(obj.getJsonArray("args"));
        return (evt);
    }
    
}
