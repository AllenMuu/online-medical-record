package com.ice.medicalrecord.api;

import com.ice.medicalrecord.api.dto.DashboardSummary;
import com.ice.medicalrecord.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘统计接口。
 * 返回首页概览需要的核心指标。
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 查询患者数、病历数、本月病历数和医生数。
     */
    @GetMapping("/summary")
    public DashboardSummary summary() {
        return dashboardService.summary();
    }
}
