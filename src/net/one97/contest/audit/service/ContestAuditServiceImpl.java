package net.one97.contest.audit.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryAuditTmp;
import net.one97.contest.audit.bean.LiveContestDataBean;
import net.one97.contest.audit.dao.ContestAuditDao;
import net.one97.contest.audit.util.HttpClientUtil;
import net.one97.contest.audit.util.PropertyLoader;

@Service("contestAuditService")
public class ContestAuditServiceImpl implements ContestAuditService {
	@Autowired
	ContestAuditDao contestAuditDao;
	private static final Logger logger = Logger.getLogger(ContestAuditServiceImpl.class);

//	private HttpClient client;

	@Override
	public List<LiveContestDataBean> getAllLiveContestData(String date) {
		return contestAuditDao.getAllLiveContestData(date);
	}

	@Override
	public boolean isRunningFirstTime(LiveContestDataBean contestDataBean) {
		return contestAuditDao.isRunningFirstTime(contestDataBean);
	}

	@Override
	public boolean truncateTempTables() {
		logger.info("Inside truncateTempTables service method");
		return contestAuditDao.truncateTempTables();
	}

	@Override
	public boolean insertDataIntoTempTables(LiveContestDataBean contestDataBean, String startDate, String endDate, String considerSessionId) {
		logger.info(contestDataBean.getContest_identifier() + " Inside insertDataIntoTempTables method");
		return contestAuditDao.insertDataIntoTempTables(contestDataBean, startDate, endDate,considerSessionId);
	}

	@Override
	public List<ContestPlayerUpdateHistoryAuditTmp> getProducerData(long id, int size) {
		return contestAuditDao.getProducerData(id, size);

	}

	@Override
	public void sendSms(String paramString) {
		String smsUrl = PropertyLoader.getProperty("sms.url");
		logger.info(" SMS URL..." +smsUrl);
		ArrayList localArrayList = new ArrayList();
		String msisdnList = PropertyLoader.getProperty("msisdn");
		logger.info(" msisdnList " +msisdnList);
		StringTokenizer localStringTokenizer = new StringTokenizer(msisdnList, ",");
		Object localObject;
		for (int i = 1; localStringTokenizer.hasMoreTokens(); i++) {
			localObject = localStringTokenizer.nextToken();
			localArrayList.add(localObject);
		}
		try {
			if (!localArrayList.isEmpty()) {
				localObject = localArrayList.iterator();
				while (((Iterator) localObject).hasNext()) {
					String msisdn = (String) ((Iterator) localObject).next();
					String finalSmsUrl = smsUrl.replace("<MSISDN>", URLEncoder.encode(msisdn, "utf-8")).replace(
							"<TEXT>", URLEncoder.encode(paramString, "utf-8"));
					logger.info("Preparing SMS URL... " +finalSmsUrl);

					if (finalSmsUrl != null) {

						logger.info("Notifying " + finalSmsUrl + "...");

						boolean bool = sendGetRequest(finalSmsUrl);
						String msg = "";
						if (!bool) {
							msg = msisdn + " could not be notified.";
						} else {
							msg = msisdn + " is notified.";
						}

						logger.info(msg);

					}
				}
			}
		} catch (Exception localUnsupportedEncodingException) {
			logger.error("UnsupportedEncodingException while notifying administrators.",
					localUnsupportedEncodingException);
		}

	}

	private boolean sendGetRequest(String paramString) {
		boolean bool = false;
		GetMethod localGetMethod = new GetMethod(paramString);
		try {
			logger.info("Hitting URL " + paramString);
			HttpClient client = HttpClientUtil.getHttpClientInstance();
			int i = client.executeMethod(localGetMethod);
			if (i == 200) {
				bool = true;
			}
			logger.info("Notification send and response code is " + i);
		} catch (IOException localIOException) {
			logger.error("IOException while sending HTTP GET request to the URL " + paramString, localIOException);
		} finally {
			localGetMethod.releaseConnection();
		}
		return bool;
	}

	@Override
	public boolean truncateAuditHistoryTable(String contest_table) {
		logger.info("Inside truncateAuditHistoryTable service method");
		return contestAuditDao.truncateAuditHistoryTable(contest_table);
		
	}

}
