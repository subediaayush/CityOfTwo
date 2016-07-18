package com.messenger.cityoftwo;

import java.util.ArrayList;

/**
 * Created by Aayush on 4/20/2016.
 */
public class Queue<E> extends ArrayList<E> {
    public E dequeue() {
        E item = get(0);
        remove(0);
        return item;
    }

    public void enqueue(E item) {
        add(item);
    }
}
