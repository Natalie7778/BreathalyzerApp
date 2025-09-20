package com.dfrobot.angelo.blunobasicdemo;

@SuppressWarnings("unchecked")
public class RingBuffer<T> {

    private T[] buffer;
    private int count = 0;
    private int indexOut = 0;
    private int indexIn = 0;

    public RingBuffer(int capacity) {
        buffer = (T[]) new Object[capacity];
    }

    public boolean isEmpty() {
        return count == 0;
    }
    
    public boolean isFull() {
        return count == buffer.length;
    }

    public int size() {
        return count;
    }

    public void clear() {
        count=0;
    }
    
    public void push(T item) {
        if (count == buffer.length) {
        	System.out.println("Ring buffer overflow");
        }
        buffer[indexIn] = item;
        indexIn = (indexIn + 1) % buffer.length;
        if(count++ == buffer.length)
        {
        	count = buffer.length;
        }
    }

    public T pop() {
        if (isEmpty()) {
        	System.out.println("Ring buffer pop underflow");

        }
        T item = buffer[indexOut];
        buffer[indexOut] = null;
        if(count-- == 0)
        {
        	count = 0;
        }
        indexOut = (indexOut + 1) % buffer.length;
        return item;
    }
    
    public T next() {
        if (isEmpty()) {
        	System.out.println("Ring buffer next underflow");
        }
        return buffer[indexOut];
    }


}