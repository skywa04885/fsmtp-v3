package nl.fannst.models;

import org.bson.Document;

public abstract class DatabaseModel {
    public abstract void save();
    public abstract Document toDocument();
}
