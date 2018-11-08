package heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.IOException;

/**
 * @author TheLudlows
 */
public  class HeartBeatServerHandller extends SimpleChannelInboundHandler {
  //连续超过N次未收到client的ping消息，那么关闭该通道，等待client重连
  private static final int MAX_UN_REC_PING_TIMES = 3 ;
  private int failTimes = 0;
  //收到一个client的ping消息的个数
  private int allPings = 0;
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state()== IdleState.READER_IDLE){
        System.out.println("5秒内没有收到"+ctx.channel().remoteAddress()+" PING");
        // 失败计数器次数大于等于3次的时候，关闭链接，等待client重连
        if (failTimes >= MAX_UN_REC_PING_TIMES) {
          System.out.println("15秒内没有收到"+ctx.channel().remoteAddress()+"PING ,即将关闭连接！");
          ctx.close();
        } else {
          // 失败计数器加1
          failTimes++;
        }
      }else {
        super.userEventTriggered(ctx,evt);
      }
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg!=null && msg.equals("PING")){
      System.out.println("客户端"+ctx.channel().remoteAddress()+"第 "+(++allPings)+" 个PING");
      ctx.writeAndFlush("PONG");
    }
  }
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    System.out.println(ctx.channel().remoteAddress()+"客户端已连接");
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    System.out.println(ctx.channel().remoteAddress()+"客户端已断开");
  }
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if(cause instanceof IOException) {
      System.out.println("client "+ctx.channel().remoteAddress()+"强制关闭连接");
    }
  }
}
