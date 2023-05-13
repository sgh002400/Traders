package com.traders.traders.common.secret;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties(prefix = "secret")
public final class SecretKeyManager {
	private final String jwt;
}