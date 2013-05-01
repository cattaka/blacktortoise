
package net.cattaka.android.ultimatetank.net.data;

public enum OpCode {
    ECHO((byte)0), //
    MOVE((byte)1), //
    HEAD((byte)2), //
    UNKNOWN((byte)-1);

    private byte value;

    private OpCode(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static OpCode fromValue(byte value) {
        for (OpCode oc : values()) {
            if (oc.getValue() == value) {
                return oc;
            }
        }
        return UNKNOWN;
    }

}
