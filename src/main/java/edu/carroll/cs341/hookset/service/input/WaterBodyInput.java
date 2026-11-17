package edu.carroll.cs341.hookset.service.input;

import java.util.List;

/**
 * The details a caller provides when adding a water body.
 *
 * <p>This is the water body service's input, separate from any web form, so the
 * service doesn't depend on where the data came from. The water body ID is never
 * part of it, so the database always assigns that itself.</p>
 *
 * <p>The slug is passed in for now instead of being generated from the name, since
 * adding water bodies is only used by tests.</p>
 *
 * @param name the name of the water body
 * @param type the type of water body, either River, Lake, or Stream
 * @param state the state the water body is in
 * @param description a description of the water body
 * @param slug the URL slug used for the water body's details page
 * @param fishIds the IDs of the fish found in the water body, or {@code null} or empty for none
 */
public record WaterBodyInput(String name,
                             String type,
                             String state,
                             String description,
                             String slug,
                             List<Long> fishIds) {
}