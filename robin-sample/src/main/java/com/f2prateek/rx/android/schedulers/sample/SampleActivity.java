package com.f2prateek.rx.android.schedulers.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Button;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.f2prateek.rx.android.schedulers.AndroidSchedulers;
import rx.Observable;
import rx.Subscriber;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class SampleActivity extends Activity {

  Handler handler;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sample_activity);
    ButterKnife.inject(this);

    BackgroundThread backgroundThread = new BackgroundThread();
    backgroundThread.start();
    handler = new Handler(backgroundThread.getLooper());
  }

  static class BackgroundThread extends HandlerThread {
    BackgroundThread() {
      super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
    }
  }

  @OnClick({ R.id.hello, R.id.hey, R.id.hi }) public void greetingClicked(final Button button) {
    Observable.create(new Observable.OnSubscribe<Void>() {
      @Override public void call(Subscriber<? super Void> subscriber) {
        try {
          Thread.sleep(10 * 1000);
          if (!subscriber.isUnsubscribed()) {
            subscriber.onCompleted();
          }
        } catch (InterruptedException e) {
          if (!subscriber.isUnsubscribed()) {
            subscriber.onError(e);
          }
        }
      }
    })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(AndroidSchedulers.handlerThread(handler))
        .subscribe(new Subscriber<Void>() {
          @Override public void onCompleted() {
            button.setText("Boo!");
          }

          @Override public void onError(Throwable e) {

          }

          @Override public void onNext(Void aVoid) {

          }
        });
  }
}
