package com.sustbbgz.virtualspringbootbackend.opcua.subscription;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Service
public class DataChangeMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DataChangeMonitor.class);

    private final Map<String, List<DataChangeListener>> listeners = new ConcurrentHashMap<>();
    private final Map<String, DataValue> lastValues = new ConcurrentHashMap<>();

    public void addListener(String nodeId, DataChangeListener listener) {
        listeners.computeIfAbsent(nodeId, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.info("添加数据变化监听器: nodeId={}", nodeId);
    }

    public void removeListener(String nodeId, DataChangeListener listener) {
        List<DataChangeListener> nodeListeners = listeners.get(nodeId);
        if (nodeListeners != null) {
            nodeListeners.remove(listener);
            if (nodeListeners.isEmpty()) {
                listeners.remove(nodeId);
            }
        }
    }

    public void removeAllListeners(String nodeId) {
        listeners.remove(nodeId);
        lastValues.remove(nodeId);
        logger.info("移除所有监听器: nodeId={}", nodeId);
    }

    public void notifyDataChange(String nodeId, DataValue newValue) {
        DataValue oldValue = lastValues.put(nodeId, newValue);
        
        List<DataChangeListener> nodeListeners = listeners.get(nodeId);
        if (nodeListeners != null && !nodeListeners.isEmpty()) {
            DataChangeEvent event = new DataChangeEvent(nodeId, oldValue, newValue);
            
            for (DataChangeListener listener : nodeListeners) {
                try {
                    listener.onDataChange(event);
                } catch (Exception e) {
                    logger.error("数据变化监听器执行失败: nodeId={}", nodeId, e);
                }
            }
        }
    }

    public void notifyDataChange(String nodeId, Object value) {
        DataValue dataValue = new DataValue(new Variant(value));
        notifyDataChange(nodeId, dataValue);
    }

    public DataValue getLastValue(String nodeId) {
        return lastValues.get(nodeId);
    }

    public List<String> getMonitoredNodes() {
        return new ArrayList<>(listeners.keySet());
    }

    public int getListenerCount(String nodeId) {
        List<DataChangeListener> nodeListeners = listeners.get(nodeId);
        return nodeListeners != null ? nodeListeners.size() : 0;
    }

    public interface DataChangeListener {
        void onDataChange(DataChangeEvent event);
    }

    public static class DataChangeEvent {
        private final String nodeId;
        private final DataValue oldValue;
        private final DataValue newValue;
        private final long timestamp;

        public DataChangeEvent(String nodeId, DataValue oldValue, DataValue newValue) {
            this.nodeId = nodeId;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.timestamp = System.currentTimeMillis();
        }

        public String getNodeId() {
            return nodeId;
        }

        public DataValue getOldValue() {
            return oldValue;
        }

        public DataValue getNewValue() {
            return newValue;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean hasChanged() {
            if (oldValue == null || newValue == null) {
                return true;
            }
            return !oldValue.equals(newValue);
        }
    }
}
