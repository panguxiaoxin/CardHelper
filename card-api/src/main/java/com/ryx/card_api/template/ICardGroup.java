package com.ryx.card_api.template;


import com.ryx.card_annotation.model.CardMeta;

import java.util.Map;

/**
 * Group element.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/23 16:37
 */
public interface ICardGroup {
    /**
     * Fill the atlas with routes in group.
     */
    Map<String, CardMeta> getPathMap();
}
