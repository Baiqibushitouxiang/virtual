package com.sustbbgz.virtualspringbootbackend.opcua.namespace;

import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilter;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilterContext;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class TemperatureVariableNode extends UaVariableNode {

    private static final Logger logger = LoggerFactory.getLogger(TemperatureVariableNode.class);

    private final String deviceId;
    private final BiConsumer<String, Double> temperatureDataCallback;

    public TemperatureVariableNode(
        UaNodeContext context,
        NodeId nodeId,
        QualifiedName browseName,
        LocalizedText displayName,
        String deviceId,
        BiConsumer<String, Double> temperatureDataCallback
    ) {
        super(context, nodeId, browseName, displayName);
        this.deviceId = deviceId;
        this.temperatureDataCallback = temperatureDataCallback;
        
        UByte accessLevel = UByte.valueOf(3);
        setAccessLevel(accessLevel);
        setUserAccessLevel(accessLevel);
        
        getFilterChain().addLast(new AttributeFilter() {
            @Override
            public void setAttribute(
                AttributeFilterContext.SetAttributeContext ctx,
                AttributeId attributeId,
                Object value
            ) {
                ctx.setAttribute(attributeId, value);
                
                if (attributeId == AttributeId.Value && temperatureDataCallback != null && value instanceof DataValue) {
                    try {
                        DataValue dataValue = (DataValue) value;
                        Variant variant = dataValue.getValue();
                        if (variant != null && variant.getValue() instanceof Number) {
                            Double temperature = ((Number) variant.getValue()).doubleValue();
                            temperatureDataCallback.accept(deviceId, temperature);
                            logger.debug("Temperature data persisted: deviceId={}, value={}", deviceId, temperature);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to persist temperature data: {}", e.getMessage());
                    }
                }
            }
        });
    }
}
