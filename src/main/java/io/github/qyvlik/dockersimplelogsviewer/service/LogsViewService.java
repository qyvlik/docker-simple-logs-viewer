package io.github.qyvlik.dockersimplelogsviewer.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import com.google.common.collect.Lists;
import io.github.qyvlik.dockersimplelogsviewer.config.Constant;
import io.github.qyvlik.dockersimplelogsviewer.entity.LogRow;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcMethod;
import io.github.qyvlik.jsonrpclite.core.jsonrpc.annotation.RpcService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@RpcService
@Service
public class LogsViewService {

    @Autowired
    @Qualifier("dockerClient")
    private DockerClient dockerClient;

    @RpcMethod(group = Constant.GROUP, value = "docker.container.list")
    public List<Container> dockerContainerList() {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();
    }

    @RpcMethod(group = Constant.GROUP, value = "docker.logs")
    public List<LogRow> dockerLogs(String containerId, Integer tail, String filter) {
        FrameReaderCallback frameReaderCallback = new FrameReaderCallback();
        frameReaderCallback.filter = filter;
        try {
            dockerClient.logContainerCmd(containerId)
                    .withFollowStream(false)
                    .withTail(tail)
                    .withStdOut(true)
                    .withStdErr(true)
                    .exec(frameReaderCallback)
                    .awaitCompletion();
            return frameReaderCallback.logRows;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static class FrameReaderCallback extends LogContainerResultCallback {
        public String filter;
        public List<LogRow> logRows = Lists.newLinkedList();

        @Override
        public void onNext(Frame item) {
            LogRow logRow = new LogRow(item);
            if (StringUtils.isBlank(filter) || logRow.getContent().contains(filter)) {
                logRows.add(logRow);
            }
            super.onNext(item);
        }
    }

}
