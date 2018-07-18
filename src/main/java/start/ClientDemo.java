package start;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class ClientDemo {

    private final String host = "127.0.0.1";

    private final int port = 9111;

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        //创建Bootstrap
        Bootstrap bootstrap = new Bootstrap();

        try {
            //指定EventLoopGroup处理客户端事件
            bootstrap.group(group)
                    //适用于Nio传输的Channel类型
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    //在创建Channl时，向ChannelPipeline添加一个ClientHandler实例
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });

            //连接到服务器，阻塞直到连接完成
            ChannelFuture future = bootstrap.connect().sync();
            //阻塞到Channel关闭
            future.channel().closeFuture().sync();
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            //关闭线程池并释放资源
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String args[]) throws Exception {
        new ClientDemo().start();
    }

}
