package io.github.qyvlik.dockersimplelogsviewer.live.pusher;

import com.alibaba.fastjson.JSON;
import io.github.qyvlik.dockersimplelogsviewer.config.Constant;
import io.github.qyvlik.jsonrpclite.core.handle.WebSocketSessionContainer;
import io.github.qyvlik.jsonrpclite.core.jsonsub.pub.ChannelMessage;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.ChannelSession;
import io.github.qyvlik.jsonrpclite.core.jsonsub.sub.SubChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

public class LogsLivePushTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private WebSocketSessionContainer webSocketSessionContainer;

    private Object message;

    private String toSessionId;

    private String channel;

    private Runnable errorHandle;

    public LogsLivePushTask(WebSocketSessionContainer webSocketSessionContainer,
                            Object message,
                            String toSessionId,
                            String channel,
                            Runnable errorHandle) {
        this.webSocketSessionContainer = webSocketSessionContainer;
        this.message = message;
        this.toSessionId = toSessionId;
        this.channel = channel;
        this.errorHandle = errorHandle;
    }

    @Override
    public void run() {
        SubChannel subChannel = webSocketSessionContainer.getSubscribeChannelMap().get(channel);

        if (subChannel == null) {
            logger.debug("sendMessage return subChannel is null, channel:{}", channel);
            this.errorHandle.run();
            return;
        }

        ChannelSession channelSession = subChannel.getSessionMap().get(toSessionId);
        if (channelSession == null) {
            logger.debug("sendMessage return channelSession is null, toSessionId:{}", toSessionId);
            this.errorHandle.run();
            return;
        }

        ChannelMessage<Object> tickMessage = new ChannelMessage<Object>();
        tickMessage.setChannel(Constant.SUB_LIVE_DOCKER_LOGS);
        tickMessage.setResult(message);

        String rawText = JSON.toJSONString(tickMessage);

        boolean r = webSocketSessionContainer.safeSend(
                channelSession.getWebSocketSession(),
                new TextMessage(rawText)
        );

        if (!r) {
            webSocketSessionContainer.onUnSub(Constant.SUB_LIVE_DOCKER_LOGS, channelSession.getWebSocketSession());
            this.errorHandle.run();
        }

    }
}
