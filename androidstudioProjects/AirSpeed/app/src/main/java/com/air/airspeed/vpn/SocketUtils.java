package com.air.airspeed.vpn;

import android.util.Log;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.Random;

//created by liu 2020-01-10

public class SocketUtils {
    private static String TAG = SocketUtils.class.getSimpleName();

    private static Random random;

    private SocketUtils() {

    }


    public static void closeResources(Closeable... resources) {
        for (Closeable resource : resources) {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (Exception e) {
                Log.d(TAG, "failed to close resource error is:" + e.getMessage());
            }
        }
    }


    public static long getRandomSequence() {
        return getRandom().nextInt(Short.MAX_VALUE + 1);
    }

    private static Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    public static ByteBuffer getByteBuffer() {
        return ByteBuffer.allocate(VPNConstants.BUFFER_SIZE);
    }


    public static int getUid(int port) {
        return 0;
    }
}
