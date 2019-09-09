package com.zc.playground.server.traditional;

import com.zc.playground.server.MyWebSocketServer;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import java.util.HashSet;
import java.util.Set;

/**
 * 编程式 endPoint 的配置类，配置webSocket的请求地址等其他配置信息
 * @author wangy
 * @version 1.0
 * @date 2019/4/19 / 16:38
 */
public class MyEndpointConfig implements ServerApplicationConfig {
    /**
     * Return a set of ServerEndpointConfig instances that the server container
     * will use to deploy the programmatic endpoints. The set of Endpoint classes passed in to this method is
     * the set obtained by scanning the archive containing the implementation
     * of this ServerApplicationConfig. This set passed in
     * may be used the build the set of ServerEndpointConfig instances
     * to return to the container for deployment.
     *
     * @param endpointClasses the set of all the Endpoint classes in the archive containing
     *                        the implementation of this interface.
     * @return the non-null set of ServerEndpointConfig s to deploy on the server, using the empty set to
     * indicate none.
     * @see javax.websocket.server.ServerContainer
     */
    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
        Set<ServerEndpointConfig> result = new HashSet<>();
        if (endpointClasses.contains(MyEndpoint.class)) {
            // 配置 endpoint ，请求地址
            result.add(ServerEndpointConfig.Builder.create(MyEndpoint.class, "/ep").build());
            result.add(ServerEndpointConfig.Builder.create(MyWebSocketServer.class, "/webSocket").build());
        }
        return result;
    }

    /**
     * Return a set of annotated endpoint classes that the server container
     * must deploy. The set of classes passed in to this method is
     * the set obtained by scanning the archive containing the implementation
     * of this interface. Therefore, this set passed in contains all the annotated endpoint classes
     * in the JAR or WAR file containing the implementation of this interface. This set passed in
     * may be used the build the set to return to the container for deployment.
     *
     * @param scanned the set of all the annotated endpoint classes in the archive containing
     *                the implementation of this interface.
     * @return the non-null set of annotated endpoint classes to deploy on the server, using the empty
     * set to indicate none.
     */
    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        return null;
    }
}
