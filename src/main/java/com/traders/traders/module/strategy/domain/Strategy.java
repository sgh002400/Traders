package com.traders.traders.module.strategy.domain;

import static com.traders.traders.module.strategy.domain.TradingType.*;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.traders.traders.common.jpa.AuditTime;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Strategy extends AuditTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private double profitFactor;

	@Column(nullable = false)
	private double netProfitRate;

	@Column(nullable = false)
	private double winningRate;

	@Column(nullable = false)
	private double totalProfitRate;

	@Column(nullable = false)
	private double totalLossRate;

	@Column(nullable = false)
	private int totalTradeCount;

	@Column(nullable = false)
	private int winCount;

	@Column(nullable = false)
	private int lossCount;

	@Embedded
	private Position currentPosition;

	@Builder
	private Strategy(String name, double profitFactor, double netProfitRate, double winningRate,
		double totalProfitRate, double totalLossRate, int winCount, int lossCount, Position currentPosition) {
		this.name = name;
		this.profitFactor = profitFactor;
		this.netProfitRate = netProfitRate;
		this.winningRate = winningRate;
		this.totalProfitRate = totalProfitRate;
		this.totalLossRate = totalLossRate;
		this.totalTradeCount = winCount + lossCount;
		this.winCount = winCount;
		this.lossCount = lossCount;
		this.currentPosition = currentPosition;
	}

	public static Strategy of(String name, double profitFactor, double netProfitRate, double winningRate,
		double totalProfitRate, double totalLossRate, int winCount, int lossCount, Position currentPosition) {
		return Strategy.builder()
			.name(name)
			.profitFactor(profitFactor)
			.netProfitRate(netProfitRate)
			.winningRate(winningRate)
			.totalProfitRate(totalProfitRate)
			.totalLossRate(totalLossRate)
			.winCount(winCount)
			.lossCount(lossCount)
			.currentPosition(currentPosition)
			.build();
	}

	public void updateCurrentPosition(Position position) {
		this.currentPosition = position;
	}

	public void updateMetaData(Position position) {
		double profitRate = calculateProfitRate(position);

		if (isWin(profitRate)) {
			addWinCount();
			updateTotalProfitRate(profitRate);
		} else {
			addLossCount();
			updateTotalLossRate(profitRate);
		}

		addTotalTradeCount();
		updateProfitFactor();
		updateWinRate();
		updateNetProfitRate();
		updateCurrentPosition(position);
	}

	public boolean isLongPosition() {
		return this.currentPosition.getTradingType() == LONG;
	}

	public boolean isShortPosition() {
		return this.currentPosition.getTradingType() == SHORT;
	}

	private void addTotalTradeCount() {
		this.totalTradeCount++;
	}

	private boolean isWin(double profitRate) {
		return profitRate > 0;
	}

	private boolean isCurrentPositionLong() {
		return this.currentPosition.getTradingType() == LONG;
	}

	private void addWinCount() {
		this.winCount++;
	}

	private void addLossCount() {
		this.lossCount++;
	}

	private void updateTotalProfitRate(double profitRate) {
		this.totalProfitRate += profitRate;
	}

	private void updateTotalLossRate(double profitRate) {
		this.totalLossRate -= profitRate;
	}

	private void updateProfitFactor() {
		this.profitFactor = this.totalProfitRate / this.totalLossRate;
	}

	private void updateWinRate() {
		this.winningRate = (double)this.winCount / this.totalTradeCount * 100;
	}

	private void updateNetProfitRate() {
		this.netProfitRate = this.totalProfitRate - this.totalLossRate;
	}

	private double calculateProfitRate(Position position) {
		if (isCurrentPositionLong()) {
			return ((double)(position.getPrice() - this.currentPosition.getPrice()) / this.currentPosition.getPrice())
				* 100;
		}

		return ((double)(this.currentPosition.getPrice() - position.getPrice()) / this.currentPosition.getPrice())
			* 100;
	}
}
