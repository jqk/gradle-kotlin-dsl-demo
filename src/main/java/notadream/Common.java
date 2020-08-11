package notadream;

import org.jetbrains.annotations.NotNull;

/**
 * 工具类。
 *
 * @author Jason
 */
public final class Common {
    private Common() {
    }

    @NotNull
    public static String greeting() {
        return "Hello Gradle Kotlin DSL!";
    }
}
