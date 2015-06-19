package com.rallydev.pusher;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
import com.pusher.rest.Pusher;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import spark.utils.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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



            if (StringUtils.isNotEmpty(message)) {
                handleMessage(gson, message);
            }
        }


        System.out.println("Shutting down Thread: " + m_threadNumber);
    }

    private void handleMessage(Gson gson, String message) {
        try {

            LinkedTreeMap result = gson.fromJson(message , LinkedTreeMap.class);


            String channel = "private-" + result.get("project");
            Object action = result.get("action");
            Map<String, Map> changes = (Map<String, Map>) result.get("changes");
            // TODO encrypt values
//            Map<String, Object> artifact = (Map<String, Object>) result.get("artifact");

            String eventName = "update";
            Map changesPayload = encryptValues(changes);
            String cipherText = cryptoHolder.encrypt(action.toString());
            ImmutableMap<String, Object> payload = ImmutableMap.of("message", cipherText, "changes", changesPayload);
            System.out.println("Sending: " + payload.toString() + "topusher on channel: " + channel);
            pusher.trigger(channel,eventName, payload);
        }
        catch (Exception ex)   {
            ex.printStackTrace();
            System.out.println("error parsing json");
        }
    }

    private Map encryptValues(Map<String, Map> changes) {
        Map<String, Map> encryptedMap = new LinkedHashMap<>();
        for(String key: changes.keySet()) {
            Map valMap = changes.get(key);

            String plaintextValue = valMap.get("value").toString();
            String cipherText =   cryptoHolder.encrypt(plaintextValue);

            Map payloadMap = new LinkedHashMap();
            payloadMap.putAll(valMap);
            payloadMap.put("value", cipherText);
            encryptedMap.put(key, payloadMap);
        }
        return encryptedMap;
    }
}
