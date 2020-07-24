package com.zjx.cardhelper;

import com.jingyu.medium.widget.listpage.Model;
import com.ryx.card_annotation.annotation.Card;

@Card(typeName = "abc",presenter = MainActivity.class)
public class TestModel extends Model {

    @Override
    public Object getModelType() {
        return null;
    }
}
