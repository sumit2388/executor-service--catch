package net.one97.contest.audit.bean;

import java.util.Date;

public class PlayerLevelActive {
	private Long id;
	private String msisdn;
	private Double total_score;
	private Double day_score;
	private Integer total_question_played;
	private Integer question_played_day;
	private Double total_billing_amount;
	private Double day_total_billing;
	private Date last_billing_update;
	private Date last_score_update;
	private Date last_updation_date;
	private String level;
	private Long session_order_id;
	
	private Integer max_allowed_question;
	private Integer max_allowed_question_per_day;
	
	
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

	public Double getTotal_score() {
		return total_score;
	}

	public void setTotal_score(Double total_score) {
		if (total_score == null) {
			this.total_score = 0.0;
		} else {
			this.total_score = total_score;
		}

	}

	public Double getDay_score() {
		return day_score;
	}

	public void setDay_score(Double day_score) {
		if (day_score == null) {
			this.day_score = 0.0;
		} else {
			this.day_score = day_score;
		}

	}

	public Integer getTotal_question_played() {
		return total_question_played;
	}

	public void setTotal_question_played(Integer total_question_played) {
		if (total_question_played == null) {
			this.total_question_played = 0;
		} else {
			this.total_question_played = total_question_played;
		}

	}

	public Integer getQuestion_played_day() {
		return question_played_day;
	}

	public void setQuestion_played_day(Integer question_played_day) {
		if (question_played_day == null) {
			this.question_played_day = 0;
		} else {
			this.question_played_day = question_played_day;
		}

	}

	public Double getTotal_billing_amount() {
		return total_billing_amount;
	}

	public void setTotal_billing_amount(Double total_billing_amount) {
		if (total_billing_amount == null) {
			this.total_billing_amount = 0.0;
		} else {
			this.total_billing_amount = total_billing_amount;
		}

	}

	public Double getDay_total_billing() {
		return day_total_billing;
	}

	public void setDay_total_billing(Double day_total_billing) {
		if (day_total_billing == null) {
			this.day_total_billing = 0.0;
		} else {
			this.day_total_billing = day_total_billing;
		}

	}

	public Date getLast_billing_update() {
		return last_billing_update;
	}

	public void setLast_billing_update(Date last_billing_update) {
		this.last_billing_update = last_billing_update;

	}

	public Date getLast_score_update() {
		return last_score_update;
	}

	public void setLast_score_update(Date last_score_update) {
		this.last_score_update = last_score_update;
	}

	public Date getLast_updation_date() {
		return last_updation_date;
	}

	public void setLast_updation_date(Date last_updation_date) {
		this.last_updation_date = last_updation_date;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@Override
	public String toString() {
		return "PlayerLevelActive [id=" + id + ", msisdn=" + msisdn + ", total_score=" + total_score + ", day_score="
				+ day_score + ", total_question_played=" + total_question_played + ", question_played_day="
				+ question_played_day + ", total_billing_amount=" + total_billing_amount + ", day_total_billing="
				+ day_total_billing + ", last_billing_update=" + last_billing_update + ", last_score_update="
				+ last_score_update + ", last_updation_date=" + last_updation_date + ", level=" + level + "]";
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

	public Integer getMax_allowed_question() {
		return max_allowed_question;
	}

	public void setMax_allowed_question(Integer max_allowed_question) {
		this.max_allowed_question = max_allowed_question;
	}

	public Integer getMax_allowed_question_per_day() {
		return max_allowed_question_per_day;
	}

	public void setMax_allowed_question_per_day(Integer max_allowed_question_per_day) {
		this.max_allowed_question_per_day = max_allowed_question_per_day;
	}

	
}
