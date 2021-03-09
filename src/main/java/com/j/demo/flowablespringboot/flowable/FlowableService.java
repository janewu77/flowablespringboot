package com.j.demo.flowablespringboot.flowable;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Attachment;
import org.flowable.form.api.FormDefinition;
import org.flowable.form.api.FormInfo;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
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
    public Task doCompleteTask(String taskId, Map<String, Object> variables) {

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
        //todo:检查variables

        //存task VariableLocal
        for (String key : variables.keySet()) {
            String value = variables.get(key).toString();
            taskService.setVariableLocal(taskId, key, value);
        }

//        formService.saveFormData(taskId,variables);

//        if(formDefinition == null){
//            taskService.complete(taskId,variables);
//        }else {

        //complete时，只接收formDefinition内定义的数据
        //当require不存时，会抛出exception
        taskService.completeTaskWithForm(taskId, formDefinition.getId(), null, variables);

        return task;
    }

    //完成某项任务(带上传的附件）
    public Task doCompleteTask(String taskId,
                               Map<String, Object> variables,
                               MultipartFile attachFile) {

        Task task = doCompleteTask(taskId,variables);

        //处理上传的附件
        //MultipartFile attachFile = (MultipartFile)variables.get("UPLOADFILE");
        if( attachFile == null) {
            return task;
        }
        _saveAttachment(task, attachFile);
        return task;
    }

    //保存附件
    private String _saveAttachment(Task task, MultipartFile file) {
        String attachmentId = null;

        if (file.isEmpty()) {
            return attachmentId;
        }

        try {
            InputStream contentInputStream = file.getInputStream();

            // Get the file and save it somewhere
            //for debug
//                byte[] bytes = file.getBytes();
//                Path path = Paths.get("/Users/janewu/1upload.txt");// + file.getOriginalFilename());
//                Files.write(path, bytes);

            String desc = task.getId() + ".attachementdescripton." + file.getOriginalFilename();

            Attachment attachment = taskService.createAttachment(file.getContentType(),
                    task.getId(), task.getProcessInstanceId(),
                    file.getName(), desc,
                    contentInputStream);
            //taskService.saveAttachment(attachment);

            //for debug
            attachmentId = attachment.getId();// attachment.getContentId();
            System.out.println("===attachmentId:" + attachmentId);

            taskService.setVariableLocal(task.getId(), "sj.attachmentId", attachmentId);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return attachmentId;

    }


    //显示task form表单（且带出已有数据）
    public FormInfo fetchForm4Task(String taskId){
        //form
        try {
            System.out.println("taskService.getTaskFormModel(taskId)");
            FormInfo formInfo = taskService.getTaskFormModel(taskId);
            FlowUtil.print(formInfo);
            return formInfo;
        }catch(FlowableObjectNotFoundException e){
            //没有设定时 flowable:formKey="default-approve-form"
            System.out.println("");
            System.out.println("[error]===FlowableObjectNotFoundException:taskService.getTaskFormModel(taskId);" );
            System.out.println("");
            throw e;
        }
    }


    //取得本流程的活跃instance的未完成任务（不带变量）
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
//                .includeProcessVariables()
//                .includeTaskLocalVariables()
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


        FlowUtil.print(historicTaskInstance);

        //variables(history)
        fetchTaskHistoricVariables(historicTaskInstance);
        //varibales(runtime)
        fetchTaskRuntimeVariables(historicTaskInstance);

        //form
        fetchForm4Task(taskId);

        //attachment
        String attachmentId = (String)historicTaskInstance.getTaskLocalVariables().get("sj.attachmentId");
        if(attachmentId != null){
            Attachment attachment = taskService.getAttachment(attachmentId);
            FlowUtil.print(attachment);
            //取得上传的内容
            //InputStream inputStream = contentService.getContentItemData(attachmentId);

            //for debug
//                byte[] bytes = file.getBytes();
//                Path path = Paths.get("/Users/janewu/1upload.txt");// + file.getOriginalFilename());
//                Files.write(path,bytes)

        }

        return historicTaskInstance;
    }


    private void fetchTaskHistoricVariables(TaskInfo task){
        System.out.println("-------HistoricVariable.begin------");
        List<HistoricVariableInstance> varList_task = historyService.createHistoricVariableInstanceQuery()
                .taskId(task.getId())
                .orderByVariableName().asc()
                .list();
        FlowUtil.print(varList_task,"taskId");



        List<HistoricVariableInstance> varList_execute = historyService.createHistoricVariableInstanceQuery()
                .executionId(task.getExecutionId())
                .orderByVariableName().asc()
                .list();
        FlowUtil.print(varList_execute,"executionId");

        List<HistoricVariableInstance> varList = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .orderByVariableName().asc()
                .list();
        FlowUtil.print(varList,"processInstanceId");
        System.out.println("-------HistoricVariable.end------");

    }


    private void fetchTaskRuntimeVariables(TaskInfo task){

        try {
            System.out.println("-------RuntimeVariables.begin------");

            Map<String, Object> varList_task = taskService.getVariables(task.getId());
            FlowUtil.print(varList_task,"taskService.getVariables.taskId");

            Map<String, Object> varList_task_local = taskService.getVariablesLocal(task.getId());
            FlowUtil.print(varList_task_local,"taskService.getVariablesLocal.taskId");

            Map<String, Object> varList_execution = runtimeService.getVariables(task.getExecutionId());
            FlowUtil.print(varList_execution,"runtimeService.getVariables.ExecutionId");

            Map<String, Object> varList_execution_local = runtimeService.getVariablesLocal(task.getExecutionId());
            FlowUtil.print(varList_execution_local,"runtimeService.getVariablesLocal.ExecutionId");


        }catch (FlowableObjectNotFoundException e){
            System.out.println("===FlowableObjectNotFoundException in fetchTaskRuntimeVariables");
        }finally {
            System.out.println("-------RuntimeVariables.End------");
        }
    }


    //启动一个流程
    public ProcessInstance startProcess(String ProcessKey){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(ProcessKey);
        FlowUtil.print(processInstance);
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

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        FlowUtil.print(deployment,processDefinition);
        return deployment;
    }

    //发布流程：指定本地目录进行发布
    public Deployment deployProcessFromClasspath(String filename){
        Deployment deployment = repositoryService.createDeployment()
                .name("deployFromClasspathN")
                .category("deployFromClasspathC")  //deploy category
                .addClasspathResource(filename)
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        FlowUtil.print(deployment,processDefinition);
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

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        FlowUtil.print(deployment,processDefinition);
        return deployment;
    }


}
