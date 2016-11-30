package com.raapgames.puzzlegame;

/**
 * Created by anandh on 11/18/16.
 */

public class FBProfileDetailsVO
{
    private String name;
    private String id;
    FBProfileDetailsVO(String name, String id)
    {
        this.name = name;
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}