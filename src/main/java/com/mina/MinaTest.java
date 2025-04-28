package com.mina;

import com.hub900.HubManager;
import com.hub900.callback.*;
import com.hub900.entity.*;

public class MinaTest {

    private static float mWheelCadence = -1;
    private static int mLastWheelEventTime = -1;
    private static long mLastWheelRevolutions = -1;

    public static void main(String[] args) {
        HubManager manager = HubManager.getInstance()
                .setIdleDuration(60000)
                .setIdleEnabled(true)
//                .setHeartBeatDataCallback(new HeartBeatDataCallback() {
//                    @Override
//                    public void onHeartBeatData(HeartBeatData data) {
//                        System.out.println("HeartBeatData:" + data.toString());
//                    }
//                })
                .setAntHeartRateDataCallback(new AntHeartRateDataCallback() {
                    @Override
                    public void onAntHeartRateData(AntHeartRateData data) {
//                        System.out.println("AntHeartRateData:" + data.toString());
                    }
                })
                .setAntCadenceDataCallback(new AntCadenceDataCallback() {
                    @Override
                    public void onAntCadenceData(AntCadenceDta data) {
//                        System.out.println("AntCadenceDta:" + data.toString());
                    }
                })
                .setAntSpeedDataCallback(new AntSpeedDataCallback() {
                    @Override
                    public void onAntSpeedData(AntSpeedData data) {
//                        System.out.println("AntSpeedData:" + data.toString());
                    }
                })
                .setBleSOSCallback(new BleSOSCallback() {
                    @Override
                    public void onBleSOS(BleHeartRateData data) {
                        System.out.println("BleSOSCallback:" + data.toString());
                    }
                })
                .setBleCadenceDataCallback(new BleCadenceDataCallback() {
                    @Override
                    public void onBleCadenceData(BleCadenceDta data) {
                        System.out.println("BleCadenceDta:" + data.toString());
                        onCadenceMeasurement(data.getWheel(), data.getTime());
                    }
                })
                .setBleHeartRateDataCallback(new BleHeartRateDataCallback() {
                    @Override
                    public void onBleHeartRateData(BleHeartRateData data) {
                        int oxygen = data.getOxygen();//0-no function //255-invalid value
                        if (oxygen == 0) {
                            System.out.println("onBleHeartRateData: Oxygen- No function");
                        } else if (oxygen == 255) {
                            System.out.println("onBleHeartRateData: Oxygen- Invalid value");
                        }
//                        System.out.println("onBleHeartRateData:" + data.toString());
                    }
                })
                .setBleBoxingDataCallback(new BleBoxingDataCallback() {
                    @Override
                    public void onBleBoxingData(BleBoxingData data) {
//                        System.out.println("onBleBoxingData:" + data.toString());
                    }
                })
                .setBleBoxingHeartRateDataCallback(new BleBoxingHeartRateDataCallback() {
                    @Override
                    public void onBleBoxingHeartRateData(BleBoxingHeartRateData data) {
//                        System.out.println("onBleBoxingHeartRateData:" + data.toString());
                    }
                })
                .setDataIdleCallback(new DataIdleCallback() {
                    @Override
                    public void onDataIdle() {
                        System.out.println("onDataIdle");
                    }
                });
        InitHub900.getInstance().startServer(manager, 9000);
    }

    public static void onCadenceMeasurement(final long wheelRevolutions, final int lastWheelEventTime) {
        if (mLastWheelEventTime == lastWheelEventTime)
            return;
        if (mLastWheelRevolutions >= 0) {
            float timeDifference;
            if (lastWheelEventTime < mLastWheelEventTime) {
                timeDifference = (65535 + lastWheelEventTime - mLastWheelEventTime) / 1024.0f;
            } else {
                timeDifference = (lastWheelEventTime - mLastWheelEventTime) / 1024.0f;
            }
            mWheelCadence = (wheelRevolutions - mLastWheelRevolutions) * 60.0f / timeDifference;
            System.out.println(mWheelCadence + " RPM");
        }
        mLastWheelRevolutions = wheelRevolutions;
        mLastWheelEventTime = lastWheelEventTime;
    }
}
