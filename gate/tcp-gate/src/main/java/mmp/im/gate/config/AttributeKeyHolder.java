package mmp.im.gate.config;

import io.netty.util.AttributeKey;

import java.util.concurrent.atomic.AtomicLong;

public class AttributeKeyHolder {

    public static  AttributeKey<String> USER_ID = AttributeKey.valueOf("USER_ID");
    public static  AttributeKey<AtomicLong> SEQ = AttributeKey.valueOf("SEQ");

}