package com.rallydev.com.pusher;

import com.rallydev.pusher.CryptoHolder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by smelody on 6/12/15.
 */
public class CryptoHolderTest {

    @Test
    public void testSameCipherText() {

        CryptoHolder ch = new CryptoHolder();
        String s1 = ch.encrypt("foo");
        String s2 = ch.encrypt("foo");
        System.out.println(s1);
        assertEquals(s1, s2);

        assertEquals("foo", ch.decrypt(s1));


    }

}
