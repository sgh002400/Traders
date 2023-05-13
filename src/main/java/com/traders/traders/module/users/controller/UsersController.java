package com.traders.traders.module.users.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.traders.traders.module.users.controller.dto.request.SignInRequestDto;
import com.traders.traders.module.users.controller.dto.request.SignUpRequestDto;
import com.traders.traders.module.users.controller.dto.response.TokenResponseDto;
import com.traders.traders.module.users.service.UsersService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "v1/users")

public class UsersController {
	private final UsersService usersService;

	@PostMapping("/signUp")
	public ResponseEntity<TokenResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request) {
		return ResponseEntity.ok(usersService.signUp(request.toServiceDto()));
	}

	@PostMapping("/signIn")
	public ResponseEntity<TokenResponseDto> signIn(@Valid @RequestBody SignInRequestDto request) {
		return ResponseEntity.ok(usersService.signIn(request.toServiceDto()));
	}
}
