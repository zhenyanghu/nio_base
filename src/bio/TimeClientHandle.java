package bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Rocky on 2018-12-03.
 */
public class TimeClientHandle implements Runnable {
	
	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;
	
	public TimeClientHandle(String host, int port) {
		this.host = host == null ? "127.0.0.1" : host;
		this.port = port;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			System.exit(1);
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			doConnect();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		try {
			//轮询多路复用器Selector，当有就绪的Channel是，执行handleInput方法
			while (!stop) {
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectionKeys.iterator();
				SelectionKey key = null;
				if (it.hasNext()) {
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if (key != null) {
							key.cancel();
							if (key.channel() != null) {
								key.channel().close();
							}
						}
					}
				}				
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	private void doConnect() throws IOException {
		//如果连接成功，则注册到多路复用器上，发送请求消息，读应答
		if (socketChannel.connect(new InetSocketAddress(host, port))) {
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
		} else {
			socketChannel.register(selector, SelectionKey.OP_CONNECT);			
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			//判断是否连接成功
			SocketChannel sc = (SocketChannel) key.channel();
			if (key.isConnectable()) {
				if (sc.finishConnect()) {
					sc.register(selector, SelectionKey.OP_READ);
					doWrite(sc);
				} else {
					System.exit(1);//连接失败，程序退出
				}
			}
			if (key.isReadable()) {
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("Now is : " + body);
					this.stop = true;
				} else if (readBytes < 0) {
					key.cancel();
					sc.close();
				} else 
					;
			}
		}
	}

	private void doWrite(SocketChannel sc) throws IOException {
		byte[] req = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		if (!writeBuffer.hasRemaining()) {
			System.out.println("Send order 2 server succeed");
		}
		
	}
	

}
