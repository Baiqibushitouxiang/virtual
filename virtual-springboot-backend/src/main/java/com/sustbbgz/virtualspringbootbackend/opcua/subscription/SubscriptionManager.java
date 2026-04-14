package com.sustbbgz.virtualspringbootbackend.opcua.subscription;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.subscriptions.Subscription;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SubscriptionManager {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionManager.class);

    private static final double DEFAULT_PUBLISHING_INTERVAL = 1000.0;
    private static final int DEFAULT_MAX_NOTIFICATIONS = 100;
    private static final boolean DEFAULT_PUBLISHING_ENABLED = true;

    private final Map<Long, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> clientSubscriptions = new ConcurrentHashMap<>();

    private OpcUaServer opcUaServer;

    public void setOpcUaServer(OpcUaServer opcUaServer) {
        this.opcUaServer = opcUaServer;
    }

    public SubscriptionInfo createSubscription(String clientId, double publishingInterval) {
        if (opcUaServer == null) {
            throw new IllegalStateException("OPC UA Server 未初始化");
        }

        long subscriptionId = generateSubscriptionId();
        
        SubscriptionInfo info = new SubscriptionInfo(
            subscriptionId,
            clientId,
            publishingInterval > 0 ? publishingInterval : DEFAULT_PUBLISHING_INTERVAL,
            DEFAULT_MAX_NOTIFICATIONS,
            DEFAULT_PUBLISHING_ENABLED
        );

        subscriptions.put(subscriptionId, info);
        clientSubscriptions.computeIfAbsent(clientId, k -> new ArrayList<>()).add(subscriptionId);

        logger.info("创建订阅: subscriptionId={}, clientId={}, interval={}", 
            subscriptionId, clientId, info.getPublishingInterval());

        return info;
    }

    public void deleteSubscription(long subscriptionId) {
        SubscriptionInfo info = subscriptions.remove(subscriptionId);
        if (info != null) {
            List<Long> subs = clientSubscriptions.get(info.getClientId());
            if (subs != null) {
                subs.remove(Long.valueOf(subscriptionId));
            }
            logger.info("删除订阅: subscriptionId={}", subscriptionId);
        }
    }

    public void deleteSubscriptionsByClient(String clientId) {
        List<Long> subs = clientSubscriptions.remove(clientId);
        if (subs != null) {
            for (Long subId : subs) {
                subscriptions.remove(subId);
            }
            logger.info("删除客户端所有订阅: clientId={}, count={}", clientId, subs.size());
        }
    }

    public SubscriptionInfo getSubscription(long subscriptionId) {
        return subscriptions.get(subscriptionId);
    }

    public List<SubscriptionInfo> getSubscriptionsByClient(String clientId) {
        List<Long> subIds = clientSubscriptions.get(clientId);
        List<SubscriptionInfo> result = new ArrayList<>();
        if (subIds != null) {
            for (Long subId : subIds) {
                SubscriptionInfo info = subscriptions.get(subId);
                if (info != null) {
                    result.add(info);
                }
            }
        }
        return result;
    }

    public void updatePublishingInterval(long subscriptionId, double interval) {
        SubscriptionInfo info = subscriptions.get(subscriptionId);
        if (info != null) {
            info.setPublishingInterval(interval);
            logger.info("更新订阅发布间隔: subscriptionId={}, interval={}", subscriptionId, interval);
        }
    }

    public int getSubscriptionCount() {
        return subscriptions.size();
    }

    private long generateSubscriptionId() {
        return System.currentTimeMillis();
    }

    public static class SubscriptionInfo {
        private final long subscriptionId;
        private final String clientId;
        private double publishingInterval;
        private final int maxNotificationsPerPublish;
        private final boolean publishingEnabled;
        private long createdAt;

        public SubscriptionInfo(long subscriptionId, String clientId, double publishingInterval,
                               int maxNotificationsPerPublish, boolean publishingEnabled) {
            this.subscriptionId = subscriptionId;
            this.clientId = clientId;
            this.publishingInterval = publishingInterval;
            this.maxNotificationsPerPublish = maxNotificationsPerPublish;
            this.publishingEnabled = publishingEnabled;
            this.createdAt = System.currentTimeMillis();
        }

        public long getSubscriptionId() {
            return subscriptionId;
        }

        public String getClientId() {
            return clientId;
        }

        public double getPublishingInterval() {
            return publishingInterval;
        }

        public void setPublishingInterval(double publishingInterval) {
            this.publishingInterval = publishingInterval;
        }

        public int getMaxNotificationsPerPublish() {
            return maxNotificationsPerPublish;
        }

        public boolean isPublishingEnabled() {
            return publishingEnabled;
        }

        public long getCreatedAt() {
            return createdAt;
        }
    }
}
