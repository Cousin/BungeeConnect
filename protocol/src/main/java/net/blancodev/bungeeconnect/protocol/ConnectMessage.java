package net.blancodev.bungeeconnect.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConnectMessage<Data> {

    private final MessageType type;
    private final Data data;

}
