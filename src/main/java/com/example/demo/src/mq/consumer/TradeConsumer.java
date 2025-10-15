package com.example.demo.src.mq.consumer;

import com.example.demo.src.mq.event.TradeEventZeroGcCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TradeConsumer {

    // 單執行緒 listener 可重用視圖（零暫態物件）
    private final TradeEventZeroGcCodec.TradeEventView view = new TradeEventZeroGcCodec.TradeEventView();

    @KafkaListener(
            topics = "trades",
            groupId = "demo-group",
            concurrency = "1",
            containerFactory = "bytesManualAckListenerContainerFactory"
    )
    public void onMsg(ConsumerRecord<String, byte[]> rec, Acknowledgment ack) {
        byte[] v = rec.value();
        // 反序列化至可重用視圖
        TradeEventZeroGcCodec.decodeInto(v, 0, view);

        // 在這裡進行撮合/風控邏輯（零 GC）
        // e.g. view.symbolId, view.price, view.qty, view.tsNanos ...

        // 手動 ack（配合 MANUAL_IMMEDIATE）
        ack.acknowledge();

        // ✅ 加上 log，顯示消費內容成字串
        log.info(
                "✅ 收到 => topic={}, partition={}, offset={}, key={}, " +
                        "symbolId={}, tradeId={}, userId={}, price={}, qty={}, tsNanos={}",
                rec.topic(),
                rec.partition(),
                rec.offset(),
                rec.key(),
                view.symbolId,
                view.tradeId,
                view.userId,
                view.price,
                view.qty,
                view.tsNanos
        );
    }
}
