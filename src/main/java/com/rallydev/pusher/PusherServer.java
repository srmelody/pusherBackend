package com.rallydev.pusher;

import static javax.crypto.Cipher.getInstance;
import static spark.Spark.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.pusher.rest.Pusher;
import com.sun.crypto.provider.AESKeyGenerator;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

public class PusherServer {
    private static  String app_id = "123751";
    private static SecretKey secretKey = null;
    private static String key = "6d0b98669f566dfd8421";
    private static String secret = "cc60bde9a3064a89b393";
    private static Cipher cipher;
    private static CryptoHolder cryptoHolder = new CryptoHolder();


    private static Pusher pusher = new Pusher(app_id, key, secret);
    private static void trigger(String channel, String eventName, Object payload) {

       pusher.trigger(channel,eventName,payload);

    }
    public static void main(String[] args) {

        Map<String, String> channels = new HashMap();
        Map<String, List<String>> users = ImmutableMap.of(
                "sean", ImmutableList.of("project1", "project2"),
                "linus", ImmutableList.of("project2")
        );
        Map<String, Project> projects = ImmutableMap.of(
         "project1", new Project("project1", "uuid1"),
                "project2", new Project("project2", "uuid2")

        );
        staticFileLocation("/html");

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
             // simulates an authentication, obviously we would drop a cookie here, but spark is being finicky.
//                res.cookie("zsessionid", username);
//                res.redirect("/test.html");
                Map responseMap = ImmutableMap.of(
                        "username", username,
                        "secret", secretKey.toString());
              return responseMap;

            }
            else {
                res.status(403);
                return "uauthenticated";
            }


        }, new JsonTransformer()) ;
        post("/pusher/auth", (req, res) -> {
            System.out.println(req.params());
            System.out.println(req.attributes());
            System.out.println(req.contentType());
            String socketId = req.raw().getParameter("socket_id");

            String channel = req.raw().getParameter("channel_name");
            System.out.println("socket: " + socketId);
            System.out.println("cookies:" + req.cookies().toString());
            String user = req.cookie("zsessionid");
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
            trigger("private-project2", "update", Collections.singletonMap("message", "hello world"));
            return "done";
        });
        get("/hello", (req, res) -> {

            trigger("project-one", "test_event", Collections.singletonMap("message", "hello world"));
            trigger("project-two", "test_event", Collections.singletonMap("message", "hello world"));
            return "Hello World2";
        }
        );
    }

    private static String getProjectFromChannel(String channel) {
        return channel.replace("private-", "");
    }


}
