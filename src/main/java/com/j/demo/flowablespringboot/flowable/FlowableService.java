package com.j.demo.flowablespringboot.flowable;

import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
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


    //取得本流程的活跃instance的所有任务（带变量）
    public void fecthTaskListFromHistory(String ProcessKey){

        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .processUnfinished()
                .unfinished() //task unfinished
                .processDefinitionKey(ProcessKey).list();

        //System.out.println("==="+ProcessKey+" tasks.size()" + tasks.size() );
        for(HistoricTaskInstance task:tasks){
            System.out.println("=== task.getId()" + task.getId() );
            System.out.println("=== task.getName()" + task.getName() );
            //_print(task);
        }
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

        fetchTaskHistoricVariables(historicTaskInstance);

        fetchTaskRuntimeVariables(historicTaskInstance);

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


    //启动一个流程（用form)
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
            System.out.println("   var.getVariableName()" + var.getVariableName());
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


//        try {
//            //task  form data
//            TaskFormData taskFormData = formService.getTaskFormData(task.getId());
//            List<FormProperty> formPropertyList = taskFormData.getFormProperties();
//            for (FormProperty p : formPropertyList) {
//                System.out.println("=== p.getId():" + p.getId());
//                System.out.println("=== p.getType():" + p.getType());
//                System.out.println("=== p.getName():" + p.getName());
//                System.out.println("=== p.getValue():" + p.getValue());
//            }
//            System.out.println("===task.formPropertyList.size():"+formPropertyList.size() );
//        }catch (FlowableObjectNotFoundException e){
//            System.out.println("===[1] FlowableObjectNotFoundException:task.formPropertyList");
//        }

    //task form model
//        FormInfo formInfo = taskService.getTaskFormModel(taskId);
//        System.out.println("===task:formInfo.getId() "+formInfo.getId() );
//        System.out.println("===task:formInfo.getName() "+formInfo.getName() );
//        System.out.println("===task:formInfo.getDescription() "+formInfo.getDescription() );
//        System.out.println("===task:formInfo.getKey() "+formInfo.getKey() );
//        System.out.println("===task:formInfo.getVersion() "+formInfo.getVersion() );
//        FormModel formModel = formInfo.getFormModel();



//        Integer random = (Integer) taskService.getVariable(taskId,"random");
//        System.out.println("===random:"+random );
//
//        Integer X1random = (Integer) taskService.getVariable(taskId,"X1random");
//        System.out.println("===X1random:"+X1random );

}
