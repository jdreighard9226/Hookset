package edu.carroll.cs341.hookset.service.input;

/**
 * The details a caller provides when adding or editing a custom fly.
 *
 * <p>This is the fly service's input, separate from the web form, so the
 * service doesn't depend on where the data came from. It only holds the fields
 * a user is allowed to set. The fly ID, owner, slug, and image are never part
 * of it, so the service always decides those itself.</p>
 *
 * <p>The sizes are {@code Integer} instead of {@code int} so a missing size
 * can reach the service as {@code null} and fail validation cleanly.</p>
 *
 * @param name the name of the fly
 * @param type the type of fly, such as Dry, Nymph, or Streamer
 * @param color the color of the fly
 * @param minSize the smallest hook size the fly is commonly tied on
 * @param maxSize the largest hook size the fly is commonly tied on
 */
public record FlyInput(String name,
                       String type,
                       String color,
                       Integer minSize,
                       Integer maxSize) {
}