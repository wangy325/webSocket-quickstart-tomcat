package com.zc.playground.server;


import org.apache.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**<p>使用注解的形式创建一个server</p>
 * <p>@ServerEndpoint注解用来指定一个URI，客户端可以通过这个URI来连接到WebSocket。类似Servlet的注解mapping。无需在web.xml中配置</p>
 *
 * @author wangy
 * @version 1.0
 * @date 2019/4/18 / 10:43
 */

@ServerEndpoint("/webSocket")
public class MyWebSocketServer {
    private static Logger logger = Logger.getLogger(MyWebSocketServer.class);

    private static int onlineCount = 0;
    /**
     * concurrent包的线程安全集合，用来存放每个客户端对应的MyWebSocket对象
     * 若要实现服务端与单一客户端通信的话，可以使用Map来存放，其中Key可以为用户标识
     */
    private static CopyOnWriteArrayList<MyWebSocketServer> webSocketServers = new CopyOnWriteArrayList<>();

    /**
     * 与某个客户端的（单独）会话，通过其给客户端发送数据
     */
    private Session session;

    public MyWebSocketServer() {

    }

    /**
     * 连接建立成功之后调用
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        webSocketServers.add(this);
        addOnlineCount();
        logger.info("有新连接加入，当前连接数为：" + getOnlineCount());
    }

    /**
     * 连接关闭之后调用
     */
    @OnClose
    public void onClose() {
        webSocketServers.remove(this);
        subOnlineCount();
        logger.info("一个连接已断开，当前连接数为：" + getOnlineCount());
    }

    /**
     * 收到客户端消息之后调用的方法
     *
     * @param msg     消息内容
     * @param session
     */
    @OnMessage
    public void onMessage(String msg, Session session) {
        logger.info("来自客户端的消息： " + msg);
        // 群发消息
        for (MyWebSocketServer webSocketServer : webSocketServers) {
            try {
                webSocketServer.sendMsg(msg+msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发生错误后调用
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error){
       logger.error("客户端强制断开一个连接");
        error.printStackTrace();
    }

    private void sendMsg(String msg) throws IOException {
        this.session.getBasicRemote().sendText(msg);
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

