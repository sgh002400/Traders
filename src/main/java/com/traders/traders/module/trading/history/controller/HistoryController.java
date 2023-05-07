package com.traders.traders.module.trading.history.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traders.traders.module.trading.history.service.HistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trading/histories")
public class HistoryController {
	private final HistoryService historyService;
}