package org.fly.tsdk.sdk.wrapper.bean;

public class Eventer {

    public enum TYPE {
        INIT,
        INIT_FAIL,
        INIT_SUCCESS,

        LOADING,
        LOADED;
    }

    private TYPE type;
    private String message;

    public Eventer(TYPE type) {
        this.type = type;
    }

    public Eventer(TYPE type, String message) {
        this.type = type;
        this.message = message;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Eventer create(TYPE type, String message)
    {
        return new Eventer(type, message);
    }

    public static Eventer create(TYPE type)
    {
        return new Eventer(type);
    }
}
