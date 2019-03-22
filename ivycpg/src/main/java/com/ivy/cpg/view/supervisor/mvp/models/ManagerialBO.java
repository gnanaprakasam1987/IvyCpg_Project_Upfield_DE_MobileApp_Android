package com.ivy.cpg.view.supervisor.mvp.models;

import java.util.List;

/**
 * Created by ramkumard on 27/2/19.
 * To store user hierarchy details.
 */

public class ManagerialBO {

    private String userId;
    private String userLevel;
    private String levelId;
    private String parentId;
    private boolean isExpanded;
    private boolean selected;
    private boolean isChild;
    private List<ManagerialBO> children;

    private int level;

    public ManagerialBO(int level) {
        this.level = level;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(String userLevel) {
        this.userLevel = userLevel;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public List<ManagerialBO> getChildren() {
        return children;
    }

    public void addChildren(List<ManagerialBO> children) {
        this.children = children;
    }

    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }
}
