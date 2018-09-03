package https;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline ph = ch.pipeline();
        //添加sslhandler
        SSLEngine sslEngine = SSLContextFactory.getSslContext().createSSLEngine();
        sslEngine.setUseClientMode(false);
        ch.pipeline().addLast(new SslHandler(sslEngine));

        //处理http服务的关键handler
        //一个组合的HttpRequestDecoder和HttpResponseEncoder使得服务器端HTTP实现更容易。
        ph.addLast("codec", new HttpServerCodec());
        //ph.addLast("decoder",new HttpRequestDecoder());
        //ph.addLast("encoder",new HttpResponseEncoder());
        ph.addLast("aggregator", new HttpObjectAggregator(10*1024*1024));
        ph.addLast("handler", new HttpServerHandler());// 服务端业务逻辑
    }
}
