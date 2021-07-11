package com.hamdane.chatapp.listenner;

public abstract class Listener<T> {

    public void failed() {}

    public  void success() {}

    public  boolean condition(Object arg) { return false; }

    public void value(T  arg) {}

}
