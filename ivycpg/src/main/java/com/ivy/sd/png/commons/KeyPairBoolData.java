package com.ivy.sd.png.commons;

public class KeyPairBoolData {

    int id;
    String name;
    boolean isSelected;

    Object object;

    public KeyPairBoolData() {
    }

    public KeyPairBoolData(int id, String name, boolean isSelected) {
        this.id = id;
        this.name = name;
        this.isSelected = isSelected;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the isSelected
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * @param isSelected the isSelected to set
     */
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}