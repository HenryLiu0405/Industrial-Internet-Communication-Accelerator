package com.air.airspeed.vpn;

import android.os.Environment;

//created by liu 2020-01-10

public interface VPNConstants {
    int BUFFER_SIZE = 2560;
    int MAX_PAYLOAD_SIZE = 2520;
    String BASE_DIR = Environment.getExternalStorageDirectory() + "/Airspeed/Conversation/";
    String DATA_DIR = BASE_DIR + "data/";
    String CONFIG_DIR=BASE_DIR+"config/";
    String VPN_SP_NAME="vpn_sp_name";
    String IS_UDP_NEED_SAVE="isUDPNeedSave";
    String IS_UDP_SHOW = "isUDPShow";
    String DEFAULT_PACKAGE_ID = "default_package_id";
    String DEFAULT_PACAGE_NAME = "default_package_name";
}
