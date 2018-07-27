package com.ivy.sd.png.bo;

public class GenericObjectPair<T,S> {
    public T object1;
    public S object2;

    public GenericObjectPair(T object1, S object2) {
        this.object1 = object1;
        this.object2 = object2;
    }
}
