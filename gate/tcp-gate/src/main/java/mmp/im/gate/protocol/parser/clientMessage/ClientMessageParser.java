package mmp.im.gate.protocol.parser.clientMessage;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import mmp.im.gate.config.AttributeKeyHolder;
import mmp.im.server.tcp.protocol.handler.IMessageTypeHandler;
import mmp.im.server.tcp.protocol.parser.IProtocolParser;
import mmp.im.common.util.reflect.PackageUtil;
import mmp.im.gate.util.MQHolder;
import mmp.im.protocol.ClientMessageBody;
import mmp.im.protocol.ProtocolHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientMessageParser implements IProtocolParser {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());


    private Map<String, Object> msgTypeHandlers;

    {
        this.msgTypeHandlers = new HashMap<>();
        List<Class<?>> classList = PackageUtil.getSubClasses("mmp.im.gate.protocol.clientMessage", IMessageTypeHandler.class);

        classList.forEach(v -> {
            try {
                IMessageTypeHandler e = (IMessageTypeHandler) v.newInstance();
                this.msgTypeHandlers.put(e.getHandlerName(), e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }


    @Override
    public int getProtocolKind() {
        return ProtocolHeader.ProtocolType.MESSAGE.getType();
    }

    @Override
    public void parse(ChannelHandlerContext channelHandlerContext, byte[] bytes) {

        String userId = channelHandlerContext.channel().attr(AttributeKeyHolder.USER_ID).get();
        // 没登陆就关闭
        if (userId == null) {
            channelHandlerContext.channel().close();
            return;
        }

        ClientMessageBody.ClientMessage msg = null;

        try {
            msg = ClientMessageBody.ClientMessage.parseFrom(bytes);


            if (!userId.equals(msg.getFrom())) {
                channelHandlerContext.channel().close();
                LOG.warn("非法消息，通道关闭");
                return;
            }
            // 推送到logic处理，数据库操作等
            MQHolder.getMq().publish("", "", msg);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        if (msg != null) {
            String type = String.valueOf(msg.getType().getNumber());
            IMessageTypeHandler handler = (IMessageTypeHandler) this.getMsgTypeHandlers().get(type);
            if (handler != null) {
                handler.process(channelHandlerContext, msg);
            }
        }


    }

    private Map<String, Object> getMsgTypeHandlers() {
        return this.msgTypeHandlers;
    }


}
