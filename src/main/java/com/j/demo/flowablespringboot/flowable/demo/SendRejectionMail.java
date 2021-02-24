package com.j.demo.flowablespringboot.flowable.demo;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;


/**
 * demo:flowable:class
 * flowable:class="com.j.demo.flowablespringboot.flowable.demo.SendRejectionMail"
 *
 *
 */
public class SendRejectionMail implements JavaDelegate {

    public void execute(DelegateExecution execution) {
        System.out.println("send reject email to "
                + execution.getVariable("employee"));
    }

}