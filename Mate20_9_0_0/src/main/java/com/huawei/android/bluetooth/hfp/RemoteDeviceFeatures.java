package com.huawei.android.bluetooth.hfp;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.util.HashMap;

public class RemoteDeviceFeatures {
    private static final int BRSF_AG_CODEC_NEGOTIATION = 512;
    private static final int BRSF_AG_EC_NR = 2;
    private static final int BRSF_AG_ENHANCED_CALL_CONTROL = 128;
    private static final int BRSF_AG_ENHANCED_CALL_STATUS = 64;
    private static final int BRSF_AG_ENHANCED_ERR_RESULT_CODES = 256;
    private static final int BRSF_AG_IN_BAND_RING = 8;
    private static final int BRSF_AG_REJECT_CALL = 32;
    private static final int BRSF_AG_THREE_WAY_CALLING = 1;
    private static final int BRSF_AG_VOICE_RECOG = 4;
    private static final int BRSF_AG_VOICE_TAG_NUMBE = 16;
    private static final int BRSF_HF_CLIP = 4;
    private static final int BRSF_HF_CODEC_NEGOTIATION = 128;
    private static final int BRSF_HF_CW_THREE_WAY_CALLING = 2;
    private static final int BRSF_HF_EC_NR = 1;
    private static final int BRSF_HF_ENHANCED_CALL_CONTROL = 64;
    private static final int BRSF_HF_ENHANCED_CALL_STATUS = 32;
    private static final int BRSF_HF_REMOTE_VOL_CONTROL = 16;
    private static final int BRSF_HF_VOICE_REG_ACT = 8;
    private static final boolean DBG = HWFLOW;
    protected static final boolean HWFLOW;
    private static final String TAG = "RemoteDeviceFeatures";
    private HashMap<BluetoothDevice, Integer> mHeadsetBrsf = new HashMap();

    private static native int getRemoteFeaturesNative(long j, byte[] bArr);

    static {
        boolean z = Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4));
        HWFLOW = z;
        if (DBG) {
            Log.d(TAG, "Loading bluetoothcustex_jni JNI Library");
        }
        System.loadLibrary("bluetoothex_jni");
    }

    public void add(long bluetoothInterface, BluetoothDevice device, byte[] address) {
        synchronized (this) {
            int remoteBrsf = getRemoteFeaturesNative(bluetoothInterface, address);
            if (DBG) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Remote Brsf: ");
                stringBuilder.append(remoteBrsf);
                stringBuilder.append(" for device: ");
                stringBuilder.append(getFormatMacAddress(device));
                Log.d(str, stringBuilder.toString());
            }
            this.mHeadsetBrsf.put(device, Integer.valueOf(remoteBrsf));
        }
    }

    private String getFormatMacAddress(BluetoothDevice device) {
        if (device == null || device.getAddress() == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append(null);
            return stringBuilder.toString();
        }
        String address = device.getAddress();
        int len = address.length();
        String substring = address.substring(len / 2, len);
        String result = new StringBuilder();
        result.append("******");
        result.append(substring);
        return result.toString();
    }

    public void remove(BluetoothDevice device, String state) {
        synchronized (this) {
            this.mHeadsetBrsf.remove(device);
        }
    }

    public void clear() {
        synchronized (this) {
            if (this.mHeadsetBrsf != null) {
                this.mHeadsetBrsf.clear();
            }
        }
    }

    public boolean isVoiceRecognitionSupported(BluetoothDevice device) {
        int remoteBrsf = 0;
        synchronized (this) {
            Integer tempRemoteBrsf = (Integer) this.mHeadsetBrsf.get(device);
            if (tempRemoteBrsf != null) {
                remoteBrsf = tempRemoteBrsf.intValue();
            }
        }
        if (DBG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("isBluetoothVoiceDialingEnabled mRemoteBrsf: ");
            stringBuilder.append(remoteBrsf);
            stringBuilder.append(" device: ");
            stringBuilder.append(getFormatMacAddress(device));
            stringBuilder.append(" supported: ");
            stringBuilder.append(remoteBrsf & 8);
            Log.d(str, stringBuilder.toString());
        }
        return (remoteBrsf & 8) != 0;
    }
}
