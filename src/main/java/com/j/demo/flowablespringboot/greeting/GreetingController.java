package com.j.demo.flowablespringboot.greeting;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试rest接口是否正常
 */


@Api(tags = "可用性检查测试接口", description = "Greeting，检查基本rest接口是否正常")
@RestController
public class GreetingController {

    @Value("${version}")
    private String version;

    @GetMapping("/greeting")
    public List<String> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {

        List<String> greeting = new ArrayList<String>();//new Greeting(counter.incrementAndGet(), String.format(template, name));
        greeting.add(name);
        greeting.add("greeting from FlowablespringbootApplication");
        greeting.add(version);
        return greeting;
    }

}
