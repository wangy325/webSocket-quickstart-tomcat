package com.zc.playground.client;

import javax.websocket.*;
import java.io.IOException;

/**
 *  基于注解式的客户端
 *
 * @author wangy
 * @version 1.0
 * @date 2019/4/18 / 14:54
 */
@ClientEndpoint
public class MyWebSocketClient {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to endpoint: " + session.getBasicRemote());
        try {
            Client.session=session;
            System.out.println(Client.session);
            String name = "Hi Server";
            System.out.println("Sending message to endpoint: " + name);
            session.getBasicRemote().sendText(name);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @OnMessage
    public void processMessage(String message) {
        System.out.println("Received message in client: " + message);
    }

    @OnError
    public void processError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void onClose(){

    }
}
