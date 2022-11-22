package com.example.demo;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.LazyFuture;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

public class OCIwithTCCTest {
    private static final Future<String> IMAGE_FUTURE = new LazyFuture<>() {
        @Override
        protected String resolve() {
            // Find project's root dir
            File cwd;
            for (
                    cwd = new File(".");
                    !new File(cwd, "mvnw").isFile();
                    cwd = cwd.getParentFile()
            );

            // Make it unique per folder (for caching)
            var imageName = String.format(
                    "local/demo-%s:%s",
                    DigestUtils.md5DigestAsHex(cwd.getAbsolutePath().getBytes()),
                    System.currentTimeMillis()
            );

            var properties = new Properties();
            properties.put("spring-boot.build-image.imageName", imageName);
            properties.put("skipTests", "true");

            var request = new DefaultInvocationRequest()
                    .addShellEnvironment("DOCKER_HOST", DockerClientFactory.instance().getTransportConfig().getDockerHost().toString())
                    .setPomFile(new File(cwd, "pom.xml"))
                    .setGoals(List.of("spring-boot:build-image"))
                    .setMavenExecutable(new File(cwd, "mvnw"))
                    .setProperties(properties);

            InvocationResult invocationResult;
            try {
                invocationResult = new DefaultInvoker().execute(request);
            } catch (MavenInvocationException e) {
                throw new RuntimeException(e);
            }

            if (invocationResult.getExitCode() != 0) {
                throw new RuntimeException(invocationResult.getExecutionException());
            }

            return imageName;
        }
    };


    static final GenericContainer<?> APP = new GenericContainer<>(IMAGE_FUTURE)
            .withExposedPorts(8080);
    
    @Test
    void containerStartupTest() {
        APP.start();
    }
}