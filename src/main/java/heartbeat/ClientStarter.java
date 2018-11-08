package heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;

/**
 * @author TheLudlows
 */
public class ClientStarter {

  private Bootstrap bootstrap;
  private EventLoopGroup group;

  public ClientStarter(Bootstrap bootstrap, EventLoopGroup group) {
    this.bootstrap = bootstrap;
    this.group = group;
    ClientStarter clientStarter = this;
    bootstrap.group(group)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast( new StringEncoder());
            ch.pipeline().addLast( new StringDecoder());
            ch.pipeline().addLast( new IdleStateHandler(6,5,0));
            ch.pipeline().addLast(new HeartBeatClientHandler(clientStarter));
          }
        });
  }

  public static void main(String[] args) {
    ClientStarter starter = new ClientStarter(new Bootstrap(), new NioEventLoopGroup());
    starter.connect();
  }

  public void connect() {
    ChannelFuture future = bootstrap.connect(new InetSocketAddress("localhost", 1111));
    future.addListener(future1 -> {
      if (future1.isSuccess()) {
        System.out.println("Connected Success");
      }
    });
  }
}
