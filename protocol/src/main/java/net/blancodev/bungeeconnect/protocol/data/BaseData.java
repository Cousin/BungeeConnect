package net.blancodev.bungeeconnect.protocol.data;

import lombok.Getter;

@Getter
public class BaseData {

    private final long timestamp;

    public BaseData(long timestamp) {
        this.timestamp = timestamp;
    }

}
