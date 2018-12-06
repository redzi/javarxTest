package com.techcorner;

import com.google.common.collect.Lists;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.techcorner.JavaRxDemo.intenseCalculation;
import static com.techcorner.JavaRxDemo.randomGenerator;
import static com.techcorner.JavaRxDemo.sleep;

/*
*  Most of these examples have been taken from Learning RxJava by T. Niels
*
* */

@RunWith(MockitoJUnitRunner.class)
public class JavaRxDemoTest
{
    // how to create observable

    @Test
    public void observableCreate()
    {
        Observable<String> source = Observable.create(emitter -> {
            emitter.onNext("Alpha");
            emitter.onNext("Beta");
            emitter.onNext("Gamma");
            emitter.onNext("Delta");
            emitter.onNext("Epsilon");
            emitter.onComplete();
        });

        source.subscribe(s -> System.out.println("RECEIVED: " + s));
    }

    @Test
    public void observableJust()
    {
        Observable<String> source = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

        source.subscribe(s -> System.out.println("RECEIVED: " + s));
    }

    @Test
    public void observableFromIterable()
    {
        Observable<String> source = Observable.fromIterable(Lists.newArrayList("Alpha", "Beta", "Gamma", "Delta", "Epsilon"));

        source.subscribe(s -> System.out.println("RECEIVED: " + s));
    }

    // observer

    @Test
    public void observer()
    {
        Observable<String> source = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

        Observer<String> myObserver = new Observer<String>()
        {
            @Override
            public void onSubscribe(Disposable d)
            {
                System.out.println("Witaj");
            }

            @Override
            public void onNext(String value)
            {
                System.out.println("RECEIVED: " + value);
            }

            @Override
            public void onError(Throwable e)
            {
                e.printStackTrace();
            }

            @Override
            public void onComplete()
            {
                System.out.println("Done!");
            }
        };
        source.subscribe(myObserver);
    }

    @Test
    public void observerWithLambdas()
    {
        Observable<String> source = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

        source.subscribe(value -> System.out.println("RECEIVED: " + value),
                Throwable::printStackTrace,
                () -> System.out.println("Done!"));
    }


    // hot vs cold

    @Test
    public void coldObservable()
    {
        Observable<String> source = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

        source.subscribe(s -> System.out.println("Observer 1 Received: " + s));

        source.subscribe(s -> System.out.println("Observer 2 Received: " + s));
    }

    @Test
    public void coldObservableOperatorImpact()
    {
        Observable<String> source = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

        source.map(String::length).subscribe(s -> System.out.println("Observer 1 Received: " + s));

        source.subscribe(s -> System.out.println("Observer 2 Received: " + s));
    }

    @Test
    public void hotObservable() throws Exception
    {
        ConnectableObservable<Long> source = Observable.interval(500, TimeUnit.MILLISECONDS).publish();

        source.connect();

        source.subscribe(s -> System.out.println("Observer 1 Received: " + s));

        Thread.sleep(2000L);

        source.subscribe(s -> System.out.println("Observer 2 Received: " + s));

        Thread.sleep(2000L);
    }

    // Single, and Maybe

    @Test
    public void single()
    {
        Observable<String> source = Observable.just("Alpha", "Beta", "Gamma");

        source.first("Nil").subscribe(System.out::println);

        Observable.empty().first("Nil").subscribe(System.out::println);
    }

    @Test
    public void maybe()
    {
        Maybe.just(100).subscribe(
                s -> System.out.println("Process 1 received: " + s),
                Throwable::printStackTrace,
                () -> System.out.println("Process 1 done!"));

        Maybe.empty().subscribe(s -> System.out.println("Process 2 received: " + s),
                Throwable::printStackTrace,
                () -> System.out.println("Process 2 done!"));
    }

    // dispose

    @Test
    public void dispose() throws Exception
    {
        Observable<Long> source = Observable.interval(500, TimeUnit.MILLISECONDS);

        Disposable disposable = source
                .subscribe(s -> System.out.println("Observer 2 Received: " + s));

        Thread.sleep(2000L);

        disposable.dispose();
    }

    // operators

    @Test
    public void filterMap() throws Exception
    {
        Observable<Long> source = Observable.interval(1000, TimeUnit.MICROSECONDS);

        source
                .subscribe(value ->
                        System.out.println("RECEIVED: " + value + " in thread: " + Thread.currentThread().getName()),
                        Throwable::printStackTrace,
                        () -> System.out.println("done")
                );
        System.out.println("halo");
    }

    @Test
    public void basic()
    {
        Observable<String> source = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");

        source
                .take(3)
                .filter(value -> value.length() > 4)
                .skip(1)
                .subscribe(value ->
                        System.out.println("RECEIVED: " + value)
                );
    }

    @Test
    public void delay() throws Exception
    {
        Observable.just("Alpha", "Beta", "Gamma" ,"Delta", "Epsilon")
                .delay(1, TimeUnit.SECONDS)
                .subscribe(s -> System.out.println("Received: " + s));

        Thread.sleep(1100L);
    }

    @Test
    public void repeat()
    {
        Observable.just("Alpha", "Beta", "Gamma" ,"Delta", "Epsilon")
                .repeat(2)
                .subscribe(s -> System.out.println("Received: " + s));
    }

    @Test
    public void scan()
    {
        Observable.just(5, 3, 7, 10, 2, 14)
                .scan((accumulator, next) -> accumulator + next)
                .subscribe(s -> System.out.println("Received: " + s));
    }

    @Test
    public void toMap()
    {
        Observable.just("Alpha", "Alphaa", "Beta", "Gamma", "Delta", "Epsilon")
                .toMap(s -> s.charAt(0), String::length)
                .subscribe(s -> System.out.println("Received: " + s));
    }

    @Test
    public void doOnSubscribe()
    {
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .doOnSubscribe(d -> System.out.println("Subscribing!"))
                .doOnNext(System.out::println)
                .doOnDispose(() -> System.out.println("Disposing!"))
                .subscribe(i -> System.out.println("RECEIVED: " + i));
    }

    // merging operators

    @Test
    public void merge()
    {
        Observable<String> source1 =
                Observable.just("Alpha", "Beta", "Gamma", "Delta",
                        "Epsilon");
        Observable<String> source2 =
                Observable.just("Zeta", "Eta", "Theta");
        Observable.merge(source1, source2)
                .subscribe(i -> System.out.println("RECEIVED: " + i));
    }

    @Test
    public void flatMap()
    {
        Observable<String> source =
                Observable.just("Alpha", "Beta", "Gamma", "Delta",
                        "Epsilon");
        source.flatMap(s -> Observable.fromArray(s.split("")))
                .subscribe(System.out::println);
    }

    @Test
    public void zip() throws Exception
    {
        Observable<String> source1 =
                Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");
        Observable<Long> source2 = Observable.interval(5,TimeUnit.SECONDS);

        Observable.zip(source1, source2, (s,i) -> s + "-" + i)
                .subscribe(System.out::println);

        Thread.sleep(25000);
    }

    @Test
    public void grouping()
    {
        Observable<String> source =
                Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon");
        Observable<GroupedObservable<Integer,String>> byLengths =
                source.groupBy(String::length);

        byLengths.flatMapSingle(Observable::toList)
                .subscribe(System.out::println);
    }

    // multicasting

    @Test
    public void publish()
    {
        ConnectableObservable<Integer> threeIntegers =
                Observable.range(1, 3).publish();
        threeIntegers.subscribe(i -> System.out.println("Observer One: " + i));
        threeIntegers.subscribe(i -> System.out.println("Observer Two: " + i));
        threeIntegers.connect();
    }

    @Test
    public void autoConnect() throws Exception
    {
        Observable<Long> seconds =
                Observable.interval(500, TimeUnit.MILLISECONDS)
                        .publish()
                        .autoConnect();

        seconds.subscribe(i -> System.out.println("Observer 1: " + i));

        Thread.sleep(1000L);

        seconds.subscribe(i -> System.out.println("Observer 2: " + i));

        Thread.sleep(1000L);
    }

    @Test
    public void refCount() throws Exception
    {
        Observable<Long> seconds =
                Observable.interval(500, TimeUnit.MILLISECONDS)
                        .publish()
                        .refCount();

        seconds.take(5)
                .subscribe(l -> System.out.println("Observer 1: " + l));

        Thread.sleep(2000L);

        seconds.take(2)
                .subscribe(l -> System.out.println("Observer 2: " + l));

        Thread.sleep(2000L);

        seconds.subscribe(l -> System.out.println("Observer 3: " + l));

        Thread.sleep(2000L);
    }

    // replaying

    @Test
    public void replay() throws Exception
    {
        Observable<Long> seconds =
                Observable.interval(500, TimeUnit.MILLISECONDS)
                        .replay()
                        .autoConnect();

        seconds.subscribe(l -> System.out.println("Observer 1: " + l));

        Thread.sleep(2000L);

        seconds.subscribe(l -> System.out.println("Observer 2: " + l));

        Thread.sleep(2000L);
    }

    // subjects

    @Test
    public void publishSubject()
    {
        Subject<String> subject =
                PublishSubject.create();

        subject.subscribe(s -> System.out.println("Observer 1: " + s));

        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");

        subject.subscribe(s -> System.out.println("Observer 2: " + s));

        subject.onNext("Delta");
    }

    @Test
    public void behavioralSubject()
    {
        Subject<String> subject =
                BehaviorSubject.create();

        subject.subscribe(s -> System.out.println("Observer 1: " + s));

        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");

        subject.subscribe(s -> System.out.println("Observer 2: " + s));

        subject.onNext("Delta");
    }

    @Test
    public void replaySubject()
    {
        Subject<String> subject =
                ReplaySubject.create();

        subject.subscribe(s -> System.out.println("Observer 1: " + s));

        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");

        subject.subscribe(s -> System.out.println("Observer 2: " + s));

        subject.onNext("Delta");
    }

    @Test
    public void asyncSubject()
    {
        Subject<String> subject =
                AsyncSubject.create();

        subject.subscribe(s -> System.out.println("Observer 1: " + s));

        subject.onNext("Alpha");
        subject.onNext("Beta");
        subject.onNext("Gamma");

        subject.subscribe(s -> System.out.println("Observer 2: " + s));

        subject.onNext("Delta");
        subject.onComplete();
    }

    @Test
    public void unicastSubject() throws Exception
    {
        Subject<String> subject =
                UnicastSubject.create();

        Observable.interval(500, TimeUnit.MILLISECONDS)
                .map(l -> ((l + 1) * 500) + " milliseconds")
                .subscribe(subject);

        Thread.sleep(2000L);

        Observable<String> multicast = subject.publish().autoConnect();

        multicast.subscribe(s -> System.out.println("Observer 1: " + s));

        Thread.sleep(2000L);

        multicast.subscribe(s -> System.out.println("Observer 2: " + s));

        Thread.sleep(2000L);
    }

    // concurrency

    @Test
    public void singleThread()
    {
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .map(JavaRxDemo::intenseCalculation)
                .map(v -> Thread.currentThread().getName() + " " + v)
                .subscribe(System.out::println);

        Observable.range(1,6)
                .map(JavaRxDemo::intenseCalculation)
                .map(v -> Thread.currentThread().getName() + " " + v)
                .subscribe(System.out::println);
    }

    @Test
    public void schedulers() throws Exception
    {
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .subscribeOn(Schedulers.computation())
                .map(JavaRxDemo::intenseCalculation)
                .map(v -> Thread.currentThread().getName() + " " + v)
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Done on: " + Thread.currentThread().getName()));

        Observable.range(1,6)
                .subscribeOn(Schedulers.computation())
                .map(JavaRxDemo::intenseCalculation)
                .map(v -> Thread.currentThread().getName() + " " + v)
                .subscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Done on: " + Thread.currentThread().getName()));

        Thread.sleep(10000L);
    }

    @Test
    public void combiningObservablesFromDifferentThreads() throws Exception
    {
        Observable<String> source1 = Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .subscribeOn(Schedulers.computation())
                .map(JavaRxDemo::intenseCalculation);

        Observable<Integer> source2 = Observable.range(1,6)
                .subscribeOn(Schedulers.computation())
                .map(JavaRxDemo::intenseCalculation);

        Observable.zip(source1, source2, (s,i) -> s + "-" + i)
                .subscribe(System.out::println);

        Thread.sleep(10000L);
    }

    @Test
    public void blocking()
    {
        Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                .subscribeOn(Schedulers.computation())
                .map(JavaRxDemo::intenseCalculation)
                .map(v -> Thread.currentThread().getName() + " " + v)
                .blockingSubscribe(System.out::println,
                        Throwable::printStackTrace,
                        () -> System.out.println("Done on: " + Thread.currentThread().getName()));
    }

    @Test
    public void computation() throws Exception
    {
        System.out.println("cores: " + Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < 10; i++)
        {
            Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                    .subscribeOn(Schedulers.computation())
                    .subscribe(v -> System.out.println(Thread.currentThread().getName() + " " + v));
        }

        Thread.sleep(500L);
    }

    @Test
    public void io() throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                    .subscribeOn(Schedulers.io())
                    .subscribe(v -> System.out.println(Thread.currentThread().getName() + " " + v));
        }

        Thread.sleep(500L);
    }

    @Test
    public void newThread() throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(v -> System.out.println(Thread.currentThread().getName() + " " + v));
        }

        Thread.sleep(500L);
    }

    @Test
    public void singleT() throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                    .subscribeOn(Schedulers.single())
                    .subscribe(v -> System.out.println(Thread.currentThread().getName() + " " + v));
        }

        Thread.sleep(500L);
    }

    @Test
    public void trampoline() throws Exception
    {
        for (int i = 0; i < 10; i++)
        {
            Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                    .subscribeOn(Schedulers.trampoline())
                    .subscribe(v -> System.out.println(Thread.currentThread().getName() + " " + v));
        }

        Thread.sleep(500L);
    }

    @Test
    public void custom()
    {
        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        Scheduler scheduler = Schedulers.from(executor);

        for (int i = 0; i < 10; i++)
        {
            Observable.just("Alpha", "Beta", "Gamma", "Delta", "Epsilon")
                    .subscribeOn(scheduler)
                    .subscribe(v -> System.out.println(Thread.currentThread().getName() + " " + v));
        }
        executor.shutdown();
    }

    @Test
    public void observeOn() throws Exception
    {
        Observable.just("WHISKEY/27653/TANGO", "6555/BRAVO", "232352/5675675/FOXTROT")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> Observable.fromArray(s.split("/")))
                .doOnNext(s -> System.out.println("Split out " + s + " on thread "
                        + Thread.currentThread().getName()))

                .observeOn(Schedulers.computation())
                .filter(s -> s.matches("[0-9]+"))
                .map(Integer::valueOf)
                .reduce((total, next) -> total + next)
                .doOnSuccess(i -> System.out.println("Calculated sum " + i + " on thread "
                        + Thread.currentThread().getName()))

                .observeOn(Schedulers.io())
                .doOnSuccess(s -> System.out.println("Writing " + s + " to file on thread "
                        + Thread.currentThread().getName()))
                .subscribe(s -> System.out.println("Done on: " + Thread.currentThread().getName() + " with value: " + s ));

        Thread.sleep(200L);
    }

    @Test
    public void beforeParallelization()
    {
        Observable.range(1,10)
                .map(JavaRxDemo::intenseCalculation)
                .subscribe(i -> System.out.println("Received " + i + " "
                        + LocalTime.now()));
    }

    @Test
    public void afterParallelization() throws Exception
    {
        Observable.range(1,10)
                .flatMap(i -> Observable.just(i)
                        .subscribeOn(Schedulers.computation())
                        .map(JavaRxDemo::intenseCalculation)
                )
                .subscribe(i -> System.out.println("Received " + i + " "
                        + LocalTime.now() + " on thread "
                        + Thread.currentThread().getName()));

        Thread.sleep(2100L);
    }

    // throttling

    @Test
    public void buffer()
    {
        Observable.range(1,50)
                .buffer(8)
                .subscribe(System.out::println);
    }


    @Test
    public void timeBuffer() throws Exception
    {
        Observable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300)
                .buffer(1, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        Thread.sleep(4000L);
    }

    @Test
    public void cutoffBuffer() throws Exception
    {
        Observable<Long> cutOffs =
                Observable.interval(1, TimeUnit.SECONDS);

        Observable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300)
                .buffer(cutOffs)
                .subscribe(System.out::println);

        Thread.sleep(5000L);
    }

    @Test
    public void window()
    {
        Observable.range(1,50)
                .window(8)
                .flatMapSingle(obs -> obs.reduce("", (total, next) -> total
                        + (total.equals("") ? "" : "|") + next))
                .subscribe(System.out::println);
    }

    @Test
    public void beforeThrottle() throws Exception
    {
        Observable<String> source1 = Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);

        Observable<String> source2 = Observable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);

        Observable<String> source3 = Observable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Observable.concat(source1, source2, source3)
                .subscribe(System.out::println);

        Thread.sleep(6000L);
    }

    @Test
    public void throttle() throws Exception
    {
        Observable<String> source1 = Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);

        Observable<String> source2 = Observable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);

        Observable<String> source3 = Observable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Observable.concat(source1, source2, source3)
                .throttleLatest(1, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        Thread.sleep(6000L);
    }

    @Test
    public void debounce() throws Exception
    {
        Observable<String> source1 = Observable.interval(100, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 100) // map to elapsed time
                .map(i -> "SOURCE 1: " + i)
                .take(10);

        Observable<String> source2 = Observable.interval(300, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 300) // map to elapsed time
                .map(i -> "SOURCE 2: " + i)
                .take(3);

        Observable<String> source3 = Observable.interval(2000, TimeUnit.MILLISECONDS)
                .map(i -> (i + 1) * 2000) // map to elapsed time
                .map(i -> "SOURCE 3: " + i)
                .take(2);

        Observable.concat(source1, source2, source3)
                .throttleWithTimeout(1, TimeUnit.SECONDS)
                .subscribe(System.out::println);

        Thread.sleep(6000);
    }

    // flowable

    @Test
    public void noProblemWhenOneThread() throws Exception
    {
        Observable.range(1, 999_999_999)
                .map(JavaRxDemo.MyItem::new)
                .subscribe(myItem -> {
                    Thread.sleep(50L);
                    System.out.println("Received MyItem " + myItem.id);
                });

        Thread.sleep(6000);
    }

    @Test
    public void problemWithMultiThreading() throws Exception
    {
        Observable.range(1, 999_999_999)
                .map(JavaRxDemo.MyItem::new)
                .observeOn(Schedulers.io())
                .subscribe(myItem -> {
                    Thread.sleep(50);
                    System.out.println("Received MyItem " + myItem.id);
                });

        Thread.sleep(10000L);
    }

    @Test
    public void flowable() throws Exception
    {
        Flowable.range(1, 999_999_999)
                .map(JavaRxDemo.MyItem::new)
                .observeOn(Schedulers.io())
                .subscribe(myItem -> {
                    Thread.sleep(50);
                    System.out.println("Received MyItem " + myItem.id);
                });

        Thread.sleep(10000L);
    }

    @Test
    public void subscriber() throws Exception
    {
        Flowable.range(1,1000)
                .doOnNext(s -> System.out.println("Source pushed " + s))
                .observeOn(Schedulers.io())
                .map(JavaRxDemo::intenseCalculation)
                .subscribe(s -> System.out.println("Subscriber received " + s),
                        Throwable::printStackTrace,
                        () -> System.out.println("Done!")
                );

        Thread.sleep(10000L);
    }

    @Test
    public void manageBackpressure() throws Exception
    {
        Flowable.range(1,1000)
                .doOnNext(s -> System.out.println("Source pushed " + s))
                .observeOn(Schedulers.io())
                .map(JavaRxDemo::intenseCalculation)
                .subscribe(new Subscriber<Integer>()
                {
                    Subscription subscription;
                    AtomicInteger count = new AtomicInteger(0);

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        System.out.println("Requesting 40 items!");
                        subscription.request(40);
                    }
                    @Override
                    public void onNext(Integer s) {
                        sleep(50L);
                        System.out.println("Subscriber received " + s);
                        if (count.incrementAndGet() % 20 == 0 && count.get() >= 40)
                            System.out.println("Requesting 20 more!");
                        subscription.request(20);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Done!");
                    }
                });

        Thread.sleep(35000L);
    }

    @Test
    public void observableIntoFlowable() throws Exception
    {
        Observable<Integer> source = Observable.range(1,1000);
        source.toFlowable(BackpressureStrategy.LATEST)
                .observeOn(Schedulers.io())
                .subscribe(System.out::println);

        Thread.sleep(10000L);
    }

    @Test
    public void flowableGenerate() throws Exception
    {
        randomGenerator(1,10000)
                .subscribeOn(Schedulers.computation())
                .doOnNext(i -> System.out.println("Emitting " + i))
                .observeOn(Schedulers.io())
                .subscribe(i -> {
                    sleep(50);
                    System.out.println("Received " + i);
                });

        Thread.sleep(10000L);
    }

}
