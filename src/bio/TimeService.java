package bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Rocky on 2018-11-30.
 * 同步阻塞式I/O通信
 * 一请求一应答的通信模型
 */
public class TimeService {

	public static void main(String[] args) {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				//采用默认值
			}
		}
		ServerSocket server = null;
		try {
			server = new ServerSocket(port);
			System.out.println("The time server is start in port : " + port);
			Socket socket = null;
			while (true) {
				socket = server.accept();//监听客户端的连接，
				new Thread(new TimeServerHandle(socket)).start();//一连接一线程
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (server != null) {
				System.out.println("The time server close");
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				server = null;
			}
		}
	}
	
}
