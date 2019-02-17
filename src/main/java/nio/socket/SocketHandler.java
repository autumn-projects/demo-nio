package nio.socket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SocketHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channel active>>>>>>>");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf message = (ByteBuf) msg;
        byte[] response = new byte[message.readableBytes()];
        message.readBytes(response);
        System.out.println("receive client info: " + new String(response));

        String sendContent = "hello client ,im server, this is u say:" + new String(response);
        ByteBuf seneMsg = Unpooled.buffer(sendContent.length());
        seneMsg.writeBytes(sendContent.getBytes());

        ctx.writeAndFlush(seneMsg);
        System.out.println("send info to client:" + sendContent);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}