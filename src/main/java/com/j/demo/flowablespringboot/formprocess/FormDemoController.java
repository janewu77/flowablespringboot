package com.j.demo.flowablespringboot.formprocess;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
public class FormDemoController {


    @Autowired
    FormDemoService formDemoService;

    @Autowired
    RepositoryService repositoryService;


    @PostMapping("/deploy")
    public List<FormProperty> doDeploy() {
        _doDeploy(true);
        return formDemoService.fetchStarterForm();
    }

    @GetMapping("/showStarterForm")
    public List<FormProperty> showStaterForm() {
        _doDeploy(false);
        return formDemoService.fetchStarterForm();
    }

    @GetMapping("/showTaskForm")
    public List<FormProperty> showTaskForm(@RequestParam(value = "taskId") String taskId) {
        _doDeploy(false);
        return formDemoService.fetchTaskForm(taskId);
    }

    @PostMapping("/startProcess")
    public Map<String,Object> startProcess() {

        ProcessInstance processInstance = formDemoService.startProcess();

        Map<String,Object> result= new HashMap<String, Object>();
        result.put("processInstanceId",processInstance.getId());
        result.put("processInstanceName",processInstance.getName());
        result.put("processInstanceIsEnd",processInstance.isEnded());

        return result;
    }

    @GetMapping("/fetchAllTasks")
    public void getAllTask() {
        formDemoService.fecthAllTask();
    }


    @GetMapping("/fetchTaskInfo")
    public void getATask(@RequestParam(value = "taskId") String taskId) {
        formDemoService.fetchTaskInfo(taskId);
    }

    @GetMapping("/completeTaskInfo")
    public void doCompleteATask(@RequestParam(value = "taskId") String taskId) {
        formDemoService.doCompleteTask(taskId);
    }

    @GetMapping("/fetchTaskInfoFromHistory")
    public void getATaskFromHistory(@RequestParam(value = "taskId") String taskId) {
        formDemoService.fetchTaskInfoFromHistory(taskId);
    }


    private void _doDeploy(boolean isForce){
        if (isForce) {
            formDemoService.doDeploy();
            return;
        }
        ProcessDefinition processDefinition = repositoryService.
                createProcessDefinitionQuery().
                processDefinitionKey(formDemoService.ProcessKey).
                latestVersion().singleResult();

        if(processDefinition == null){
            formDemoService.doDeploy();
        }
    }
}
