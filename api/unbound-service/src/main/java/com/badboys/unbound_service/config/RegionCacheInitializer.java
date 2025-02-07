package com.badboys.unbound_service.config;

import com.badboys.unbound_service.api.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RegionCacheInitializer implements ApplicationRunner {

    private final RegionService regionService;

    @Autowired
    public RegionCacheInitializer(RegionService regionService) {
        this.regionService = regionService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        regionService.getAllRegions(); // 서버 시작 후 캐싱 실행
    }
}

