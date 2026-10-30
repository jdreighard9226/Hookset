package edu.carroll.cs341.hookset.service.input;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * The details a caller provides when adding or editing a catch.
 *
 * <p>This is the catch service's input, separate from the web form, so the
 * service doesn't depend on where the data came from. It only holds the fields
 * a user is allowed to set. The catch ID and owner are never part of it, so
 * the service always decides those itself.</p>
 *
 * <p>The fly, fish, and water body are passed as IDs so the service can check
 * that each one exists before building the catch.</p>
 *
 * @param flyId the ID of the fly used to make the catch
 * @param fishId the ID of the fish species that was caught
 * @param waterBodyId the ID of the water body the catch was made on
 * @param fishLength the length of the fish in inches, or {@code null} if not recorded
 * @param dateCaught the date and time the fish was caught
 * @param notes any notes about the catch, or {@code null} if none
 */
public record CatchInput(Long flyId,
                         Long fishId,
                         Long waterBodyId,
                         BigDecimal fishLength,
                         LocalDateTime dateCaught,
                         String notes) {
}