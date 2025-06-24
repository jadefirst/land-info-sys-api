package com.example.propertyanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor  
@NoArgsConstructor
public class InvestmentScoreResponse {

	
    private int totalScore;        // 종합 점수
    private int profitabilityScore; // 수익성 점수
    private int transportScore;     // 교통 점수
    private int convenienceScore;   // 편의성 점수
    private int activityScore;      // 거래활성도 점수
    private String address;         // 주소
    
	public int getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}
	public int getProfitabilityScore() {
		return profitabilityScore;
	}
	public void setProfitabilityScore(int profitabilityScore) {
		this.profitabilityScore = profitabilityScore;
	}
	public int getTransportScore() {
		return transportScore;
	}
	public void setTransportScore(int transportScore) {
		this.transportScore = transportScore;
	}
	public int getConvenienceScore() {
		return convenienceScore;
	}
	public void setConvenienceScore(int convenienceScore) {
		this.convenienceScore = convenienceScore;
	}
	public int getActivityScore() {
		return activityScore;
	}
	public void setActivityScore(int activityScore) {
		this.activityScore = activityScore;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
    
}
