package net.blancodev.bungeeconnect.common.protocol;

import lombok.Getter;

@Getter
public class KeepAliveData extends BaseData {

    public KeepAliveData(long timestamp) {
        super(timestamp);
    }

}
