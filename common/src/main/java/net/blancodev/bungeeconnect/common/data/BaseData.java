package net.blancodev.bungeeconnect.common.data;

import lombok.Getter;

@Getter
public class BaseData {

    private final long timestamp;

    public BaseData(long timestamp) {
        this.timestamp = timestamp;
    }

}
