package my.testcontainers;

import com.github.dockerjava.api.model.PortBinding;
import java.io.File;
import org.apache.commons.compress.utils.IOUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class Test {

  public static void main(String[] args) throws Exception {

    File file =
        new File("scalecube-configuration/target/scalecube-configuration-2.1.7-SNAPSHOT-tests.jar");

    if (!file.exists()) {
      Process process = Runtime.getRuntime().exec("mvn package -DskipTests");
      IOUtils.copy(process.getInputStream(), System.out);
    }

    file =
        new File("scalecube-configuration/target/scalecube-configuration-2.1.7-SNAPSHOT-tests.jar");

    if (!file.exists()) {
      throw new IllegalStateException();
    }

    ImageFromDockerfile image =
        new ImageFromDockerfile()
            .withFileFromFile("scalecube-configuration/target/tests.jar", file)
            .withFileFromFile(
                "scalecube-configuration/target/lib",
                new File("scalecube-configuration/target/lib"))
            .withFileFromClasspath("Dockerfile", "TestDockerfile");

    GenericContainer container =
        new GenericContainer<>(image)
            .withExposedPorts(4801)
            .withEnv("JAVA_OPTS", "-DSEEDS=localhost:4801 -DDISCOVERY_PORT=4801 -DMEMBER_HOST=seed")
            //        .withEnv("MEMBER_HOST", "4801")
            //        .withEnv("MEMBER_PORT", "4801")
            .withEnv("logLevel", "DEBUG")
            .withNetwork(Network.SHARED)
            .withNetworkAliases("seed")
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName("seed").withPortBindings(PortBinding.parse(4801 + ":" + 4801));
                })
        //            .waitingFor(new HostPortWaitStrategy())
        ;

    container.start();

    GenericContainer container2 =
        new GenericContainer<>(image)
            .withExposedPorts(4802)
            .withEnv(
                "JAVA_OPTS", "-DSEEDS=localhost:4802 -DDISCOVERY_PORT=4802 -DMEMBER_HOST=follower")
            //        .withEnv("MEMBER_HOST", "4801")
            //        .withEnv("MEMBER_PORT", "4801")
            .withEnv("logLevel", "DEBUG")
            .withNetwork(Network.SHARED)
            .withNetworkAliases("follower")
            .withCreateContainerCmdModifier(
                cmd -> {
                  cmd.withName("follower").withPortBindings(PortBinding.parse(4802 + ":" + 4802));
                })
        //            .waitingFor(new HostPortWaitStrategy())
        ;

    container2.start();

    Thread.currentThread().join();
  }
}
