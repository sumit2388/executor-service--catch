package net.one97.contest.audit.mainEntry;

import net.one97.contest.audit.scheduler.ExecutorScheduler;

import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MainEntry {
	private static final Logger logger = Logger.getLogger(MainEntry.class);
	private static ExecutorScheduler executorScheduler;
	public static ApplicationContext applicationContext;
	public static void main(String[] args) {
		logger.info("inside Audit process Main method");
		processAudit();
	}

	public static void processAudit() {
		logger.info("Inside Process Audit");
		applicationContext = new ClassPathXmlApplicationContext("contest-audit-servlet.xml");
		executorScheduler = (ExecutorScheduler) applicationContext.getBean("executorScheduler");
		executorScheduler.createProducerConsumerExecutors();
		
	}
}
