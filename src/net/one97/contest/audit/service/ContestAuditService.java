package net.one97.contest.audit.service;

import java.util.List;

import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryAuditTmp;
import net.one97.contest.audit.bean.LiveContestDataBean;

public interface ContestAuditService {
	public List<LiveContestDataBean> getAllLiveContestData(String date);
	public boolean isRunningFirstTime(LiveContestDataBean contestDataBean);
	public boolean truncateTempTables();
	public boolean insertDataIntoTempTables(LiveContestDataBean contestDataBean, String startDate, String endDate, String considerSessionId);
	public List<ContestPlayerUpdateHistoryAuditTmp> getProducerData(long id, int i);
	public void sendSms(String paramString);
	public boolean truncateAuditHistoryTable(String contest_identifier);
}
