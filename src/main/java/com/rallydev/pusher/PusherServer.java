package com.rallydev.pusher;

import static javax.crypto.Cipher.getInstance;
import static spark.Spark.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.pusher.rest.Pusher;

import javax.crypto.*;
import java.util.*;

public class PusherServer {
    private static  String app_id = "123751";
    private static SecretKey secretKey = null;
    private static String pusherKey = "6d0b98669f566dfd8421";
    private static String pusherSecret = "cc60bde9a3064a89b393";
    private static Cipher cipher;
    private static CryptoHolder cryptoHolder = new CryptoHolder();


    private static Pusher poxaPusher = new Pusher("http://" + pusherKey + ":" + pusherSecret + "@localhost:4444/apps/" + app_id);
    private static Pusher cloudPusher = new Pusher(app_id, pusherKey, pusherSecret);
    private static Pusher pusher = null;

    private static void trigger(String channel, String eventName, Object payload) {

       pusher.trigger(channel,eventName,payload);

    }
    public static void main(String[] args) {
        initPusher(args);

        ChangeMessageProcessor processor = new ChangeMessageProcessor(cryptoHolder);
        processor.subscribe(pusher);
        Map<String, String> channels = new HashMap();
        Map<String, List<String>> users = ImmutableMap.of(
                "sean", ImmutableList.of("project1", "project2"),
                "linus", ImmutableList.of("project2")
        );
        Map<String, Project> projects = ImmutableMap.of(
         "project1", new Project("project1", "uuid1"),
                "project2", new Project("project2", "uuid2")

        );
        staticFileLocation("/public");

        get("/subscribe/:project", (req,res) -> {
            String project = req.params(":project");
            String key = "foo";

            return key;

        });
        get("encrypt/:plaintext", (req,res) -> {
          String plaintext = req.params(":plaintext");
          String cipherText = cryptoHolder.encrypt(plaintext);
          return cipherText;
        });
        get("/login/:username",  (req,res)-> {
            String username = req.params(":username");
            if ( users.containsKey(username) ) {
             // simulates an authentication, this is an ajax response though so I'm sending a payload instead of dropping
            // a cookie
//                res.cookie("zsessionid", username);
//                res.redirect("/test.public");
                Map responseMap = ImmutableMap.of(
                        "username", username,
                        "secret", cryptoHolder.getKey(),
                        "iv", cryptoHolder.getIV(),
                        "applicationKey", pusherKey);
              return responseMap;

            }
            else {
                res.status(403);
                return "uauthenticated";
            }


        }, new JsonTransformer()) ;
        post("/realtime/auth", (req, res) -> {
            // It's a form parameter, which is missing from Spark, hence going to the HttpServletRequest
            String socketId = req.raw().getParameter("socket_id");
            String channel = req.raw().getParameter("channel_name");

            String user = req.cookie("username");
            List userProjects = users.get(user);
            String projectName = getProjectFromChannel(channel);
            if (userProjects.contains(projectName)) {
                return pusher.authenticate(socketId, channel);

            } else {
                res.status(403);
                return "Unauthorized";
            }
        });
        get("/private", (req,res) -> {
            String cipherText = cryptoHolder.encrypt("hello world " + System.currentTimeMillis());
            trigger("private-project1", "update", Collections.singletonMap("message", cipherText));

            String project2Text = cryptoHolder.encrypt("hi project2! " + System.currentTimeMillis());
            trigger("private-project2", "update", Collections.singletonMap("message", project2Text));

            return "done";
        });
        get("/hello", (req, res) -> {

            trigger("project-one", "test_event", Collections.singletonMap("message", "hello world"));
            trigger("project-two", "test_event", Collections.singletonMap("message", "hello world"));
            return "Hello World2";
        }
        );
    }

    /**
     * Initializes pusher either to use the cloud version or a local poxa.
     * @param args
     */
    private static void initPusher(String[] args) {
        if (args.length > 0 && args[0].equals("poxa")) {
            pusher = poxaPusher;
        }
        else {
            pusher = cloudPusher;
        }

    }

    private static String getProjectFromChannel(String channel) {
        return channel.replace("private-", "");
    }


}
