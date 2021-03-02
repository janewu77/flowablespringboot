package com.j.demo.flowablespringboot.hello;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * demo:
 * <serviceTask id="sayHello" flowable:expression="#{helloService.sayHello(name)}" />
 *
 *
 */


@Component
public class HelloService {

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    final static public String ProcessKey = "helloProcess";
    final static private String ProcessDefRes = "pre-processes/hello.bpmn20.xml";


    public String sayHello(String name) {
        System.out.println("hello, " + name + " from HelloService.sayHello");
        return name;
    }


    public ProcessInstance startProcessInstance(String name){
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("name", name);
        return runtimeService.startProcessInstanceByKey(ProcessKey,variables);
    }


    public void doDeploy(){
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource(ProcessDefRes)
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("deploymentID  : " + deployment.getId());
        System.out.println("Found process definition : " + processDefinition.getName());

    }


}