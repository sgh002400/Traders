package com.traders.traders.module.trading.history;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.traders.traders.common.jpa.AuditTime;
import com.traders.traders.module.trading.strategy.Position;
import com.traders.traders.module.trading.strategy.Strategy;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class History extends AuditTime {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Position entryPosition;

	@Column
	private Position exitPosition;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "strategy_id", nullable = false)
	private Strategy strategy;

	@Builder
	private History(Position entryPosition, Position exitPosition, Strategy strategy) {
		this.entryPosition = entryPosition;
		this.exitPosition = exitPosition;
		this.strategy = strategy;
	}

	public static History of(Position entryPosition, Position exitPosition, Strategy strategy) {
		return History.builder()
			.entryPosition(entryPosition)
			.exitPosition(exitPosition)
			.strategy(strategy)
			.build();
	}
}
