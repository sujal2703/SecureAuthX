package com.secureauthx.server.passkey.service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

public final class CoseKeyParser {

    private CoseKeyParser() {}

    public static PublicKey parsePublicKey(byte[] coseKey) {
        CborMap map = CborDecoder.decodeMap(coseKey);
        long kty = map.getInt(1);

        if (kty == 2) {
            return parseEc2Key(map);
        } else if (kty == 3) {
            return parseRsaKey(map);
        }
        throw new IllegalArgumentException("Unsupported COSE key type: " + kty);
    }

    private static PublicKey parseEc2Key(CborMap map) {
        BigInteger x = new BigInteger(1, map.getBytes(-2));
        BigInteger y = new BigInteger(1, map.getBytes(-3));

        ECParameterSpec params;
        long crv = map.containsKey(-1) ? map.getInt(-1) : 1;
        if (crv == 1) {
            params = EccCurves.P256;
        } else if (crv == 2) {
            params = EccCurves.P384;
        } else if (crv == 3) {
            params = EccCurves.P521;
        } else {
            throw new IllegalArgumentException("Unsupported EC curve: " + crv);
        }

        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(new ECPublicKeySpec(new ECPoint(x, y), params));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse EC public key.", e);
        }
    }

    private static PublicKey parseRsaKey(CborMap map) {
        BigInteger n = new BigInteger(1, map.getBytes(-1));
        BigInteger e;
        if (map.containsKey(-2)) {
            e = new BigInteger(1, map.getBytes(-2));
        } else {
            e = BigInteger.valueOf(65537);
        }
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new RSAPublicKeySpec(n, e));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse RSA public key.", ex);
        }
    }

    private static class CborMap {
        private final long[] intKeys;
        private final long[] intValues;
        private final long[] bytesKeys;
        private final byte[][] bytesValues;

        CborMap(long[] intKeys, long[] intValues, long[] bytesKeys, byte[][] bytesValues) {
            this.intKeys = intKeys;
            this.intValues = intValues;
            this.bytesKeys = bytesKeys;
            this.bytesValues = bytesValues;
        }

        long getInt(long key) {
            for (int i = 0; i < intKeys.length; i++) {
                if (intKeys[i] == key) return intValues[i];
            }
            throw new IllegalArgumentException("Key not found: " + key);
        }

        byte[] getBytes(long key) {
            for (int i = 0; i < bytesKeys.length; i++) {
                if (bytesKeys[i] == key) return bytesValues[i];
            }
            throw new IllegalArgumentException("Key not found: " + key);
        }

        boolean containsKey(long key) {
            for (long k : intKeys) if (k == key) return true;
            for (long k : bytesKeys) if (k == key) return true;
            return false;
        }
    }

    private static class CborDecoder {
        private final byte[] data;
        private int offset;

        CborDecoder(byte[] data) {
            this.data = data;
            this.offset = 0;
        }

        static CborMap decodeMap(byte[] data) {
            CborDecoder decoder = new CborDecoder(data);
            int initialByte = decoder.data[decoder.offset++] & 0xFF;
            int majorType = initialByte >> 5;
            if (majorType != 5) {
                throw new IllegalArgumentException("Expected CBOR map, got major type: " + majorType);
            }
            long count = decoder.readLength(initialByte & 0x1F);

            long[] intKeys = new long[(int) count];
            long[] intValues = new long[(int) count];
            long[] bytesKeys = new long[(int) count];
            byte[][] bytesValues = new byte[(int) count][];
            int intIdx = 0;
            int bytesIdx = 0;

            for (long i = 0; i < count; i++) {
                long key = decoder.readIntValue();
                int keyMajorType = decoder.peekMajorType();
                if (keyMajorType == 0 || keyMajorType == 1) {
                    long value = decoder.readIntValue();
                    intKeys[intIdx] = key;
                    intValues[intIdx] = value;
                    intIdx++;
                } else if (keyMajorType == 2) {
                    byte[] value = decoder.readBytesValue();
                    bytesKeys[bytesIdx] = key;
                    bytesValues[bytesIdx] = value;
                    bytesIdx++;
                } else {
                    throw new IllegalArgumentException("Unsupported CBOR major type for value: " + keyMajorType);
                }
            }

            return new CborMap(
                    Arrays.copyOf(intKeys, intIdx),
                    Arrays.copyOf(intValues, intIdx),
                    Arrays.copyOf(bytesKeys, bytesIdx),
                    Arrays.copyOf(bytesValues, bytesIdx)
            );
        }

        private int peekMajorType() {
            return (data[offset] & 0xFF) >> 5;
        }

        private long readIntValue() {
            int initialByte = data[offset++] & 0xFF;
            int majorType = initialByte >> 5;
            int additionalInfo = initialByte & 0x1F;
            if (majorType == 0) {
                return readLength(additionalInfo);
            } else if (majorType == 1) {
                long value = readLength(additionalInfo);
                return ~value;
            }
            throw new IllegalArgumentException("Expected CBOR integer (major type 0/1), got: " + majorType);
        }

        private byte[] readBytesValue() {
            int initialByte = data[offset++] & 0xFF;
            int majorType = initialByte >> 5;
            if (majorType != 2) {
                throw new IllegalArgumentException("Expected CBOR byte string (major type 2), got: " + majorType);
            }
            long length = readLength(initialByte & 0x1F);
            byte[] result = Arrays.copyOfRange(data, offset, offset + (int) length);
            offset += (int) length;
            return result;
        }

        private long readLength(int additionalInfo) {
            if (additionalInfo <= 23) return additionalInfo;
            if (additionalInfo == 24) return data[offset++] & 0xFF;
            if (additionalInfo == 25) {
                return ((data[offset++] & 0xFF) << 8) | (data[offset++] & 0xFF);
            }
            if (additionalInfo == 26) {
                return ((long)(data[offset++] & 0xFF) << 24)
                        | ((long)(data[offset++] & 0xFF) << 16)
                        | ((long)(data[offset++] & 0xFF) << 8)
                        | ((long)(data[offset++] & 0xFF));
            }
            if (additionalInfo == 27) {
                long value = 0;
                for (int i = 0; i < 8; i++) value = (value << 8) | (data[offset++] & 0xFF);
                return value;
            }
            throw new IllegalArgumentException("Unsupported CBOR additional info: " + additionalInfo);
        }
    }

    private static final class EccCurves {
        static final ECParameterSpec P256 = createSpec("secp256r1");
        static final ECParameterSpec P384 = createSpec("secp384r1");
        static final ECParameterSpec P521 = createSpec("secp521r1");

        private static ECParameterSpec createSpec(String name) {
            try {
                java.security.AlgorithmParameters params =
                        java.security.AlgorithmParameters.getInstance("EC");
                params.init(new java.security.spec.ECGenParameterSpec(name));
                return params.getParameterSpec(ECParameterSpec.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create EC curve: " + name, e);
            }
        }
    }
}
