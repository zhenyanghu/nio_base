package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * 一个独立的线程，负责轮询多路复用器Secletor，可以处理多个客户端的并发接入
 * Created by Rocky on 2018-12-03.
 */
public class MultipexerTimeServer implements Runnable {

	private Selector selector;
	
	private ServerSocketChannel servChannel;
	
	private volatile boolean stop;
	
	public MultipexerTimeServer(int port) {
		try {
			//创建多路复用器：Selector，ServerSocketChannel
			selector = Selector.open();
			servChannel = ServerSocketChannel.open();
			servChannel.configureBlocking(false);//设置为非阻塞模式，
			servChannel.socket().bind(new InetSocketAddress(port), 1024);//绑定监听地址
			servChannel.register(selector, SelectionKey.OP_ACCEPT);//将ServerSocketChannel注册到Selector,监听SelectionKey.OP_ACCEPT操作位
			System.out.println("The time server is start in port: " + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void stop () {
		this.stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectionKeys.iterator();
				SelectionKey key = null;
				while (it.hasNext()) {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			//处理新接入的消息
			if (key.isAcceptable()) {
				//Accept thw new connection
				ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
				SocketChannel sc = ssc.accept();
				sc.configureBlocking(false);
				//Add the new connection to selector
				sc.register(selector, SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
				//Read the data
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if (readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("The time server receive order : " + body);
					String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
					doWrite(sc, currentTime);
				} else if (readBytes < 0) {
					//对端链路关闭
					key.cancel();
					sc.close();
				} else
					; //读到0，忽略
			}
		}
	}
	
	/**
	 * 将应答消息异步发送给客户端
	 * @param channal
	 * @param response
	 * @throws IOException
	 */
	private void doWrite(SocketChannel channal, String response) throws IOException {
		if (response != null && response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channal.write(writeBuffer);
		}
	}
	
	
}
