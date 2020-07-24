package com.ryx.card_api.template;

import java.util.Map;

/**
 * Root element.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 16:36
 */
public interface ICardRoot {

    /**
     * Load routes to input
     * @param
     */

    Map<String, Class<? extends ICardGroup>> getGroupMap();
}