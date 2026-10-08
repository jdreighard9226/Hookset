package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.CatchDto;

import java.util.List;

public interface CatchService {
    List<CatchDto> getAllCatches();
    List<CatchDto> getAllUserCatches();
}
