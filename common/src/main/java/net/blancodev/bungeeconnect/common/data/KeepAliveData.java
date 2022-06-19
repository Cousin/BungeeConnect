package net.blancodev.bungeeconnect.common.data;

import lombok.Getter;

@Getter
public class KeepAliveData extends BaseData {

    public KeepAliveData(long timestamp) {
        super(timestamp);
    }

}
