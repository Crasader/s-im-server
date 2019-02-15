package mmp.im.gate.configuration;


import mmp.im.common.protocol.handler.INettyMessageHandler;
import mmp.im.common.protocol.handler.MessageHandlerHolder;
import mmp.im.common.server.cache.connection.AcceptorChannelMap;
import mmp.im.gate.acceptor.ClientToGateAcceptor;
import mmp.im.gate.acceptor.ClientToGateAcceptorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AcceptorConfig {



    @Bean
    public AcceptorChannelMap acceptorChannelHandlerMap() {
        return new AcceptorChannelMap();
    }

    //////////////
    @Value("${clientToGateAcceptor.bind.port}")
    private Integer clientToGateAcceptorPort;


    @Value("${acceptor.serverId}")
    private String serverId;


    @Bean
    public ClientToGateAcceptorHandler clientToGateAcceptorHandler(AcceptorChannelMap acceptorChannelMap) {
        ClientToGateAcceptorHandler acceptorHandler = new ClientToGateAcceptorHandler();
        acceptorHandler.setAcceptorChannelMap(acceptorChannelMap);
        acceptorHandler.setMessageHandlerHolder(new MessageHandlerHolder("mmp.im.gate.acceptor.handler", INettyMessageHandler.class));
        return acceptorHandler;
    }

    @Bean
    public ClientToGateAcceptor clientToGateAcceptor(ClientToGateAcceptorHandler acceptorHandler) {
        ClientToGateAcceptor clientToGateAcceptor = new ClientToGateAcceptor(clientToGateAcceptorPort);
        clientToGateAcceptor.setServeId(serverId);
        clientToGateAcceptor.setAcceptorHandler(acceptorHandler);
        return clientToGateAcceptor;
    }


}
