package edu.carroll.cs341.hookset.service.input;

/**
 * The details a caller provides when adding a fish species.
 *
 * <p>This is the fish service's input, separate from any web form, so the
 * service doesn't depend on where the data came from. The fish ID is never
 * part of it, so the database always assigns that itself.</p>
 *
 * @param family the biological family of the fish
 * @param species the species name of the fish
 * @param image the image file name shown for the fish
 */
public record FishInput(String family,
                        String species,
                        String image) {
}