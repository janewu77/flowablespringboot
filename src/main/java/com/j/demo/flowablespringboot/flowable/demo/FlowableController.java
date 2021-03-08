package com.j.demo.flowablespringboot.flowable.demo;


import com.j.demo.flowablespringboot.flowable.FlowableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.form.api.FormInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "Flowable 演示", description = "demo: flowable")
@RequestMapping(path="/demo")
@RestController
public class FlowableController {

    @Autowired
    FlowableDemoService demoService;

    @Autowired
    FlowableService flowableService;


    @ApiOperation("演示:变量在流程中的简单传递")
    @GetMapping("/setVarsInProcess")
    public void demoSetVarsInProcess() {
        demoService.demoSetVarsInProcess();
    }


    @GetMapping("/fetchTaskInfo")
    public void getATask(@RequestParam(value = "taskId") String taskId) {
        flowableService.fetchTaskFromHistory(taskId);
    }

    @GetMapping("/fetchAllTask")
    public Object getAllTask() {
        List<HistoricTaskInstance> tasks = flowableService.fecthTaskListFromHistory("DemoFormProcess");

        List<String> result = new ArrayList<>();
        for(HistoricTaskInstance task:tasks){
            result.add( "["+task.getId() +"]:"+ task.getName());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/showTaskForm")
    public FormInfo doDemoShowTaskFrom(@RequestParam(value = "taskId") String taskId) {
        //显示 task的表单 （带数据）
        return flowableService.fetchForm4Task(taskId);
    }


    @PostMapping("/completeTask")
    public void dodemoCompleteTask(@RequestParam(value = "taskId") String taskId) {
        demoService.demoCompleteTask(taskId);
    }

    //
    @PostMapping("/completeTaskWithForm")
    public void doCompleteTaskWithFrom(@RequestParam(value = "taskId") String taskId,
                                       @RequestParam(value = "no") String no) {
            demoService.demoCompleteTaskWithForm(taskId);
    }

    @PostMapping("/startProcess")
    public Map<String,Object> startProcess() {

        //process key : DemoFormProcess
        ProcessInstance processInstance = demoService.demoStartProcess_DemoFormProcess();

        Map<String,Object> result= new HashMap<String, Object>();
        result.put("processInstanceId",processInstance.getId());
        result.put("processInstanceName",processInstance.getName());
        result.put("processInstanceIsEnd",processInstance.isEnded());

        return result;
    }

}