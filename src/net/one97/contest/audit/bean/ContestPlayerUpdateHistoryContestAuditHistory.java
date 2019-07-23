package net.one97.contest.audit.bean;

import java.util.Date;

public class ContestPlayerUpdateHistoryContestAuditHistory {
	private Long id;
	private String msisdn;
	private String question_level;
	private Integer question_played;
	private Integer score;
	private Double billing_amount;
	private Integer max_allowed_question;
	private Integer allowed_question_per_day;
	private Date upto_date;
	private Long session_order_id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		if (id == null) {
			this.id = 0l;
		} else {
			this.id = id;
		}

	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getQuestion_level() {
		return question_level;
	}

	public void setQuestion_level(String question_level) {
		this.question_level = question_level;
	}

	public Integer getQuestion_played() {
		return question_played;
	}

	public void setQuestion_played(Integer question_played) {
		if (question_played == null) {
			this.question_played = 0;
			;
		} else {
			this.question_played = question_played;
		}

	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		if (score == null) {
			this.score = 0;
		} else {
			this.score = score;
		}

	}

	public Double getBilling_amount() {
		return billing_amount;
	}

	public void setBilling_amount(Double billing_amount) {
		if (billing_amount == null) {
			this.billing_amount = 0.0;
		} else {
			this.billing_amount = billing_amount;
		}

	}

	public Integer getMax_allowed_question() {
		return max_allowed_question;
	}

	public void setMax_allowed_question(Integer max_allowed_question) {
		if (max_allowed_question == null) {
			this.max_allowed_question = 0;
		} else {
			this.max_allowed_question = max_allowed_question;
		}

	}

	public Integer getAllowed_question_per_day() {
		return allowed_question_per_day;
	}

	public void setAllowed_question_per_day(Integer allowed_question_per_day) {
		if (allowed_question_per_day == null) {
			this.allowed_question_per_day = 0;
		} else {
			this.allowed_question_per_day = allowed_question_per_day;
		}

	}

	public Date getUpto_date() {
		return upto_date;
	}

	public void setUpto_date(Date upto_date) {
		this.upto_date = upto_date;
	}

	public Long getSession_order_id() {
		return session_order_id;
	}

	public void setSession_order_id(Long session_order_id) {
		if (session_order_id == null) {
			this.session_order_id = 0l;
		} else {
			this.session_order_id = session_order_id;
		}
	}

}
