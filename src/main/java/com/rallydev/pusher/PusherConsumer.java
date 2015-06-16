package com.rallydev.pusher;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
import com.pusher.rest.Pusher;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import java.util.Collections;

public class PusherConsumer implements Runnable
{

    private final CryptoHolder cryptoHolder;
    private KafkaStream stream;
    private int m_threadNumber;
    private Pusher pusher;

    public PusherConsumer(KafkaStream a_stream, int a_threadNumber, Pusher pusher, CryptoHolder cryptoHolder) {

        m_threadNumber = a_threadNumber;
        stream = a_stream;
        this.pusher = pusher;
        this.cryptoHolder = cryptoHolder;
    }

    public void run()
    {
        System.out.println( "calling PusherConsumer.run()" );
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        Gson gson = new Gson();


        while (it.hasNext())
        {

            String message = new String(it.next().message());
            System.out.println("Thread " + m_threadNumber + ": " + message);




          handleMessage(gson, message);
        }


        System.out.println("Shutting down Thread: " + m_threadNumber);
    }

    private void handleMessage(Gson gson, String message) {
        try {
            LinkedTreeMap result = gson.fromJson(message , LinkedTreeMap.class);
            System.out.println("Map: " + result.toString());

            String channel = "private-" + result.get("project");
            Object payload = result.get("message");
            String eventName = "update";

            System.out.println("payload: " + payload);
            System.out.println("Sending to pusher on channel " + channel);
            String cipherText = cryptoHolder.encrypt(payload.toString());
            System.out.println("ciphertext" + cipherText);
            pusher.trigger(channel,eventName, Collections.singletonMap("message", cipherText));
        }
        catch (JsonParseException ex)   {
            System.out.println("error parsing json");
        }
    }
}
