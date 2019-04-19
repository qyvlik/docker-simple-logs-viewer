package io.github.qyvlik.dockersimplelogsviewer.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientBeansConfig {

    @Bean("dockerClient")
    public DockerClient dockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .withDockerTlsVerify(false)
                .withApiVersion("1.37")
                .withRegistryUrl("https://index.docker.io/v1/")
                .build();
        return DockerClientBuilder.getInstance(config).build();
    }
}
