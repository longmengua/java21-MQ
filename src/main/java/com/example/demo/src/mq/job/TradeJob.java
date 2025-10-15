package com.example.demo.src.mq.job;

import com.example.demo.src.mq.event.TradeEvent;
import com.example.demo.src.mq.producer.TradeProducerZeroGc;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TradeJob {

    private final TradeProducerZeroGc producer;

    /** 依照 application.yml 的 cron 產生樣本交易事件 */
    @Scheduled(cron = "${app.cron.produce-trades}", zone = "${app.cron.tz}")
    public void produceSampleTrade() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        TradeEvent evt = new TradeEvent(
                rnd.nextInt(10),                // symbolId (例如 BTCUSDT=1)
                rnd.nextLong(1_000_000_000L),   // tradeId
                rnd.nextLong(100_000L),         // userId
                50000.0 + rnd.nextDouble(-10, 10), // price
                rnd.nextDouble(0.001, 0.01),    // qty
                Instant.now().toEpochMilli() * 1_000_000 // 轉 ns
        );

        producer.send("trades", "BTCUSDT", evt);
    }
}
