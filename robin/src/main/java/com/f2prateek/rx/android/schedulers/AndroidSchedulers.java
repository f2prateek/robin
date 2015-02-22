package com.f2prateek.rx.android.schedulers;

import android.os.Handler;
import android.os.Looper;
import rx.Scheduler;

/** Schedulers that have Android-specific functionality. */
public final class AndroidSchedulers {
  private AndroidSchedulers() {
    throw new AssertionError("No instances");
  }

  private static final Scheduler MAIN_THREAD_SCHEDULER =
      new HandlerThreadScheduler(new Handler(Looper.getMainLooper()));

  /** {@link Scheduler} which uses the provided {@link Handler} to execute actions. */
  public static Scheduler handlerThread(final Handler handler) {
    return new HandlerThreadScheduler(handler);
  }

  /** {@link Scheduler} which will execute actions on the Android UI thread. */
  public static Scheduler mainThread() {
    return MAIN_THREAD_SCHEDULER;
  }
}
