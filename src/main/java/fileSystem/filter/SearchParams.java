package fileSystem.filter;

import lombok.Getter;

/**
 * Pattern:  TYPED PARAMETER OBJECT:
 *      PRIVATE CONSTRUCTOR + STATIC FACTORIES WHICH EXPRESS INTENT THROUGH THEIR NAME.
 *
 * What? Typed parameter object carrying search constraints for file system node queries.
 *
 * Why static factory methods instead of a public constructor?
 * Clean Code (Martin) discourages functions with many arguments — each additional
 * argument increases cognitive load for the caller. A public constructor with
 * nullable fields forces callers to reason about positional nulls:
 * <pre>
 *     new SearchParams("file.*\\.txt", null, null);  // which nulls are which?
 * </pre>
 * Static factory methods eliminate this by giving each combination a name that
 * expresses intent directly:
 * <pre>
 *     SearchParams.byName("file.*\\.txt");               // one argument, intent clear
 *     SearchParams.bySize(5, 50);                        // two arguments, obviously related
 *     SearchParams.byNameAndSize("file.*\\.txt", 5, 50); // three, all meaningful, no nulls
 * </pre>
 * This is also consistent with Effective Java (Bloch), Item 1: "Consider static
 * factory methods instead of constructors" — one advantage being that unlike
 * constructors, they have names.
 *
 * <p><b>Why is the constructor private?</b><br>
 * A public constructor alongside factory methods would allow callers to bypass
 * them entirely, reintroducing positional nulls at call sites. The private
 * constructor enforces the factory methods as the only construction path —
 * clean call sites become mandatory, not just available.
 *
 * <p><b>Why {@code Integer} instead of {@code int} for size fields?</b><br>
 * Primitive {@code int} cannot be null. Using the boxed {@code Integer} allows
 * null to express "no constraint", avoiding a separate boolean flag per field.
 * Filters treat null as pass-through:
 * <pre>
 *     boolean aboveMin = params.getMinSize() == null || size >= params.getMinSize();
 * </pre>
 *
 * <p><b>Extension:</b> adding a new search constraint requires only:
 * <ol>
 *   <li>A new field here</li>
 *   <li>A new {@link NodeFilter} implementation that reads it</li>
 * </ol>
 * {@code FileSystem}, {@code NodeFilterChain}, and all existing filters are
 * unaffected — OCP preserved via the Parameter Object + Filter Chain combination.
 */
@Getter
public class SearchParams {
    private final String fileNameRegex;
    private final Integer minSize;
    private final Integer maxSize;

    private SearchParams(String fileNameRegex, Integer minSize, Integer maxSize) {
        this.fileNameRegex = fileNameRegex;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public static SearchParams byName(String fileNameRegex) {
        return new SearchParams(fileNameRegex, null, null);
    }

    public static SearchParams bySize(int minSize, int maxSize) {
        return new SearchParams(null, minSize, maxSize);
    }

    public static SearchParams byNameAndSize(String fileNameRegex, int minSize, int maxSize) {
        return new SearchParams(fileNameRegex, minSize, maxSize);
    }
}
