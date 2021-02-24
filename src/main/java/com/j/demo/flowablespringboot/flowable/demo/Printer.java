package com.j.demo.flowablespringboot.flowable.demo;

import org.springframework.stereotype.Component;

/**
 *
 * hello.bpmn20.xml内  flowable:expression="#{printer.printMessage()}"
 *
 *
 */

@Component
public class Printer {

    public void printMessage() {
        System.out.println("hello world from printer");
    }
}