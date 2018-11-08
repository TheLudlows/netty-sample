package heartbeat;

import com.sun.deploy.util.SessionState.Client;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author TheLudlows
 */
public class HeartBeatClientHandler extends SimpleChannelInboundHandler<String> {

  private ClientStarter clientStarter;

  public HeartBeatClientHandler(ClientStarter clientStarter) {
    this.clientStarter = clientStarter;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
    if (msg.equals("PONG")) {
      System.out.println("receive form server PONG");
    }
    ReferenceCountUtil.release(msg);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if(evt instanceof IdleStateEvent) {
      IdleState state = ((IdleStateEvent)evt).state();
      if(state == IdleState.WRITER_IDLE) {
        ctx.writeAndFlush("PING");
        System.out.println("send PING");
      }
      if(state == IdleState.READER_IDLE) {
        System.out.println("五秒内没有收到PONG");
      }
    }
    super.userEventTriggered(ctx, evt);
  }
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    System.err.println("客户端与服务端断开连接,断开的时间为："+(new Date()).toString());
    // 定时线程 断线重连
    final EventLoop eventLoop = ctx.channel().eventLoop();
    eventLoop.schedule(() -> clientStarter.connect(), 10, TimeUnit.SECONDS);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if(cause instanceof IOException) {
      System.out.println("server "+ctx.channel().remoteAddress()+"强制关闭连接");
    }
  }
}
