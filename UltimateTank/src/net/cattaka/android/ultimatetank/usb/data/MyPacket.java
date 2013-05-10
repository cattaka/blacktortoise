
package net.cattaka.android.ultimatetank.usb.data;

import net.cattaka.libgeppa.data.IPacket;
import android.os.Parcel;
import android.os.Parcelable;

public class MyPacket implements IPacket, Parcelable {
    private static final long serialVersionUID = 1L;

    private OpCode opCode;

    private int dataLen;

    private byte[] data;

    private MyPacket() {
    }

    public MyPacket(OpCode opCode, int dataLen, byte[] data) {
        super();
        this.opCode = opCode;
        this.dataLen = dataLen;
        this.data = data;
    }

    public OpCode getOpCode() {
        return opCode;
    }

    public void setOpCode(OpCode opCode) {
        this.opCode = opCode;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(opCode.getValue());
        dest.writeInt(this.dataLen);
        dest.writeByteArray(data, 0, dataLen);
    }

    public static final Parcelable.Creator<MyPacket> CREATOR = new Parcelable.Creator<MyPacket>() {
        @Override
        public MyPacket[] newArray(int size) {
            return new MyPacket[size];
        }

        @Override
        public MyPacket createFromParcel(Parcel source) {
            MyPacket p = new MyPacket();
            p.opCode = OpCode.fromValue(source.readByte());
            p.dataLen = source.readInt();
            p.data = new byte[p.dataLen];
            source.readByteArray(p.data);
            return p;
        }
    };
}
