package Reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client1 {

    public static void main(String[] args) throws IOException, InterruptedException {
        for(int i=0; i<20;i++) {
            new Client1().run();
        }
    }
    public void run() throws IOException, InterruptedException{
        SocketChannel socketChannel;
        socketChannel = SocketChannel.open();
        //socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 1234));
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        String str = dateFormat.format( now );
        byte[] requst = str.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(requst.length);
        buffer.put(requst);
        buffer.flip();
        try {
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        socketChannel.close();
    }
}
