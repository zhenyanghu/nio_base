package nio;

/**
 * Created by Rocky on 2018-12-03.
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
		MultipexerTimeServer timeServer = new MultipexerTimeServer(port);
		new Thread(timeServer, "NIO-MultiplexerTimeServer-001");
	}
}
