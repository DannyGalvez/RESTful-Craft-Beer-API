package edu.lvc.cds;

import java.util.ArrayList;

public class Brewery {

    private String name;
    private ArrayList<Beer> beers = new ArrayList<>(3);
    private int ID;
    private String URL = Main.BASE_URI + "breweries/" + ID;


    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setID(int ID){
        this.ID = ID;
    }
    public int getID(){
        return ID;
    }
    public void insertBeer(Beer beer){
        beers.add(beer);
    }
    public ArrayList<Beer> getBeers(){
        return beers;
    }
    public String getURL(){
        return URL;
    }
    public void setBeers(ArrayList<Beer> beers){
        this.beers = beers;
    }
}
