package com.tradin.common.jwt;

import com.tradin.common.exception.ExceptionMessage;
import com.tradin.common.exception.TradinException;
import com.tradin.module.auth.service.dto.UserDataDto;
import com.tradin.module.feign.client.dto.cognito.JwkDto;
import com.tradin.module.feign.client.dto.cognito.JwkDtos;
import com.tradin.module.feign.service.CognitoFeignService;
import com.tradin.module.users.domain.Users;
import com.tradin.module.users.service.UsersService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.tradin.common.exception.ExceptionMessage.NOT_FOUND_JWK_PARTS_EXCEPTION;
import static com.tradin.common.exception.ExceptionMessage.PUBLIC_KEY_GENERATE_FAIL_EXCEPTION;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JwtUtil {
    private static final long JWK_KEY_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;    // 7일
    private final UsersService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CognitoFeignService cognitoFeignService;

    public UserDataDto extractUserDataFromIdToken(String idToken) {
        Claims claims = validateToken(idToken);

        String sub = claims.get("sub", String.class);
        String email = claims.get("email", String.class);
        String socialId = claims.get("username", String.class);

        return UserDataDto.of(sub, email, socialId);
    }

    private Claims parseClaim(String accessToken, PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireExpiration(new Date())
                    .requireIssuer("https://cognito-idp.ap-northeast-2.amazonaws.com/ap-northeast-2_45OUbYhf2")
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (Exception e) {
            throw new TradinException(ExceptionMessage.INVALID_JWT_TOKEN_EXCEPTION);
        }
    }

    private String getKidFromAccessTokenHeader(String accessToken) {
        return Jwts.parserBuilder()
                .build()
                .parseClaimsJws(accessToken)
                .getHeader()
                .get("kid").toString();
    }

    public Claims validateToken(String token) {
        String kidFromTokenHeader = getKidFromAccessTokenHeader(token);
        Map<String, String> jwtKeyParts = getNAndEFromCachedJwkKey(kidFromTokenHeader).orElseThrow(() -> new TradinException(NOT_FOUND_JWK_PARTS_EXCEPTION));
        String n = jwtKeyParts.get("n");
        String e = jwtKeyParts.get("e");
        PublicKey publicKey = generateRSAPublicKey(n, e);

        return parseClaim(token, publicKey);
    }

    private Optional<Map<String, String>> getNAndEFromCachedJwkKey(String kid) {
        final String cacheKey = "JwkKey:Kid" + kid;
        JwkDto jwkDto = getJwkFromCache(cacheKey);

        if (jwkDto == null) {
            cacheJwkKey();
            jwkDto = getJwkFromCache(cacheKey);
        }

        return Optional.ofNullable(jwkDto).map(dto -> {
            Map<String, String> map = new HashMap<>();
            map.put("n", dto.getN());
            map.put("e", dto.getE());
            return map;
        });
    }


    private JwkDto getJwkFromCache(String cacheKey) {
        return (JwkDto) redisTemplate.opsForValue().get(cacheKey);
    }

    private void cacheJwkKey() {
        JwkDtos jwkDtos = getJwkKeyFromCognito();

        for (JwkDto jwkDto : jwkDtos.getKeys()) {
            final String cacheKey = "JwkKey:Kid" + jwkDto.getKid();
            redisTemplate.opsForValue().set(cacheKey, jwkDto, JWK_KEY_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        }
    }

    private JwkDtos getJwkKeyFromCognito() {
        return cognitoFeignService.getJwkKey();
    }

    private PublicKey generateRSAPublicKey(String n, String e) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(n);
            byte[] eBytes = Base64.getUrlDecoder().decode(e);

            BigInteger nBigInt = new BigInteger(1, nBytes);
            BigInteger eBigInt = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(nBigInt, eBigInt);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);

        } catch (Exception ex) {
            throw new TradinException(PUBLIC_KEY_GENERATE_FAIL_EXCEPTION);
        }
    }

    public Authentication getAuthentication(String sub) {
        Users user = userService.loadUserByUsername(sub);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}