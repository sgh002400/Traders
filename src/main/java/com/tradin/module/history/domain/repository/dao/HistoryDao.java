package com.tradin.module.history.domain.repository.dao;

import com.querydsl.core.annotations.QueryProjection;
import com.tradin.module.strategy.domain.Position;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryDao {
    private final Long id;
    private final Position entryPosition;
    private final Position exitPosition;
    private final double profitRate;
    private double compoundProfitRate;

    @QueryProjection
    public HistoryDao(Long id, Position entryPosition, Position exitPosition, double profitRate) {
        this.id = id;
        this.entryPosition = entryPosition;
        this.exitPosition = exitPosition;
        this.profitRate = profitRate;
        this.compoundProfitRate = 0;
    }
}
