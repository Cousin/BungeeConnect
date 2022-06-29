package net.blancodev.bungeeconnect.spigot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Protocol {

    V1_7_R4("e"),
    V1_8_R1("f"),
    V1_8_R2("g"),
    V1_8_R3("g"),
    V1_9_R1("g"),
    V1_9_R2("g"),
    V1_10_R1("g"),
    V1_11_R1("g"),
    V1_12_R1("g"),
    V1_14_R1("f"),
    V1_15_R1("listeningChannels"),
    V1_16_R1("listeningChannels"),
    V1_17_R1("f");

    private final String channelFuturesField;

}
