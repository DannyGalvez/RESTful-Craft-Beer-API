package edu.lvc.cds;

import java.util.ArrayList;

public class User {

    private String username;
    ArrayList<String> favorites;
    private int ID;


    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public ArrayList<String> getFavorites(){
        return favorites;
    }
    public int getID(){
        return ID;
    }
    public void setID(int ID){
        this.ID = ID;
    }

}
