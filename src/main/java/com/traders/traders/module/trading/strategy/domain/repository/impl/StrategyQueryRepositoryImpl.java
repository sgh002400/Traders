package com.traders.traders.module.trading.strategy.domain.repository.impl;

import static com.traders.traders.module.trading.strategy.domain.QStrategy.*;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.traders.traders.module.trading.strategy.domain.repository.StrategyQueryRepository;
import com.traders.traders.module.trading.strategy.domain.repository.dao.QStrategyInfoDao;
import com.traders.traders.module.trading.strategy.domain.repository.dao.StrategyInfoDao;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StrategyQueryRepositoryImpl implements StrategyQueryRepository {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public Optional<List<StrategyInfoDao>> findStrategiesInfoDao() {
		return Optional.ofNullable(jpaQueryFactory
			.select(
				new QStrategyInfoDao(strategy.id, strategy.name, strategy.profitFactor, strategy.netProfitRate,
					strategy.winningRate, strategy.totalProfitRate, strategy.totalLossRate, strategy.totalTradeCount,
					strategy.winCount, strategy.lossCount, strategy.currentPosition.tradingType,
					strategy.currentPosition.time, strategy.currentPosition.price)
			)
			.from(strategy)
			.orderBy(strategy.id.asc())
			.fetch());
	}
}
