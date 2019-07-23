package net.one97.contest.audit.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class ProducerExecuter {
	private static final Logger logger = Logger.getLogger(ProducerExecuter.class);

	public static Map<String, ExecutorService> producerExecuterMap=new HashMap<String, ExecutorService>();

	public static void addProducerTask(String key, Runnable producerTask) throws Exception{
		ExecutorService ex = producerExecuterMap.get(key);
		if (ex==null)
			throw new Exception("Executor not exist for key: " + key + ". Hence task can't be added.");
		logger.info("Pushing item #" + key);
		try{
			ex.execute(producerTask);
		}catch (RejectedExecutionException e) {
			e.printStackTrace();
			System.out.println("inside catch of execute method ::"+e.getMessage());
		}
	}

	/**
	 * @param String key
	 * @param integer poolSize
	 */
	public  static void createProducerExecuter(String key){
		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
		logger.info("Inside Create Producer Executer Method");
		producerExecuterMap.put(key, threadExecutor);
	}

	/**
	 * @param key
	 */
	public static void stopProducerExecutor(String key){
		logger.info("Stopping Producer Executer for Key ::: " + key);
		ExecutorService ex = producerExecuterMap.get(key.trim());
		if (ex==null) {
			return ;
		}
		logger.info("ProducerExecutor: Shutting down Producer Executor...of key ....."+key.trim());
		ex.shutdown();
	}

	/**
	 * @param key
	 */
	public static void stopProducerExecutorNow(String key){
		ExecutorService ex = producerExecuterMap.get(key.trim());
		if (ex==null) {
			return ;
		}
//		log.info("ProducerExecutor: Shutting down now Producer Executor...");
		ex.shutdownNow();
	}

	/**
	 * @param key
	 * @return
	 */
	public static boolean isExecutorExist(String key){
		return producerExecuterMap.containsKey(key);
	}

	/**
	 * @param key
	 * @return
	 */
	public static boolean isTerminated(String key){
		ExecutorService ex = producerExecuterMap.get(key);
		if (ex == null) return true ;
		return ex.isTerminated();
	}

	/**
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	
	public static boolean awaitTermination(String key, long timeout, TimeUnit unit) throws InterruptedException{
		ExecutorService ex = producerExecuterMap.get(key);
		if (ex==null) {
			return true;
		}
		return ex.awaitTermination(timeout, unit);
	}



	/**
	 * @param key
	 * @param timeout
	 */
	public static void finishExecutor(String key, long timeout){
		logger.info("Producer Executor: Awaiting Termination of Producer Executor...");
		boolean success = false;
		try {
			success = awaitTermination(key, timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) { 
			/* ignore */ 
			logger.info("InterruptedException occurred during awaitTermination of ProducerExecutor");
		}
		if (!success){
			logger.info("Calling ProducerExecutor ShutdownNow after waiting for specified shutdown time.");
			stopProducerExecutorNow(key);
		}
		logger.info("Producer Executor: Await Termination of Producer Executor completed.");		
	}
}
