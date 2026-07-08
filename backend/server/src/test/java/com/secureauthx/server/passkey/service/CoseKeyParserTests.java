package com.secureauthx.server.passkey.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.PublicKey;
import org.junit.jupiter.api.Test;

class CoseKeyParserTests {

    @Test
    void parseEc2P256Key() {
        byte[] coseKey = buildEc2CoseKey();
        PublicKey publicKey = CoseKeyParser.parsePublicKey(coseKey);
        assertThat(publicKey).isNotNull();
        assertThat(publicKey.getAlgorithm()).isEqualTo("EC");
    }

    @Test
    void parseRsaKey() {
        byte[] coseKey = buildRsaCoseKey();
        PublicKey publicKey = CoseKeyParser.parsePublicKey(coseKey);
        assertThat(publicKey).isNotNull();
        assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
    }

    private byte[] buildEc2CoseKey() {
        byte[] x = new byte[32];
        byte[] y = new byte[32];
        x[31] = 1;
        y[31] = 2;
        return encodeCoseEc2Key(x, y, 1, -7);
    }

    private byte[] buildRsaCoseKey() {
        byte[] n = new byte[256];
        n[0] = 1;
        byte[] e = new byte[]{0x01, 0x00, 0x01};
        return encodeCoseRsaKey(n, e, -257);
    }

    private byte[] encodeCoseEc2Key(byte[] x, byte[] y, long crv, long alg) {
        int mapEntries = 5;
        byte[] result = new byte[1024];
        int pos = 0;
        result[pos++] = (byte) (0xA0 | mapEntries);

        pos = writeCborInt(result, pos, 1);
        pos = writeCborInt(result, pos, 2);

        pos = writeCborInt(result, pos, 3);
        pos = writeCborNegInt(result, pos, alg);

        pos = writeCborInt(result, pos, -1);
        pos = writeCborInt(result, pos, crv);

        pos = writeCborInt(result, pos, -2);
        pos = writeCborByteString(result, pos, x);

        pos = writeCborInt(result, pos, -3);
        pos = writeCborByteString(result, pos, y);

        byte[] trimmed = new byte[pos];
        System.arraycopy(result, 0, trimmed, 0, pos);
        return trimmed;
    }

    private byte[] encodeCoseRsaKey(byte[] n, byte[] e, long alg) {
        int mapEntries = 4;
        byte[] result = new byte[2048];
        int pos = 0;
        result[pos++] = (byte) (0xA0 | mapEntries);

        pos = writeCborInt(result, pos, 1);
        pos = writeCborInt(result, pos, 3);

        pos = writeCborInt(result, pos, 3);
        pos = writeCborNegInt(result, pos, alg);

        pos = writeCborInt(result, pos, -1);
        pos = writeCborByteString(result, pos, n);

        pos = writeCborInt(result, pos, -2);
        pos = writeCborByteString(result, pos, e);

        byte[] trimmed = new byte[pos];
        System.arraycopy(result, 0, trimmed, 0, pos);
        return trimmed;
    }

    private int writeCborInt(byte[] buf, int pos, long value) {
        if (value >= 0) {
            if (value <= 23) {
                buf[pos++] = (byte) value;
            } else if (value <= 0xFF) {
                buf[pos++] = 0x18;
                buf[pos++] = (byte) value;
            } else if (value <= 0xFFFF) {
                buf[pos++] = 0x19;
                buf[pos++] = (byte) (value >> 8);
                buf[pos++] = (byte) value;
            } else {
                buf[pos++] = 0x1A;
                buf[pos++] = (byte) (value >> 24);
                buf[pos++] = (byte) (value >> 16);
                buf[pos++] = (byte) (value >> 8);
                buf[pos++] = (byte) value;
            }
        } else {
            long neg = -(value + 1);
            if (neg <= 23) {
                buf[pos++] = (byte) (0x20 | neg);
            } else if (neg <= 0xFF) {
                buf[pos++] = (byte) 0x38;
                buf[pos++] = (byte) neg;
            } else if (neg <= 0xFFFF) {
                buf[pos++] = (byte) 0x39;
                buf[pos++] = (byte) (neg >> 8);
                buf[pos++] = (byte) neg;
            } else {
                buf[pos++] = (byte) 0x3A;
                buf[pos++] = (byte) (neg >> 24);
                buf[pos++] = (byte) (neg >> 16);
                buf[pos++] = (byte) (neg >> 8);
                buf[pos++] = (byte) neg;
            }
        }
        return pos;
    }

    private int writeCborByteString(byte[] buf, int pos, byte[] data) {
        long len = data.length;
        if (len <= 23) {
            buf[pos++] = (byte) (0x40 | len);
        } else if (len <= 0xFF) {
            buf[pos++] = (byte) 0x58;
            buf[pos++] = (byte) len;
        } else if (len <= 0xFFFF) {
            buf[pos++] = (byte) 0x59;
            buf[pos++] = (byte) (len >> 8);
            buf[pos++] = (byte) len;
        } else {
            buf[pos++] = (byte) 0x5A;
            buf[pos++] = (byte) (len >> 24);
            buf[pos++] = (byte) (len >> 16);
            buf[pos++] = (byte) (len >> 8);
            buf[pos++] = (byte) len;
        }
        System.arraycopy(data, 0, buf, pos, data.length);
        return pos + data.length;
    }

    private int writeCborNegInt(byte[] buf, int pos, long value) {
        long encoded = -1 - value;
        if (encoded <= 23) {
            buf[pos++] = (byte) (0x20 | encoded);
        } else if (encoded <= 0xFF) {
            buf[pos++] = (byte) 0x38;
            buf[pos++] = (byte) encoded;
        } else if (encoded <= 0xFFFF) {
            buf[pos++] = (byte) 0x39;
            buf[pos++] = (byte) (encoded >> 8);
            buf[pos++] = (byte) encoded;
        } else {
            buf[pos++] = (byte) 0x3A;
            buf[pos++] = (byte) (encoded >> 24);
            buf[pos++] = (byte) (encoded >> 16);
            buf[pos++] = (byte) (encoded >> 8);
            buf[pos++] = (byte) encoded;
        }
        return pos;
    }
}
