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

public class DeviceDataVariableNode extends UaVariableNode {

    private static final Logger logger = LoggerFactory.getLogger(DeviceDataVariableNode.class);

    private final String deviceId;
    private final BiConsumer<String, Object> dataWriteCallback;

    public DeviceDataVariableNode(
        UaNodeContext context,
        NodeId nodeId,
        QualifiedName browseName,
        LocalizedText displayName,
        String deviceId,
        BiConsumer<String, Object> dataWriteCallback
    ) {
        super(context, nodeId, browseName, displayName);
        this.deviceId = deviceId;
        this.dataWriteCallback = dataWriteCallback;
        
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
                
                if (attributeId == AttributeId.Value && dataWriteCallback != null && value instanceof DataValue) {
                    try {
                        DataValue dataValue = (DataValue) value;
                        Variant variant = dataValue.getValue();
                        if (variant != null && variant.getValue() != null) {
                            dataWriteCallback.accept(deviceId, variant.getValue());
                            logger.debug("Device data written: deviceId={}, value={}", deviceId, variant.getValue());
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process device data write: {}", e.getMessage());
                    }
                }
            }
        });
    }
}
