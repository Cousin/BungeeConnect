package net.blancodev.bungeeconnect.common.protocol;

import lombok.Getter;

@Getter
public class BootData extends BaseData {

    public BootData(long timestamp) {
        super(timestamp);
    }

}
