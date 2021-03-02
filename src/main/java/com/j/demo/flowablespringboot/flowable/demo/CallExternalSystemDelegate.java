package com.j.demo.flowablespringboot.flowable.demo;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;


/**
 * flowable:class="com.j.demo.flowablespringboot.flowable.demo.CallExternalSystemDelegate"
 */
public class CallExternalSystemDelegate implements JavaDelegate {

    public void execute(DelegateExecution execution) {
        System.out.println("Calling the external system for employee "
                + execution.getVariable("employee"));
    }

}