package net.blancodev.bungeeconnect.protocol.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class KeepAliveData extends BaseData {

    public KeepAliveData(long timestamp) {
        super(timestamp);
    }

}
