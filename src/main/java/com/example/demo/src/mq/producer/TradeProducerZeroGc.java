package com.example.demo.src.mq.producer;

import com.example.demo.src.mq.event.TradeEvent;
import com.example.demo.src.mq.event.TradeEventZeroGcCodec;
import com.example.demo.src.mq.pool.ByteArrayPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TradeProducerZeroGc {

    private final KafkaTemplate<String, byte[]> tpl;
    private final ByteArrayPool pool = new ByteArrayPool(TradeEventZeroGcCodec.BYTES, 1024);

    public TradeProducerZeroGc(KafkaTemplate<String, byte[]> tpl) {
        this.tpl = tpl;
    }

    public void send(String topic, String symbolKey, TradeEvent e) {
        byte[] buf = pool.rent();
        TradeEventZeroGcCodec.encode(e, buf, 0);

        CompletableFuture<SendResult<String, byte[]>> f = tpl.send(topic, symbolKey, buf);
        f.whenComplete((res, ex) -> {
            // 無論成功/失敗都歸還 buffer（避免洩漏）
            try {
                if (ex != null) {
                    log.warn("Kafka send failed: topic={}, key={}, tradeId={}, symbolId={}, userId={}, price={}, qty={}, tsNanos={}",
                            topic, symbolKey, e.tradeId(), e.symbolId(), e.userId(), e.price(), e.qty(), e.tsNanos(), ex);
                } else {
                    RecordMetadata md = res.getRecordMetadata();

                    // ✅ 加上 log，顯示內容成字串
                    log.info(
                            "✅ 發出 => topic={}, partition={}, offset={}, key={}, " +
                                    "symbolId={}, tradeId={}, userId={}, price={}, qty={}, tsNanos={}",
                            md.topic(),
                            md.partition(),
                            md.offset(),
                            symbolKey,
                            e.symbolId(),
                            e.tradeId(),
                            e.userId(),
                            e.price(),
                            e.qty(),
                            e.tsNanos()
                    );
                }
            } finally {
                pool.giveBack(buf);
            }
        });
    }
}
