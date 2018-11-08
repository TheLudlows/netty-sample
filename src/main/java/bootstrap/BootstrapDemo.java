package bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

public class    BootstrapDemo {

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new SimpleChannelInboundHandler<ByteBuf>() {

                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                        System.out.println(ctx.channel());
                        System.out.println("received:" + msg.toString(CharsetUtil.UTF_8));
                    }
                });
        ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost", 8080));
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("Connected!");
                    System.out.println(future.channel());
                    ChannelFuture future1 = future.channel().writeAndFlush(Unpooled.copiedBuffer("four you from client！", CharsetUtil.UTF_8));
                    if (future1.isSuccess()) {
                        System.out.println("写过去了！");
                    } else {
                        System.out.println("写失败了！");
                        future1.cause().printStackTrace();
                    }
                } else {
                    System.out.println("error");
                    future.cause().printStackTrace();
                }
            }
        });

    }
}
