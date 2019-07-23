package net.one97.contest.audit.bean;

import java.util.Date;

public class ContestPlayerUpdateHistoryContest {

	private Long id;
	private String msisdn;
	private Long request_id;
	private String transaction_id;
	private Integer contest_id;
	private Integer billing_validity;
	private Date request_date;
	private String 	zone;
	private String operator;
	private String circle;
	private String short_code;
	private String channel;
	private String language;
	private String request_type;
	private String request_value;
	private String ans_type;
	private String qusetion_id;
	private String operation;
	private String param;
	private String param1;
	private String param2;
	private String param3;
	private String question_level;
	private Double score;
	private Double time_taken;
	private Double billing_amount;
	private Integer max_allowed_question;
	private Integer allowed_question_per_day;
	private Integer session_order_id;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public Long getRequest_id() {
		return request_id;
	}
	public void setRequest_id(Long request_id) {
		this.request_id = request_id;
	}
	public String getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	public Integer getContest_id() {
		return contest_id;
	}
	public void setContest_id(Integer contest_id) {
		this.contest_id = contest_id;
	}
	public Integer getBilling_validity() {
		return billing_validity;
	}
	public void setBilling_validity(Integer billing_validity) {
		this.billing_validity = billing_validity;
	}
	public Date getRequest_date() {
		return request_date;
	}
	public void setRequest_date(Date request_date) {
		this.request_date = request_date;
	}
	public String getZone() {
		return zone;
	}
	public void setZone(String zone) {
		this.zone = zone;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getCircle() {
		return circle;
	}
	public void setCircle(String circle) {
		this.circle = circle;
	}
	public String getShort_code() {
		return short_code;
	}
	public void setShort_code(String short_code) {
		this.short_code = short_code;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getRequest_type() {
		return request_type;
	}
	public void setRequest_type(String request_type) {
		this.request_type = request_type;
	}
	public String getRequest_value() {
		return request_value;
	}
	public void setRequest_value(String request_value) {
		this.request_value = request_value;
	}
	public String getAns_type() {
		return ans_type;
	}
	public void setAns_type(String ans_type) {
		this.ans_type = ans_type;
	}
	public String getQusetion_id() {
		return qusetion_id;
	}
	public void setQusetion_id(String qusetion_id) {
		this.qusetion_id = qusetion_id;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getParam1() {
		return param1;
	}
	public void setParam1(String param1) {
		this.param1 = param1;
	}
	public String getParam2() {
		return param2;
	}
	public void setParam2(String param2) {
		this.param2 = param2;
	}
	public String getParam3() {
		return param3;
	}
	public void setParam3(String param3) {
		this.param3 = param3;
	}
	public String getQuestion_level() {
		return question_level;
	}
	public void setQuestion_level(String question_level) {
		this.question_level = question_level;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public Double getTime_taken() {
		return time_taken;
	}
	public void setTime_taken(Double time_taken) {
		this.time_taken = time_taken;
	}
	public Double getBilling_amount() {
		return billing_amount;
	}
	public void setBilling_amount(Double billing_amount) {
		this.billing_amount = billing_amount;
	}
	public Integer getMax_allowed_question() {
		return max_allowed_question;
	}
	public void setMax_allowed_question(Integer max_allowed_question) {
		this.max_allowed_question = max_allowed_question;
	}
	public Integer getAllowed_question_per_day() {
		return allowed_question_per_day;
	}
	public void setAllowed_question_per_day(Integer allowed_question_per_day) {
		this.allowed_question_per_day = allowed_question_per_day;
	}
	public Integer getSession_order_id() {
		return session_order_id;
	}
	public void setSession_order_id(Integer session_order_id) {
		this.session_order_id = session_order_id;
	}
	@Override
	public String toString() {
		return "contest_player_update_history_contest [id=" + id + ", msisdn=" + msisdn + ", request_id=" + request_id
				+ ", transaction_id=" + transaction_id + ", contest_id=" + contest_id + ", billing_validity="
				+ billing_validity + ", request_date=" + request_date + ", zone=" + zone + ", operator=" + operator
				+ ", circle=" + circle + ", short_code=" + short_code + ", channel=" + channel + ", language="
				+ language + ", request_type=" + request_type + ", request_value=" + request_value + ", ans_type="
				+ ans_type + ", qusetion_id=" + qusetion_id + ", operation=" + operation + ", param=" + param
				+ ", param1=" + param1 + ", param2=" + param2 + ", param3=" + param3 + ", question_level="
				+ question_level + ", score=" + score + ", time_taken=" + time_taken + ", billing_amount="
				+ billing_amount + ", max_allowed_question=" + max_allowed_question + ", allowed_question_per_day="
				+ allowed_question_per_day + ", session_order_id=" + session_order_id + "]";
	}
	
	
	
}
