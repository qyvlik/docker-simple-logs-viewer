package io.github.qyvlik.dockersimplelogsviewer.live.listener;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import io.github.qyvlik.dockersimplelogsviewer.entity.LogRow;
import io.github.qyvlik.dockersimplelogsviewer.live.pusher.LogsLivePushTask;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class LogsLiveListener implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private WebSocketSessionContainer webSocketSessionContainer;

    private DockerClient dockerClient;

    private Executor pusherExecutor;

    private String channel;             // 订阅的通道

    private String sessionId;           // 订阅的ID

    private String containerId;

    private Integer tail;

    private String filter;               // 过滤日志

    public LogsLiveListener(WebSocketSessionContainer webSocketSessionContainer,
                            DockerClient dockerClient,
                            Executor pusherExecutor,
                            String channel,
                            String sessionId,
                            String containerId,
                            Integer tail,
                            String filter) {
        this.webSocketSessionContainer = webSocketSessionContainer;
        this.dockerClient = dockerClient;
        this.pusherExecutor = pusherExecutor;
        this.channel = channel;
        this.sessionId = sessionId;
        this.containerId = containerId;
        this.tail = tail;
        this.filter = filter;
    }

    public WebSocketSessionContainer getWebSocketSessionContainer() {
        return webSocketSessionContainer;
    }

    public void setWebSocketSessionContainer(WebSocketSessionContainer webSocketSessionContainer) {
        this.webSocketSessionContainer = webSocketSessionContainer;
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public void setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public Executor getPusherExecutor() {
        return pusherExecutor;
    }

    public void setPusherExecutor(Executor pusherExecutor) {
        this.pusherExecutor = pusherExecutor;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Integer getTail() {
        return tail;
    }

    public void setTail(Integer tail) {
        this.tail = tail;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public void run() {
        LogContainerCmd cmd = dockerClient.logContainerCmd(containerId)
                .withFollowStream(true)
                .withTail(tail)
                .withStdOut(true)
                .withStdErr(true);

        FrameReadAndPushCallback callback = new FrameReadAndPushCallback();
        cmd.exec(callback);
    }

    public class FrameReadAndPushCallback extends LogContainerResultCallback {

        @Override
        public void onNext(Frame item) {
            LogRow logRow = new LogRow(item);

            if (StringUtils.isBlank(filter) || logRow.getContent().contains(filter)) {
                Runnable errorHandle = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // stop the cmd
                            FrameReadAndPushCallback.this.close();
                        } catch (Exception e) {
                            logger.error("errorHandle run failure:{}", e.getMessage());
                        }
                    }
                };

                pusherExecutor.execute(new LogsLivePushTask(
                        webSocketSessionContainer,
                        logRow,
                        sessionId,
                        channel,
                        errorHandle
                ));
            }

            super.onNext(item);
        }
    }
}
