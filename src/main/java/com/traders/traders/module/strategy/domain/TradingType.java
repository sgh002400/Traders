package com.traders.traders.module.strategy.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum TradingType {
    LONG("매수"),
    SHORT("매도"),
    NONE("무");

    private final String value;
}
