package edu.carroll.cs341.hookset.service.input;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CatchInput(Long flyId, Long fishId, Long waterBodyId, BigDecimal fishLengh, LocalDateTime dateCaught, String notes) {
}
