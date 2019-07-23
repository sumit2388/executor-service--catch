package net.one97.contest.audit.task;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryAuditTmp;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContest;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContestAuditHistory;
import net.one97.contest.audit.bean.ContestPlayerUpdateHistoryContestData;
import net.one97.contest.audit.bean.LiveContestDataBean;
import net.one97.contest.audit.bean.PlayerActive;
import net.one97.contest.audit.bean.PlayerLevelActive;
import net.one97.contest.audit.dao.ContestAuditDao;
import net.one97.contest.audit.scheduler.ExecutorScheduler;
import net.one97.contest.audit.util.PropertyLoader;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConsumerTask implements Runnable {
	private static final Logger logger = Logger.getLogger(ConsumerTask.class);
	private static LiveContestDataBean liveContest = null;

	public static String contestNameSMS;
	
	public static boolean stopThread = false;
	
	public static LiveContestDataBean getLiveContestBean() {
		return liveContest;
	}

	public static void setLiveContestBean(LiveContestDataBean liveContest) {
		ConsumerTask.liveContest = liveContest;
	}

	@Autowired
	private ContestAuditDao contestAuditDao;
	
	

	@Override
	public void run() {
		while(!stopThread){
			try {				
				if(!ExecutorScheduler.blockingQueue.isEmpty()){
					ContestPlayerUpdateHistoryAuditTmp proObj = ExecutorScheduler.blockingQueue.take();  // producer data
					LiveContestDataBean liveContestBean = ConsumerTask.liveContest.clone();
					logger.info("inside consumer task :: msisdn is " + proObj.getMsisdn()+" level is "+proObj.getQuestion_level());
					
					String contestName = liveContestBean.getContest_identifier();
					
					String correction =	PropertyLoader.getProperty("data.correction");
					int dataCorrection = 0;
					if(correction != null && correction.equals("1")){
						dataCorrection = 1;
					}
					String historyTableCorrection =	PropertyLoader.getProperty("history.data.correction");
					int historyCorrectionEnable = 0;
					if(historyTableCorrection != null && historyTableCorrection.equals("1")){
						historyCorrectionEnable = 1;
					}
					
					String considerSessionId =	PropertyLoader.getProperty("considerSessionId");
					String levelActiveDeleteDuplicate =	PropertyLoader.getProperty("levelActive.delete.duplicate");
					//boolean historyEditFlag=false;
					
					
				    Date startDate ;
					if(liveContestBean.isRunningFirstTime()){
						 startDate = liveContestBean.getStart_date();
						 logger.info("Running first time -- start date "+startDate+"  :: msisdn is " + proObj.getMsisdn());
						
					}else{
						 startDate =( DateTime.now().withTime(0, 0, 0, 0).minusDays(Integer.parseInt(PropertyLoader.getProperty("contest.check.startDate")))).toDate();
						 logger.info("Not running first time -- start date "+startDate+"  :: msisdn is " + proObj.getMsisdn());
						 if(startDate.before(liveContestBean.getStart_date())){
							 startDate = liveContestBean.getStart_date();
							 logger.info("contest start date is after property file start date "+startDate+"  :: msisdn is " + proObj.getMsisdn());
						 }
					}
					
					Date endDate =( DateTime.now().withTime(23, 59, 59, 999).minusDays(Integer.parseInt(PropertyLoader.getProperty("contest.check.endDate")))).toDate();
					logger.info("End date "+endDate+" :: msisdn is " + proObj.getMsisdn());
					int diff=	(Days.daysBetween(new DateTime(startDate.getTime()), new DateTime(endDate).withTime(23, 59, 59, 999)).getDays())+1;
					logger.info("Day difference btw start and end date "+diff+" :: msisdn is " + proObj.getMsisdn());
					
			//			temp question played .., this * day count  ... this >= temp  :: contest multiply by number of days , contest >= temp 
					
					logger.info("fetching PlayerLevelActive data :: msisdn is " + proObj.getMsisdn());
					
					List<PlayerLevelActive> playerLevelActiveList = contestAuditDao.getLiveContestEntry(proObj.getMsisdn(),proObj.getQuestion_level(), liveContestBean, proObj.getSession_order_id(),considerSessionId);   // active contest data
					PlayerLevelActive playerLevelActive;
					if(playerLevelActiveList!= null &&  playerLevelActiveList.size()>1 ){				
						playerLevelActive = playerLevelActiveList.get(0);
						contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),null, "Multiple records found for this number in Level Active table :: Count is "+playerLevelActiveList.size(),"",dataCorrection); // log table entry
					//	if(dataCorrection == 1){
						if(levelActiveDeleteDuplicate != null && levelActiveDeleteDuplicate.equals("1")){						
							for(PlayerLevelActive obj :playerLevelActiveList){
								if(playerLevelActive.getId().longValue() != obj.getId().longValue() ){
									boolean flag=	contestAuditDao.deleteLiveRecords(obj, liveContestBean);
									if(flag){						
										contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),null, "Duplicate msisdn value deleted from Level Active ",obj.toString(),dataCorrection); // log table entry
										logger.info("successfully deleted live data :: id "+obj.getId());
									}
								}
							}
						}
					//  ExecutorScheduler.smsContestList.add(contestName);					
					}else if (playerLevelActiveList!= null &&  playerLevelActiveList.size() == 1){
						playerLevelActive=playerLevelActiveList.get(0);
					}
					else{
						playerLevelActive = null;
						contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),null, "No record found for this number in level active table"); // log table entry
						ExecutorScheduler.smsContestList.add(contestName);
					}
					
					
					if (playerLevelActive != null) {
						logger.info("fetch success PlayerLevelActive data :: msisdn is " + proObj.getMsisdn());	
						
						ContestPlayerUpdateHistoryContestAuditHistory auditHistoryObj = contestAuditDao.getAuditHistoryEntry(proObj.getMsisdn(), proObj.getQuestion_level(),liveContestBean,proObj.getSession_order_id(),considerSessionId); // audit history
						if (auditHistoryObj != null) {
							logger.info("fetch success Audit History data :: msisdn is "+ proObj.getMsisdn());
												
								if (!DateUtils.isSameDay(auditHistoryObj.getUpto_date(),endDate)) {
									logger.info("Audit history object is not null :: msisdn is " + proObj.getMsisdn());							
								
									//if((liveContestBean.getMax_question_per_day_level() *diff) < proObj.getQuestion_played() ){
//									if( ((playerLevelActive.getMax_allowed_question_per_day().intValue() *diff) < proObj.getQuestion_played()) || (playerLevelActive.getMax_allowed_question() < proObj.getQuestion_played()) ){
//										contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Max Question per day error :: Max question "+playerLevelActive.getMax_allowed_question_per_day().intValue()+" , day difference "+diff +" , Question_played "+proObj.getQuestion_played()); // log table entry
//										if(historyCorrectionEnable == 1 ){
//											proObj = 	questionPlayedDataCorrection(diff,proObj,startDate,endDate,playerLevelActive.getMax_allowed_question_per_day().intValue(),liveContestBean);
//											dataCorrection=1; // update data after deletion from istory table
//										}
//										ExecutorScheduler.smsContestList.add(contestName);									
//									}
									if( (playerLevelActive.getMax_allowed_question_per_day().intValue() *diff) < proObj.getQuestion_played()  ){
										logger.info("Max_allowed_question_per_day 1 :: msisdn is " + proObj.getMsisdn());
										
										if(historyCorrectionEnable == 1 ){
											proObj = 	questionPlayedPerDayDataCorrection(diff,proObj,startDate,endDate,playerLevelActive.getMax_allowed_question_per_day().intValue(),liveContestBean,considerSessionId,contestName,playerLevelActive);
											dataCorrection=1; // update data after deletion from history table
										}else{
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Max Question per day error :: Max question per day"+playerLevelActive.getMax_allowed_question_per_day().intValue()+" , day difference "+diff +" , Question_played "+proObj.getQuestion_played()); // log table entry
										}
										ExecutorScheduler.smsContestList.add(contestName);									
									}
									if( playerLevelActive.getMax_allowed_question() < proObj.getQuestion_played() ){
										logger.info("Max_allowed_question 1 :: msisdn is " + proObj.getMsisdn());
										
										if(historyCorrectionEnable == 1 ){
											proObj = 	questionPlayedMaxAllowedDataCorrection(diff,proObj,startDate,endDate,playerLevelActive.getMax_allowed_question(),liveContestBean,considerSessionId,contestName,playerLevelActive);
											dataCorrection=1; // update data after deletion from istory table
										}else{
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Max Question error :: Max question "+playerLevelActive.getMax_allowed_question()+" , day difference "+diff +" , Question_played "+proObj.getQuestion_played()); // log table entry
										}
										ExecutorScheduler.smsContestList.add(contestName);									
									}
																								
									if (playerLevelActive.getLast_score_update() == null || endDate.after(playerLevelActive.getLast_score_update()) || DateUtils.isSameDay(endDate, playerLevelActive.getLast_score_update())) { 	//2  ##
										
											String msg =" Temp question played " + proObj.getQuestion_played()+ " , Audit history question played " + auditHistoryObj.getQuestion_played()
														+ " , live total question played "+ playerLevelActive.getTotal_question_played() ;
										
											logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
											
											if (proObj.getQuestion_played().intValue() + auditHistoryObj.getQuestion_played().intValue() != playerLevelActive.getTotal_question_played().intValue()) {
												contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Question played error :: "+msg,"",dataCorrection); // log table entry
											
												if(dataCorrection == 1 ){
													int totalQuestionPlayed =proObj.getQuestion_played().intValue() + auditHistoryObj.getQuestion_played().intValue();
													contestAuditDao.updateLevelActiveTotalQuestionPlayedRecord(playerLevelActive.getId(), totalQuestionPlayed, liveContestBean);
												}
												ExecutorScheduler.smsContestList.add(contestName);
											}
											
											  msg =" Temp score " + proObj.getScore()+ " , Audit history score " + auditHistoryObj.getScore()
											  			+ " , live total score "+ playerLevelActive.getTotal_score() ;
											 
											logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
											
											if (proObj.getScore().doubleValue() + auditHistoryObj.getScore().doubleValue() != playerLevelActive.getTotal_score().doubleValue()) {
												contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Score error :: "+msg,"",dataCorrection); // log table entry
												if(dataCorrection == 1){
													double totalScore=proObj.getScore().doubleValue() + auditHistoryObj.getScore().doubleValue(); 
													contestAuditDao.updateLevelActiveTotalScoreRecord(playerLevelActive.getId(), totalScore, liveContestBean);
												}
												ExecutorScheduler.smsContestList.add(contestName);
											}			
									} 
								
									if (playerLevelActive.getLast_score_update() != null && endDate.before(playerLevelActive.getLast_score_update())) {//  2 ##
									
											String msg =" Temp question played " + proObj.getQuestion_played()+ " , Audit history question played " + auditHistoryObj.getQuestion_played()
															+ " , live total question played "+ playerLevelActive.getTotal_question_played()
																+ " , live Question_played_day " + playerLevelActive.getQuestion_played_day();
											
											logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
										
											if (proObj.getQuestion_played().intValue()+ auditHistoryObj.getQuestion_played().intValue() != (playerLevelActive.getTotal_question_played().intValue()- playerLevelActive.getQuestion_played_day().intValue())) {
												contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Question played error :: "+msg,"",dataCorrection); 
												
												if(dataCorrection == 1 ){
													int totalQuestionPlayed =proObj.getQuestion_played().intValue()+ auditHistoryObj.getQuestion_played().intValue()+playerLevelActive.getQuestion_played_day().intValue();
													contestAuditDao.updateLevelActiveTotalQuestionPlayedRecord(playerLevelActive.getId(), totalQuestionPlayed, liveContestBean);
												}
												ExecutorScheduler.smsContestList.add(contestName);
											}
											
											 msg =" Temp score " + proObj.getScore()+ " , Audit history score " + auditHistoryObj.getScore()
													+ " , live total score "+ playerLevelActive.getTotal_score()
														+ " , live day score " + playerLevelActive.getDay_score();
											 
											 logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
											
											if (proObj.getScore().doubleValue()+ auditHistoryObj.getScore().doubleValue() != (playerLevelActive.getTotal_score().doubleValue()- playerLevelActive.getDay_score().doubleValue())) {
												contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Score error :: "+msg,"",dataCorrection); 
												if(dataCorrection == 1 ){
													double totalScore=proObj.getScore().doubleValue()+ auditHistoryObj.getScore().doubleValue()+playerLevelActive.getDay_score().doubleValue();
													contestAuditDao.updateLevelActiveTotalScoreRecord(playerLevelActive.getId(), totalScore, liveContestBean);
												}
												ExecutorScheduler.smsContestList.add(contestName);
											}
									}
									
									if (playerLevelActive.getLast_billing_update() == null || endDate.after(playerLevelActive.getLast_billing_update())  || DateUtils.isSameDay(endDate, playerLevelActive.getLast_billing_update())) {   //1  ##
											logger.info("playerLevelActive is less than today date :: msisdn is " + proObj.getMsisdn());
						
											String msg="Temp billing amount " + proObj.getBilling_amount()+ " , Audit history billing amount " + auditHistoryObj.getBilling_amount()
											+ " , live total billing amount "+ playerLevelActive.getTotal_billing_amount() ;
											
											logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
											
											if (proObj.getBilling_amount().doubleValue() + auditHistoryObj.getBilling_amount().doubleValue() != playerLevelActive.getTotal_billing_amount().doubleValue()) {
												contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_billing_update(), "Billing amount error :: "+msg,"",dataCorrection); // log table entry
												if(dataCorrection == 1){
													double billingAmount=proObj.getBilling_amount().doubleValue() + auditHistoryObj.getBilling_amount().doubleValue();
													contestAuditDao.updateLevelActiveTotalBillingAmountRecord(playerLevelActive.getId(),billingAmount, liveContestBean);
												}
												ExecutorScheduler.smsContestList.add(contestName);										
											}
									}
															 
									
									
									if (playerLevelActive.getLast_billing_update() != null && (endDate.before(playerLevelActive.getLast_billing_update()))) { //1  ##
											logger.info("playerLevelActive is same day as today  :: msisdn is " + proObj.getMsisdn());
						
											String msg =" Temp billing amount " + proObj.getBilling_amount()+ " , Audit history billing amount " + auditHistoryObj.getBilling_amount()
															+ " , live total billing amount "+ playerLevelActive.getTotal_billing_amount() + " , live Day_total_billing"
																+ playerLevelActive.getDay_total_billing() ;
											
											logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
											
											if (proObj.getBilling_amount().doubleValue()+ auditHistoryObj.getBilling_amount().doubleValue() != (playerLevelActive.getTotal_billing_amount().doubleValue()- playerLevelActive.getDay_total_billing().doubleValue())) {
												contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_billing_update(), "Billing amount error :: "+msg,"",dataCorrection); 
												if(dataCorrection == 1){
													double billingAmount=proObj.getBilling_amount().doubleValue() + auditHistoryObj.getBilling_amount().doubleValue()+playerLevelActive.getDay_total_billing().doubleValue();
													contestAuditDao.updateLevelActiveTotalBillingAmountRecord(playerLevelActive.getId(),billingAmount, liveContestBean);
												}
												ExecutorScheduler.smsContestList.add(contestName);
											
											}
									}	
								//update audit history
								ContestPlayerUpdateHistoryContestAuditHistory auditHistory = new ContestPlayerUpdateHistoryContestAuditHistory();
								auditHistory.setAllowed_question_per_day(auditHistoryObj.getAllowed_question_per_day()+proObj.getAllowed_question_per_day());					
								auditHistory.setBilling_amount(auditHistoryObj.getBilling_amount()+proObj.getBilling_amount());
								auditHistory.setMax_allowed_question(auditHistoryObj.getMax_allowed_question());
								auditHistory.setMsisdn(auditHistoryObj.getMsisdn());
								auditHistory.setQuestion_level(auditHistoryObj.getQuestion_level());
								auditHistory.setQuestion_played(auditHistoryObj.getQuestion_played()+proObj.getQuestion_played());
								auditHistory.setScore(auditHistoryObj.getScore()+proObj.getScore());
								auditHistory.setUpto_date(endDate);
								auditHistory.setSession_order_id(proObj.getSession_order_id());
								auditHistory.setId(auditHistoryObj.getId());
								contestAuditDao.updateAuditHistory(auditHistory,contestName);			
							} 
						}else {
									logger.info("Audit history object is null :: msisdn is " + proObj.getMsisdn());						
								
								//if((liveContestBean.getMax_question_per_day_level()*diff) < proObj.getQuestion_played() ){
//									 if( ((playerLevelActive.getMax_allowed_question_per_day().intValue() *diff) < proObj.getQuestion_played()) || (playerLevelActive.getMax_allowed_question() < proObj.getQuestion_played()) ){
//										contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Max Question per day error :: Max question "+playerLevelActive.getMax_allowed_question_per_day().intValue()+" , day difference "+diff +" ,Question_played "+proObj.getQuestion_played()); // log table entry
//										if(historyCorrectionEnable == 1 ){
//											proObj =	questionPlayedDataCorrection(diff,proObj,startDate,endDate,playerLevelActive.getMax_allowed_question_per_day().intValue(),liveContestBean);
//											dataCorrection=1;
//											
//										}
//										ExecutorScheduler.smsContestList.add(contestName);
//									 }	
									if( (playerLevelActive.getMax_allowed_question_per_day().intValue() *diff) < proObj.getQuestion_played()  ){
										logger.info(" :: msisdn is " + proObj.getMsisdn()+" log Max_allowed_question_per_day ");
										
										if(historyCorrectionEnable == 1 ){
											proObj = 	questionPlayedPerDayDataCorrection(diff,proObj,startDate,endDate,playerLevelActive.getMax_allowed_question_per_day().intValue(),liveContestBean,considerSessionId,contestName,playerLevelActive);
											dataCorrection=1; // update data after deletion from history table
										}else{
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), 
													"Max Question per day error :: Max question per day"+playerLevelActive.getMax_allowed_question_per_day().intValue()+" , day difference "+diff +" , Question_played "+proObj.getQuestion_played()); // log table entry
										}
										ExecutorScheduler.smsContestList.add(contestName);									
									}
									if( playerLevelActive.getMax_allowed_question() < proObj.getQuestion_played() ){
										logger.info(" :: msisdn is " + proObj.getMsisdn()+" log Max_allowed_question ");
										
										if(historyCorrectionEnable == 1 ){
											proObj = 	questionPlayedMaxAllowedDataCorrection(diff,proObj,startDate,endDate,playerLevelActive.getMax_allowed_question(),liveContestBean,considerSessionId,contestName,playerLevelActive);
									     	dataCorrection=1; // update data after deletion from history table
										}
										else{
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Max Question error :: Max question "+playerLevelActive.getMax_allowed_question()+" , day difference "+diff +" , Question_played "+proObj.getQuestion_played()); // log table entry
										}
										ExecutorScheduler.smsContestList.add(contestName);									
									}
							
									
									if (playerLevelActive.getLast_score_update() == null || endDate.after(playerLevelActive.getLast_score_update()) || DateUtils.isSameDay(endDate, playerLevelActive.getLast_score_update())) {  //4
										String msg =" Temp question played " + proObj.getQuestion_played()+ " , live total question played "
														+ playerLevelActive.getTotal_question_played() ;
										
										logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
										
										if (proObj.getQuestion_played().intValue() != playerLevelActive.getTotal_question_played().intValue()) {
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Question played error :: "+msg,"",dataCorrection); 
											
											if(dataCorrection == 1 ){
												int totalQuestionPlayed =proObj.getQuestion_played().intValue();
												contestAuditDao.updateLevelActiveTotalQuestionPlayedRecord(playerLevelActive.getId(), totalQuestionPlayed, liveContestBean);
											}
											ExecutorScheduler.smsContestList.add(contestName);
										}
										
										msg = " Temp score " + proObj.getScore()+ " , live total score "
													+ playerLevelActive.getTotal_score() ;
										
										logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
									
										if (proObj.getScore().doubleValue() != playerLevelActive.getTotal_score().doubleValue()) {
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Score error :: "+msg,"",dataCorrection); 
											if(dataCorrection == 1 ){
												double totalScore=proObj.getScore().doubleValue();
												contestAuditDao.updateLevelActiveTotalScoreRecord(playerLevelActive.getId(), totalScore, liveContestBean);
											}
											ExecutorScheduler.smsContestList.add(contestName);
										}
			
									} 
									
									if ( playerLevelActive.getLast_score_update() != null && endDate.before(playerLevelActive.getLast_score_update())) {//4  ##
										
										String msg =" Temp question played " + proObj.getQuestion_played()+ " , live total question played "
														+ playerLevelActive.getTotal_question_played()+ " , live Question_played_day " + playerLevelActive.getQuestion_played_day()
														;
										logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
										
										if (proObj.getQuestion_played().intValue() != (playerLevelActive.getTotal_question_played().intValue()- playerLevelActive.getQuestion_played_day().intValue())) {
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Question played error :: "+msg,"",dataCorrection);
											
											if(dataCorrection == 1 ){
												int totalQuestionPlayed =proObj.getQuestion_played().intValue()+playerLevelActive.getQuestion_played_day().intValue();
												contestAuditDao.updateLevelActiveTotalQuestionPlayedRecord(playerLevelActive.getId(), totalQuestionPlayed, liveContestBean);
											}
											ExecutorScheduler.smsContestList.add(contestName);
										}
										
										msg=" Temp score " + proObj.getScore()+ " , live total score "
												+ playerLevelActive.getTotal_score()+ " , live day score " + playerLevelActive.getDay_score();
										
										logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
									
										if (proObj.getScore().doubleValue() != (playerLevelActive.getTotal_score().doubleValue()- playerLevelActive.getDay_score().doubleValue())) {
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Score error :: "+msg,"",dataCorrection);
											if(dataCorrection == 1 ){
												double totalScore=proObj.getScore().doubleValue()+playerLevelActive.getDay_score().doubleValue();
												contestAuditDao.updateLevelActiveTotalScoreRecord(playerLevelActive.getId(), totalScore, liveContestBean);
											}
											ExecutorScheduler.smsContestList.add(contestName);
										}
									}
									
									if (playerLevelActive.getLast_billing_update() == null || endDate.after(playerLevelActive.getLast_billing_update()) || DateUtils.isSameDay(endDate, playerLevelActive.getLast_billing_update())) {  //3   ##
										logger.info("playerLevelActive is less than today date :: msisdn is " + proObj.getMsisdn());
				
										String msg =" Temp billing amount " + proObj.getBilling_amount()+ " , live total billing amount "+ playerLevelActive.getTotal_billing_amount() ;
										
										logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
										
										if (proObj.getBilling_amount().doubleValue() != playerLevelActive.getTotal_billing_amount().doubleValue()) {
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_billing_update(), "Billing amount error :: "+msg,"",dataCorrection);
											if(dataCorrection == 1 ){
												double billingAmount=proObj.getBilling_amount().doubleValue() ;
												contestAuditDao.updateLevelActiveTotalBillingAmountRecord(playerLevelActive.getId(),billingAmount, liveContestBean);
											}
											ExecutorScheduler.smsContestList.add(contestName);
										}
									}
									 
									
									if (playerLevelActive.getLast_billing_update() != null && endDate.before(playerLevelActive.getLast_billing_update())) { //3 ##
										logger.info("playerLevelActive is same day as today  :: msisdn is " + proObj.getMsisdn());
				
										String msg =" Temp billing amount " + proObj.getBilling_amount()+ " , live total billing amount "
														+ playerLevelActive.getTotal_billing_amount() + " , live Day_total_billing "+ playerLevelActive.getDay_total_billing() ;
										
										logger.info(msg+ " :: msisdn is " + proObj.getMsisdn());
										
										if (proObj.getBilling_amount().doubleValue() != (playerLevelActive.getTotal_billing_amount().doubleValue()- playerLevelActive.getDay_total_billing().doubleValue())) {
											contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_billing_update(), "Billing amount error :: "+msg,"",dataCorrection); 
											if(dataCorrection == 1 ){
												double billingAmount=proObj.getBilling_amount().doubleValue() +playerLevelActive.getDay_total_billing().doubleValue();
												contestAuditDao.updateLevelActiveTotalBillingAmountRecord(playerLevelActive.getId(),billingAmount, liveContestBean);
											}
											ExecutorScheduler.smsContestList.add(contestName);
										}
									}
									//insert audit history
									ContestPlayerUpdateHistoryContestAuditHistory auditHistory = new ContestPlayerUpdateHistoryContestAuditHistory();
									auditHistory.setAllowed_question_per_day(proObj.getAllowed_question_per_day());
									auditHistory.setBilling_amount(proObj.getBilling_amount());
									auditHistory.setMax_allowed_question(proObj.getMax_allowed_question());
									auditHistory.setMsisdn(proObj.getMsisdn());
									auditHistory.setQuestion_level(proObj.getQuestion_level());
									auditHistory.setQuestion_played(proObj.getQuestion_played());
									auditHistory.setScore(proObj.getScore());
									auditHistory.setUpto_date(endDate);	
									auditHistory.setSession_order_id(proObj.getSession_order_id());
									contestAuditDao.insertIntoAuditHistory(auditHistory,contestName);
									logger.info("insert success for Audit history"+ " :: msisdn is " + proObj.getMsisdn());
									
									//if(dataCorrection == 1 ){
									updateActiveTable( playerLevelActive, liveContestBean,considerSessionId,levelActiveDeleteDuplicate,dataCorrection);
								//	}
								}
					}else{			
						logger.info("data not found in PlayerLevelActive" + proObj.getMsisdn() +" level is "+proObj.getQuestion_level());
					}
				}						
			}catch (Exception e) {
				logger.error("Error Occured while consumer Task ",e);
			}
		} // while end
	} // run end

//	private ContestPlayerUpdateHistoryAuditTmp questionPlayedDataCorrection(int diff, ContestPlayerUpdateHistoryAuditTmp proObj, Date startDate, Date endDate, int maxQuestionPerDay, LiveContestDataBean liveContestBean) {
//		logger.info("Inside questionPlayedDataCorrection :: msisdn  "+proObj.getMsisdn());
//		List<ContestPlayerUpdateHistoryContestData> mainTableDataList=	contestAuditDao.getContestPlayerUpdateHistoryContestData(proObj.getMsisdn(),proObj.getQuestion_level(),startDate,maxQuestionPerDay,diff,liveContestBean);
//		String id="";
//		for(ContestPlayerUpdateHistoryContestData obj :mainTableDataList){
//			List<String> objlist = Arrays.asList(obj.getIds().split(","));
//			int count=0;
//		    for(String s : objlist){
//		    	if(count >= maxQuestionPerDay){
//		    		id=id+","+s;
//		    		System.out.println(s);
//		    	}
//		    	count++;		    	
//		    }
//		}
//		logger.info("questionPlayedDataCorrection id "+id+":: msisdn  "+proObj.getMsisdn());
//		if(id.length() >0){	
//		ContestPlayerUpdateHistoryAuditTmp historyObj=	contestAuditDao.getDataToUpdateTempObj(id,liveContestBean);
//		proObj.setQuestion_played(proObj.getQuestion_played()-historyObj.getQuestion_played());
//		proObj.setScore(proObj.getScore()-historyObj.getScore());
//		proObj.setBilling_amount(proObj.getBilling_amount()-historyObj.getBilling_amount());
//		proObj.setMax_allowed_question(proObj.getMax_allowed_question()-historyObj.getMax_allowed_question());
//		proObj.setAllowed_question_per_day(proObj.getAllowed_question_per_day()-historyObj.getAllowed_question_per_day());	
//		logger.info(" deletion of ids "+id+":: msisdn  "+proObj.getMsisdn());
//		contestAuditDao.deleteContestPlayerUpdateHistoryContestData(id,liveContestBean);
//		}
//		logger.info("return proObj "+proObj.toString()+" :: msisdn  "+proObj.getMsisdn());
//		return proObj;
//	}
	
	
	private ContestPlayerUpdateHistoryAuditTmp questionPlayedPerDayDataCorrection(int diff, ContestPlayerUpdateHistoryAuditTmp proObj, Date startDate, Date endDate, int maxQuestionPerDay, LiveContestDataBean liveContestBean, String considerSessionId, String contestName, PlayerLevelActive playerLevelActive) {
		logger.info("Inside questionPlayedDataCorrection :: msisdn  "+proObj.getMsisdn());
     	String sDate=	new SimpleDateFormat("yyyy-MM-dd").format(startDate);
		List<ContestPlayerUpdateHistoryContestData> mainTableDataList=	contestAuditDao.getContestPlayerUpdateHistoryContestData(proObj.getMsisdn(),proObj.getQuestion_level(),sDate,maxQuestionPerDay,diff,liveContestBean,considerSessionId,proObj.getSession_order_id());
		String id="";
		for(ContestPlayerUpdateHistoryContestData obj :mainTableDataList){
			List<String> objlist = Arrays.asList(obj.getIds().split(","));
			int count=0;
		    for(String s : objlist){
		    	if(count >= maxQuestionPerDay){
		    		id=id+","+s;
		    		System.out.println(s);
		    	}
		    	count++;		    	
		    }
		}
		logger.info("questionPlayedDataCorrection id "+id+":: msisdn  "+proObj.getMsisdn());
		if(id.length() >0){
		id = id.trim().substring(1);
		//	System.out.println(id.trim().substring(1));
		String previousData="";
		List<ContestPlayerUpdateHistoryContest> ContestPlayerUpdateHistoryContestList = contestAuditDao.getHistoryDataByIds(id,liveContestBean,considerSessionId);
		for(ContestPlayerUpdateHistoryContest obj : ContestPlayerUpdateHistoryContestList){
			if(previousData.equals("")){
				previousData=obj.toString();
			}
			else{
				previousData=previousData+" :: "+obj.toString();
			}
		}
		contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Max Question per day error :: Max question per day"+playerLevelActive.getMax_allowed_question_per_day().intValue()+" , day difference "+diff +" , Question_played "+proObj.getQuestion_played(),previousData,1); // log table entry
		ContestPlayerUpdateHistoryAuditTmp historyObj=	contestAuditDao.getDataToUpdateTempObj(id,liveContestBean);
	//	proObj.setQuestion_level(id);
		proObj.setQuestion_played(proObj.getQuestion_played()-historyObj.getQuestion_played());
		proObj.setScore(proObj.getScore()-historyObj.getScore());
		proObj.setBilling_amount(proObj.getBilling_amount()-historyObj.getBilling_amount());
		proObj.setMax_allowed_question(proObj.getMax_allowed_question()-historyObj.getMax_allowed_question());
		proObj.setAllowed_question_per_day(proObj.getAllowed_question_per_day()-historyObj.getAllowed_question_per_day());	
		logger.info(" deletion of ids "+id+":: msisdn  "+proObj.getMsisdn());
		contestAuditDao.deleteContestPlayerUpdateHistoryContestData(id,liveContestBean);
		}
		logger.info("return proObj "+proObj.toString()+" :: msisdn  "+proObj.getMsisdn());
		return proObj;
	}
	
	
	
	private ContestPlayerUpdateHistoryAuditTmp questionPlayedMaxAllowedDataCorrection(int diff, ContestPlayerUpdateHistoryAuditTmp proObj, Date startDate, Date endDate, int maxQuestion, LiveContestDataBean liveContestBean, String considerSessionId, String contestName, PlayerLevelActive playerLevelActive) {
		logger.info("Inside questionPlayedMaxAllowedDataCorrection :: msisdn  "+proObj.getMsisdn());
		List<ContestPlayerUpdateHistoryContestData> mainTableDataList=	contestAuditDao.historyMaxAllowedData(proObj.getMsisdn(),proObj.getQuestion_level(),startDate,maxQuestion,diff,liveContestBean,considerSessionId,proObj.getSession_order_id());
		String id="";
		for(ContestPlayerUpdateHistoryContestData obj :mainTableDataList){
			List<String> objlist = Arrays.asList(obj.getIds().split(","));
			int count=0;
		    for(String s : objlist){
		    	if(count >= maxQuestion){
		    		id=id+","+s;
		    		System.out.println(s);
		    	}
		    	count++;		    	
		    }
		}
		logger.info("questionPlayedMaxAllowedDataCorrection id "+id+":: msisdn  "+proObj.getMsisdn());
		if(id.length() >0){
	    id = id.trim().substring(1);
	    String previousData="";
		List<ContestPlayerUpdateHistoryContest> ContestPlayerUpdateHistoryContestList = contestAuditDao.getHistoryDataByIds(id,liveContestBean,considerSessionId);
		for(ContestPlayerUpdateHistoryContest obj : ContestPlayerUpdateHistoryContestList){
			if(previousData.equals("")){
				previousData=obj.toString();
			}
			else{
				previousData=previousData+" :: "+obj.toString();
			}
		}
		contestAuditDao.createLog(proObj.getMsisdn(), contestName, proObj.getQuestion_level(),playerLevelActive.getLast_score_update(), "Max Question error :: Max question "+playerLevelActive.getMax_allowed_question()+" , day difference "+diff +" , Question_played "+proObj.getQuestion_played(),previousData,1); // log table entry
		ContestPlayerUpdateHistoryAuditTmp historyObj=	contestAuditDao.getDataToUpdateTempObj(id,liveContestBean);
		proObj.setQuestion_played(proObj.getQuestion_played()-historyObj.getQuestion_played());
		proObj.setScore(proObj.getScore()-historyObj.getScore());
		proObj.setBilling_amount(proObj.getBilling_amount()-historyObj.getBilling_amount());
	//	proObj.setMax_allowed_question(proObj.getMax_allowed_question()-historyObj.getMax_allowed_question());
		proObj.setAllowed_question_per_day(proObj.getAllowed_question_per_day()-historyObj.getAllowed_question_per_day());	
		logger.info("questionPlayedMaxAllowedDataCorrection :: deletion of ids "+id+":: msisdn  "+proObj.getMsisdn());
		contestAuditDao.deleteContestPlayerUpdateHistoryContestData(id,liveContestBean);
		}
		logger.info("questionPlayedMaxAllowedDataCorrection :: return proObj "+proObj.toString()+" :: msisdn  "+proObj.getMsisdn());
		return proObj;
	}
	
	
	

	private void updateActiveTable(PlayerLevelActive playerLevelActive, LiveContestDataBean liveContestBean, String considerSessionId, String levelActiveDeleteDuplicate, int dataCorrection) {			
		try{
			logger.info("Inside updateActiveTable ");
			if((levelActiveDeleteDuplicate != null && levelActiveDeleteDuplicate.equals("1")) || dataCorrection ==1){
				List<PlayerLevelActive> levelActiveList = contestAuditDao.getLevelActiveByMsisdn(playerLevelActive.getMsisdn(),liveContestBean, playerLevelActive.getSession_order_id() ,considerSessionId);
				logger.info("levelActiveList by msisdn size "+levelActiveList.size());
				
				double totalBillingAmount = 0;
				double totalScore=0;
				int totalQuestionPlayed=0;
				
				for(PlayerLevelActive obj : levelActiveList){
					totalBillingAmount += obj.getTotal_billing_amount().doubleValue();	
					totalScore+=obj.getTotal_score();
					totalQuestionPlayed+=obj.getTotal_question_played();
				}
				logger.info("totalBillingAmount of msisdn  "+totalBillingAmount+" ,totalScore "+totalScore+" ,totalQuestionPlayed "+totalQuestionPlayed);
				
				List<PlayerActive> playerActiveList = contestAuditDao.getActiveTableListByMsisdn(playerLevelActive.getMsisdn(), liveContestBean,playerLevelActive.getSession_order_id(),considerSessionId);   // active contest data
				PlayerActive playerActive;
				if(playerActiveList != null &&  playerActiveList.size()>1 ){	  				
	//				int count =0;
					playerActive = playerActiveList.get(0);	
					logger.info("Multiple entries in Active table for msisdn "+playerActive.getMsisdn() +" , session_order_id "+playerActive.getSession_order_id()+" :: count is "+playerActiveList.size());
					contestAuditDao.createLog(playerActive.getMsisdn(), liveContestBean.getContest_identifier(), "",null, "Multiple records found for this number in Active table :: Count is "+playerActiveList.size(),"",1); // log table entry
					if(levelActiveDeleteDuplicate != null && levelActiveDeleteDuplicate.equals("1")){
						for(PlayerActive obj :playerActiveList){
							if(playerActive.getId().longValue() != obj.getId().longValue() ){
								boolean flag=	contestAuditDao.deleteActiveRecords(obj, liveContestBean);
								if(flag){						
									contestAuditDao.createLog(obj.getMsisdn(), liveContestBean.getContest_identifier(), "",obj.getLast_updation_date(), "Duplicate msisdn value deleted from Active table ",obj.toString(),1); // log table entry
									logger.info("successfully deleted active data :: id "+obj.getId());
								}
							}
						}
					}
				}
				
				if(dataCorrection==1){
					boolean update =contestAuditDao.UpdateActiveRecord(playerLevelActive.getMsisdn(),totalBillingAmount,totalScore,totalQuestionPlayed,liveContestBean,playerLevelActive.getSession_order_id(),considerSessionId);
					if(update){
						logger.info("Active table updated success for msisdn  "+playerLevelActive.getMsisdn()+ " totalBillingAmount "+totalBillingAmount);
					}
				}
			}
		}
		catch(Exception e){
			logger.error("Error Occured while consumer Task ",e);
		}
	}

}
