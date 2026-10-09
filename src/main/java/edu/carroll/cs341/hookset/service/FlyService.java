package edu.carroll.cs341.hookset.service;

import edu.carroll.cs341.hookset.web.dto.FlyDto;
import edu.carroll.cs341.hookset.web.form.FlyForm;
import org.springframework.validation.BindingResult;
import java.util.List;

public interface FlyService {
    List<FlyDto> getAllFlies();
    List<FlyDto> getAllHooksetFlies();
    List<FlyDto> getAllUserFlies();
    FlyDto getFlyFromSlug(String flySlug);
    void addFly(FlyForm flyForm);
    void validateFly(FlyForm flyForm, BindingResult result);
    void deleteFly(Long flyId);
}
