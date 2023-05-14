package com.traders.traders.module.strategy.controller;

import java.util.concurrent.CompletableFuture;

import javax.validation.Valid;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traders.traders.module.strategy.controller.dto.request.CreateStrategyDto;
import com.traders.traders.module.strategy.controller.dto.request.WebHookRequestDto;
import com.traders.traders.module.strategy.controller.dto.response.FindStrategiesInfoResponseDto;
import com.traders.traders.module.strategy.service.StrategyService;
import com.traders.traders.module.users.controller.dto.request.SubscribeStrategyRequestDto;
import com.traders.traders.module.users.domain.Users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/strategies")
@Slf4j
public class StrategyController {
	private final StrategyService strategyService;

	@KafkaListener(topics = "Trading", groupId = "trading-strategy-executors")
	public void handleWebHook(@RequestBody WebHookRequestDto request) {
		//TODO - 예외 처리 변경하기 (로그 남기게, 재시도)
		CompletableFuture.runAsync(() -> strategyService.handleWebHook(request.toServiceDto()))
			.exceptionally(ex -> {
				log.error("Error occurred while handling webhook: ", ex);
				return null;
			});
	}

	@GetMapping()
	public FindStrategiesInfoResponseDto findStrategiesInfos() {
		return strategyService.findStrategiesInfo();
	}

	@PostMapping("/{id}/subscriptions")
	public void subScribe(@AuthenticationPrincipal Users user, @Valid @RequestBody SubscribeStrategyRequestDto request,
		@PathVariable Long id) {
		strategyService.subscribeStrategy(user, request.toServiceDto(id));
	}

	//TODO - 개발용 메서드!! 추후 삭제하기
	@PostMapping("/create")
	public void createStrategy(@Valid @RequestBody CreateStrategyDto request) {
		strategyService.createStrategy(request);
	}
}
