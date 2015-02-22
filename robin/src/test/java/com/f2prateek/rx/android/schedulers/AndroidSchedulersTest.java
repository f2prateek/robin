package com.f2prateek.rx.android.schedulers;

import android.os.Handler;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class) //
@Config(manifest = Config.NONE) //
public class AndroidSchedulersTest {

  @Test public void shouldScheduleImmediateActionOnHandlerThread() {
    Handler handler = mock(Handler.class);
    @SuppressWarnings("unchecked") Action0 action = mock(Action0.class);

    Scheduler scheduler = AndroidSchedulers.handlerThread(handler);
    Worker inner = scheduler.createWorker();
    inner.schedule(action);

    // verify that we post to the given Handler
    ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
    verify(handler).postDelayed(runnable.capture(), eq(0L));

    // verify that the given handler delegates to our action
    runnable.getValue().run();
    verify(action).call();
  }

  @Test public void shouldScheduleDelayedActionOnHandlerThread() {
    Handler handler = mock(Handler.class);
    @SuppressWarnings("unchecked") Action0 action = mock(Action0.class);

    Scheduler scheduler = AndroidSchedulers.handlerThread(handler);
    Worker inner = scheduler.createWorker();
    inner.schedule(action, 1L, TimeUnit.SECONDS);

    // verify that we post to the given Handler
    ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
    verify(handler).postDelayed(runnable.capture(), eq(1000L));

    // verify that the given handler delegates to our action
    runnable.getValue().run();
    verify(action).call();
  }

  @Test public void shouldRemoveCallbacksFromHandlerWhenUnsubscribedSubscription() {
    Handler handler = spy(new Handler());
    Observable.OnSubscribe<Integer> onSubscribe = mock(Observable.OnSubscribe.class);
    Subscription subscription = Observable.create(onSubscribe)
        .subscribeOn(AndroidSchedulers.handlerThread(handler))
        .subscribe();

    verify(onSubscribe).call(Matchers.any(Subscriber.class));

    subscription.unsubscribe();

    verify(handler).removeCallbacks(Matchers.any(Runnable.class));
  }

  @Test public void shouldNotCallOnSubscribeWhenSubscriptionUnsubscribedBeforeDelay() {
    Observable.OnSubscribe<Integer> onSubscribe = mock(Observable.OnSubscribe.class);
    Handler handler = spy(new Handler());

    Scheduler scheduler = AndroidSchedulers.handlerThread(handler);
    Worker worker = spy(scheduler.createWorker());

    Scheduler spyScheduler = spy(scheduler);
    when(spyScheduler.createWorker()).thenReturn(worker);

    Subscription subscription = Observable.create(onSubscribe)
        .delaySubscription(1, TimeUnit.MINUTES, spyScheduler)
        .subscribe();

    verify(worker).schedule(Matchers.any(Action0.class), Matchers.eq(1L),
        Matchers.eq(TimeUnit.MINUTES));
    verify(handler).postDelayed(Matchers.any(Runnable.class),
        Matchers.eq(TimeUnit.MINUTES.toMillis(1L)));

    subscription.unsubscribe();

    Robolectric.runUiThreadTasksIncludingDelayedTasks();

    verify(onSubscribe, never()).call(Matchers.any(Subscriber.class));
    verify(handler).removeCallbacks(Matchers.any(Runnable.class));
  }
}