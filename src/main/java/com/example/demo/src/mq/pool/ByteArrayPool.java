package com.example.demo.src.mq.pool;

import java.util.ArrayDeque;

/** 單執行緒簡易池（若提高併發，請改成分片或加鎖/並發佇列） */
public final class ByteArrayPool {
    private final ArrayDeque<byte[]> q = new ArrayDeque<>();
    private final int chunkSize;
    public ByteArrayPool(int chunkSize, int preallocate) {
        this.chunkSize = chunkSize;
        for (int i=0;i<preallocate;i++) q.add(new byte[chunkSize]);
    }
    public byte[] rent() {
        byte[] b = q.pollFirst();
        return (b != null) ? b : new byte[chunkSize];
    }
    public void giveBack(byte[] b) {
        if (b!=null && b.length==chunkSize) q.addFirst(b);
    }
}
