package com.ivy.ui.profile.create.model;

public class ContactTitle {

    private String listName;

    private int listId;

    public ContactTitle(){

    }

    public ContactTitle(int listId, String listName) {
        this.listId = listId;

        this.listName = listName;
    }


    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public int getListId() {
        return listId;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }
}
