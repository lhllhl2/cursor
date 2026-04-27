package com.jasolar.mis.framework.websocket.core.message;

import com.jasolar.mis.framework.websocket.core.listener.WebSocketMessageListener;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * JSON 格式的 WebSocket 消息帧
 *
 * @author zhaohuang
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class JsonWebSocketMessage implements Serializable {

    /**
     * 消息类型
     *
     * 目的：用于分发到对应的 {@link WebSocketMessageListener} 实现类
     */
    private String type;
    /**
     * 消息内容
     *
     * 要求 JSON 对象
     */
    private String content;

}
