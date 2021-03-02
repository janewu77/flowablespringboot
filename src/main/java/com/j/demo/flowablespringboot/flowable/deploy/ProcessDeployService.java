package com.j.demo.flowablespringboot.flowable.deploy;


import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

@Component
public class ProcessDeployService {

    @Autowired
    RepositoryService repositoryService;

//    final static public String ProcessKey = "formProcess";
//    final static private String ProcessDefRes = "pre-processes/form.bpmn20.xml";
    //自动发布 processes目录下的流程文件

    //demo
    public void demoDeployFromClasspath(){
        //每次都会delopy，生成新的版本。（无论xml的内容是否有变化）
        String filename = "pre-processes/demo-process2.bpmn20.xml";
        deployFromClasspath(filename);

        //当active日期设为未来时，所有已发布版本都不可发起流程
//        Date activeDate = new Date(1648742400000L);//1648742400: 2022.4.1
//        deployFromClasspath(filename,activeDate);
    }

    //demo
    public void demoDeployFromInputStream(){
        InputStream inputStream = null;
        String filename = "";
        try {

            //String filename = "pre-processes/demo-process2.bpmn20.xml";
            //InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);

//            String filename = "/Users/janewu/janewu/demo-process2.bpmn20.xml";
//            File initialFile = new File(filename);
//            final InputStream inputStream = new DataInputStream(new FileInputStream(initialFile));

            filename = "src/main/resources/pre-processes/demo-process2.bpmn20.xml";
            File initialFile = new File(filename);
            inputStream = new FileInputStream(initialFile);

        }catch (FileNotFoundException e){
            System.out.println(e);
        }

        Deployment deployment = deployFromInputStream("process2.bpmn20.xml",inputStream);
    }


    //发布流程：以inputStream形式发布
    public Deployment deployFromInputStream(String rname, InputStream inputStream){
        Deployment deployment = repositoryService.createDeployment()
                .name("DeployFromInputStreamN")
                .category("DeployFromInputStreamC")  //deploy category
                .addInputStream(rname,inputStream)//
                .deploy();

        _printDeployInfo(deployment);
        return deployment;
    }

    //发布流程：指定本地目录进行发布
    public Deployment deployFromClasspath(String filename){
        Deployment deployment = repositoryService.createDeployment()
                .name("deployFromClasspathN")
                .category("deployFromClasspathC")  //deploy category
                .addClasspathResource(filename)
                .deploy();

        _printDeployInfo(deployment);
        return deployment;
    }

    //发布流程：指定本地目录 & 指定生效日期
    public Deployment deployFromClasspath(String filename,Date activeDate){
        Deployment deployment = repositoryService.createDeployment()
                .activateProcessDefinitionsOn(activeDate)
                .name("deployFromClasspathN")
                .category("deployFromClasspathCWithDate")  //deploy category
                .addClasspathResource(filename)
                .deploy();

        _printDeployInfo(deployment);
        return deployment;
    }


    private void _printDeployInfo(Deployment deployment ){
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("deploymentID  : " + deployment.getId());
        System.out.println("deploymentname : " + deployment.getName());
        System.out.println("Found process definition : " + processDefinition.getName());
        System.out.println("Found process getVersion : " + processDefinition.getVersion());
    }

}
