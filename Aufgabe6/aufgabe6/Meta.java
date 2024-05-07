package aufgabe6;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Meta(author = Meta.Author.MUTH)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Meta {
    Author author() default Author.NONE;

    @Meta(author = Author.MUTH)
    enum Author {
        NONE("Unknown"), VOROBEVA("Aksinia Vorobeva"), MUTH("Wendelin Muth"), PRIVAS("Sebastian Privas");

        public final String name;

        Author(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}


