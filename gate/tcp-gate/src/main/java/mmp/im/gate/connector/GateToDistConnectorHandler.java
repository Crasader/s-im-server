package mmp.im.gate.connector;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import mmp.im.common.protocol.handler.NettyMessageHandlerHolder;
import mmp.im.common.server.cache.acknowledge.ResendMessage;
import mmp.im.common.server.cache.connection.ConnectorChannelHolder;
import mmp.im.common.server.util.AttributeKeyHolder;
import mmp.im.common.server.util.MessageBuilder;
import mmp.im.gate.util.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.ArrayList;


import static mmp.im.common.protocol.ProtobufMessage.ServerRegister;

@Data
@Accessors(chain = true)
@ChannelHandler.Sharable
public class GateToDistConnectorHandler extends ChannelInboundHandlerAdapter {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private NettyMessageHandlerHolder NettyMessageHandlerHolder;

    private ConnectorChannelHolder connectorChannelHolder;

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object message) throws Exception {

        Channel channel = channelHandlerContext.channel();
        // 处理消息
        if (message instanceof MessageLite) {
            channel.eventLoop().execute(() -> NettyMessageHandlerHolder.assignHandler(channelHandlerContext, (MessageLite) message));
        } else {
            // 从InBound里读取的ByteBuf要手动释放，自己创建的ByteBuf要自己负责释放
            // write Bytebuf到OutBound时由netty负责释放，不需要手动调用release
            ReferenceCountUtil.release(message);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        LOG.warn("channelActive... remoteAddress... {} ", channelHandlerContext.channel().remoteAddress());

        Channel channel = channelHandlerContext.channel();
        // 维护一个已接收的消息列表 避免重传造成的重复接收
        channel.attr(AttributeKeyHolder.REV_SEQ_LIST).set(new ArrayList<>());


        channelHandlerContext.channel().writeAndFlush(MessageBuilder.buildHeartbeat());

        ServerRegister m = MessageBuilder.buildServerRegister(ContextHolder.getServeId(), "mmp");

        // 注册
        ContextHolder.getMessageSender().reply(channelHandlerContext, m);

        // 发的消息待确认
        ContextHolder.getResendMessageMap().put(m.getSeq(), new ResendMessage(m.getSeq(), m, channelHandlerContext));

        channelHandlerContext.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {

        Channel channel = channelHandlerContext.channel();

        if (channel != null) {
            channel.attr(AttributeKeyHolder.REV_SEQ_LIST).set(null);
            SocketAddress socketAddress = channel.remoteAddress();
            if (socketAddress != null) {
                LOG.warn("channelInactive... remove remoteAddress... {}", channel.remoteAddress());
                connectorChannelHolder.setChannelHandlerContext(null);
            }
            // 关闭连接
            if (channel.isOpen()) {
                channel.close();
                LOG.warn("channelInactive... close remoteAddress... {}", channel.remoteAddress());
            }
        }

        channelHandlerContext.fireChannelInactive();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception {
        channelHandlerContext.fireExceptionCaught(cause);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                LOG.warn("IdleState.WRITER_IDLE");
                // 发送心跳
                ContextHolder.getMessageSender().reply(channelHandlerContext, MessageBuilder.buildHeartbeat());
            }
        }

        super.userEventTriggered(channelHandlerContext, evt);
    }

}

