package com.j.demo.flowablespringboot.flowable;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.*;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormDefinition;
import org.flowable.form.api.FormInfo;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.form.model.FormField;
import org.flowable.form.model.SimpleFormModel;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
public class FlowableService {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    TaskService taskService;

    @Autowired
    FormService formService;

    @Autowired
    HistoryService historyService;

    @Autowired
    FormRepositoryService formRepositoryService;


    //取得starter节点上的form信息(获取用于显示表单的参数)
//    public List<FormProperty> fetchFormPropertyList_Starter(String ProcessKey) {
//
//        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
//                .processDefinitionKey(ProcessKey).latestVersion()
//                .singleResult();
//
//        //表单属性列表
//        StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
//        List<FormProperty> formPropertyList = startFormData.getFormProperties();
//
//        _printFormProperty(formPropertyList,"processDefinitionId:"+processDefinition.getId());
//
//        //runtimeService.getStartFormModel()
//
//        return formPropertyList;
//    }

//    public List<FormProperty> fetchFormPropertyList_Task(String taskId) {
//
//        try{
//            TaskFormData taskFormData = formService.getTaskFormData(taskId);
//            List<FormProperty> formPropertyList = taskFormData.getFormProperties();
//
//            _printFormProperty(formPropertyList,"taskid:"+taskId);
//            return formPropertyList;
////
//        }catch (FlowableObjectNotFoundException e) {
//
//        }
//        return new ArrayList<FormProperty>();
//    }


    //完成某项任务
    public void doCompleteTask(String taskId, Map<String, Object> variables){

        //取得task
        Task task = fetchTaskFromRuntime(taskId);

        //取得表单定义
        FormDefinition formDefinition = null;
//        try {
        formDefinition = formRepositoryService.createFormDefinitionQuery()
                .formDefinitionKey(task.getFormKey())
                .latestVersion()
                .singleResult();
//        }catch(){
//
//        }

        //处理 variables
        //存task VariableLocal
        for(String key: variables.keySet()){
            String value = variables.get(key).toString();
            taskService.setVariableLocal(taskId,key,value);
        }

//        formService.saveFormData(taskId,variables);

//        if(formDefinition == null){
//            taskService.complete(taskId,variables);
//        }else {

        //complete时，只接收formDefinition内定义的数据
        //当require不存时，会抛出exception
        taskService.completeTaskWithForm(taskId, formDefinition.getId(), null, variables);

//        }

    }

    //显示task form表单（且带出已有数据）
    public FormInfo fetchForm4Task(String taskId){
        //form
        try {
            System.out.println("taskService.getTaskFormModel(taskId)");
            FormInfo formInfo = taskService.getTaskFormModel(taskId);
            _print(formInfo);
            return formInfo;
        }catch(FlowableObjectNotFoundException e){
            //没有设定时 flowable:formKey="default-approve-form"
            System.out.println("");
            System.out.println("[error]===FlowableObjectNotFoundException:taskService.getTaskFormModel(taskId);" );
            System.out.println("");
            throw e;
        }
    }


    //取得本流程的活跃instance的所有任务（不带变量）
    public List<HistoricTaskInstance> fecthTaskListFromHistory(String ProcessKey){

        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
//                .includeProcessVariables()
//                .includeTaskLocalVariables()
                .processUnfinished()
                .unfinished() //task unfinished
                .processDefinitionKey(ProcessKey).list();

        return tasks;
    }

    public Task fetchTaskFromRuntime(String taskId){
        System.out.println("===task found from runtime(taskService):"+taskId );
        Task task = taskService.createTaskQuery()
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .taskId(taskId).singleResult();
        return task;
    }

    public HistoricTaskInstance fetchTaskFromHistory(String taskId){

        System.out.println("===task found from history:"+taskId );
        HistoricTaskInstance historicTaskInstance = historyService
                .createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .includeIdentityLinks()
                .singleResult();

        _print(historicTaskInstance);

        //variables(history)
        fetchTaskHistoricVariables(historicTaskInstance);
        //varibales(runtime)
        fetchTaskRuntimeVariables(historicTaskInstance);

        //form
        fetchForm4Task(taskId);


        return historicTaskInstance;
    }


    private void fetchTaskHistoricVariables(TaskInfo task){
        System.out.println("-------HistoricVariable.begin------");
        List<HistoricVariableInstance> varList_task = historyService.createHistoricVariableInstanceQuery()
                .taskId(task.getId())
                .orderByVariableName().asc()
                .list();
        _print(varList_task,"taskId");

        List<HistoricVariableInstance> varList_execute = historyService.createHistoricVariableInstanceQuery()
                .executionId(task.getExecutionId())
                .orderByVariableName().asc()
                .list();
        _print(varList_execute,"executionId");

        List<HistoricVariableInstance> varList = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .orderByVariableName().asc()
                .list();
        _print(varList,"processInstanceId");
        System.out.println("-------HistoricVariable.end------");
    }


    private void fetchTaskRuntimeVariables(TaskInfo task){

        try {
            System.out.println("-------RuntimeVariables.begin------");

            Map<String, Object> varList_task = taskService.getVariables(task.getId());
            _print(varList_task,"taskService.getVariables.taskId");

            Map<String, Object> varList_task_local = taskService.getVariablesLocal(task.getId());
            _print(varList_task_local,"taskService.getVariablesLocal.taskId");

            Map<String, Object> varList_execution = runtimeService.getVariables(task.getExecutionId());
            _print(varList_execution,"runtimeService.getVariables.ExecutionId");

            Map<String, Object> varList_execution_local = runtimeService.getVariablesLocal(task.getExecutionId());
            _print(varList_execution_local,"runtimeService.getVariablesLocal.ExecutionId");


        }catch (FlowableObjectNotFoundException e){
            System.out.println("===FlowableObjectNotFoundException in fetchTaskRuntimeVariables");
        }finally {
            System.out.println("-------RuntimeVariables.End------");
        }
    }


    //启动一个流程
    public ProcessInstance startProcess(String ProcessKey){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ProcessKey);
        _print(processInstance);
        return processInstance;
    }


    //DEPLOY
    //发布流程：以inputStream形式发布
    public Deployment deployProcessFromInputStream(String rname, InputStream inputStream){
        Deployment deployment = repositoryService.createDeployment()
                .name("DeployFromInputStreamN")
                .category("DeployFromInputStreamC")  //deploy category
                .addInputStream(rname,inputStream)//
                .deploy();

        _print(deployment);
        return deployment;
    }

    //发布流程：指定本地目录进行发布
    public Deployment deployProcessFromClasspath(String filename){
        Deployment deployment = repositoryService.createDeployment()
                .name("deployFromClasspathN")
                .category("deployFromClasspathC")  //deploy category
                .addClasspathResource(filename)
                .deploy();

        _print(deployment);
        return deployment;
    }

    //发布流程：指定本地目录 & 指定生效日期
    public Deployment deployProcessFromClasspath(String filename, Date activeDate){
        Deployment deployment = repositoryService.createDeployment()
                .activateProcessDefinitionsOn(activeDate)
                .name("deployFromClasspathN")
                .category("deployFromClasspathCWithDate")  //deploy category
                .addClasspathResource(filename)
                .deploy();

        _print(deployment);
        return deployment;
    }


    //for debug
    private void _print(Deployment deployment){
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("deploymentID  : " + deployment.getId());
        System.out.println("deploymentname : " + deployment.getName());
        System.out.println("Found process definition : " + processDefinition.getName());
        System.out.println("Found process getVersion : " + processDefinition.getVersion());
    }

    private void _print(ProcessInstance processInstance){
        System.out.println("processInstance.getId():"+processInstance.getId());
        System.out.println("processInstance.getName():"+processInstance.getName());
        System.out.println("processInstance.getProcessVariables().size():"+processInstance.getProcessVariables().size());
    }

    private void _print(HistoricTaskInstance task){

        System.out.println("===task info begin ID:["+task.getId()+"]=======");
        System.out.println("===task.getName():"+  task.getName());
        System.out.println("===task.getOwner():"+  task.getOwner());
        System.out.println("===task.getAssignee():"+  task.getAssignee());
        System.out.println("===task.getExecutionId():"+  task.getExecutionId());
        System.out.println("===task.getTaskDefinitionKey():"+  task.getTaskDefinitionKey());
        System.out.println("===task.getFormKey():"+  task.getFormKey());

        //System.out.println("===task.getOwner():"+  task.get());

        System.out.println("-------h.begin------");
        System.out.println("===task.h.getClaimTime():"+  task.getClaimTime());
        System.out.println("===task.h.getEndTime():"+  task.getEndTime());
        System.out.println("===task.h.getWorkTimeInMillis():"+  task.getWorkTimeInMillis());
        System.out.println("===task.h.getDurationInMillis():"+  task.getDurationInMillis());
        System.out.println("===task.h.getDeleteReason():"+  task.getDeleteReason());
        System.out.println("-------h.end------");

        //Process Variables
        Map<String, Object> taskVariables = task.getProcessVariables();
        System.out.println("===task.getProcessVariables().size():"+ taskVariables.size());
        for(String k: taskVariables.keySet()){
            System.out.println("   task.ProcessVariables:"+k+":"+taskVariables.get(k));
        }

        //task local variable
        Map<String, Object> taskLocalVariables = task.getTaskLocalVariables();
        System.out.println("===task.taskLocalVariables.size():"+taskLocalVariables.size());
        for(String k: taskLocalVariables.keySet()){
            System.out.println("   TaskLocalVariables:"+k+":"+taskLocalVariables.get(k));
        }

        System.out.println("===task info End ==========");
        System.out.println("============================");
    }

    private void _print(List<HistoricVariableInstance> varList, String name){

        System.out.println("===["+name+"].HistoricVariableInstance.varList.size():" + varList.size());
        for (HistoricVariableInstance var : varList) {
//            System.out.println("   HistoricVariableInstance var.getId()" + var.getId());
            System.out.println("   var.getVariableName()" + var.getVariableName()+":"+var.getValue());
//            System.out.println("   HistoricVariableInstance var.getValue()" + var.getValue());
//            System.out.println("   HistoricVariableInstance var.getId()" + var.getId());
        }
        System.out.println("===["+name+"]End");
        System.out.println("");
    }

    private void _print(Map<String, Object> vars,String name){
        System.out.println("===["+name+"].HistoricVariableInstance.varList.size():" + vars.size());

        for (String k : vars.keySet()) {
            System.out.println("   var:" + k + ":" + vars.get(k));
        }

        System.out.println("===["+name+"]End");
        System.out.println("");
    }

    public void _printFormProperty(List<FormProperty> formPropertyList, String name){

        System.out.println("===["+name+"]formPropertyList.size():"+formPropertyList.size() );
        for (FormProperty p : formPropertyList) {
            System.out.println("=== p.getId():" + p.getId());
            System.out.println("=== p.getType():" + p.getType());
            System.out.println("=== p.getName():" + p.getName());
            System.out.println("=== p.getValue():" + p.getValue());

            System.out.println("=== p.isRequired():" + p.isRequired());
            System.out.println("=== p.isWritable():" + p.isWritable());
            System.out.println("=== p.isReadable():" + p.isReadable());
            System.out.println("");

        }
        System.out.println("===["+name+"]end");
        System.out.println("");
        System.out.println("");
    }

    private void _print(FormInfo formInfo){
        System.out.println("===formInfo.getId() "+formInfo.getId() );
        System.out.println("===formInfo.getName() "+formInfo.getName() );
        System.out.println("===formInfo.getDescription() "+formInfo.getDescription() );
        System.out.println("===formInfo.getKey() "+formInfo.getKey() );
        System.out.println("===formInfo.getVersion() "+formInfo.getVersion() );

//        FormModel formModel = formInfo.getFormModel();
        SimpleFormModel formModel = (SimpleFormModel)formInfo.getFormModel();
        //System.out.println("formModel:"+formModel);
        Map<String, FormField> fieldsMap = formModel.allFieldsAsMap();

        System.out.println("===formModel.getName() "+formModel.getName() );
        System.out.println("===formModel.getVersion() "+formModel.getVersion() );
        System.out.println("===formModel.getKey() "+formModel.getKey() );
        System.out.println("===formModel.getDescription() "+formModel.getDescription() );
        System.out.println("===formModel.getClass() "+formModel.getClass() );


        for(String k : fieldsMap.keySet()){
            FormField field = fieldsMap.get(k);
            System.out.println("formModel.field.getId()=" + field.getId());
            System.out.println("formModel.field.getName()=" + field.getName());
            System.out.println("formModel.field.getType()=" + field.getType());
            System.out.println("formModel.field.getValue()=" + field.getValue());

            System.out.println("formModel.field.getPlaceholder()=" + field.getPlaceholder());
            //field.getParams()
            // System.out.println("formModel.field.getValue()=" + field.getValue());

        }

    }

}
