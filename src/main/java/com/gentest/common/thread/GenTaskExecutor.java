package com.gentest.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class GenTaskExecutor extends ThreadPoolTaskExecutor {

    private static final long serialVersionUID = 4691723846357706003L;

    private static final Logger logger = LoggerFactory.getLogger(FundExecTaskCallback.class);
    private ThreadLocal<Map<String, ListenableFuture<?>>> listenableFutures = new ThreadLocal<>();

    private ThreadLocal<Map<String, LinkedBlockingQueue<Future<?>>>> futures = new ThreadLocal<>();

    private Map<String, AtomicInteger> taskRegister = new ConcurrentHashMap<>();

    public GenTaskExecutor() {
        super();
        //无队列，申请不到线程直接拒绝
        setQueueCapacity(0);
        //默认核心线程数为1
        setRejectedExecutionHandler(new MeifanRejectedExecutionHandler());
    }

    public void setName(String poolName) {
        setThreadFactory(new NamedThreadFactory(poolName));
    }

    /**
     * 添加任务
     *
     * @param taskName
     * @param execTaskCallback
     */
    public <T> void addTask(String taskName, FundExecTaskCallback<T> execTaskCallback) {
        try {
            execTaskCallback.setTaskName(taskName);
            Future<T> f = this.submit(wrap(execTaskCallback, MDC.getCopyOfContextMap()));
            registerTask(taskName, f);
        } catch (Exception e) {
            logger.error("Subimt task {} failed", taskName,e);
        }
    }

    private synchronized <T> void registerTask(String taskName, Future<T> f) {
        Map<String, LinkedBlockingQueue<Future<?>>> m = futures.get();
        if (m == null) {
            m = new ConcurrentHashMap<>();
        }
        LinkedBlockingQueue<Future<?>> l = m.get(taskName);
        if (l == null) {
            l = new LinkedBlockingQueue<>();
        }
        l.add(f);
        m.put(taskName, l);
        futures.set(m);
        recordTaskCount(taskName, true);
    }

    /**
     * 添加异步任务
     *
     * @param taskName
     * @param execTaskCallback
     * @param handleResultCallback
     */
    public <T> void addAsyncTask(String taskName, FundExecTaskCallback<T> execTaskCallback, FundHandleResultCallback<T> handleResultCallback) {
        execTaskCallback.setTaskName(taskName);
        try {
            ListenableFuture<T> f = this.submitListenable(wrap(execTaskCallback, MDC.getCopyOfContextMap()));
            registerAsyncTask(taskName, handleResultCallback, f);
        } catch (Exception e) {
            logger.error("Subimt async task {} failed", taskName,e);
        }
    }

    private <T> void registerAsyncTask(String taskName, FundHandleResultCallback<T> handleResultCallback,
                                       ListenableFuture<T> f) {
        f.addCallback(wrap(handleResultCallback, taskName));
        Map<String, ListenableFuture<?>> m = listenableFutures.get();
        if (m == null) {
            m = new ConcurrentHashMap<>();
        }
        m.put(taskName, f);
        listenableFutures.set(m);
        recordTaskCount(taskName, true);
    }

    private int recordTaskCount(String taskName, boolean increase) {
        AtomicInteger cnt = taskRegister.get(taskName);
        if (cnt == null) {
            cnt = new AtomicInteger();
            taskRegister.put(taskName, cnt);
        }
        if (increase) {
            return cnt.incrementAndGet();
        } else {
            return cnt.decrementAndGet();
        }

    }

    /**
     * @param taskName
     * @param timeoutMs
     * @return 只在日志里记录异常，任何异常时都返回null
     */
    public <T> T get(String taskName, long timeoutMs) {
        try {
            return getWithException(taskName, timeoutMs);
        } catch (InterruptedException | ExecutionException | TimeoutException | CancellationException | IllegalArgumentException e) {
            logger.error(String.format("Task %s throws %s:%s, timeout %sms", taskName, e.getClass().getSimpleName(), e.getMessage(), timeoutMs));
        } catch (Exception e) {
            logger.error("Task {} throws Unknown Excepton", taskName, e);
        }
        return null;
    }

    /**
     * @param taskName
     * @param timeoutMs
     * @return
     * @throws Exception 由上层调用者去处理异常
     */
    @SuppressWarnings("unchecked")
    public <T> T getWithException(String taskName, long timeoutMs) throws Exception {
        T v = null;
        Map<String, LinkedBlockingQueue<Future<?>>> m = futures.get();
        LinkedBlockingQueue<Future<?>> l = m.get(taskName);
        if (CollectionUtils.isEmpty(l)) {
            throw new IllegalArgumentException("Task " + taskName + " is not set or submit failed before");
        }
        if (l.size() > 1) {
            throw new IllegalArgumentException("Task " + taskName + " has more then one result, " + l.size());
        }
        Future<?> f = l.take();
        try {
            v = (T) f.get(timeoutMs, TimeUnit.MILLISECONDS);
        } finally {
            recordTaskCount(taskName, false);
            if (!f.isDone()) {
                f.cancel(true);
            }
        }
        return v;
    }

    /**
     * 收集该taskName下所有的返回结果
     *
     * @param taskName
     * @param timeoutMs
     * @return 有异常情况下在日志里记录异常，结果并不添加到返回list中
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getAll(String taskName, long timeoutMs) {
        List<T> vs = new ArrayList<>();
        Map<String, LinkedBlockingQueue<Future<?>>> m = futures.get();
        if(m == null){
            return vs;
        }
        LinkedBlockingQueue<Future<?>> l = m.get(taskName);
        if (CollectionUtils.isEmpty(l)) {
            throw new IllegalArgumentException("task " + taskName + " is not set before, please confirm");
        }
        List<Future<?>> c = new ArrayList<>();
        l.drainTo(c);
        for (Future<?> f : c) {
            try {
                T v = (T) f.get(timeoutMs, TimeUnit.MILLISECONDS);
                vs.add(v);
            } catch (InterruptedException | ExecutionException | TimeoutException | CancellationException e) {
                logger.error(String.format("Task %s throws %s, timeout %sms", taskName, e.getClass().getSimpleName(), timeoutMs), e);
            } catch (Exception e) {
                logger.error("Task {} throws Unknown Excepton", taskName, e);
            } finally {
                recordTaskCount(taskName, false);
                if (!f.isDone()) {
                    f.cancel(true);
                }
            }
        }
        return vs;
    }

    public <T> ListenableFutureCallback<T> wrap(final ListenableFutureCallback<T> callable, final String taskName) {
        return new ListenableFutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                recordTaskCount(taskName, false);
                callable.onSuccess(result);

            }

            @Override
            public void onFailure(Throwable ex) {
                recordTaskCount(taskName, false);
                callable.onFailure(ex);
            }
        };
    }

    public <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                Map<String, String> childMDC = MDC.getCopyOfContextMap();
                if (context == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(context);
                }
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw e;
                } finally {
                    if (childMDC == null) {
                        MDC.clear();
                    } else {
                        MDC.setContextMap(childMDC);
                    }
                }
            }
        };
    }

    /**
     * 超出线程池的等待队列长度后，打印线程池状态
     */
    class MeifanRejectedExecutionHandler implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor pool) {
            throw new RejectedExecutionException("Reject task " + r.toString() + " ," + pool.toString());
        }
    }

    /**
     * 打印threadpool运行状态， 以及哪些任务在运行
     */
    @Override
    public String toString() {
        return this.getThreadPoolExecutor() + ", active tasks:" + taskRegister.toString();
    }


    public static void main(String[] args) throws Exception{
        GenTaskExecutor fundThreadPoolTaskExecutor = new GenTaskExecutor();
        fundThreadPoolTaskExecutor.setName("feed");
        fundThreadPoolTaskExecutor.setMaxPoolSize(1024);

        for (int i=0; i< 10; i++){
            final int _m = i;
            fundThreadPoolTaskExecutor.addAsyncTask("love", new FundExecTaskCallback(){

                @Override
                public Object onExec() throws Exception {
                    logger.info("执行任务："+_m);
                    System.out.println("执行任务："+_m);
                    return null;
                }
            }, new FundHandleResultCallback<String>() {
                @Override
                public void onSucc(String result) {

                }
            });
        }
        Thread.sleep(10000);
    }
}
