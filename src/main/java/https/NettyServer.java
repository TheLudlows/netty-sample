package https;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;



public class NettyServer {

    public void bind(int port){
        // 创建EventLoopGroup
        EventLoopGroup bossGroup = new NioEventLoopGroup();        //创建BOSS线程组 用于服务端接受客户端的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();      //创建WORK线程组 用于进行SocketChannel的网络读写
        try {
            // 创建ServerBootStrap实例
            // ServerBootstrap 用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度
            ServerBootstrap b = new ServerBootstrap();
            // 绑定Reactor线程池
            b.group(bossGroup, workerGroup)
                    // 设置并绑定服务端Channel
                    // 指定所使用的NIO传输的Channel
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new HttpServerInitializer());
            // 绑定端口，同步等待成功
            ChannelFuture future = b.bind(port).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅地关闭
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args){
        new NettyServer().bind(8899);
    }


}