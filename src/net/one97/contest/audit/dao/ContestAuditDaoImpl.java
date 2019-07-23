package net.one97.contest.audit.dao;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryAuditTmp;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContest;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContestAuditHistory;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContestData;
import net.one97.contest.audit.bean.LiveContestDataBean;
import net.one97.contest.audit.bean.PlayerActive;
import net.one97.contest.audit.bean.PlayerLevelActive;

@Repository("contestAuditDao")
public class ContestAuditDaoImpl implements ContestAuditDao {
	Logger logger = Logger.getLogger(ContestAuditDaoImpl.class);
	@Autowired
	@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
	@Autowired
	@Qualifier("jdbcTemplateLive")
	private JdbcTemplate jdbcTemplateLive;
	
	
	public JdbcTemplate getJdbcTemplateLive() {
		return jdbcTemplateLive;
	}

	@Override
	public List<LiveContestDataBean> getAllLiveContestData(String date) {
		String sql = "SELECT distinct contest_config_master.contest_id,contest_identifier,contest_config_master.max_question_per_day, contest_config_master.max_question,LEVEL,contest_config_score_level_point.max_question_per_day max_question_per_day_level,contest_config_score_level_point.max_question max_question_level, level_params,price_question_details,carry_forward , contest_config_master.start_date FROM contest_config_master,contest_config_location,contest_config_score_level_point  WHERE contest_config_master.contest_id=contest_config_location.contest_id AND contest_config_location.location_id =contest_config_score_level_point.location_id AND start_date<=date('"
				+ date + "') AND end_date>=date('" + date + "') AND isactive='1'";
		logger.info("getAllLiveContestData Sql query --> " + sql);
		List<LiveContestDataBean> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<LiveContestDataBean>(
				LiveContestDataBean.class));
		return list;
	}

	@Override
	public boolean isRunningFirstTime(LiveContestDataBean contestDataBean) {
		String sql = "select count(1) from contest_player_update_history_" + contestDataBean.getContest_identifier()
				+ "_audit where question_level = '" + contestDataBean.getLevel() + "'";
		logger.info("isRunningFirstTime Sql query --> " + sql);
		int count = 0;
		try {
			count = jdbcTemplate.queryForObject(sql, Integer.class);
		} catch (DataAccessException e) {
			String msg = "contest_player_update_history_" + contestDataBean.getContest_identifier()
					+ "_audit' doesn't exist";
			logger.error("Exception occured --> " + msg);
			if (StringUtils.contains(e.getMessage(), msg)) {
				try {
					jdbcTemplate.update("create table contest_player_update_history_"
							+ contestDataBean.getContest_identifier()
							+ "_audit like contest_player_update_history_identifier_audit");
					logger.info("table created --> contest_player_update_history_"
							+ contestDataBean.getContest_identifier()+ "_audit");
				} catch (DataAccessException ex) {
					logger.error("Error while creating audit history table", ex);
					return false;
				}
			}
		}
		return (count == 0) ? true : false;
	}

	@Override
	public boolean truncateTempTables() {
		logger.info("Inside truncateTempTables method");
		try {
			jdbcTemplate.execute("truncate table contest_player_update_history_audit_tmp");
			jdbcTemplate.execute("truncate table contest_player_update_history_audit_log_tmp");
		} catch (DataAccessException ex) {
			logger.error("Exception occured while truncating tables", ex);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean truncateAuditHistoryTable(String contestName) {
		logger.info("Inside truncateAuditHistoryTable method");
		try {
			jdbcTemplate.execute("truncate table contest_player_update_history_"+contestName+"_audit ");			
		} catch (DataAccessException ex) {
			logger.error("Exception occured while truncating tables", ex);
			return false;
		}
		return true;
	}

	@Override
	public boolean insertDataIntoTempTables(LiveContestDataBean contestDataBean, String startDate, String endDate,String considerSessionId) {
		logger.info("In insertDataIntoTempTables method");
		StringBuilder tableName = new StringBuilder("contest_player_update_history_");
		tableName.append(contestDataBean.getContest_identifier() + "_" + contestDataBean.getContest_id());
		logger.info("Calling SP insertDataIntoAuditTempTable --> values --> startDate-" + startDate + " End Date-"
				+ endDate + " Table Name- " + tableName.toString());
		int rows = 0;
		try {
			if(considerSessionId != null && considerSessionId.equals("1")){
				rows = jdbcTemplate.update("call InsertDataInTempTableWithSessionId(?,?,?,?)", startDate, endDate,tableName.toString(), contestDataBean.getLevel());
			}else{
				rows = jdbcTemplate.update("call insertDataIntoAuditTempTable(?,?,?,?)", startDate, endDate,tableName.toString(), contestDataBean.getLevel());
			}
			logger.info(rows + " rows inserted for contest--> " + contestDataBean.getContest_identifier());
		} catch (DataAccessException e) {
			logger.error("Exception occured while Calling Stored procedure --> insertDataIntoAuditTempTable ", e);
			return false;
		}
		return (rows > 0) ? true : false;
	}

	public List<ContestPlayerUpdateHistoryAuditTmp> getProducerData(long id, int size) {
		logger.info("_[Inside getProducerData]");
		List<ContestPlayerUpdateHistoryAuditTmp> playerHistoryTempData = null;
		String query = "select id ,msisdn,question_level,question_played,score,billing_amount,max_allowed_question,allowed_question_per_day, session_order_id from contest_player_update_history_audit_tmp where id >"
				+ id + " order by 1  limit " + size;
		logger.info("_getProducerData QUERY - " + query);
		try {
			playerHistoryTempData = jdbcTemplate.query(query,
					new BeanPropertyRowMapper<ContestPlayerUpdateHistoryAuditTmp>(
							ContestPlayerUpdateHistoryAuditTmp.class));
		} catch (DataAccessException e) {
			logger.error("table id " + id + "" + "_[DataAccessException occured]  : ", e);
			return null;
		}
		return playerHistoryTempData;

	}

	@Override
	public ContestPlayerUpdateHistoryContestAuditHistory getAuditHistoryEntry(String msisdn, String question_level,
			LiveContestDataBean liveContestBean , Long SessionOrderId , String considerSessionId) {
		logger.info("_[Inside getAuditHistoryEntry]");
		ContestPlayerUpdateHistoryContestAuditHistory auditHistory = null;
		String query="";
		if(considerSessionId != null && considerSessionId.equals("1")){
			 query = "select id ,msisdn,question_level,question_played,score,billing_amount,max_allowed_question,allowed_question_per_day,upto_date, session_order_id from contest_player_update_history_"
					+ liveContestBean.getContest_identifier()
					+ "_audit where msisdn = '"
					+ msisdn
					+ "'and question_level ='" + question_level + "' and session_order_id="+SessionOrderId+"";
		}
		else{
			 query = "select id ,msisdn,question_level,question_played,score,billing_amount,max_allowed_question,allowed_question_per_day,upto_date from contest_player_update_history_"
					+ liveContestBean.getContest_identifier()
					+ "_audit where msisdn = '"
					+ msisdn
					+ "'and question_level ='" + question_level + "' ";
		}		
		
		logger.info("getAuditHistoryEntry QUERY - " + query);
		try {
			auditHistory = jdbcTemplate.queryForObject(query,
					new BeanPropertyRowMapper<ContestPlayerUpdateHistoryContestAuditHistory>(
							ContestPlayerUpdateHistoryContestAuditHistory.class));

		} catch (DataAccessException e) {
			//logger.error("msisdn " + msisdn + "_[DataAccessException occured]  : ", e);
			logger.error("No data found for msisdn " + msisdn + " and question level  "+question_level + " in contest_player_update_history_"+liveContestBean.getContest_identifier());
		}
		return auditHistory;
	}
	
	

	@Override
	public List<PlayerLevelActive> getLiveContestEntry(String msisdn, String question_level,
			LiveContestDataBean liveContestBean,Long SessionOrderId,String considerSessionId) {
		logger.info("_[Inside getLiveContestEntry]");
		List<PlayerLevelActive> playerLevelActive = null;
		String query="";
		if(considerSessionId != null && considerSessionId.equals("1")){
			
			query = "select id,msisdn,total_score,day_total_billing,day_score,total_question_played,question_played_day,total_billing_amount, last_billing_update,last_score_update, last_updation_date, level, session_order_id ,max_allowed_question ,max_allowed_question_per_day  from contest_player_level_active_data_"
					+ liveContestBean.getContest_identifier()
					+ "_"
					+ liveContestBean.getContest_id()
					+ " where msisdn = '"
					+ msisdn + "'and level ='" + question_level + "' and session_order_id="+SessionOrderId+" order by total_billing_amount desc";
		}
		else{			
			query = "select id,msisdn,total_score,day_total_billing,day_score,total_question_played,question_played_day,total_billing_amount, last_billing_update,last_score_update, last_updation_date, level ,max_allowed_question ,max_allowed_question_per_day  from contest_player_level_active_data_"
					+ liveContestBean.getContest_identifier()
					+ "_"
					+ liveContestBean.getContest_id()
					+ " where msisdn = '"
					+ msisdn + "'and level ='" + question_level + "'  order by total_billing_amount desc";
		}	
		
		logger.info("getLiveContestEntry QUERY - " + query);
		try {
			playerLevelActive = jdbcTemplateLive.query(query, new BeanPropertyRowMapper<PlayerLevelActive>(PlayerLevelActive.class));
			logger.info("_[Inside getLiveContestEntry] fetch success ");
		} catch (DataAccessException e) {
			logger.error("msisdn " + msisdn + "_[DataAccessException occured]  : ", e);
		}
		return playerLevelActive;

	}

	@Override
	public void createLog(String msisdn, String contestName, String level, Date issueDate, String problem) {
		logger.info("_[Inside createLog] _ msisdn - " + msisdn + " contestName - " + contestName + " level -" + level
				+ " issueDate - " + issueDate + " problem - " + problem);
		String sql = "INSERT INTO audit_log_error "
				+ "(msisdn, contest_name, level,issue_date,execution_date,problems) VALUES (?, ?, ?, ?, ?, ?)";
		try {
			jdbcTemplate.update(sql, new Object[] { msisdn, contestName, level, issueDate, new Date(), problem });
			logger.info(" log created in audit_log_error");
		} catch (DataAccessException e) {
			logger.error(msisdn + " Exception occured while inserting data ", e);
		}
	}
	@Override
	public void createLog(String msisdn, String contestName, String level, Date issueDate, String problem,String previousData, int correction) {
		logger.info("_[Inside createLog] _ msisdn - " + msisdn + " contestName - " + contestName + " level -" + level
				+ " issueDate - " + issueDate + " problem - " + problem);
		String sql = "INSERT INTO audit_log_error "
				+ "(msisdn, contest_name, level,issue_date,execution_date,problems,previous_data,correction) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			jdbcTemplate.update(sql, new Object[] { msisdn, contestName, level, issueDate, new Date(), problem,previousData,correction });
			logger.info(" log created in audit_log_error");
		} catch (DataAccessException e) {
			logger.error(msisdn + " Exception occured while inserting data ", e);
		}
	}

	@Override
	public void insertIntoAuditHistory(ContestPlayerUpdateHistoryContestAuditHistory data, String contestName) {
		logger.info("_[Inside insertIntoAuditHistory] _ msisdn - " + data.getMsisdn());
		String sql = "INSERT INTO contest_player_update_history_"
				+ contestName
				+ "_audit "
				+ "(msisdn, question_level, question_played,score,billing_amount,max_allowed_question,allowed_question_per_day,upto_date,session_order_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		logger.info("_[Inside insertIntoAuditHistory] _  contestName" + contestName +" ,sql "+sql);
		try {
			jdbcTemplate.update(
					sql,
					new Object[] { data.getMsisdn(), data.getQuestion_level(), data.getQuestion_played(),
							data.getScore(), data.getBilling_amount(), data.getMax_allowed_question(),
							data.getAllowed_question_per_day(), data.getUpto_date() ,data.getSession_order_id()});
			logger.info("_[Inside insertIntoAuditHistory] _  success insert :: msisdn " + data.getMsisdn() +" level " +data.getQuestion_level()+" contestname "+contestName+" sessionOrderId "+data.getSession_order_id());
		} catch (DataAccessException e) {
			logger.error(data.getMsisdn() + " Exception occured while inserting data ", e);
		}
	}

	@Override
	public void updateAuditHistory(ContestPlayerUpdateHistoryContestAuditHistory data, String contestName) {
		logger.info("_[Inside updateAuditHistory] _ msisdn - " + data.getMsisdn());
	//	String sqlUpdate = "UPDATE contest_player_update_history_" + contestName+ "_audit set question_played=? , score=? , billing_amount=? , allowed_question_per_day=? , max_allowed_question=? , upto_date=? where msisdn=? and question_level=?";
		String sqlUpdate = "UPDATE contest_player_update_history_" + contestName
				+ "_audit set question_played=? , score=? , billing_amount=? , allowed_question_per_day=? , max_allowed_question=? , upto_date=? where id=?";
		try {
			jdbcTemplate.update(sqlUpdate, data.getQuestion_played(), data.getScore(), data.getBilling_amount(), data.getAllowed_question_per_day(),data.getMax_allowed_question(),data.getUpto_date(),
					data.getId());
			logger.info("update success in Audit history  msisdn " + data.getMsisdn() +" level " +data.getQuestion_level()+" contestname "+contestName);
		} catch (DataAccessException e) {
			logger.error(data.getMsisdn() + " Exception occured while inserting data ", e);
		}
	}
	

	@Override
	public boolean deleteLiveRecords(PlayerLevelActive playerLevelActivelist ,LiveContestDataBean liveContestBean) {
		logger.info("msisdn = "+playerLevelActivelist.getMsisdn() +" _id "+playerLevelActivelist.getId()+ "_[deleteing multiple msisdn ]");
		boolean delete = false;
		String deleteQuery =" DELETE  from  contest_player_level_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where id=?" ;
		
		logger.info("msisdn = "+playerLevelActivelist.getMsisdn() +" _id "+playerLevelActivelist.getId()+ "_delete TXN DETAIL QUERY - " + deleteQuery);
		try {
			
		int flag=	jdbcTemplateLive.update(deleteQuery,playerLevelActivelist.getId());
			if(flag==1){
				delete = true;
			}			
		} catch (DataAccessException e) {
			delete = false;
			logger.info("msisdn = "+playerLevelActivelist.getMsisdn() +" _id "+playerLevelActivelist.getId()+ "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return delete;

	}
	
	@Override
	public boolean updateLevelActiveTotalBillingAmountRecord(Long id , double billingamount ,LiveContestDataBean liveContestBean) {
		logger.info(" contest_player_level_active_data_ id "+id+ "_[ Update Live record ]");
		boolean delete = false;
		String updateQuery =" Update contest_player_level_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" set total_billing_amount=? where id=?" ;
		
		logger.info("id "+id+ "_updateQuery - " + updateQuery);
		try {
			
		int flag=	jdbcTemplateLive.update(updateQuery,billingamount,id);
			if(flag==1){
				delete = true;
			}			
		} catch (DataAccessException e) {
			delete = false;
			logger.info("id "+id+ "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return delete;

	}
	
	@Override
	public boolean updateLevelActiveTotalQuestionPlayedRecord(Long id , int totalQuestionPlayed ,LiveContestDataBean liveContestBean) {
		logger.info(" Inside updateLevelActiveTotalQuestionPlayedRecord id "+id+ "_[ Update Live record ]");
		boolean delete = false;
		String updateQuery =" Update contest_player_level_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" set total_question_played=? where id=?" ;
		
		logger.info("id "+id+ "_updateQuery - " + updateQuery);
		try {
			
		int flag=	jdbcTemplateLive.update(updateQuery,totalQuestionPlayed,id);
			if(flag==1){
				delete = true;
			}			
		} catch (DataAccessException e) {
			delete = false;
			logger.info("id "+id+ "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return delete;

	}
	
	@Override
	public boolean updateLevelActiveTotalScoreRecord(Long id , double totalScore ,LiveContestDataBean liveContestBean) {
		logger.info(" Inside updateLevelActiveTotalScoreRecord id "+id+ "_[ Update Live record ]");
		boolean delete = false;
		String updateQuery =" Update contest_player_level_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" set total_score=? where id=?" ;
		
		logger.info("id "+id+ "_updateQuery - " + updateQuery);
		try {
			
		int flag=	jdbcTemplateLive.update(updateQuery,totalScore,id);
			if(flag==1){
				delete = true;
			}			
		} catch (DataAccessException e) {
			delete = false;
			logger.info("id "+id+ "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return delete;

	}
	
	@Override
	public List<PlayerLevelActive> getLevelActiveByMsisdn(String msisdn,LiveContestDataBean liveContestBean ,Long SessionOrderId, String considerSessionId) {
		logger.info("_[Inside getLevelActiveByMsisdn]");
		List<PlayerLevelActive> playerLevelActive = null;
		String query ="";
		if(considerSessionId != null && considerSessionId.equals("1")){
			 query = "select id,msisdn,total_score,day_total_billing,day_score,total_question_played,question_played_day,total_billing_amount, last_billing_update,last_score_update, last_updation_date, level   from contest_player_level_active_data_"
					+ liveContestBean.getContest_identifier()
					+ "_"
					+ liveContestBean.getContest_id()
					+ " where msisdn = '"
					+ msisdn + "' and session_order_id="+SessionOrderId+" ";
		}
		else{
			 query = "select id,msisdn,total_score,day_total_billing,day_score,total_question_played,question_played_day,total_billing_amount, last_billing_update,last_score_update, last_updation_date, level   from contest_player_level_active_data_"
					+ liveContestBean.getContest_identifier()
					+ "_"
					+ liveContestBean.getContest_id()
					+ " where msisdn = '"
					+ msisdn + "'  ";
		}
		
		logger.info("getLevelActiveByMsisdn QUERY - " + query);
		try {
			playerLevelActive = jdbcTemplateLive.query(query, new BeanPropertyRowMapper<PlayerLevelActive>(PlayerLevelActive.class));
			logger.info("_[Inside getLevelActiveByMsisdn] fetch success ");
		} catch (DataAccessException e) {
			logger.error("msisdn " + msisdn + "_[DataAccessException occured]  : ", e);
		}
		return playerLevelActive;

	}

	
	@Override
	public boolean UpdateActiveRecord(String msisdn, double billingamount, double totalScore, int totalQuestionPlayed,	LiveContestDataBean liveContestBean ,Long SessionOrderId , String considerSessionId) {
		logger.info(" Inside UpdateActiveRecord _ msisdn "+msisdn+ "_[ Update Active table record ]");
		boolean update = false;
		String updateQuery ="";
		int flag=0;
		if(considerSessionId != null && considerSessionId.equals("1")){
		 updateQuery =" Update contest_player_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" set total_billing_amount=? , total_score=? , total_question_played=?  where msisdn=? and session_order_id=?" ;
		  flag=	jdbcTemplateLive.update(updateQuery,billingamount,totalScore,totalQuestionPlayed,msisdn,SessionOrderId);
		}
		else{
		 updateQuery =" Update contest_player_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" set total_billing_amount=? , total_score=? , total_question_played=?  where msisdn=? " ;
		  flag=	jdbcTemplateLive.update(updateQuery,billingamount,totalScore,totalQuestionPlayed,msisdn);
		}
		logger.info("msisdn "+msisdn+ "_updateQuery - " + updateQuery);
		try {		
			if(flag==1){
				update = true;
			}			
		} catch (DataAccessException e) {
			update = false;
			logger.info("msisdn "+msisdn+ "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return update;

	}
	
	@Override
	public List<PlayerActive> getActiveTableListByMsisdn(String msisdn,LiveContestDataBean liveContestBean, Long SessionOrderId, String considerSessionId) {
		logger.info("_[Inside getActiveTableListByMsisdn]");
		List<PlayerActive> playerActiveList = null;
		String query ="";
		if(considerSessionId != null && considerSessionId.equals("1")){
			 query = "select id, msisdn ,total_question_played ,total_score, total_billing_amount, last_updation_date,session_order_id  from contest_player_active_data_"
					+ liveContestBean.getContest_identifier()
					+ "_"
					+ liveContestBean.getContest_id()
					+ " where msisdn = '"
					+ msisdn + "' and session_order_id="+SessionOrderId+" ";
		}
		else{
			 query = "select id, msisdn ,total_question_played ,total_score, total_billing_amount, last_updation_date  from contest_player_active_data_"
					+ liveContestBean.getContest_identifier()
					+ "_"
					+ liveContestBean.getContest_id()
					+ " where msisdn = '"
					+ msisdn + "'  ";
		}
		
		logger.info("getActiveTableListByMsisdn QUERY - " + query);
		try {
			playerActiveList = jdbcTemplateLive.query(query, new BeanPropertyRowMapper<PlayerActive>(PlayerActive.class));
			logger.info("_[Inside getActiveTableListByMsisdn] fetch success ");
		} catch (DataAccessException e) {
			logger.error("msisdn " + msisdn + "_[DataAccessException occured]  : ", e);
		}
		return playerActiveList;

	}


	@Override
	public int deleteActiveRecords(String ids,LiveContestDataBean liveContestBean) {
		logger.info("Inside deleteActiveRecords");
		int count=0;
		String deleteQuery =" DELETE  from  contest_player_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where id in ( "+ids+" )" ;
		
		logger.info("delete duplicate records from active table query - " + deleteQuery);
		try {
			
		 count=	jdbcTemplateLive.update(deleteQuery);
					
		} catch (DataAccessException e) {
			
			logger.info( "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return count;

	}
	
	@Override
	public boolean deleteActiveRecords(PlayerActive playerActive ,LiveContestDataBean liveContestBean) {
		logger.info("Inside deleteActiveRecords :: msisdn = "+playerActive.getMsisdn() +" _id "+playerActive.getId()+ "_[deleteing multiple msisdn  from active table]");
		boolean delete = false;
		String deleteQuery =" DELETE  from  contest_player_active_data_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where id=?" ;
		
		logger.info("msisdn = "+playerActive.getMsisdn() +" _id "+playerActive.getId()+ "_delete QUERY - " + deleteQuery);
		try {
			
		int flag=	jdbcTemplateLive.update(deleteQuery,playerActive.getId());
			if(flag==1){
				delete = true;
			}			
		} catch (DataAccessException e) {
			delete = false;
			logger.info("msisdn = "+playerActive.getMsisdn() +" _id "+playerActive.getId()+ "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return delete;

	}

	public List<ContestPlayerUpdateHistoryContestData> getContestPlayerUpdateHistoryContestData(String msisdn, String question_level, String startDate,
			int maxQuestionPerDay,int diff, LiveContestDataBean liveContestBean ,String considerSessionId ,Long sessionOrderId) {		
		
		logger.info("_[Inside getContestPlayerUpdateHistoryContestData]");
		List<ContestPlayerUpdateHistoryContestData> playerActiveList = null;
		String query="";
		if(considerSessionId!= null && considerSessionId.equals("1")){
		if(diff==1){
		 query ="SELECT request_date as date , GROUP_CONCAT(id) as ids  FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where  session_order_id="+sessionOrderId+" and Date(request_date)='"+startDate+"' and  msisdn='"+msisdn+"' and question_level='"+question_level+"'  and (operation='SCOREUPDATE' or operation='SCOREANDBILLING') GROUP BY date(request_date) having count(id) > "+maxQuestionPerDay+"  order by id";
		}else{
		 query ="SELECT request_date as date , GROUP_CONCAT(id) as ids FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where  session_order_id="+sessionOrderId+" and  msisdn='"+msisdn+"' and question_level='"+question_level+"'  and (operation='SCOREUPDATE' or operation='SCOREANDBILLING') GROUP BY date(request_date) having count(id) > "+maxQuestionPerDay+" order by id ";
		}
		}
		else{
			if(diff==1){
				 query ="SELECT request_date as date , GROUP_CONCAT(id) as ids  FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where  Date(request_date)='"+startDate+"' and  msisdn='"+msisdn+"' and question_level='"+question_level+"'  and (operation='SCOREUPDATE' or operation='SCOREANDBILLING') GROUP BY date(request_date) having count(id) > "+maxQuestionPerDay+"  order by id";
				}else{
				 query ="SELECT request_date as date , GROUP_CONCAT(id) as ids FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where   msisdn='"+msisdn+"' and question_level='"+question_level+"'  and (operation='SCOREUPDATE' or operation='SCOREANDBILLING') GROUP BY date(request_date) having count(id) > "+maxQuestionPerDay+" order by id ";
				}
		}
		logger.info("getContestPlayerUpdateHistoryContestData QUERY - " + query);
		try {
			playerActiveList = jdbcTemplateLive.query(query, new BeanPropertyRowMapper<ContestPlayerUpdateHistoryContestData>(ContestPlayerUpdateHistoryContestData.class));
			logger.info("_[Inside getContestPlayerUpdateHistoryContestData] fetch success ");
		} catch (DataAccessException e) {
			logger.error("msisdn " + msisdn + "_[DataAccessException occured]  : ", e);
		}
		return playerActiveList;
	}
	
	public List<ContestPlayerUpdateHistoryContestData> historyMaxAllowedData(String msisdn, String question_level,
			Date startDate, int maxQuestionPerDay, int diff, LiveContestDataBean liveContestBean,
			String considerSessionId, Long sessionOrderId) {	
		
		logger.info("_[Inside historyMaxAllowedData]");
		List<ContestPlayerUpdateHistoryContestData> playerActiveList = null;
		String query="";
		if(considerSessionId!= null && considerSessionId.equals("1")){
		   query ="SELECT request_date as date , GROUP_CONCAT(id) as ids  FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where  session_order_id="+sessionOrderId+"  and  msisdn='"+msisdn+"' and question_level='"+question_level+"'  and (operation='SCOREUPDATE' or operation='SCOREANDBILLING')  order by id";
		}
		else{
	       query ="SELECT request_date as date , GROUP_CONCAT(id) as ids FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where   msisdn='"+msisdn+"' and question_level='"+question_level+"'  and (operation='SCOREUPDATE' or operation='SCOREANDBILLING')  order by id ";
		}
		logger.info("getContestPlayerUpdateHistoryContestData QUERY - " + query);
		try {
			playerActiveList = jdbcTemplateLive.query(query, new BeanPropertyRowMapper<ContestPlayerUpdateHistoryContestData>(ContestPlayerUpdateHistoryContestData.class));
			logger.info("_[Inside historyMaxAllowedData] fetch success ");
		} catch (DataAccessException e) {
			logger.error("msisdn " + msisdn + "_[DataAccessException occured]  : ", e);
		}
		return playerActiveList;
	}
	
	
	@Override
	public int deleteContestPlayerUpdateHistoryContestData(String ids,LiveContestDataBean liveContestBean) {
		logger.info("Inside deleteContestPlayerUpdateHistoryContestData");
		int count=0;
		String deleteQuery =" DELETE  from  contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where id in ( "+ids+" )" ;
		
		logger.info("delete records from deleteContestPlayerUpdateHistoryContestData table query - " + deleteQuery);
		try {
			
		 count=	jdbcTemplateLive.update(deleteQuery);
					
		} catch (DataAccessException e) {
			
			logger.info( "_[exception while deleting]  : " + e.getMessage());
			e.printStackTrace();
		}
		return count;

	}
	
	
	
	@Override
	public ContestPlayerUpdateHistoryAuditTmp getDataToUpdateTempObj(String id, LiveContestDataBean liveContestBean) {
		logger.info("_[Inside getDataToUpdateTempObj]");
		ContestPlayerUpdateHistoryAuditTmp historyTemp = null;
		String query = "SELECT msisdn, question_level, SUM(IF(operation='SCOREUPDATE' || operation='SCOREANDBILLING',1,0)) AS question_played,  "
				+ "	SUM(score) AS score, SUM(billing_amount) AS billing_amount, max_allowed_question, SUM(allowed_question_per_day) AS allowed_question_per_day, now(), session_order_id "
				+ "	FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where id in ("+id+") GROUP BY msisdn ";
				
		logger.info("getDataToUpdateTempObj QUERY - " + query);
		try {
			historyTemp = jdbcTemplate.queryForObject(query,
					new BeanPropertyRowMapper<ContestPlayerUpdateHistoryAuditTmp>(
							ContestPlayerUpdateHistoryAuditTmp.class));

		} catch (DataAccessException e) {
			logger.error("history table ids  " + id + "_[DataAccessException occured]  : ", e);
		}
		return historyTemp;
		
	}

	@Override
	public List<ContestPlayerUpdateHistoryContest> getHistoryDataByIds(String id, LiveContestDataBean liveContestBean,String considerSessionId) {
		logger.info("_[Inside getHistoryDataByIds]");
		List<ContestPlayerUpdateHistoryContest> historyDataList = null;
		String query="";
		if(considerSessionId!= null && considerSessionId.equals("1")){
		   query ="select id, msisdn, request_id, transaction_id, contest_id, billing_validity, request_date,zone, operator, circle,short_code, channel, language, request_type,request_value, ans_type , qusetion_id,operation,param ,param1, param2, param3,question_level,score,time_taken,billing_amount, max_allowed_question,allowed_question_per_day,session_order_id FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where id in ("+id+") ";
		}
		else{
	       query ="select id, msisdn, request_id, transaction_id, contest_id, billing_validity, request_date,zone, operator, circle,short_code, channel, language, request_type,request_value, ans_type , qusetion_id,operation,param ,param1, param2, param3,question_level,score,time_taken,billing_amount, max_allowed_question,allowed_question_per_day  FROM contest_player_update_history_"+liveContestBean.getContest_identifier()+"_"+ liveContestBean.getContest_id()+" where id in ("+id+")";
		}
		logger.info("getContestPlayerUpdateHistoryContestData QUERY - " + query);
		try {
			historyDataList = jdbcTemplateLive.query(query, new BeanPropertyRowMapper<ContestPlayerUpdateHistoryContest>(ContestPlayerUpdateHistoryContest.class));
			logger.info("_[Inside historyMaxAllowedData] fetch success ");
		} catch (DataAccessException e) {
			logger.error("_[DataAccessException occured]  : ", e);
		}
		return historyDataList;
	}

	
	
}
