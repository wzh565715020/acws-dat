package com.tyyd.framework.dat.ec.injvm;

import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.ec.EventCenter;
import com.tyyd.framework.dat.ec.EventInfo;
import com.tyyd.framework.dat.ec.EventSubscriber;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 在一个jvm中的pub sub 简易实现
 *
 */
public class InjvmEventCenter implements EventCenter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCenter.class.getName());

    private final ConcurrentHashMap<String, Set<EventSubscriber>> ecMap =
            new ConcurrentHashMap<String, Set<EventSubscriber>>();

    private final ExecutorService executor = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 2, new NamedThreadFactory("DAT-InjvmEventCenter-Executor", true));

    public void subscribe(EventSubscriber subscriber, String... topics) {
        for (String topic : topics) {
            Set<EventSubscriber> subscribers = ecMap.get(topic);
            if (subscribers == null) {
                subscribers = new ConcurrentHashSet<EventSubscriber>();
                Set<EventSubscriber> oldSubscribers = ecMap.putIfAbsent(topic, subscribers);
                if (oldSubscribers != null) {
                    subscribers = oldSubscribers;
                }
            }
            subscribers.add(subscriber);
        }
    }

    public void unSubscribe(String topic, EventSubscriber subscriber) {
        Set<EventSubscriber> subscribers = ecMap.get(topic);
        if (subscribers != null) {
            for (EventSubscriber eventSubscriber : subscribers) {
                if (eventSubscriber.getId().equals(subscriber.getId())) {
                    subscribers.remove(eventSubscriber);
                }
            }
        }
    }

    public void publishSync(EventInfo eventInfo) {
        Set<EventSubscriber> subscribers = ecMap.get(eventInfo.getTopic());
        if (subscribers != null) {
            for (EventSubscriber subscriber : subscribers) {
                eventInfo.setTopic(eventInfo.getTopic());
                try {
                    subscriber.getObserver().onObserved(eventInfo);
                } catch (Throwable t) {      // 防御性容错
                    LOGGER.error(" eventInfo:{}, subscriber:{}",
                            JSON.toJSONString(eventInfo), JSON.toJSONString(subscriber), t);
                }
            }
        }
    }

    public void publishAsync(final EventInfo eventInfo) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                String topic = eventInfo.getTopic();

                Set<EventSubscriber> subscribers = ecMap.get(topic);
                if (subscribers != null) {
                    for (EventSubscriber subscriber : subscribers) {
                        try {
                            eventInfo.setTopic(topic);
                            subscriber.getObserver().onObserved(eventInfo);
                        } catch (Throwable t) {     // 防御性容错
                            LOGGER.error(" eventInfo:{}, subscriber:{}",
                                    JSON.toJSONString(eventInfo), JSON.toJSONString(subscriber), t);
                        }
                    }
                }
            }
        });
    }
}
