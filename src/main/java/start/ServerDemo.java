package start;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class ServerDemo {
    private int prot;

    public ServerDemo(int prot) {
        this.prot = prot;
    }

    public static void main(String[] args) throws InterruptedException {
        new ServerDemo(9111).satart();
    }

    public void satart() throws InterruptedException {
        final ServerHandller serverHandller = new ServerHandller();
        //创建ServerBootstrap实例
        ServerBootstrap boot = new ServerBootstrap();
        //创建一个EventLoopGroup
        EventLoopGroup group = new NioEventLoopGroup();
        //指定实例来接受和处理新的连接。
        boot.group(group)
                //指定使用NIO Channel传输
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(prot))
                //当一个新的连接被接受时，一个新的子Channel会被创建，
                // ChannelInitializer会把serverHandller的实例加入到该Channel的ChannelPipeLine
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(serverHandller);
                    }
                });
        try {
            //异步绑定到服务器，调用sync方法阻塞当前线程，直到绑定完成
            ChannelFuture future = boot.bind().sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}

