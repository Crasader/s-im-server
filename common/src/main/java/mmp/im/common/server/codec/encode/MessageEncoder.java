package mmp.im.common.server.codec.encode;


import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import mmp.im.common.protocol.util.ProtocolHeader;
import mmp.im.common.protocol.util.ProtocolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<MessageLite> {


    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf out) throws Exception {
        LOG.warn("encode start...");

        // TODO 加密消息体

        byte[] body = msg.toByteArray();
        byte[] header = encodeHeader(msg, (short) body.length);
        LOG.warn("encode header... {}", header);
        LOG.warn("encode body... {}", body);
        LOG.warn("bodyLength... {}", body.length);
        out.writeBytes(header);
        out.writeBytes(body);

        // ByteBuf buf = Unpooled.buffer(header.length + body.length);
        // buf.writeBytes(header);
        // buf.writeBytes(body);
        // out.writeBytes(buf);

        LOG.warn("encode finished...");
    }

    private byte[] encodeHeader(MessageLite msg, short bodyLength) {

        byte[] header = new byte[ProtocolHeader.HEAD_LENGTH];

        header[0] = (byte) ((ProtocolHeader.FLAG_NUM >> 24) & 0xff);
        header[1] = (byte) ((ProtocolHeader.FLAG_NUM >> 16) & 0xff);
        header[2] = (byte) ((ProtocolHeader.FLAG_NUM >> 8) & 0xff);
        header[3] = (byte) (ProtocolHeader.FLAG_NUM & 0xff);

        byte commandId = ProtocolUtil.encodeCommandId(msg);
        header[4] = commandId;

        header[5] = (byte) ((bodyLength >> 8) & 0xff);

        header[6] = (byte) (bodyLength & 0xff);

        return header;
    }
}
