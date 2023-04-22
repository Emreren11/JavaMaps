package com.emre.javamaps.roomDB;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.emre.javamaps.model.Place;

@Database(entities = {Place.class}, version = 1)
public abstract class PlaceDatabase extends RoomDatabase { //abstarct - soyut
    public abstract PlaceDao placeDao();
}
