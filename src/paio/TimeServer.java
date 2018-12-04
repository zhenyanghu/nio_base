package paio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import bio.TimeServerHandle;

/**
 * Created by Rocky on 2018-12-01.
 */
public class TimeServer {

	public static void main(String[] args) {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				
			}
		}
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server is start in port : " + port);
			Socket socket = null;
			/*
			 * 采用线程池，因此避免了为每个请求都创建一个独立的线程造成线程资源耗尽的问题。但是由于它底层的通讯依然采用同步阻塞模型，因此无法从根本上解决问题。
			 */
			TimeServerHandleExecutePool singleExecutor = new TimeServerHandleExecutePool(50, 10000);
			while (true) {
				socket = server.accept();
				singleExecutor.execute(new TimeServerHandle(socket));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
