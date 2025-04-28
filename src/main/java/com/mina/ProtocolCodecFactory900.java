package com.mina;


import com.hub900.HubManager;
import com.hub900.callback.AckBackCallback;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

import java.nio.ByteOrder;

public class ProtocolCodecFactory900 implements ProtocolCodecFactory {

    private ProtocolDecoder900 decoder;

    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return null;
    }

    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return this.decoder;
    }

    public ProtocolCodecFactory900(HubManager manager) {
        decoder = new ProtocolDecoder900(manager);
    }

    public ProtocolCodecFactory900() {
    }

    private static class ProtocolDecoder900 extends ProtocolDecoderAdapter {

        private HubManager manager;

        @Override
        public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) {
            in.order(ByteOrder.LITTLE_ENDIAN);
            String[] receivedMessageArray = in.getHexDump().split(" ");
            StringBuffer stringBuffer = new StringBuffer();
            if (receivedMessageArray != null && receivedMessageArray.length > 0) {
                int length = receivedMessageArray.length;
                for (int i = 0; i < length; i++) {
                    stringBuffer.append(receivedMessageArray[i]);
                }
            }
            byte[] msg = hexString2Bytes(stringBuffer.toString());
            try {
                manager.onDataReceived(msg, new AckBackCallback() {
                    @Override
                    public void onAckBack(byte[] bytes) {
                        out.write(bytes);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            in.position(in.limit());
        }

        public ProtocolDecoder900(HubManager manager) {
            this.manager = manager;
        }

        public ProtocolDecoder900() {
        }

        /**
         * 16进制字符串转字节数组
         *
         * @param hexString
         * @return
         */
        public static byte[] hexString2Bytes(String hexString) {
            if (hexString == null || hexString.equals("")) {
                return new byte[0];
            }
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }
            return d;
        }

        public static byte charToByte(char c) {
            return (byte) "0123456789ABCDEF".indexOf(c);
        }


    }
}
