package com.j.demo.flowablespringboot.flowable.demo;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Flowable 演示", description = "demo: flowable")
@RequestMapping(path="/demo")
@RestController
public class FlowableController {

    @Autowired
    FlowableDemoService flowableDemoService;


    @ApiOperation("演示:变量在流程中的简单传递")
    @GetMapping("/setVarsInProcess")
    public void demoSetVarsInProcess() {
        flowableDemoService.demoSetVarsInProcess();
    }
}
