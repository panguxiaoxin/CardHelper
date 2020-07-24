package com.ryx.card_api.core;

import com.ryx.card_annotation.model.CardMeta;
import com.ryx.card_api.template.ICardGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hezhiqiang on 2018/11/20.
 */

class Warehouse {
    // Cache route and metas
    static Map<String, Class<? extends ICardGroup>> groupsIndex = new HashMap<>();
    static Map<String, CardMeta> templateCards = new HashMap<>();
    static Map<String, CardMeta> cards = new HashMap<>();//key 为 typeName
    static Map<Integer, CardMeta> cards_int = new HashMap<>();// key 为 typeint
    // Cache provider

    static void clear() {

    }
}
