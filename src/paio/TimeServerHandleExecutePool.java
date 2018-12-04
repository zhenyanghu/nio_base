package paio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 时间服务器的处理线程池
 * Created by Rocky on 2018-12-01.
 */
public class TimeServerHandleExecutePool {

	private ExecutorService executor;
	
	/**
	 * 由于线程池和消息队列都是有界的，因此，无论客户端并发连接数多大，它都不会导致线程个数过于膨胀或者内存溢出，相比传统的已连接一线线程模型，是一种改良
	 * @param maxPoolSize
	 * @param queueSize
	 */
	public TimeServerHandleExecutePool(int maxPoolSize, int queueSize) {
		executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), maxPoolSize, 120L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueSize));
	}
	
	public void execute(Runnable task) {
		executor.execute(task);
	}
}
