package io.github.qyvlik.dockersimplelogsviewer.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LogsViewServiceTest {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void listContainers() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withDockerTlsVerify(false)
                .withApiVersion("1.37")
                .withRegistryUrl("https://index.docker.io/v1/")
                .build();
        DockerClient docker = DockerClientBuilder.getInstance(config).build();

        List<Container> containerList = docker.listContainersCmd().withShowAll(true).exec();
        logger.info("containerList:{}", containerList);
    }

    @Test
    public void liveLogsView() throws InterruptedException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withDockerTlsVerify(false)
                .withApiVersion("1.37")
                .withRegistryUrl("https://index.docker.io/v1/")
                .build();
        DockerClient docker = DockerClientBuilder.getInstance(config).build();

        FrameReaderITestCallback collectFramesCallback = new FrameReaderITestCallback();

        docker.logContainerCmd("/local-redis")
                .withTail(10)
                .withStdOut(true)
                .withStdErr(true)
                .exec(collectFramesCallback)
                .awaitCompletion();

        for (Frame frame : collectFramesCallback.frames) {
            logger.info("frame logs:{}", new String(frame.getPayload()));
        }
    }

    public static class FrameReaderITestCallback extends LogContainerResultCallback {
        public List<Frame> frames = new ArrayList<>();

        @Override
        public void onNext(Frame item) {
            frames.add(item);
            super.onNext(item);
        }
    }

}