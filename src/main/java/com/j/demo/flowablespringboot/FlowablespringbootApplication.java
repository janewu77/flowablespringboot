package com.j.demo.flowablespringboot;


import com.j.demo.flowablespringboot.flowable.demo.MyEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//@SpringBootApplication(proxyBeanMethods = false)
@SpringBootApplication
public class FlowablespringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowablespringbootApplication.class, args);
    }

    @Autowired
    MyEventListener myEventListener;


    @Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
                                  final HistoryService historyService,
                                  final RuntimeService runtimeService,
                                  final TaskService taskService) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {

                //runtimeService.addEventListener(myEventListener);


                System.out.println("Number of process definitions : "
                        + repositoryService.createProcessDefinitionQuery().count());

                System.out.println("Number of tasks after process start: "
                        + taskService.createTaskQuery().count());

            }


            //查看历史记录
            void showHis(){

                List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
//                HistoryService historyService = processEngine.getHistoryService();
                for(ProcessInstance  processInstance:processInstances){

                    System.out.println(processInstance.getName()+":");

                    List<HistoricActivityInstance> activities =
                            historyService.createHistoricActivityInstanceQuery()
                                    .processInstanceId(processInstance.getId())
                                    .finished()
                                    .orderByHistoricActivityInstanceEndTime().asc()
                                    .list();

                    for (HistoricActivityInstance activity : activities) {
                        System.out.println("  "+activity.getActivityId() + " took "
                                + activity.getDurationInMillis() + " milliseconds");
                    }
                }
            }

            //demo:发起请假流程
            void doAskLeaveByEmployee(){
                Scanner scanner= new Scanner(System.in);

                System.out.println("Who are you?");
                String employee = scanner.nextLine();

                System.out.println("How many holidays do you want to request?");
                Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

                System.out.println("Why do you need them?");
                String description = scanner.nextLine();

//                RuntimeService runtimeService = processEngine.getRuntimeService();

                Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("employee", employee);
                variables.put("nrOfHolidays", nrOfHolidays);
                variables.put("description", description);
                ProcessInstance processInstance =
                        runtimeService.startProcessInstanceByKey("holidayRequest", variables);

            }

            //demo:执行任务
            void doManagerJob(){
                List<Task> tasks = queryManagerTasks();

                Scanner scanner= new Scanner(System.in);
                System.out.println("Which task would you like to complete?");
                int taskIndex = Integer.valueOf(scanner.nextLine());

                Task task = tasks.get(taskIndex - 1);
                Map<String, Object> processVariables = taskService.getVariables(task.getId());
                System.out.println(processVariables.get("employee") + " wants " +
                        processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?(y/n)");

                boolean approved = scanner.nextLine().toLowerCase().equals("y");
                Map variables = new HashMap<String, Object>();
                variables.put("approved", approved);
                taskService.complete(task.getId(), variables);
            }

            //查询某个group的任务
            List<Task> queryManagerTasks(){
                //TaskService taskService = processEngine.getTaskService();
                List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
                System.out.println("You(managers) have " + tasks.size() + " tasks:");
                for (int i=0; i<tasks.size(); i++) {
                    System.out.println((i+1) + ") " + tasks.get(i).getName());
                }
                return tasks;
            }

        };

    }
}
