/* Copyright (C) 2004   Versant Inc.   http://www.db4o.com */
package com.db4o.ext;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * a unique universal identify for an object. <br><br>The db4o UUID consists of
 * two parts:<br> - an indexed long for fast access,<br> - the signature of the
 * {@link com.db4o.ObjectContainer ObjectContainer} the object was created with.
 * <br><br>Db4oUUIDs are valid representations of objects over multiple
 * ObjectContainers
 */
public class Db4oUUID {

    private final long longPart;
    private final byte[] signaturePart;

    /**
     * constructs a Db4oUUID from a long part and a signature part
     *
     * @param longPart_ the long part
     * @param signaturePart_ the signature part
     */
    public Db4oUUID(long longPart_, byte[] signaturePart_) {
        longPart = longPart_;
        signaturePart = signaturePart_;
    }

    /**
     * returns the long part of this UUID. <br><br>To uniquely identify an
     * object universally, db4o uses an indexed long and a reference to the
     * Db4oDatabase object it was created on.
     *
     * @return the long part of this UUID.
     */
    public long getLongPart() {
        return longPart;
    }

    /**
     * returns the signature part of this UUID. <br><br> <br><br>To uniquely
     * identify an object universally, db4o uses an indexed long and a reference
     * to the Db4oDatabase singleton object of the {@link
     * com.db4o.ObjectContainer ObjectContainer} it was created on. This method
     * returns the signature of the Db4oDatabase object of the ObjectContainer:
     * the signature of the origin ObjectContainer.
     *
     * @return the signature of the Db4oDatabase for this UUID.
     */
    public byte[] getSignaturePart() {
        return signaturePart;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Db4oUUID other = (Db4oUUID) o;

        if (longPart != other.longPart) {
            return false;
        }
        if (signaturePart == null) {
            return other.signaturePart == null;
        }
        if (signaturePart.length != other.signaturePart.length) {
            return false;
        }
        for (int i = 0; i < signaturePart.length; i++) {
            if (signaturePart[i] != other.signaturePart[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return (int) (longPart ^ (longPart >>> 32));
    }

    public final byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + signaturePart.length + Byte.BYTES * 2);

        buffer.put((byte) 100);
        buffer.putLong(longPart);
        buffer.put(signaturePart);
        buffer.put((byte) 100);

        return buffer.array();
    }

    public final Db4oUUID fromBytes(byte[] bs) {
        ByteBuffer buffer = ByteBuffer.wrap(bs);
        buffer.get();
        long lp = buffer.getLong();
        int s = bs.length - Long.BYTES - Byte.BYTES * 2;
        byte[] ss = new byte[s];
        buffer.get(ss);
        return new Db4oUUID(lp, ss);
    }

    @Override
    public String toString() {
        return new BigInteger(toBytes()).toString(Character.MAX_RADIX);
    }

    public Db4oUUID(String str) {
        BigInteger bi = new BigInteger(str, Character.MAX_RADIX);
        byte[] bs = bi.toByteArray();
        Db4oUUID t = fromBytes(bs);
        longPart = t.longPart;
        signaturePart = t.signaturePart;
    }

    private String toString2() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(" sig: ");
        for (int i = 0; i < signaturePart.length; i++) {
            char c = (char) signaturePart[i];
            sb.append(c);
        }
        sb.append(" long: ");
        sb.append(longPart);
        return sb.toString();
    }

}
