package com.tyyd.framework.dat.ec;

import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

/**
 * 事件中心接口
 *
 */
@SPI(key = SpiExtensionKey.EVENT_CENTER, dftValue = "injvm")
public interface EventCenter {

    /**
     * 订阅主题
     */
    public void subscribe(EventSubscriber subscriber, String... topics);

    /**
     * 取消订阅主题
     */
    public void unSubscribe(String topic, EventSubscriber subscriber);

    /**
     * 同步发布主题消息
     */
    public void publishSync(EventInfo eventInfo);

    /**
     * 异步发送主题消息
     */
    public void publishAsync(EventInfo eventInfo);

}
