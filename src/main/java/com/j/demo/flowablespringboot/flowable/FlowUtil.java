package com.j.demo.flowablespringboot.flowable;

import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Attachment;
import org.flowable.form.api.FormDefinition;
import org.flowable.form.api.FormInfo;
import org.flowable.form.model.FormField;
import org.flowable.form.model.SimpleFormModel;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;

import java.util.List;
import java.util.Map;


public class FlowUtil {

    static boolean isDebug = true;

    public static void print(FormDefinition formDefinition){

        System.out.println("   attachment.getId() = " + formDefinition.getId());
        System.out.println("   attachment.getName() = " + formDefinition.getName());
        System.out.println("   attachment.getKey() = " + formDefinition.getKey());
        System.out.println("   attachment.getVersion() = " + formDefinition.getVersion());
        System.out.println("   attachment.getDescription() = " + formDefinition.getDescription());
        System.out.println("   attachment.getCategory() = " + formDefinition.getCategory());
        System.out.println("   attachment.getClass() = " + formDefinition.getClass());
        System.out.println("   attachment.getDeploymentId() = " + formDefinition.getDeploymentId());
        System.out.println("   attachment.getResourceName() = " + formDefinition.getResourceName());
//        System.out.println("   attachment.getName() = " + formDefinition.getTenantId());

    }
    static void print(Attachment attachment){
        if(attachment == null){
            System.out.println("   attachment not found.");
            return;
        }
        System.out.println("   attachment.getName() = " + attachment.getName());
        System.out.println("   attachment.getId() = " + attachment.getId());
        System.out.println("   attachment.getDescription() = " + attachment.getDescription());
        System.out.println("   attachment.getType() = " + attachment.getType());
        System.out.println("   attachment.getUrl() = " + attachment.getUrl());
        System.out.println("   attachment.getContentId() = " + attachment.getContentId());

        System.out.println("   attachment.getUserId() = " + attachment.getUserId());

        System.out.println("   attachment.END");

    }
    static void print(List<HistoricVariableInstance> varList, String name){
        if(!isDebug) return;
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

    public static void print(Map<String, Object> vars,String name){
        if(!isDebug) return;
        System.out.println("===["+name+"].HistoricVariableInstance.varList.size():" + vars.size());

        for (String k : vars.keySet()) {
            System.out.println( k + ":" + vars.get(k));
        }

        System.out.println("===["+name+"]End");
        System.out.println("");
    }


    static void print(ProcessInstance processInstance){
        if(!isDebug) return;
        System.out.println("processInstance.getId():"+processInstance.getId());
        System.out.println("processInstance.getName():"+processInstance.getName());
        System.out.println("processInstance.getProcessVariables().size():"+processInstance.getProcessVariables().size());
    }

     static void print(Deployment deployment,  ProcessDefinition processDefinition){
         if(!isDebug) return;
//        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
//                .deploymentId(deployment.getId())
//                .singleResult();
        System.out.println("deploymentID  : " + deployment.getId());
        System.out.println("deploymentname : " + deployment.getName());
        System.out.println("Found process definition : " + processDefinition.getName());
        System.out.println("Found process getVersion : " + processDefinition.getVersion());
    }


    static void print(HistoricTaskInstance task){
        if(!isDebug) return;
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

    static void print(FormInfo formInfo){
        if(!isDebug) return;
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
