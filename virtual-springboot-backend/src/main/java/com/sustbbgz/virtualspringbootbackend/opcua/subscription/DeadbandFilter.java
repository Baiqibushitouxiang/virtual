package com.sustbbgz.virtualspringbootbackend.opcua.subscription;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DeadbandFilter {

    private static final Logger logger = LoggerFactory.getLogger(DeadbandFilter.class);

    public enum DeadbandType {
        ABSOLUTE,
        PERCENT
    }

    public boolean shouldFilter(DataValue oldValue, DataValue newValue, 
                                double deadband, DeadbandType type) {
        if (deadband <= 0) {
            return false;
        }

        if (oldValue == null || newValue == null) {
            return false;
        }

        Variant oldVariant = oldValue.getValue();
        Variant newVariant = newValue.getValue();

        if (oldVariant == null || newVariant == null) {
            return false;
        }

        Object oldVal = oldVariant.getValue();
        Object newVal = newVariant.getValue();

        if (oldVal == null || newVal == null) {
            return false;
        }

        if (oldVal instanceof Number && newVal instanceof Number) {
            double oldNum = ((Number) oldVal).doubleValue();
            double newNum = ((Number) newVal).doubleValue();
            
            return shouldFilterNumeric(oldNum, newNum, deadband, type);
        }

        return false;
    }

    private boolean shouldFilterNumeric(double oldValue, double newValue, 
                                        double deadband, DeadbandType type) {
        double difference = Math.abs(newValue - oldValue);

        if (type == DeadbandType.ABSOLUTE) {
            return difference < deadband;
        } else if (type == DeadbandType.PERCENT) {
            if (oldValue == 0 && newValue == 0) {
                return true;
            }
            double range = Math.max(Math.abs(oldValue), Math.abs(newValue));
            if (range == 0) {
                return true;
            }
            double percentChange = (difference / range) * 100;
            return percentChange < deadband;
        }

        return false;
    }

    public FilterResult filter(DataValue oldValue, DataValue newValue,
                               double absoluteDeadband, double percentDeadband) {
        if (absoluteDeadband > 0) {
            if (shouldFilter(oldValue, newValue, absoluteDeadband, DeadbandType.ABSOLUTE)) {
                return new FilterResult(true, "绝对死区过滤");
            }
        }

        if (percentDeadband > 0) {
            if (shouldFilter(oldValue, newValue, percentDeadband, DeadbandType.PERCENT)) {
                return new FilterResult(true, "百分比死区过滤");
            }
        }

        return new FilterResult(false, null);
    }

    public static class FilterResult {
        private final boolean filtered;
        private final String reason;

        public FilterResult(boolean filtered, String reason) {
            this.filtered = filtered;
            this.reason = reason;
        }

        public boolean isFiltered() {
            return filtered;
        }

        public String getReason() {
            return reason;
        }
    }
}
