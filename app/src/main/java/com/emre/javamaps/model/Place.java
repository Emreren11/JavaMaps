package com.emre.javamaps.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true) // Oto ID oluşturma
    public int id;

    @ColumnInfo(name = "name") // Sütun ismi
    public String name;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitute;

    public Place(String name, double latitude, double longitute) {
        this.name = name;
        this.latitude = latitude;
        this.longitute = longitute;
    }

}
