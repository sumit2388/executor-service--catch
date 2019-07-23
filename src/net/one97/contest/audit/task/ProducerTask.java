package net.one97.contest.audit.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryAuditTmp;
import net.one97.contest.audit.bean.LiveContestDataBean;
import net.one97.contest.audit.executor.ConsumerExecuter;
import net.one97.contest.audit.executor.ProducerExecuter;
import net.one97.contest.audit.mainEntry.MainEntry;
import net.one97.contest.audit.scheduler.ExecutorScheduler;
import net.one97.contest.audit.service.ContestAuditService;
import net.one97.contest.audit.util.Constants;
import net.one97.contest.audit.util.PropertyLoader;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProducerTask implements Runnable {
	@Autowired
	private ContestAuditService contestAuditService;
	private ConsumerTask consumerTask;
	LiveContestDataBean liveContestDataBean;
	private String startDate;
	private String endDate;
	private static long id = 0;
	private static final Logger logger = Logger.getLogger(ProducerTask.class);

	@Override
	public void run() {
		boolean terminate = true;
		calculateDates();
		List<LiveContestDataBean> liveContestList = contestAuditService.getAllLiveContestData(endDate);
		if (liveContestList != null && !liveContestList.isEmpty())
			processListOfContests(liveContestList);
		ConsumerTask.stopThread = true;
		logger.info("SMS contest list size " +ExecutorScheduler.smsContestList.size());
		for(String contestName : ExecutorScheduler.smsContestList){
			logger.info(" SMS contest name" +contestName);
			String msg=PropertyLoader.getProperty("sms.msg");
			String msg1 = msg.replace("<CONTESTNAME>", contestName).replace("<DATE>", new Date().toString());
			logger.info("sending message..."+msg1);
			contestAuditService.sendSms(msg1);
		}
		ConsumerExecuter.stopConsumerExecutor(Constants.CONSUMERTASK);
		ConsumerExecuter.finishExecutor(Constants.CONSUMERTASK, 1);
		while (terminate) {
			if (ConsumerExecuter.isTerminated(Constants.CONSUMERTASK)) {
				terminate = false;
			}
		}
		ProducerExecuter.stopProducerExecutor(Constants.PRODUCERTASK);
		ProducerExecuter.finishExecutor(Constants.PRODUCERTASK, 20);

	}

	private void calculateDates() {
		DateTime start = DateTime.now().withTime(0, 0, 0, 0).minusDays(
				Integer.parseInt(PropertyLoader.getProperty("contest.check.startDate")));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateTime end = DateTime.now().withTime(23, 59, 59, 999)
				.minusDays(Integer.parseInt(PropertyLoader.getProperty("contest.check.endDate")));
		startDate = dateFormat.format(start.toDate());
		endDate = dateFormat.format(end.toDate());
		logger.info("Start Date --> " + startDate + " End Date --> " + endDate);
	}

	private void processListOfContests(List<LiveContestDataBean> contestDataBeans) {
		for (LiveContestDataBean liveContestDataBean : contestDataBeans) {
			id = 0;
			boolean startAuditProcess = false;
			if (contestAuditService.truncateTempTables()) {
				String considerWholeBase=	PropertyLoader.getProperty("considerWholeBase");
				String considerSessionId=	PropertyLoader.getProperty("considerSessionId");
				if(considerWholeBase != null && considerWholeBase.equals("1")){
					logger.info("considerWholeBase ="+considerWholeBase);
					contestAuditService.truncateAuditHistoryTable(liveContestDataBean.getContest_identifier());			
				}
			
				boolean isRunningFirstTime = contestAuditService.isRunningFirstTime(liveContestDataBean);
				liveContestDataBean.setRunningFirstTime(isRunningFirstTime);
				if (isRunningFirstTime) {
					startAuditProcess = contestAuditService.insertDataIntoTempTables(liveContestDataBean, null, endDate,considerSessionId);
				} else {
					startAuditProcess = contestAuditService.insertDataIntoTempTables(liveContestDataBean, startDate,
							endDate,considerSessionId);
				}
				logger.info("Starting the Audit Process --> " + startAuditProcess);
				if (startAuditProcess)
					try {
						produceTempData(liveContestDataBean);
					} catch (Exception e) {
						logger.error(" Error occured ", e);
					}

			} else {
				logger.error("Error Occured while trunctaing the temp tables. ");
			}
		}
	}

	private void produceTempData(LiveContestDataBean liveContestDataBean) throws Exception {
		logger.info("Inside produceTempData");
		boolean isDataLeft = true;
		ConsumerTask.setLiveContestBean(liveContestDataBean);
		int threadPool = Integer.parseInt(PropertyLoader.getProperty("contest.audit.thread.poolsize"));
		if (ConsumerExecuter.getActiveConsumerThreadCount(Constants.CONSUMERTASK) < 1) {
			for (int i = 1; i <= threadPool; i++) {
				consumerTask = (ConsumerTask) MainEntry.applicationContext.getBean(Constants.CONSUMERTASK);
				ConsumerExecuter.addConsumerTask(Constants.CONSUMERTASK, consumerTask);
			}
		}
		while (isDataLeft) {
			int chunkSize = Integer.parseInt(PropertyLoader.getProperty("contest.Queue.size"))
					- ExecutorScheduler.blockingQueue.size();
			List<ContestPlayerUpdateHistoryAuditTmp> dataList = null;
			if (chunkSize > 0)
				dataList = contestAuditService.getProducerData(id, chunkSize);
			if ((dataList == null || dataList.isEmpty()) && ExecutorScheduler.blockingQueue.isEmpty()) {
				isDataLeft = false;
				id = 0;
				logger.info("Audit temp list and blockingQueue is empty");
				break;
			} else if (dataList != null && !dataList.isEmpty()) {
				id = dataList.get(dataList.size() - 1).getId();
				ExecutorScheduler.blockingQueue.addAll(dataList);
				logger.info(dataList.size() + " size data inserted in queue");
			}
		}
	}
}
