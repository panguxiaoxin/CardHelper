package com.jingyu.medium.widget.listpage;

public abstract class Model {
    public abstract Object getModelType();


    /**
     * get model id
     *
     * @return
     */
    public long getModelId() {
        return 0;
    }

    /**
     * 用来区分 model 的 type 字段, adapter 会根据这个字段处理
     *
     */
    private String modelTypeName;

    public String getModelTypeName() {
        return modelTypeName;
    }

    public void setModelTypeName(String name) {
        this.modelTypeName = name;
    }
}
