package com.tradin.module.feign.service;

import com.tradin.generator.SignatureGenerator;
import com.tradin.module.feign.client.BinanceClient;
import com.tradin.module.feign.client.dto.binance.CurrentPositionInfoDto;
import com.tradin.module.feign.client.dto.binance.FutureAccountBalanceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BinanceFeignService {
    private static final String TICKER = "BTCUSDT";
    private static final String TIMESTAMP = "&timestamp=";
    private static final String TYPE = "&type=MARKET";
    private static final String SIDE = "&side=";
    private static final String QUANTITY = "&quantity=";
    private static final String RECV_WINDOW = "&recvWindow=1000";
    private static final String LEVERAGE = "&leverage=";
    private static final String SYMBOL = "&symbol=";

    private final BinanceClient binanceFeignClient;

    //binance는 api 요청 보낼 때 queryString은 이름 기준으로 오름차순으로 정렬해야 됨. 요청을 보낼 때 파라미터 순서는 queryString과 동일해야 됨
    public void openPosition(String apiKey, String secretKey, String side, int orderQuantity) {
        Long timestamp = Instant.now().toEpochMilli();
        double quantity = Math.abs(getBtcusdtPositionQuantity(apiKey, secretKey)) + orderQuantity;

        String queryString =
                QUANTITY + quantity + RECV_WINDOW + SIDE + side + SYMBOL + TICKER + TIMESTAMP + timestamp + TYPE;

        String signature = SignatureGenerator.generateSignature(queryString, secretKey);

        binanceFeignClient.order(apiKey, quantity, 1000L, side, signature, TICKER, timestamp, "MARKET");
    }

    public void closePosition(String apiKey, String secretKey, String side) {
        Long timestamp = Instant.now().toEpochMilli();
        double quantity = Math.abs(getBtcusdtPositionQuantity(apiKey, secretKey));

        String queryString =
                QUANTITY + quantity + RECV_WINDOW + SIDE + side + SYMBOL + TICKER + TIMESTAMP
                        + timestamp
                        + TYPE;

        String signature = SignatureGenerator.generateSignature(queryString, secretKey);

        binanceFeignClient.order(apiKey, quantity, 1000L, side, signature, TICKER, timestamp, "MARKET");
    }

    public Double getBtcusdtPositionQuantity(String apiKey, String secretKey) {
        Long timestamp = Instant.now().toEpochMilli();
        String queryString = RECV_WINDOW + SYMBOL + TICKER + TIMESTAMP + timestamp;
        String signature = SignatureGenerator.generateSignature(queryString, secretKey);

        List<CurrentPositionInfoDto> currentPositionInfoDtos = binanceFeignClient.getCurrentPositionInfo(1000L, TICKER,
                timestamp,
                apiKey, signature);

        return extractBtcusdtPositionQuantity(currentPositionInfoDtos);
    }

    public int changeLeverage(String apiKey, String secretKey, int leverage) {
        Long timestamp = Instant.now().toEpochMilli();
        String queryString = LEVERAGE + leverage + RECV_WINDOW + SYMBOL + TICKER + TIMESTAMP
                + timestamp;
        String signature = SignatureGenerator.generateSignature(queryString, secretKey);

        return binanceFeignClient.changeLeverage(apiKey, leverage, 1000L, TICKER, timestamp, signature).getLeverage();
    }

    public int getFutureAccountBalance(String apiKey, String secretKey) {
        Long timestamp = Instant.now().toEpochMilli();
        String queryString = RECV_WINDOW + TIMESTAMP + timestamp;
        String signature = SignatureGenerator.generateSignature(queryString, secretKey);

        List<FutureAccountBalanceDto> futureAccountBalanceDtos = binanceFeignClient.getFutureAccountBalance(apiKey, 1000L, timestamp, signature);

        return extractUsdtBalance(futureAccountBalanceDtos);
    }

    private Double extractBtcusdtPositionQuantity(List<CurrentPositionInfoDto> currentPositionInfoDtos) {
        for (CurrentPositionInfoDto dto : currentPositionInfoDtos) {
            if (dto.getSymbol().equals(TICKER)) {
                return Double.parseDouble(dto.getPositionAmt());
            }
        }
        return null;
    }

    private int extractUsdtBalance(List<FutureAccountBalanceDto> futureAccountBalanceDtos) {
        for (FutureAccountBalanceDto dto : futureAccountBalanceDtos) {
            if (dto.getAsset().equals("USDT")) {
                return Integer.parseInt(dto.getAvailableBalance());
            }
        }
        return 0;
    }

}
