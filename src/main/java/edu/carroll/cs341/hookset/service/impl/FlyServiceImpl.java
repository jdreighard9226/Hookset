package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.mapper.FlyMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.Fly;

import java.util.ArrayList;
import java.util.List;

@Service
public class FlyServiceImpl implements FlyService {

    private final FlyRepository flyRepository;
    private final FlyMapper flyMapper;

    public FlyServiceImpl(FlyRepository flyRepository, FlyMapper flyMapper) {
        this.flyRepository = flyRepository;
        this.flyMapper = flyMapper;
    }


    @Override
    public List<FlyDto> getAllFlies() {
        Long userId = getCurrentUserId();
        List<Fly> flies = flyRepository.findByUserIdIsNullOrUserId(userId);
        return mapAllFlyDtos(flies);

    }

    @Override
    public List<FlyDto> getAllHooksetFlies() {
        List<Fly> flies = flyRepository.findByUserIdIsNull();
        return mapAllFlyDtos(flies);
    }

    @Override
    public List<FlyDto> getAllUserFlies() {
        Long userId = getCurrentUserId();
        List<Fly> flies = flyRepository.findByUserId(userId);
        return mapAllFlyDtos(flies);
    }

    private Long getCurrentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
                authentication.getPrincipal() instanceof HooksetUserDetails userDetails) {
            return userDetails.getUserId();
        }

        return null;
    }

    private List<FlyDto> mapAllFlyDtos(List<Fly> flies) {
        if (flies.size() == 0) {
            // log warning
        } else {
            // log how many flies are being returned
        }
        List<FlyDto> flyDtos = new ArrayList<>();
        for (Fly fly : flies) {
            flyDtos.add(flyMapper.createFlyDto(fly));
        }

        return flyDtos;
    }
}
