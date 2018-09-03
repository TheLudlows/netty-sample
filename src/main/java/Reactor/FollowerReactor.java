package Reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FollowerReactor {
    private Selector selector;
    private static  ExecutorService service =Executors.newFixedThreadPool(
            2*Runtime.getRuntime().availableProcessors());
    public void register(SocketChannel socketChannel) throws ClosedChannelException {
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public void wakeUp() {
    }

    public FollowerReactor() throws IOException {
        selector = Selector.open();
        select();

    }
    public void wakeup() {
        this.selector.wakeup();
    }
    public void select() {
        service.submit(() -> {
            while(true) {
                if(selector.select(500)<=0) {
                    continue;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if(key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        SocketChannel channel = (SocketChannel) key.channel();
                        int count = channel.read(buffer);
                        if(count<0) {
                            channel.close();
                            key.cancel();
                            System.out.println(channel+"->red end !");
                            continue;
                        }else if(count==0) {
                            System.out.println(channel+",size is 0 !");
                            continue;
                        }else{
                            System.out.println(channel+",message is :"+new String(buffer.array()));

                        }
                    }
                }
            }
        });

    }
}
