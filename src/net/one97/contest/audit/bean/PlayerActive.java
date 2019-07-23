package net.one97.contest.audit.bean;

import java.util.Date;

public class PlayerActive {
	
	private Long id;
	private String msisdn;		
	private Integer total_question_played;
	private Double total_score;
	private Double total_billing_amount;
	private Date last_updation_date;
	private Long session_order_id;
	
	
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
	public Integer getTotal_question_played() {
		return total_question_played;
	}
	public void setTotal_question_played(Integer total_question_played) {
		this.total_question_played = total_question_played;
	}
	public Double getTotal_score() {
		return total_score;
	}
	public void setTotal_score(Double total_score) {
		this.total_score = total_score;
	}
	public Double getTotal_billing_amount() {
		return total_billing_amount;
	}
	public void setTotal_billing_amount(Double total_billing_amount) {
		this.total_billing_amount = total_billing_amount;
	}
	public Date getLast_updation_date() {
		return last_updation_date;
	}
	public void setLast_updation_date(Date last_updation_date) {
		this.last_updation_date = last_updation_date;
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
	@Override
	public String toString() {
		return "PlayerActive [id=" + id + ", msisdn=" + msisdn + ", total_question_played=" + total_question_played
				+ ", total_score=" + total_score + ", total_billing_amount=" + total_billing_amount
				+ ", last_updation_date=" + last_updation_date + ", session_order_id=" + session_order_id + "]";
	}

	
	
	
	
	

}
