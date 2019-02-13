package xin.zero2one.stereotype;

import java.lang.annotation.*;

/**
 * @author ZJD
 * @date 2019/2/12
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    String value() default "";
}
