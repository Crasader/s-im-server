package mmp.im;

import mmp.im.common.server.cache.acknowledge.ResendMessageMap;
import mmp.im.common.server.cache.acknowledge.ResendMessageThread;
import mmp.im.gate.acceptor.ClientToGateAcceptor;
import mmp.im.gate.connector.GateToDistConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class WebsocketGateApplication implements CommandLineRunner {


    @Autowired
    private GateToDistConnector gateToDistConnector;

    @Autowired
    private ClientToGateAcceptor clientToGateAcceptor;

    @Autowired
    private ResendMessageMap resendMessageMap;

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        SpringApplication.run(WebsocketGateApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {

        new Thread(() -> gateToDistConnector.connect()).start();

        new Thread(() -> clientToGateAcceptor.bind()).start();


        LOG.warn("starting ResendMessageThread... ");

        new Thread(new ResendMessageThread(resendMessageMap), "ResendMessageThread").start();

        LOG.warn("Spring Boot 启动完成");
    }


}
