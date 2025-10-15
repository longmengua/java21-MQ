package com.example.demo.src.mq.event;

public record TradeEvent(
        int symbolId,     // 值內用 id，key 仍用 "BTCUSDT" 字串
        long tradeId,
        long userId,
        double price,
        double qty,
        long tsNanos
) {}
