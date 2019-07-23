package net.one97.contest.audit.executor;

public class ExecuterWrapper {
	
	/**
	 * @param String key
	 * Name of the consumer executer
	 * It will stop the specified consumer executer
	 */
	public static void stopConsumer(String key){
		ConsumerExecuter.stopConsumerExecutor(key);
	}
	
	/**
	 * @param String key
	 * Name of the producer executer
	 * It will stop the specified producer executer
	 */
	public static void stopProducer(String key){
		ProducerExecuter.stopProducerExecutor(key);
	}
	
	/**
	 * @param String key
	 * @param int poolSize
	 * It will change the number of consumer threads without stopping consumer executer
	 */
	public static void setConsumerThread(String key,int poolSize){
		ConsumerExecuter.setCorePoolSize(key, poolSize);
	}
	
	
	/**
	 * @param String key
	 * Consumer Executer Name
	 * It returns Number of Active consumer threads
	 */
	public static int getActiveConsumerThreadCount(String key){
		return ConsumerExecuter.getActiveConsumerThreadCount(key);
	}
	
	public static boolean checkProducerActive(String producer){
		return ProducerExecuter.producerExecuterMap.get(producer).isShutdown();
	}
}
