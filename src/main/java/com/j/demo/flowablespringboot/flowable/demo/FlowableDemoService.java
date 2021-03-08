package com.j.demo.flowablespringboot.flowable.demo;

import com.j.demo.flowablespringboot.flowable.FlowableService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormRepositoryService;

import org.flowable.engine.*;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Component
public class FlowableDemoService {

    @Autowired
    FlowableService flowableService;

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    FormService formService;

    @Autowired
    FormRepositoryService formRepositoryService;



    public void demoCompleteTask(String taskId){
        taskService.complete(taskId);
    }


    public void demoCompleteTaskWithForm(String taskId){

        Task task = flowableService.fetchTaskFromRuntime(taskId);

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("other",  task.getName()+".other.value");  //实际上没有保存
        variables.put("operation", task.getName()+".operation.value");//required
        variables.put("operation_remark", task.getName()+".operation_remark.value");
        variables.put("remark", task.getName()+".remark.value");
        variables.put("approve",true);

        flowableService.doCompleteTask(taskId,variables);
    }


    //演示：
    //演示变量在task、execution、process上的可见性
    //流程由二个user task组成
    public void demoSetVarsInProcess(){

        String pKey = "DemoFormProcess";

        //启动一个流程
        System.out.println("1.startProcess:" + "pKey");
        ProcessInstance processInstance = flowableService.startProcess(pKey);

        //取得第一个task : <userTask id="taskform1" name ="usertask1" >
        Optional<Task> optTask = _getTask(processInstance.getId(),"taskform1");

        if(optTask.isPresent()){
            Task task1 = optTask.get();
            String taskId = task1.getId();

            System.out.println("2.taskinfo before set variables");
            flowableService.fetchTaskFromHistory(taskId);

            //设置变量
            task1.setAssignee("usertask1.do.myAssignee");
            task1.setOwner("usertask1.do.myOwner");
            taskService.saveTask(task1);
//            taskService.saveTask(task1);


            //可见: task
            taskService.setVariableLocal(task1.getId(),"task1.VariableLocal","task1.VariableLocal1.value");

            //可见：task、Execution、process
            taskService.setVariable(taskId,"task1.Variable","task1.Variable.value");

            //execute variable
            String execId = task1.getExecutionId();

            //可见：Execution
            runtimeService.setVariableLocal(execId,"task1.Execution.VariableLocal","task1.Execution.VariableLocal.value");;

            //可见：task、Execution、process
            runtimeService.setVariable(execId,"task1.Execution.Variable","task1.Execution.Variable.value");

            System.out.println("3.taskinfo after set variables");
            flowableService.fetchTaskFromHistory(taskId);

            //complete
            System.out.println("4.complete task1");
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("task1.complete.var","task1.complete.value");
            taskService.complete(taskId,variables);

            System.out.println("5.taskinfo after complete task1");
            flowableService.fetchTaskFromHistory(taskId);

        }else{
            System.out.println("not found task:" + "taskform1");
        }

        //task2:
        //取得task : <userTask id="taskform2" name ="usertask2" flowable:formKey="default-approve-form">
        Optional<Task> optTask2 = _getTask(processInstance.getId(),"taskform2");

        if(optTask2.isPresent()){
            Task task = optTask2.get();
            String taskId = task.getId();

            System.out.println("6.taskinfo task2");
            flowableService.fetchTaskFromHistory(taskId);

            //complete task2
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("task2.complete.var","task2.complete.value");
            taskService.complete(taskId,variables);

            System.out.println("7.taskinfo task2 after complete task2");
            flowableService.fetchTaskFromHistory(taskId);

        }else{
            System.out.println("not found task::" + "taskform2");
        }
    }


    //demo
    public void demoDeployFromClasspath(){
        //每次都会delopy，生成新的版本。（无论xml的内容是否有变化）
        String filename = "pre-processes/demo-process2.bpmn20.xml";
        flowableService.deployProcessFromClasspath(filename);

        //当active日期设为未来时，所有已发布版本都不可发起流程
//        Date activeDate = new Date(1648742400000L);//1648742400: 2022.4.1
//        deployFromClasspath(filename,activeDate);
    }

    //demo
    public void demoDeployFromInputStream(){
        InputStream inputStream = null;
        String filename = "";
        try {

            //String filename = "pre-processes/demo-process2.bpmn20.xml";
            //InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);

//            String filename = "/Users/janewu/janewu/demo-process2.bpmn20.xml";
//            File initialFile = new File(filename);
//            final InputStream inputStream = new DataInputStream(new FileInputStream(initialFile));

            filename = "src/main/resources/pre-processes/demo-process2.bpmn20.xml";
            File initialFile = new File(filename);
            inputStream = new FileInputStream(initialFile);

        }catch (FileNotFoundException e){
            System.out.println(e);
        }

        Deployment deployment = flowableService.deployProcessFromInputStream("process2.bpmn20.xml",inputStream);
    }


    //
    public ProcessInstance demoStartProcess_DemoFormProcess() {
        String Demo_FormProcessKey = "DemoFormProcess";
        return flowableService.startProcess(Demo_FormProcessKey);
    }

    //临时用，取得当前有效任务
    private Optional<Task> _getTask(String processInstanceId, String taskKey){
        Optional<Task> optTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(taskKey)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .listPage(0,1)
                .stream().findFirst();
        return optTask;
    }
}
