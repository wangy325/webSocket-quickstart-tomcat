package com.zc.playground.client;

import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;

/**
 * @author wangy
 * @version 1.0
 * @date 2019/4/18 / 15:03
 */
public class Client {
    static Session session;
    public static void main(String[] args) {
        try {
            for (int i = 0; i <2 ; i++) {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                String uri = "ws://localhost:8080/ws/webSocket";
                System.out.println("Connecting to " + uri);
                container.connectToServer(MyWebSocketClient.class, URI.create(uri));
//                session.getBasicRemote().sendText("hello world, " + i);
                java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
                while (true) {
                    String line = r.readLine();
                    if (line.equals("quit")) {
                        session.close();
                        break;
                    }
                    session.getBasicRemote().sendText(line);
                }
            }

        } catch ( Exception ex) {
            ex.printStackTrace();
        }
    }
}
