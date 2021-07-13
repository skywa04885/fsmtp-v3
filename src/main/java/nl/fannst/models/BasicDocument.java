package nl.fannst.models;

import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.Callable;

public class BasicDocument {
    /****************************************************
     * Fields
     ****************************************************/

    protected static final String ID_FIELD = "_id";
    protected static final String VERSION_FIELD = "_ver";

    /****************************************************
     * Classy Stuff
     ****************************************************/

    protected final Document m_Document;
    protected int m_Version;
    protected boolean m_New;

    public BasicDocument(Document document, boolean _new) {
        this.m_Document = document;
        this.m_New = _new;
    }

    public BasicDocument() {
        this (new Document(), false);
    }

    /****************************************************
     * Getters / Setters
     ****************************************************/

    public Document getDocument() {
        return this.m_Document;
    }

    public ObjectId getObjectId() {
        return this.m_Document.getObjectId(ID_FIELD);
    }

    public void setObjectId(ObjectId objectId) {
        this.setField(ID_FIELD, objectId);
    }

    public void setVersion(int version) {
        this.setField(VERSION_FIELD, version);
    }

    public int getVersion() {
        return this.m_Document.getInteger(VERSION_FIELD);
    }

    public void setDefaults() {
        this.setObjectId(ObjectId.get());
        this.setVersion(0);
    }

    public void setField(String key, Object value) {
        if (this.m_Document.get(key) != null)
            this.m_Document.remove(key);

        this.m_Document.append(key, value);
    }

    /****************************************************
     * Database Methods
     ****************************************************/

    public BasicDocument get(MongoCollection<Document> collection, ObjectId id) {
        Document document = collection.find(Filters.eq(ID_FIELD, id)).first();
        if (document == null)
            return null;

        return new BasicDocument(document, false);
    }

    public void save(MongoCollection<Document> collection) {
        if (this.m_New) {
            collection.insertOne(this.m_Document);
            this.m_New = false;
            return;
        }

        collection.updateOne(Filters.eq(ID_FIELD, this.m_Document.get(ID_FIELD)), this.m_Document);
    }
}
