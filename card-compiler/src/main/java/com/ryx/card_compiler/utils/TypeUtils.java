package com.ryx.card_compiler.utils;


import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


import static com.ryx.card_compiler.utils.Consts.PARCELABLE;
import static com.ryx.card_compiler.utils.Consts.SERIALIZABLE;


/**
 * Created by hezhiqiang on 2018/11/21.
 */

public class TypeUtils {
    private Types types;
    private Elements elements;
    private TypeMirror parcelableType;
    private TypeMirror serializableType;

    public TypeUtils(Types types,Elements elements) {
        this.types = types;
        this.elements = elements;

        parcelableType = this.elements.getTypeElement(PARCELABLE).asType();
        serializableType = this.elements.getTypeElement(SERIALIZABLE).asType();
    }



}
