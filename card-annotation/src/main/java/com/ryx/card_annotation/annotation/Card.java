package com.ryx.card_annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a page can be route by router.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/15 下午9:29
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Card {

    /**
     * name of card
     */
    String typeName();

    int type() default 0;

    String group() default "";

    Class<?>  presenter();

    int resId() default 0;

    int dividerType() default 0;

}
