package io.github.qyvlik.dockersimplelogsviewer.config;

import io.github.qyvlik.jsonrpclite.core.handle.WebSocketDispatch;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.concurrent.RpcExecutor;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.rpcinvoker.RpcDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class LogViewerBeansConfig {

    @Bean("subPushExecutor")
    public Executor subPushExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean("webSocketSessionContainer")
    public WebSocketSessionContainer webSocketSessionContainer() {
        return new WebSocketSessionContainer(2000, 20000);
    }

    @Bean(value = "rpcDispatcher", initMethod = "initInvoker")
    public RpcDispatcher rpcDispatcher(
            @Autowired ApplicationContext applicationContext) {
        return new RpcDispatcher(applicationContext);
    }

    @Bean("rpcExecutor")
    public RpcExecutor rpcExecutor(
            @Qualifier("subPushExecutor") Executor subPushExecutor) {
        return new RpcExecutor() {
            @Override
            public Executor defaultExecutor() {
                return subPushExecutor;
            }

            @Override
            public Executor getByRequest(WebSocketSession session, RequestObject requestObject) {
                return null;
            }
        };
    }

    @Bean("subPushDispatch")
    public WebSocketDispatch subPushDispatch(@Qualifier("webSocketSessionContainer") WebSocketSessionContainer webSocketSessionContainer,
                                             @Qualifier("rpcDispatcher") RpcDispatcher rpcDispatcher,
                                             @Qualifier("rpcExecutor") RpcExecutor rpcExecutor,
                                             @Autowired List<WebSocketFilter> filterList) {
        WebSocketDispatch webSocketDispatch = new WebSocketDispatch();

        webSocketDispatch.setGroup(Constant.GROUP);
        webSocketDispatch.setRpcExecutor(rpcExecutor);
        webSocketDispatch.setRpcDispatcher(rpcDispatcher);
        webSocketDispatch.addFilterList(filterList);
        webSocketDispatch.setWebSocketSessionContainer(webSocketSessionContainer);

        return webSocketDispatch;
    }
}
