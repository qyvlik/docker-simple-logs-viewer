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

    @Value("${docker.client.api.version}")
    private String apiVersion;

    @Value("${docker.client.registry.url}")
    private String registryUrl;

    @Value("${docker.client.registry.username}")
    private String registryUsername;

    @Value("${docker.client.registry.password}")
    private String registryPassword;

    @Value("${docker.client.registry.email}")
    private String registryEmail;

    @Bean("dockerClient")
    public DockerClient dockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost)
                .withDockerTlsVerify(dockerTlsVerify)
                .withDockerCertPath(dockerCertPath)
                .withDockerConfig(dockerConfigPath)
                .withApiVersion(apiVersion)
                .withRegistryUrl(registryUrl)
                .withRegistryUsername(registryUsername)
                .withRegistryPassword(registryPassword)
                .withRegistryEmail(registryEmail)
                .build();
        return DockerClientBuilder.getInstance(config).build();
    }
}
