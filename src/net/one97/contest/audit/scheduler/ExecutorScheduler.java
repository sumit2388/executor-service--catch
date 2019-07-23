package net.one97.contest.audit.scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryAuditTmp;
import net.one97.contest.audit.executor.ConsumerExecuter;
import net.one97.contest.audit.executor.ProducerExecuter;
import net.one97.contest.audit.task.ProducerTask;
import net.one97.contest.audit.util.Constants;
import net.one97.contest.audit.util.PropertyLoader;

@Component("executorScheduler")
public class ExecutorScheduler {
	private static final Logger logger = Logger.getLogger(ExecutorScheduler.class);
	public static BlockingQueue<ContestPlayerUpdateHistoryAuditTmp> blockingQueue = new LinkedBlockingQueue<ContestPlayerUpdateHistoryAuditTmp>(
			Integer.parseInt(PropertyLoader.getProperty("contest.Queue.size")));
	public static Set<String> smsContestList = new HashSet<String>();
	@Autowired
	private ProducerTask producerTask;

	public void createProducerConsumerExecutors() {
		ConsumerExecuter.createConsumerExecuter(Constants.CONSUMERTASK);
		ProducerExecuter.createProducerExecuter(Constants.PRODUCERTASK);
		try {
			ProducerExecuter.addProducerTask(Constants.PRODUCERTASK, producerTask);
		} catch (Exception e) {
			logger.error("Producer Executer with key [ producerTask ] not found.", e);
		}

	}

}
