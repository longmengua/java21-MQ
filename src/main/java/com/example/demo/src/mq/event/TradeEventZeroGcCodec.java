package com.example.demo.src.mq.event;

public final class TradeEventZeroGcCodec {
    private TradeEventZeroGcCodec() {}

    // 固定 48 bytes（含 3 bytes padding，保持 8 對齊）
    public static final int BYTES = 48;
    private static final byte VERSION = 1;

    public static void encode(TradeEvent e, byte[] buf, int off) {
        buf[off] = VERSION; off += 1;
        putInt(buf, off, e.symbolId()); off += 4;
        putLong(buf, off, e.tradeId()); off += 8;
        putLong(buf, off, e.userId());  off += 8;
        putLong(buf, off, Double.doubleToRawLongBits(e.price())); off += 8;
        putLong(buf, off, Double.doubleToRawLongBits(e.qty()));   off += 8;
        putLong(buf, off, e.tsNanos()); off += 8;
        // padding：3 bytes（此處不寫值也無妨，僅維持長度）
    }

    public static TradeEvent decode(byte[] buf, int off) {
        if (buf[off] != VERSION) throw new IllegalArgumentException("ver");
        off += 1;
        int  symId = getInt(buf, off); off += 4;
        long tid   = getLong(buf, off); off += 8;
        long uid   = getLong(buf, off); off += 8;
        double px  = Double.longBitsToDouble(getLong(buf, off)); off += 8;
        double qty = Double.longBitsToDouble(getLong(buf, off)); off += 8;
        long ts    = getLong(buf, off);
        return new TradeEvent(symId, tid, uid, px, qty, ts);
    }

    // 零物件：覆寫可變視圖
    public static void decodeInto(byte[] buf, int off, TradeEventView out) {
        if (buf[off] != VERSION) throw new IllegalArgumentException("ver");
        off += 1;
        out.symbolId = getInt(buf, off); off += 4;
        out.tradeId  = getLong(buf, off); off += 8;
        out.userId   = getLong(buf, off); off += 8;
        out.price    = Double.longBitsToDouble(getLong(buf, off)); off += 8;
        out.qty      = Double.longBitsToDouble(getLong(buf, off)); off += 8;
        out.tsNanos  = getLong(buf, off);
    }

    public static final class TradeEventView {
        public int symbolId;
        public long tradeId, userId, tsNanos;
        public double price, qty;
    }

    // ----- primitives (little-endian) -----
    private static void putInt(byte[] b, int o, int v){
        b[o  ]=(byte)v; b[o+1]=(byte)(v>>>8); b[o+2]=(byte)(v>>>16); b[o+3]=(byte)(v>>>24);
    }
    private static int getInt(byte[] b, int o){
        return (b[o]&0xff) | ((b[o+1]&0xff)<<8) | ((b[o+2]&0xff)<<16) | ((b[o+3]&0xff)<<24);
    }
    private static void putLong(byte[] b, int o, long v){
        b[o]=(byte)v; b[o+1]=(byte)(v>>>8); b[o+2]=(byte)(v>>>16); b[o+3]=(byte)(v>>>24);
        b[o+4]=(byte)(v>>>32); b[o+5]=(byte)(v>>>40); b[o+6]=(byte)(v>>>48); b[o+7]=(byte)(v>>>56);
    }
    private static long getLong(byte[] b, int o){
        return ((long)b[o]&0xff) | (((long)b[o+1]&0xff)<<8) | (((long)b[o+2]&0xff)<<16) | (((long)b[o+3]&0xff)<<24)
                | (((long)b[o+4]&0xff)<<32) | (((long)b[o+5]&0xff)<<40) | (((long)b[o+6]&0xff)<<48) | (((long)b[o+7]&0xff)<<56);
    }
}
