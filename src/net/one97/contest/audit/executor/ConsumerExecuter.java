package net.one97.contest.audit.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.one97.contest.audit.util.PropertyLoader;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ConsumerExecuter {

	public static Map<String, ExecutorService> consumerExecuterMap = new HashMap<String, ExecutorService>();

	private static Logger log = Logger.getLogger(ConsumerExecuter.class);

	/**
	 * @param String
	 *            parameter key, consumer executer name
	 * @param Runnable
	 *            Object that is consumer task It adds consumer task to the
	 *            queue of the consumer executer
	 * @throws ExecuterNotFoundException
	 */
	public static boolean addConsumerTask(String key, Runnable run) throws Exception {
		ExecutorService exe = consumerExecuterMap.get(key);
		if (exe == null)
			throw new Exception("Consumer Executer not found for the key:" + key);
		try {
			exe.execute(run);
			return true;
		} catch (RejectedExecutionException e) {
			log.error("WARNING Promotion Consumer queue is full for queue name : " + key);
			if (ProducerExecuter.producerExecuterMap.get(key).isShutdown()) {
				return false;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					log.error("Exception  :::", e);
				}
			}
			return false;
		} catch (NullPointerException e) {
			log.error("Exception  :::", e);
			return false;
		}
	}

	/**
	 * @param String
	 *            key executer name
	 * @param integer
	 *            poolSize, thread pool size It creates consumer executer with
	 *            thread pool equals to poolSize parameter
	 */

	public static void createConsumerExecuter(String key) {
		int pool = Integer.parseInt(PropertyLoader.getProperty("contest.audit.thread.poolsize"));
		ThreadPoolExecutor threadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(pool);
		log.info("Creating Consumer Executer id : [ " + key + " ]");
		consumerExecuterMap.put(key, threadExecutor);
	}

	/**
	 * @param String
	 *            key executer name It stops the running consumer executer after
	 *            finishing/completing the Queue associated with it
	 */
	public static void stopConsumerExecutor(String key) {

		log.info("Stopping Consumer Executer for Key ::: " + key);

		if (consumerExecuterMap.containsKey(key)) {
			try {
				consumerExecuterMap.get(key).shutdown();
				log.info("Shutdown Called fors Consumer Executer for Key ::: " + key);
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Exception occured while sttopping the consumer executer name ...... " + key);
			}
		}
	}

	/**
	 * @param String
	 *            key executer name It stops the running consumer executer
	 *            immediately
	 */
	public static void stopConsumerExecutorNow(String key) {

		log.info("Stopping Consumer Executor Now Key  ::::" + key.trim());

		if (consumerExecuterMap.containsKey(key.trim())) {
			try {
				consumerExecuterMap.get(key).shutdownNow();
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Exception occured while stopping the consumer executer name ........ " + key);

			}
		}

	}

	/**
	 * @param String
	 *            key executer name
	 * @return boolean true/false whether the executer with the specified name
	 *         exists or not
	 */
	public static boolean isConsumerExecutorExist(String key) {
		return consumerExecuterMap.containsKey(key);
	}

	/**
	 * @param key
	 *            executer name
	 * @return boolean, checks whether the executer is terminated or not
	 */
	public static boolean isTerminated(String key) {
		ExecutorService ex = consumerExecuterMap.get(key);
		if (ex == null)
			return true;
		return ex.isTerminated();
	}

	/**
	 * @param key
	 *            executer name
	 * @param timeout
	 *            time limit for the executer
	 * @param unit
	 *            time unit
	 * @return boolean
	 * @throws InterruptedException
	 */
	public static boolean awaitTermination(String key, long timeout, TimeUnit unit) throws InterruptedException {
		ExecutorService ex = consumerExecuterMap.get(key);

		if (ex == null) {
			return true;
		}
		return ex.awaitTermination(timeout, unit);
	}

	/**
	 * @param key
	 *            executer name
	 * @param timeout
	 *            time limit for the executer
	 */
	public static void finishExecutor(String key, long timeout) {
		log.info("Consumer Executor: Awaiting Termination of Consumer Executor...");
		boolean success = false;
		try {
			success = awaitTermination(key, timeout, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			log.error("InterruptedException occurred during awaitTermination of ConsumerExecutor");
		}
		if (!success) {
			log.info("Calling ConsumerExecutor ShutdownNow after waiting for specified shutdown time.");
			stopConsumerExecutorNow(key);
		}
		log.info("Consumer Executor: Await Termination of Consumer Executor completed.");
	}

	/**
	 * @param key
	 *            executer name
	 * @param poolSize
	 *            , thread pool size It sets the maximum pool size of the
	 *            executer
	 */
	public static void setMaxPoolSize(String key, int poolSize) {
		ThreadPoolExecutor ex = (ThreadPoolExecutor) consumerExecuterMap.get(key);

		if (ex == null) {
			return;
		}

		log.info("setting maximum pool size of '" + key + "' consumer executer to " + poolSize);
		ex.setMaximumPoolSize(poolSize);

	}

	/**
	 * @param key
	 *            executer name
	 * @param poolSize
	 *            , thread pool size It sets the core pool size of the executer
	 */
	public static void setCorePoolSize(String key, int poolSize) {
		ThreadPoolExecutor ex = (ThreadPoolExecutor) consumerExecuterMap.get(key);

		if (ex == null) {
			return;
		}

		log.info("setting core pool size of '" + key + "' consumer executer to " + poolSize);
		ex.setCorePoolSize(poolSize);

	}

	/**
	 * @param key
	 *            executer name
	 * @return Integer, Number of Active Thread Counts
	 */
	public static int getActiveConsumerThreadCount(String key) {
		ThreadPoolExecutor ex = (ThreadPoolExecutor) consumerExecuterMap.get(key.trim());

		if (ex == null) {
			return 0;
		}
		return ex.getActiveCount();
	}

	/**
	 * @param key
	 *            - ConsumerExecuter Name It starts the consumer executer of
	 *            given name
	 */
	/*
	 * public static void startConsumerExecuter(String key){
	 * if(consumerExecuterMap.containsKey(key) &&
	 * consumerExecuterMap.get(key).isShutdown()){ QueueConfigVO queue = null;
	 * for(QueueConfigVO tmp:ExecuterScheduler.configList){
	 * if(tmp.getQueueName().equalsIgnoreCase(key)) queue=tmp; }
	 * 
	 * createConsumerExecuter(key,queue.getConsPoolSize(),queue.getConsQueueSize(
	 * )); ProducerExecuter.startProducerExecuter(key);
	 * 
	 * }
	 * 
	 * }
	 */

}
