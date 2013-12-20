/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.jsocketio.api;

import com.glines.socketio.server.*;

import static com.glines.socketio.server.SocketIOFrame.*;
import java.io.*;
import java.math.*;
import java.util.*;
import javax.json.*;
import javax.json.stream.*;


/**
 *
 * @author projects
 */
public class Frame {
    
    public static SocketIOFrame send(String msg) {
        return (new SocketIOFrame(SocketIOFrame.FrameType.MESSAGE, TEXT_MESSAGE_TYPE, msg));        
    }
    
    public static SocketIOFrame send(JsonStructure json) {
        StringWriter w = new StringWriter();
        try (JsonWriter writer = Json.createWriter(w)) {
            writer.write(json);
        }
        return (new SocketIOFrame(FrameType.JSON_MESSAGE, JSON_MESSAGE_TYPE, w.toString()));
    }
    
    public static SocketIOFrame event(String event, List<Map<String, String>> items) {
        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (Map<String, String> i: items) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            for (String k: i.keySet())
                ob.add(k, i.get(k));
            ab.add(ob);
        }
        return (emit(event, ab.build()));
    }
    
    public static SocketIOFrame emit(String event, String... values) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (String s: values)
            builder.add(s);            
        return (emit(event, builder.build()));
    }
    
    public static SocketIOFrame emit(String event, Map<String, String> map) {
        JsonObjectBuilder ob = Json.createObjectBuilder();
        for (String k: map.keySet())
            ob.add(k, map.get(k));
        return (emit(event, ob.build()));
    }
    
    public static SocketIOFrame emit(String event, JsonArray array) {
        JsonObjectBuilder builder = Json.createObjectBuilder();      
        builder.add("name", event)
                .add("args", array);
        StringWriter wr = new StringWriter();
        try (JsonWriter w = Json.createWriter(wr)) {
            w.writeObject(builder.build());
        }
        return (new SocketIOFrame(SocketIOFrame.FrameType.EVENT, JSON_MESSAGE_TYPE, wr.toString()));        
    }
    
    public static SocketIOFrame emit(String event, JsonObject obj) {
        return (emit(event, Json.createArrayBuilder().add(obj).build()));        
    }
    
    public static SocketIOFrame connect() {
        return (new SocketIOFrame(FrameType.CONNECT, TEXT_MESSAGE_TYPE, ""));
    }
    
    public static SocketIOFrame disconnect() {
        return (new SocketIOFrame(FrameType.CLOSE, TEXT_MESSAGE_TYPE, "server"));
    }
    
    public static SocketIOFrame heartbeat() {
        return (new SocketIOFrame(FrameType.HEARTBEAT, 0, ""));
    }
    
    public static String asString(JsonStructure o) {
        StringWriter wr = new StringWriter();
        try (JsonWriter w = Json.createWriter(wr)) {
            w.write(o);
        }
        return (wr.toString());
    }        
    
    public static JsonObject parseJson(String s) {
        StringReader sr = new StringReader(s);
        JsonReader r = Json.createReader(sr);
        return (r.readObject());                    
    }    
}
