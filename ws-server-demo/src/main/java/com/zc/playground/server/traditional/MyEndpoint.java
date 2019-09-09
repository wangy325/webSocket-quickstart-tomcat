package com.zc.playground.server.traditional;

import org.apache.log4j.Logger;

import javax.websocket.*;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>编程式 实现的web socket服务端</p>
 * @author wangy
 * @version 1.0
 * @date 2019/4/19 / 15:26
 */
public class MyEndpoint extends Endpoint {

    private static Logger logger = Logger.getLogger(MyEndpoint.class);

    private static int onlineCount = 0;

    private Session session;

    private static CopyOnWriteArrayList<MyEndpoint> endpoints = new CopyOnWriteArrayList<>();

    public MyEndpoint() {
        super();
    }

    private static class ChatMessageHandler implements MessageHandler.Partial<String>{
        private Session session;

        private ChatMessageHandler(Session session) {
            this.session = session;
        }

        /**
         * Called when the next part of a message has been fully received.
         *
         * @param partialMessage the partial message data.
         * @param last           flag to indicate if this partialMessage is the last of the whole message being delivered.
         */
        @Override
        public void onMessage(String partialMessage, boolean last) {
            String msg = String.format("%s %s %s", session.getId(), "said:", partialMessage);
            // 向客户端发送消息
            broadcast(msg);
        }
    }

    private static void broadcast(String msg) {
        for (MyEndpoint server : endpoints) {
            try {
                synchronized (server) {
                    server.session.getBasicRemote().sendText(msg);
//                    server.session.getAsyncRemote().se
                }
            } catch (IOException e) {
                endpoints.remove(server);
                try {
                    server.session.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                String message = String.format("%s %s", server.session.getId(), "has been disconnected.");
                broadcast(message);
            }
        }
    }

    /**
     * Developers must implement this method to be notified when a new conversation has
     * just begun.
     *
     * @param session the session that has just been activated.
     * @param config  the configuration used to configure this endpoint.
     */
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        endpoints.add(this);
        addOnlineCount();
        this.session.addMessageHandler(new ChatMessageHandler(session));
        String message = String.format("%s %s", session.getId(), "has joined.");
        broadcast(message);
        logger.info("有新连接加入，当前连接数为：" + getOnlineCount());

    }

    /**
     * This method is called immediately prior to the session with the remote
     * peer being closed. It is called whether the session is being closed
     * because the remote peer initiated a close and sent a close frame, or
     * whether the local websocket container or this endpoint requests to close
     * the session. The developer may take this last opportunity to retrieve
     * session attributes such as the ID, or any application data it holds before
     * it becomes unavailable after the completion of the method. Developers should
     * not attempt to modify the session from within this method, or send new
     * messages from this call as the underlying
     * connection will not be able to send them at this stage.
     *
     * @param session     the session about to be closed.
     * @param closeReason the reason the session was closed.
     */
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        this.session = session;
        endpoints.remove(this);
        subOnlineCount();
        this.session.addMessageHandler(new ChatMessageHandler(session));
        String message = String.format("%s %s", session.getId(), "has closed.");
        broadcast(message);
        logger.info("客户端断开连接，当前连接数为：" + getOnlineCount());
    }

    /**
     * Developers may implement this method when the web socket session
     * creates some kind of error that is not modeled in the web socket protocol. This may for example
     * be a notification that an incoming message is too big to handle, or that the incoming message could not be encoded.
     *
     * <p>There are a number of categories of exception that this method is (currently) defined to handle:
     * <ul>
     * <li>connection problems, for example, a socket failure that occurs before
     * the web socket connection can be formally closed. These are modeled as
     * {@link SessionException}s</li>
     * <li>runtime errors thrown by developer created message handlers calls.</li>
     * <li>conversion errors encoding incoming messages before any message handler has been called. These
     * are modeled as {@link DecodeException}s</li>
     * </ul>
     *
     * @param session the session in use when the error occurs.
     * @param thr     the throwable representing the problem.
     */
    @Override
    public void onError(Session session, Throwable thr) {
        super.onError(session, thr);
    }

    private synchronized void subOnlineCount() {
        onlineCount--;
    }

    private synchronized int getOnlineCount() {
        return onlineCount;
    }

    private synchronized void addOnlineCount() {
        onlineCount++;
    }
}
