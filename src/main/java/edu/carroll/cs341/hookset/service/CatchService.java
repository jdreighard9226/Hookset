package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.CatchDto;
import edu.carroll.cs341.hookset.web.form.CatchForm;

import java.util.List;

public interface CatchService {
    List<CatchDto> getAllCatches();
    List<CatchDto> getAllUserCatches();
    CatchDto getCatchByCatchId(int catchId);
    boolean addNewCatch(CatchForm catchForm);
}
