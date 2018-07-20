package Reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reactor2 {
    private static ExecutorService pool = Executors.newFixedThreadPool(100);
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(1234));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(true) {
            if(selector.selectNow() < 0){
                continue;
            }
            Set<SelectionKey> sets = selector.selectedKeys();
            Iterator<SelectionKey> keys = sets.iterator();
            while(keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if(key.isAcceptable()) {
                    ServerSocketChannel Serverchannel = (ServerSocketChannel) key.channel();
                    SocketChannel channel = Serverchannel.accept();
                    channel.configureBlocking(false);
                    System.out.println("accept from "+channel.socket().getInetAddress().toString());
                    channel.register(selector, SelectionKey.OP_READ);
                }else if(key.isValid()&&key.isReadable()) {
                    pool.submit(new Processor(key));
                }
            }
        }

    }

}
