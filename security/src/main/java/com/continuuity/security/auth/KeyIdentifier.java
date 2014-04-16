package com.continuuity.security.auth;

import com.continuuity.internal.io.Schema;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Map;

/**
 * Represents a secret key to use for message signing, plus a unique random number identifying it.
 */
public final class KeyIdentifier {
  private SecretKey key;
  private final byte[] encodedKey;
  private final String algorithm;
  private final int keyId;

  static final class Schemas {
    private static final int VERSION = 1;
    private static final Map<Integer, Schema> schemas = Maps.newHashMap();
    static {
      schemas.put(1, Schema.recordOf("KeyIdentifier",
                                     Schema.Field.of("algorithm", Schema.of(Schema.Type.STRING)),
                                     Schema.Field.of("encodedKey", Schema.of(Schema.Type.BYTES)),
                                     Schema.Field.of("keyId", Schema.of(Schema.Type.INT))));
    }

    public static int getVersion() {
      return VERSION;
    }

    public static Schema getSchemaVersion(int version) {
      return schemas.get(version);
    }

    public static Schema getCurrentSchema() {
      return schemas.get(VERSION);
    }
  }

  public KeyIdentifier(SecretKey key, int id) {
    this.encodedKey = key.getEncoded();
    this.algorithm = key.getAlgorithm();
    this.key = key;
    this.keyId = id;
  }

  public SecretKey getKey() {
    if (key == null && encodedKey != null) {
      key = new SecretKeySpec(encodedKey, algorithm);
    }
    return key;
  }

  public int getKeyId() {
    return keyId;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof KeyIdentifier) {
      KeyIdentifier other = (KeyIdentifier) object;
      return Arrays.equals(encodedKey, other.encodedKey) &&
        keyId == other.keyId && algorithm.equals(other.algorithm)
        && Objects.equal(getKey(), other.getKey());
    }
    return false;
  }
}
