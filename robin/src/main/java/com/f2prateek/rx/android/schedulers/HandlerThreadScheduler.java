package com.f2prateek.rx.android.schedulers;

import android.os.Handler;
import java.util.concurrent.TimeUnit;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

class HandlerThreadScheduler extends Scheduler {
  private final Handler handler;

  HandlerThreadScheduler(Handler handler) {
    this.handler = handler;
  }

  @Override public Worker createWorker() {
    return new InnerHandlerThreadScheduler(handler);
  }

  private static class InnerHandlerThreadScheduler extends Worker {
    private final Handler handler;
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    public InnerHandlerThreadScheduler(Handler handler) {
      this.handler = handler;
    }

    @Override public void unsubscribe() {
      compositeSubscription.unsubscribe();
    }

    @Override public boolean isUnsubscribed() {
      return compositeSubscription.isUnsubscribed();
    }

    @Override public Subscription schedule(final Action0 action, long delayTime, TimeUnit unit) {
      final ScheduledAction scheduledAction = new ScheduledAction(action);
      scheduledAction.add(Subscriptions.create(new Action0() {
        @Override public void call() {
          handler.removeCallbacks(scheduledAction);
        }
      }));
      scheduledAction.addParent(compositeSubscription);
      compositeSubscription.add(scheduledAction);

      handler.postDelayed(scheduledAction, unit.toMillis(delayTime));

      return scheduledAction;
    }

    @Override public Subscription schedule(final Action0 action) {
      return schedule(action, 0, TimeUnit.MILLISECONDS);
    }
  }
}
