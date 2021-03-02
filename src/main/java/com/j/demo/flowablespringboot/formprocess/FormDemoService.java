package com.j.demo.flowablespringboot.formprocess;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.*;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.form.TaskFormData;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Component
public class FormDemoService {


    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    FormService formService;

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;


    final static public String ProcessKey = "formProcess";
    final static private String ProcessDefRes = "pre-processes/form.bpmn20.xml";

    //发布流程
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


    //取得starter节点上的form信息(获取用于显示表单的参数)
    public List<FormProperty> fetchStarterForm() {

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(ProcessKey).latestVersion()
                .singleResult();

        //表单属性列表
        List<FormProperty> propertyList = formService.getStartFormData(processDefinition.getId()).getFormProperties();
        System.out.println("startFormData.getFormProperties().size:" + propertyList.size());

        //runtimeService.getStartFormModel()
        return propertyList;
    }

    public List<FormProperty> fetchTaskForm(String taskId) {
        TaskFormData taskFormData = formService.getTaskFormData(taskId);

        System.out.println("taskId:" + taskId);
        System.out.println("taskFormData.getFormProperties():" + taskFormData.getFormProperties().size());
        return taskFormData.getFormProperties();
    }


    //启动一个流程（用form)
    public ProcessInstance startProcess(){

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(ProcessKey).latestVersion()
                .singleResult();

        Integer randomInt = new Random().nextInt();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("room",randomInt);
        variables.put("random", randomInt);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ProcessKey,variables);

//        ProcessInstance processInstance = runtimeService.startProcessInstanceWithForm(processDefinition.getId(),
//                "startBy",variables,"startByForm");

        System.out.println("processInstance.getId():"+processInstance.getId());
        System.out.println("processInstance.getName():"+processInstance.getName());
        System.out.println("processInstance.getProcessVariables().size():"+processInstance.getProcessVariables().size());

        return processInstance;
    }

    //取得本流程的所有任务（带变量）
    public void fecthAllTask(){

        List<Task> tasks = taskService.createTaskQuery()
                .includeProcessVariables()//带上变量
                .processDefinitionKey(ProcessKey).list();

//        runtimeService.getVariables(task.getProcessInstanceId()).size());
        for(Task task:tasks){
            System.out.println("===task.getId():"+  task.getId());
            System.out.println("===task.getName():"+  task.getName());
            Map<String, Object> taskVariables = task.getProcessVariables();
            for(String k: taskVariables.keySet()){
                System.out.println("   task:"+k+":"+taskVariables.get(k));
            }
            System.out.println("   task.getProcessVariables().size():"+ task.getProcessVariables().size());

            //taskService.complete(task.getId());
        }

    }

    public void fetchTaskInfo(String taskId){

        try {
            Task task = taskService.createTaskQuery().includeProcessVariables().taskId(taskId).singleResult();
            _showTaskInfo(task);
        }catch (NullPointerException e){
            System.out.println("===task not found:"+taskId );
        }

        //task form model
//        FormInfo formInfo = taskService.getTaskFormModel(taskId);
//        System.out.println("===task:formInfo.getId() "+formInfo.getId() );
//        System.out.println("===task:formInfo.getName() "+formInfo.getName() );
//        System.out.println("===task:formInfo.getDescription() "+formInfo.getDescription() );
//        System.out.println("===task:formInfo.getKey() "+formInfo.getKey() );
//        System.out.println("===task:formInfo.getVersion() "+formInfo.getVersion() );
//        FormModel formModel = formInfo.getFormModel();



        Integer random = (Integer) taskService.getVariable(taskId,"random");
        System.out.println("===random:"+random );

        Integer X1random = (Integer) taskService.getVariable(taskId,"X1random");
        System.out.println("===X1random:"+X1random );

    }

    public void fetchTaskInfoFromHistory(String taskId){
        HistoricTaskInstance historicTaskInstance = historyService
                .createHistoricTaskInstanceQuery()
                .taskId(taskId)
                //.finished()
                .includeTaskLocalVariables().singleResult();

        _showTaskInfo(historicTaskInstance);
    }

    private void _showTaskInfo(TaskInfo task){

        //historyService.createHistoricTaskInstanceQuery().taskId(taskId);

        System.out.println("===task.getId():"+  task.getId());
        System.out.println("===task.getName():"+  task.getName());

        //process variables
        Map<String, Object> taskVariables = task.getProcessVariables();
        System.out.println("===task.getProcessVariables:"+taskVariables.size());
        for(String k: taskVariables.keySet()){
            System.out.println("===task:"+k+":"+taskVariables.get(k));
        }

        //task local variable
        Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();
        System.out.println("===task.getTaskLocalVariables:"+taskLocalVariables.size());
        for(String k: taskLocalVariables.keySet()){
            System.out.println("===task:"+k+":"+taskLocalVariables.get(k));
        }

        try {
            //task  form data
            TaskFormData taskFormData = formService.getTaskFormData(task.getId());
            List<FormProperty> formPropertyList = taskFormData.getFormProperties();
            if (formPropertyList.size() > 0) {
                System.out.println("===task.formPropertyList:"+formPropertyList.size() );
                for (FormProperty p : formPropertyList) {
                    System.out.println("=== p.getId():" + p.getId());
                    System.out.println("=== p.getType():" + p.getType());
                    System.out.println("=== p.getName():" + p.getName());
                    System.out.println("=== p.getValue():" + p.getValue());
                }
            }
        }catch (FlowableObjectNotFoundException e){
            System.out.println("=== FlowableObjectNotFoundException");
        }
    }

    public void doCompleteTask(String taskId) {

        Integer randomInt = new Random().nextInt();

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("room",randomInt);  //修改原有变量
        variables.put("X1room",randomInt);
        variables.put("X1random", randomInt);
        taskService.complete(taskId,variables);

//        taskService.completeTaskWithForm(taskId, formDefinitionId, outcome2, formProperties2);
//
//        //获取个人任务表单
        //FormModel taskFM = taskService.getTaskFormModel(taskId);
//
    }


}
