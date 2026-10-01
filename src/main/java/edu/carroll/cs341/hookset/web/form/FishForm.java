package edu.carroll.cs341.hookset.web.form;

import edu.carroll.cs341.hookset.web.dto.FishDto;

import java.util.ArrayList;
import java.util.List;

public class FishForm {
    private List<FishDto> fishDtoList = new ArrayList<>();

    public FishForm(List<FishDto> fishDtoList) {
        this.fishDtoList = fishDtoList;
    }
}
