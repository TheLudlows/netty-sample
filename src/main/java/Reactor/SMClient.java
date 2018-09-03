package reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class SMClient implements Runnable {

    SocketChannel socketChannel;
    Selector selector;
    boolean stop = false;
    @Override
    public void run() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            selector = Selector.open();
            doConnect();
            while(!stop) {
                try {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    SelectionKey key = null;
                    while(iterator.hasNext()) {
                        key = iterator.next();
                        iterator.remove();
                        handlerInput(key);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void handlerInput(SelectionKey key) throws IOException {
        if(key.isValid()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            if(key.isConnectable()) {
                //finishConnect方法表示完成完成连接套接字通道的过程，如果成功返回true
                if(socketChannel.finishConnect()) {
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    doWrite(socketChannel);
                }
            }
            if(key.isReadable()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int num = socketChannel.read(buffer);
                if(num>0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    String string = new String(bytes, "utf-8");
                    System.out.println("Now is:"+string);
                    this.stop = true;
                }
            }
        }
    }
    private void doConnect() throws IOException {
        //如果连接成功，则将SocketChannel注册到复用器上，监听read事件，并且发送请求数据
        if(socketChannel.connect(new InetSocketAddress("localhost", 9999))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
            //如果没有连接成功，则说明服务端没有返回tcp握手信息，但并不代表连接失败，只需要将SocketChannel注册到
            //Selector上，监听connect事件，当服务端返回syn-acy消息后，Selector就能轮询到这个SocketChannel出于连接就绪了
        }else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }

    }

    private void doWrite(SocketChannel socketChannel) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        String str = dateFormat.format( now );
        byte[] requst = str.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(requst.length);
        buffer.put(requst);
        buffer.flip();
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Thread(new SMClient()).start();
       // new Thread(new SMClient()).start();
    }
}
