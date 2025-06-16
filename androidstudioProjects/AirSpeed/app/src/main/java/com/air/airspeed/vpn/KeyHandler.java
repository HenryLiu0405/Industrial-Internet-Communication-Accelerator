package com.air.airspeed.vpn;

import java.nio.channels.SelectionKey;

//created by liu 2020-01-10

public interface KeyHandler {
    void onKeyReady(SelectionKey key);
}
