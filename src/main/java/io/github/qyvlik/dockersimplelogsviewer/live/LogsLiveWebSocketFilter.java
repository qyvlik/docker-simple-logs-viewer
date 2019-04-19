package io.github.qyvlik.dockersimplelogsviewer.live;

import com.alibaba.fastjson.JSONArray;
import com.github.dockerjava.api.DockerClient;
import io.github.qyvlik.dockersimplelogsviewer.config.Constant;
import io.github.qyvlik.dockersimplelogsviewer.live.listener.LogsLiveListener;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketFilter;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.entity.request.RequestObject;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.SubRequestObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.Executor;

@Service
public class LogsLiveWebSocketFilter extends WebSocketFilter {

    @Autowired
    @Qualifier("webSocketSessionContainer")
    private WebSocketSessionContainer webSocketSessionContainer;

    @Autowired
    private DockerClient dockerClient;

    @Autowired
    @Qualifier("subPushExecutor")
    private Executor subPushExecutor;

    public LogsLiveWebSocketFilter() {
        setGroup(Constant.GROUP);
    }

    @Override
    public boolean filter(WebSocketSession session, RequestObject requestObject) {
        return true;
    }

    @Override
    public boolean filter(WebSocketSession session, SubRequestObject subRequestObject) {
        if (subRequestObject.getChannel().equalsIgnoreCase(Constant.SUB_LIVE_DOCKER_LOGS)) {
            JSONArray params = (JSONArray) subRequestObject.getParams();
            // install listener
            subPushExecutor.execute(new LogsLiveListener(
                    webSocketSessionContainer,
                    dockerClient,
                    subPushExecutor,
                    Constant.SUB_LIVE_DOCKER_LOGS,
                    session.getId(),
                    params.getString(0),                // containerId
                    params.getInteger(1),               // tail
                    params.getString(2)                 // filter
            ));
        }

        return true;
    }
}
