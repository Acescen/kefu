package com.kefu.admin.netty.protocol.response;

import com.kefu.admin.netty.protocol.Packet;
import com.kefu.admin.netty.protocol.command.Command;

import lombok.Data;

/**
 * @author jurui
 * @date 2020-04-21
 */
@Data
public class HeartBeatResponsePacket extends Packet {

    @Override
    public Short getCommand() {
        return Command.HEART_BEAT_RESPONSE;
    }
}
