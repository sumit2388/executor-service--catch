package net.one97.contest.audit.bean;

import java.util.Date;

public class LiveContestDataBean implements Cloneable {

	private int contest_id;
	private String contest_identifier;
	private int max_question_per_day;
	private int max_question;
	private String level;
	private int max_question_per_day_level;
	private int max_question_level;
	private String level_params;
	private String price_question_details;
	private int carry_forward;
	private Date start_date;
	private boolean isRunningFirstTime;

	public int getContest_id() {
		return contest_id;
	}

	public void setContest_id(int contest_id) {
		this.contest_id = contest_id;
	}

	public String getContest_identifier() {
		return contest_identifier;
	}

	public void setContest_identifier(String contest_identifier) {
		this.contest_identifier = contest_identifier;
	}

	public int getMax_question_per_day() {
		return max_question_per_day;
	}

	public void setMax_question_per_day(int max_question_per_day) {
		this.max_question_per_day = max_question_per_day;
	}

	public int getMax_question() {
		return max_question;
	}

	public void setMax_question(int max_question) {
		this.max_question = max_question;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public int getMax_question_per_day_level() {
		return max_question_per_day_level;
	}

	public void setMax_question_per_day_level(int max_question_per_day_level) {
		this.max_question_per_day_level = max_question_per_day_level;
	}

	public int getMax_question_level() {
		return max_question_level;
	}

	public void setMax_question_level(int max_question_level) {
		this.max_question_level = max_question_level;
	}

	public String getLevel_params() {
		return level_params;
	}

	public void setLevel_params(String level_params) {
		this.level_params = level_params;
	}

	public String getPrice_question_details() {
		return price_question_details;
	}

	public void setPrice_question_details(String price_question_details) {
		this.price_question_details = price_question_details;
	}

	public int getCarry_forward() {
		return carry_forward;
	}

	public void setCarry_forward(int carry_forward) {
		this.carry_forward = carry_forward;
	}

	public Date getStart_date() {
		return start_date;
	}

	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}

	public boolean isRunningFirstTime() {
		return isRunningFirstTime;
	}

	public void setRunningFirstTime(boolean isRunningFirstTime) {
		this.isRunningFirstTime = isRunningFirstTime;
	}

	@Override
	public String toString() {
		return "LiveContestDataBean [contest_id=" + contest_id + ", contest_identifier=" + contest_identifier
				+ ", max_question_per_day=" + max_question_per_day + ", max_question=" + max_question + ", level="
				+ level + ", max_question_per_day_level=" + max_question_per_day_level + ", max_question_level="
				+ max_question_level + ", level_params=" + level_params + ", price_question_details="
				+ price_question_details + ", carry_forward=" + carry_forward + ", start_date=" + start_date
				+ ", isRunningFirstTime=" + isRunningFirstTime + "]";
	}
	
	@Override
	public synchronized LiveContestDataBean clone() throws CloneNotSupportedException {
		return (LiveContestDataBean)super.clone();
	}

}
