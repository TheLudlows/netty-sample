package heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @author TheLudlows
 */
public class ServerStarter {

  public static void main(String[] args) {
    EventLoopGroup bossgroup = new NioEventLoopGroup();
    EventLoopGroup workergroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossgroup, workergroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline().addLast(new IdleStateHandler(5, 0, 0));
              ch.pipeline().addLast(new StringDecoder());
              ch.pipeline().addLast(new StringEncoder());
              ch.pipeline().addLast(new HeartBeatServerHandller());
            }
          });
      // 服务器绑定端口监听
      ChannelFuture future = bootstrap.bind(1111).sync();
      System.out.println("server start ,port: "+1111);
      // 监听服务器关闭监听，此方法会阻塞
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      bossgroup.shutdownGracefully();
      workergroup.shutdownGracefully();
    }
  }

}
