package com.safeheron.common.thread;

import org.springframework.util.concurrent.ListenableFutureCallback;

public abstract class FundHandleResultCallback<T> implements ListenableFutureCallback<T> {
    
    public abstract void onSucc(T result);
    
    /**
     * 继承类可以override来处理错误情况
     */
    public void onFail() {};
    
    @Override
    public void onSuccess(T result) {
        onSucc(result);
    }

    @Override
    public void onFailure(Throwable t) {
        onFail();
    }
    
}
