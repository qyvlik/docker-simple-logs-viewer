package io.github.qyvlik.dockersimplelogsviewer.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientBeansConfig {

    @Value("${docker.client.docker.host}")
    private String dockerHost;

    @Value("${docker.client.docker.tlsVerify}")
    private Boolean dockerTlsVerify;

    @Value("${docker.client.docker.certPath}")
    private String dockerCertPath;

    @Value("${docker.client.docker.config}")
    private String dockerConfigPath;

    @Value("${docker.client.docker.api.version}")
    private String dockerApiVersion;

    @Value("${docker.client.docker.registry.url}")
    private String dockerRegistryUrl;

    @Value("${docker.client.docker.registry.username}")
    private String dockerRegistryUsername;

    @Value("${docker.client.docker.registry.password}")
    private String dockerRegistryPassword;

    @Value("${docker.client.docker.registry.email}")
    private String dockerRegistryEmail;

    @Bean("dockerClient")
    public DockerClient dockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withDockerTlsVerify(dockerTlsVerify)
                .withDockerCertPath(dockerCertPath)
                .withDockerConfig(dockerConfigPath)
                .withApiVersion(dockerApiVersion)
                .withRegistryUrl(dockerRegistryUrl)
                .withRegistryUsername(dockerRegistryUsername)
                .withRegistryPassword(dockerRegistryPassword)
                .withRegistryEmail(dockerRegistryEmail)
                .build();
        return DockerClientBuilder.getInstance(config).build();
    }
}
