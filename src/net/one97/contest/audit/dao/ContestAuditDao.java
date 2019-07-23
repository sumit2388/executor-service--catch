package net.one97.contest.audit.dao;

import java.util.Date;
import java.util.List;

import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryAuditTmp;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContest;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContestAuditHistory;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContestData;
import net.one97.contest.audit.bean.LiveContestDataBean;
import net.one97.contest.audit.bean.PlayerActive;
import net.one97.contest.audit.bean.PlayerLevelActive;

public interface ContestAuditDao {
	public List<LiveContestDataBean> getAllLiveContestData(String date);
	public boolean isRunningFirstTime(LiveContestDataBean contestDataBean);
	public boolean truncateTempTables();
	public boolean insertDataIntoTempTables(LiveContestDataBean contestDataBean, String startDate, String endDate, String considerSessionId);

	public List<ContestPlayerUpdateHistoryAuditTmp> getProducerData(long id,int size);
	public ContestPlayerUpdateHistoryContestAuditHistory getAuditHistoryEntry(String msisdn, String question_level,LiveContestDataBean liveContestBean, Long SessionOrderId, String considerSessionId );
	public List<PlayerLevelActive> getLiveContestEntry(String msisdn, String question_level, LiveContestDataBean liveContestBean, Long SessionOrderId, String considerSessionId);
	public void createLog(String msisdn, String contestName, String question_level, Date last_billing_update,
			String string);
	public void insertIntoAuditHistory(ContestPlayerUpdateHistoryContestAuditHistory data, String contestName);
	public void updateAuditHistory(ContestPlayerUpdateHistoryContestAuditHistory data, String contestName);
	public boolean deleteLiveRecords(PlayerLevelActive playerLevelActive,
			LiveContestDataBean liveContestBean);
	
	public boolean updateLevelActiveTotalBillingAmountRecord(Long id, double billingamount, LiveContestDataBean liveContestBean);
	public List<PlayerLevelActive> getLevelActiveByMsisdn(String msisdn, LiveContestDataBean liveContestBean, Long SessionOrderId, String considerSessionId);
	public boolean UpdateActiveRecord(String msisdn, double billingamount, double totalScore, int totalQuestionPlayed, LiveContestDataBean liveContestBean, Long SessionOrderId, String considerSessionId);
	public void createLog(String msisdn, String contestName, String level, Date issueDate, String problem, String previousData,
			int correction);
	public boolean updateLevelActiveTotalQuestionPlayedRecord(Long id, int totalQuestionPlayed,
			LiveContestDataBean liveContestBean);	
	public boolean updateLevelActiveTotalScoreRecord(Long id, double totalScore, LiveContestDataBean liveContestBean);
	public List<PlayerActive> getActiveTableListByMsisdn(String msisdn, LiveContestDataBean liveContestBean, Long SessionOrderId, String considerSessionId);
	public int deleteActiveRecords(String ids, LiveContestDataBean liveContestBean);
	public boolean deleteActiveRecords(PlayerActive playerActive, LiveContestDataBean liveContestBean);
	public boolean truncateAuditHistoryTable(String contestName);
	public List<ContestPlayerUpdateHistoryContestData> getContestPlayerUpdateHistoryContestData(String msisdn, String question_level, String startDate,
			int maxQuestionPerDay, int diff, LiveContestDataBean liveContestBean, String considerSessionId, Long sessionOrderId);
	public int deleteContestPlayerUpdateHistoryContestData(String ids, LiveContestDataBean liveContestBean);
	public ContestPlayerUpdateHistoryAuditTmp getDataToUpdateTempObj(String id, LiveContestDataBean liveContestBean);
	public List<ContestPlayerUpdateHistoryContestData> historyMaxAllowedData(String msisdn, String question_level,
			Date startDate, int maxQuestionPerDay, int diff, LiveContestDataBean liveContestBean,
			String considerSessionId, Long sessionOrderId);
	public List<ContestPlayerUpdateHistoryContest> getHistoryDataByIds(String id, LiveContestDataBean liveContestBean, String considerSessionId);
}
