package com.j.demo.flowablespringboot.hello;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;


/**
 *
 * Simple process: say hello
 *
 */

@Api(tags = "Simple process", description = "demo: flowable:expression")
@RestController
public class HelloController {

    @Autowired
    private HelloService helloService;

    @Autowired
    private RepositoryService repositoryService;


    //    @GetMapping("/hello")
    @ApiOperation("演示hello")
//    @ApiImplicitParam(value="名字")
    @RequestMapping(value="/hello", method= RequestMethod.GET)
    public Map<String,Object> startProcessInstance(
            @RequestParam(value = "name", defaultValue = "World") String name
    ) {

        ProcessDefinition processDefinition = repositoryService.
                createProcessDefinitionQuery().
                processDefinitionKey(helloService.ProcessKey).
                latestVersion().singleResult();

        if(processDefinition == null){
            helloService.doDeploy();
        }
        ProcessInstance processInstance = helloService.startProcessInstance(name);

        Map<String,Object> result= new HashMap<String, Object>();
        //result.put("processInstanceName",processInstance.getName());
        result.put("processInstanceIsEnd",processInstance.isEnded());
        return result;
    }


}
