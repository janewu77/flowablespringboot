package com.j.demo.flowablespringboot.flowable.demo;


import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.springframework.stereotype.Component;

/**
 * demo flowable eventListener
 *
 */

@Component
public class MyEventListener implements FlowableEventListener {

    @Override
    public void onEvent(FlowableEvent event) {
        switch (event.getType().name()) {

            case "PROCESS_CREATED":
                System.out.println("PROCESS_CREATED!");
                break;

//            case JOB_EXECUTION_FAILURE:
//                System.out.println("A job has failed...");
//                break;

            default:
                System.out.println("Event received: " + event.getType());
        }
    }

    @Override
    public boolean isFailOnException() {
        // onEvent方法中的逻辑并不重要，可以忽略日志失败异常……

        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}