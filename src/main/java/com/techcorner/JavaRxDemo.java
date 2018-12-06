package com.techcorner;

import io.reactivex.Flowable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Hello world!
 */
public class JavaRxDemo
{
    public static void main(String[] args)
    {
        System.out.println("Hello World!");
    }

    public static <T> T intenseCalculation(T value) throws Exception
    {
        Thread.sleep(ThreadLocalRandom.current().nextInt(2000));
        return value;
    }

    public static void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    static Flowable<Integer> randomGenerator(int min, int max)
    {
        return Flowable.generate(emitter -> emitter.onNext(ThreadLocalRandom.current().nextInt(min, max)));
    }

    static final class MyItem
    {
        final int id;

        MyItem(int id)
        {
            this.id = id;
            System.out.println("Constructing MyItem " + id);
        }
    }
}


