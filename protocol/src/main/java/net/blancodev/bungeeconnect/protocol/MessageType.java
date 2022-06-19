package net.blancodev.bungeeconnect.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.blancodev.bungeeconnect.protocol.data.BootData;
import net.blancodev.bungeeconnect.protocol.data.KeepAliveData;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    KEEP_ALIVE(KeepAliveData.class),
    BOOT(BootData.class);

    private final Class<?> dataClass;

}
