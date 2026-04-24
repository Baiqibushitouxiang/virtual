package com.sustbbgz.virtualspringbootbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {

    @Select("SELECT * FROM alert_rule WHERE (enabled = 1 OR enabled IS NULL) AND device_code = #{deviceCode} AND data_type = #{dataType}")
    List<AlertRule> findEnabledRules(@Param("deviceCode") String deviceCode, @Param("dataType") String dataType);
}
