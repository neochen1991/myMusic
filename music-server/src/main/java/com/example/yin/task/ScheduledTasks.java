package com.example.yin.task;

import com.example.yin.service.AdminService;
import com.example.yin.service.HkStockService;
import com.example.yin.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@EnableScheduling
@Component
public class ScheduledTasks {

    @Autowired
    AdminService adminService;

    @Autowired
    ScheduleService scheduleService;
    @Autowired
    HkStockService hkStockService;
    @Scheduled(cron = "0 45 23 * * ?")
    public void testScheduled() throws IOException {
       adminService.doFixTask();

    }

    @Scheduled(cron = "0 45 23 * * ?")
    public void dailyTask() throws IOException {
       adminService.doFixTask();

    }
}
