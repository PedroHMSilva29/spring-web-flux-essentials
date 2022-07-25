package br.com.pehenmo.springwebfluxessentials;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import reactor.blockhound.BlockHound;

/**
 * https://linuxhint.com/postgresql_docker/
 * https://github.com/reactor/BlockHound/issues/33
 */
@SpringBootApplication
public class SpringWebFluxEssentialsApplication {

	static{
		BlockHound.install(builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID")
				.allowBlockingCallsInside("java.io.InputStream", "readNBytes")
				.allowBlockingCallsInside("java.io.FilterInputStream", "read")
				.allowBlockingCallsInside("java.io.RandomAccessFile", "readBytes")
				.allowBlockingCallsInside("java.lang.Object", "wait")
				.allowBlockingCallsInside("java.util.stream.ReferencePipeline", "collect")
				.allowBlockingCallsInside("java.util.concurrent.ForkJoinTask", "externalAwaitDone")
				.allowBlockingCallsInside("java.util.concurrent.ForkJoinTask", "doInvoke")
				.allowBlockingCallsInside("org.springdoc.core.OpenAPIService", "initializeHiddenRestController")
		);
	}

	public static void main(String[] args) {


		SpringApplication.run(SpringWebFluxEssentialsApplication.class, args);
	}

}
