package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.FlyRepository;
import edu.carroll.cs341.hookset.service.FlyService;
import edu.carroll.cs341.hookset.userDetails.HooksetUserDetails;
import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import edu.carroll.cs341.hookset.web.mapper.FlyMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
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

    @Override
    public FlyDto getFlyFromSlug(String flySlug) {
        Fly fly = flyRepository.getFlyByFlySlug(flySlug);
        if (fly == null) {
            return null;
        }

        return flyMapper.createFlyDto(fly);
    }

    @Override
    public void addFly(FlyForm flyForm) {
        Fly fly = flyMapper.createFlyEntity(flyForm);
        fly.setUserId(getCurrentUserId());
        fly.setFlySlug(generateFlySlug(fly.getFlyName()));

        String flyType =  fly.getFlyType();
        if (flyType.equals("Dry Fly")) {
            fly.setFlyImage("dry.png");
        } else if (flyType.equals("Streamer")) {
            fly.setFlyImage("streamer.png");
        } else {
            fly.setFlyImage("nymph.png");
        }

        flyRepository.save(fly);
    }

    @Override
    public void validateFly(FlyForm flyForm, BindingResult result) {

        if (flyForm.getMinSize() != null &&
                flyForm.getMaxSize() != null) {

            if (flyForm.getMinSize() > flyForm.getMaxSize()) {
                result.rejectValue(
                        "minSize",
                        "invalid.size",
                        "Minimum size cannot exceed maximum size"
                );
            }
        }

        if (flyRepository.existsByUserIdAndFlyNameIgnoreCase(
                getCurrentUserId(), flyForm.getFlyName().trim())) {

            result.rejectValue(
                    "flyName",
                    "duplicate.flyName",
                    "You already have a fly with this name"
            );
        }
    }

    public void deleteFly(Long flyId) {
        Fly fly = flyRepository.findById(flyId).orElseThrow();

        if (!getCurrentUserId().equals(fly.getUserId())) {
            throw new AccessDeniedException("You cannot delete this fly");
        }

        flyRepository.delete(fly);
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
        if (flies.isEmpty()) {
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

    private String generateFlySlug(String flyName) {
        return flyName.trim().toLowerCase().replaceAll("\\s+", "-");
    }
}
