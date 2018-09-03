package reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleReactorMulThread implements Runnable {

    ServerSocketChannel serverChannel;
    Selector selector;

    //线程池处理业务逻辑
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Override
    public void run() {
        try {
            //1.打开channel
            serverChannel = ServerSocketChannel.open();
            //2.监听端口，设置为非阻塞模式
            serverChannel.socket().bind(new InetSocketAddress(9999));
            serverChannel.configureBlocking(false);
            //3.创建多路复用器
            selector = Selector.open();
            //4.将ServerSocketChannel注册到Selector上，监听accept事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Accepter 注册完成");
            while (true) {
                //5.多路复用器无限的循环，当有就绪的channel时，selector就返回就绪状态的channel的Selectionkey集合
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                SelectionKey key = null;
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    //处理新的事件,提交给线程池
                    executorService.execute(new Myhandler(selector, key));
                    //TODO及时对key的释放
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Myhandler implements Runnable {

        Selector selector;
        SelectionKey sk;

        public Myhandler(Selector selector, SelectionKey sk) {
            this.selector = selector;
            this.sk = sk;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().toString()+"子线程开始执行任务！");
            if (sk.isValid()) {
                try {
                    if (sk.isAcceptable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel) sk.channel()).accept();
                        socketChannel.configureBlocking(false);
                        //将新的socketchannel注册到selector上，监听Read操作。
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sk.isReadable()) {
                SocketChannel socketChannel = (SocketChannel) sk.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int length = 0;
                try {
                    length = socketChannel.read(readBuffer);
                    if (length > 0) {
                        readBuffer.flip();
                        byte[] bytes = new byte[readBuffer.remaining()];
                        readBuffer.get(bytes);
                        String string = null;
                        string = new String(bytes, "utf-8");
                        System.out.println("Receive :" + string);
                        System.out.println("thread:"+Thread.currentThread().toString());
                        doWrite(socketChannel, Thread.currentThread().toString());
                        //selector.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }
    private void doWrite(SocketChannel socketChannel, String responseStr) throws IOException {
        System.out.println("子线程开始执写任务了！");
        //10.将服务端要发送的消息存到buffer，发给客户端
        if(responseStr!=null&&responseStr.trim().length()>0) {
            byte[] bytes = responseStr.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
            buffer.put(bytes);
            System.out.println(responseStr);
            buffer.flip();
            while(buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new SingleReactorMulThread()).start();
    }
}

