package com.sustbbgz.virtualspringbootbackend.controller;

import com.sustbbgz.virtualspringbootbackend.common.Result;
import com.sustbbgz.virtualspringbootbackend.entity.AlertRule;
import com.sustbbgz.virtualspringbootbackend.entity.User;
import com.sustbbgz.virtualspringbootbackend.service.AlertRuleService;
import com.sustbbgz.virtualspringbootbackend.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/alert-rules")
public class AlertRuleController {

    @Resource
    private AlertRuleService alertRuleService;

    @GetMapping
    public Result list() {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        return Result.success(alertRuleService.findRules(userId));
    }

    @PostMapping
    public Result create(@RequestBody AlertRule rule) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        return Result.success(alertRuleService.saveRule(rule, userId));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody AlertRule rule) {
        User currentUser = TokenUtils.getCurrentUser();
        Long userId = currentUser != null ? currentUser.getId() : null;
        rule.setId(id);
        return Result.success(alertRuleService.saveRule(rule, userId));
    }

    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer enabled) {
        AlertRule rule = alertRuleService.getById(id);
        if (rule == null) {
            return Result.error("告警规则不存在");
        }
        rule.setEnabled(enabled);
        return Result.success(alertRuleService.saveRule(rule, rule.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        alertRuleService.removeById(id);
        return Result.success();
    }
}
