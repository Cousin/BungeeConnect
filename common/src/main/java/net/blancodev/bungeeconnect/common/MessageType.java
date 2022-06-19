package net.blancodev.bungeeconnect.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.blancodev.bungeeconnect.common.data.BootData;
import net.blancodev.bungeeconnect.common.data.KeepAliveData;

@Getter
@RequiredArgsConstructor
public enum MessageType {

    KEEP_ALIVE(KeepAliveData.class),
    BOOT(BootData.class);

    private final Class<?> dataClass;

}
