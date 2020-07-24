package com.ryx.card_annotation.model;


import javax.lang.model.element.Element;

/**
 * It contains basic route information.
 *
 * @author Alex <a href="mailto:zhilong.liu@aliyun.com">Contact me.</a>
 * @version 1.0
 * @since 16/8/24 09:45
 */
public class CardMeta {
    // 为了以后的发展
    public enum TypeEnum {
        MODEL
    }

    // 枚举类型：Activity
    private TypeEnum typeEnum;

    private String typeName;         // Type of route
    private Class<?> clazz;   // Destination
    private Class<?> presenter;   // presenter
    private int  resId;
    private int type;//
    private int  dividerType;           // dividerType
    private Element element;        // Raw type of route
    private String group;
    public CardMeta() {
    }

    public Element getElement() {
        return element;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getPresenter() {
        return presenter;
    }

    public void setPresenter(Class<?> presenter) {
        this.presenter = presenter;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public int getDividerType() {
        return dividerType;
    }

    public void setDividerType(int dividerType) {
        this.dividerType = dividerType;
    }
   private CardMeta(TypeEnum typeEnum,String typeName,Class<?> clazz,int resId,String group){
        this.typeEnum =typeEnum;
    this.typeName = typeName;
    this.clazz =clazz;
    this.group = group;
    this.resId=resId;
}
    // 构建者模式相关
    private CardMeta(Builder builder) {
        this.typeName = builder.typeName;
        this.typeEnum=builder.typeEnum;
        this.element = builder.element;
        this.clazz = builder.clazz;
        this.presenter = builder.presenter;
        this.group = builder.group;
        this.resId=builder.resId;
        this.dividerType=builder.dividerType;
    }
    // 对外暴露
    // 对外提供简易版构造方法，主要是为了方便APT生成代码
    public static CardMeta create(TypeEnum typeEnum,String typeName, Class<?> clazz, int resId, String group) {
        return new CardMeta(typeEnum,typeName, clazz, resId, group);
    }

    public static class Builder {
        private TypeEnum typeEnum;

        // 类节点
        private Element element;
        // 注解使用的类对象
        private Class<?> clazz;
        //  card 类型名称
        private String typeName;
        // 路由组
        private String group;
           //布局文件资源id
         private int resId;
         // card 业务处理类
        private Class<?> presenter;

       // card 分割类型
        private int dividerType;
        public Builder addTypeEnum(TypeEnum typeEnum) {
            this.typeEnum = typeEnum;
            return this;
        }
        public Builder addTypeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public Builder addElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder addPresenter(Class<?> presenter) {
            this.presenter = presenter;
            return this;
        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }
        public Builder addResId(int resId) {
            this.resId = resId;
            return this;
        }
        public Builder addDividerType(int dividerType) {
            this.dividerType = dividerType;
            return this;
        }
        // 最后的build或者create，往往是做参数的校验或者初始化赋值工作
        public CardMeta build() {
            if (typeName == null || typeName.length() == 0) {
                throw new IllegalArgumentException("typeName必填项为空，如：/app/MainActivity");
            }
            return new CardMeta(this);
        }
    }
}