package com.messenger.cityoftwo;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Aayush on 3/23/2016.
 */
public class ConversationTest {

    @org.junit.Test
    public void ToString() throws Exception {
        Conversation origConversation = new Conversation(
                "Hello",
                CityOfTwo.RECEIVED
        );

        String c = origConversation.toString();

        Conversation newConversation = new Conversation(c);

//        assertEquals(0, c.length());
        assertEquals(c, newConversation.toString());
    }
}