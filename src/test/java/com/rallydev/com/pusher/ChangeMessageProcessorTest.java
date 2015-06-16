package com.rallydev.com.pusher;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.rallydev.pusher.ChangeMessageProcessor;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by smelody on 6/16/15.
 */
public class ChangeMessageProcessorTest {

    private ChangeMessageProcessor changeMessageProcessor;

    @Before
    public void setUp() {
        //this.changeMessageProcessor = new ChangeMessageProcessor();
    }
    @Test
    public void testSubscribe() {
       // changeMessageProcessor.subscribe(null);
    }
    @Test
    public void testSerialize() {
        String msg = "{\"project\": \"project1\", \"message\": \"something changed\"}";

        Gson gson = new Gson();

        LinkedTreeMap result = gson.fromJson(msg , LinkedTreeMap.class);
        System.out.println("Map: " + result.toString());

    }
}
