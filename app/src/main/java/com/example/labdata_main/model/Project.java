package com.example.labdata_main.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "deadline")
    private String deadline;

    @ColumnInfo(name = "create_time")
    private long createTime;

    @ColumnInfo(name = "is_accessible")
    private boolean isAccessible;

    @ColumnInfo(name = "has_access")
    private boolean hasAccess;

    public Project() {
        this.createTime = System.currentTimeMillis();
        this.isAccessible = true;
        this.hasAccess = true;
    }

    @Ignore
    public Project(int id, String name, boolean isAccessible) {
        this();
        this.id = id;
        this.name = name;
        this.isAccessible = isAccessible;
    }

    @Ignore
    public Project(String name, String deadline) {
        this();
        this.name = name;
        this.deadline = deadline;
    }

    @Ignore
    public Project(int id, String name, String deadline, long createTime, boolean isAccessible) {
        this.id = id;
        this.name = name;
        this.deadline = deadline;
        this.createTime = createTime;
        this.isAccessible = isAccessible;
        this.hasAccess = true;
    }

    @Ignore
    public Project(int id, String name, String deadline, long createTime, boolean isAccessible, boolean hasAccess) {
        this.id = id;
        this.name = name;
        this.deadline = deadline;
        this.createTime = createTime;
        this.isAccessible = isAccessible;
        this.hasAccess = hasAccess;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }

    public boolean hasAccess() {
        return hasAccess;
    }

    public void setHasAccess(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }
}
