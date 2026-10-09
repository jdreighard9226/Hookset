package edu.carroll.cs341.hookset.service.impl;

import edu.carroll.cs341.hookset.jpa.repo.*;
import edu.carroll.cs341.hookset.service.CatchService;
import edu.carroll.cs341.hookset.web.dto.*;
import edu.carroll.cs341.hookset.web.form.CatchForm;
import edu.carroll.cs341.hookset.web.mapper.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import shared.jpa.entity.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatchServiceImpl implements CatchService {

    private final CatchRepository catchRepository;
    private final FishRepository fishRepository;
    private final FlyRepository flyRepository;
    private final WaterBodyRepository waterBodyRepository;
    private final UserRepository userRepository;

    private final CatchMapper catchMapper;
    private final FishMapper fishMapper;
    private final FlyMapper flyMapper;
    private final WaterBodyMapper waterBodyMapper;

    public CatchServiceImpl(CatchRepository catchRepository,
                            FishRepository fishRepository,
                            FlyRepository flyRepository,
                            WaterBodyRepository waterBodyRepository,
                            UserRepository userRepository,
                            CatchMapper catchMapper,
                            FishMapper fishMapper,
                            FlyMapper flyMapper,
                            WaterBodyMapper waterBodyMapper) {

        this.catchRepository = catchRepository;
        this.fishRepository = fishRepository;
        this.flyRepository = flyRepository;
        this.waterBodyRepository = waterBodyRepository;
        this.userRepository = userRepository;

        this.catchMapper = catchMapper;
        this.fishMapper = fishMapper;
        this.flyMapper = flyMapper;
        this.waterBodyMapper = waterBodyMapper;
    }

    @Override
    public List<CatchDto> getAllCatches() {
        List<Catch> catches = catchRepository.findAll();

        if (catches.isEmpty()) {
            // log no catches found
        }
        else {
            // log catch count
        }

        return mapAllCatchDtos(catches);
    }

    @Override
    public List<CatchDto> getAllUserCatches() {
        Long userId = getCurrentUserId();

        List<Catch> catches = catchRepository.findByUserId(userId);

        if (catches.isEmpty()) {
            // log no user catches found
        }
        else {
            // log user catch count
        }

        return mapAllCatchDtos(catches);
    }

    @Override
    public CatchDto getCatchByCatchId(int catchId) {
        Catch catchRecord = catchRepository.findById((long) catchId)
                .orElseThrow(() -> new IllegalArgumentException("Catch not found"));

        return mapCatchDto(catchRecord);
    }

    @Override
    public boolean addNewCatch(CatchForm catchForm) {
        Long userId = getCurrentUserId();

        if (!flyRepository.existsById(catchForm.getFlyId())
                || !fishRepository.existsById(catchForm.getFishId())
                || !waterBodyRepository.existsById(catchForm.getWaterBodyId())) {
            // log invalid catch information
            return false;
        }

        Catch catchRecord = new Catch();

        catchRecord.setUserId(userId);
        catchRecord.setFlyId(catchForm.getFlyId());
        catchRecord.setFishId(catchForm.getFishId());
        catchRecord.setWaterBodyId(catchForm.getWaterBodyId());
        catchRecord.setFishLength(catchForm.getFishLength());
        catchRecord.setDateCaught(catchForm.getDateCaught());
        catchRecord.setNotes(catchForm.getNotes());

        catchRepository.save(catchRecord);

        // log successful catch creation
        return true;
    }

    private List<CatchDto> mapAllCatchDtos(List<Catch> catches) {
        if (catches.isEmpty()) {
            // log no catches found
        }
        else {
            // log catch count
        }

        List<CatchDto> catchDtos = new ArrayList<>();

        for (Catch catchRecord : catches) {
            catchDtos.add(mapCatchDto(catchRecord));
        }

        return catchDtos;
    }

    private CatchDto mapCatchDto(Catch catchRecord) {
        Fly fly = flyRepository.findById(catchRecord.getFlyId())
                .orElseThrow();;

        Fish fish = fishRepository.findById(catchRecord.getFishId())
                .orElseThrow();

        WaterBody waterBody = waterBodyRepository.findById(catchRecord.getWaterBodyId())
                .orElseThrow();

        FlyDto flyDto = flyMapper.toDto(fly);
        FishDto fishDto = fishMapper.toDto(fish);
        WaterBodyDto waterBodyDto = waterBodyMapper.toDto(waterBody);

        return catchMapper.toDto(
                catchRecord,
                flyDto,
                fishDto,
                waterBodyDto
        );
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username);
        return user.getUserId();
    }
}