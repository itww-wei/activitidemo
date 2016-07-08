package cn.hfzs.compet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class CompeteProcessTest {
	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

	/**
	 * 创建23张表
	 */
	@Test
	public void createTable() {
		ProcessEngine processEngine = ProcessEngineConfiguration//
				.createProcessEngineConfigurationFromResource("activiti.cfg.xml")	//
									.buildProcessEngine();
		System.out.println("processEngine:"+processEngine);
	}

	/** 部署流程定义（从inputStream） */
	@Test
	public void deploymentProcessDefinition_inputStream() {
		InputStream inputStreamBpmn = this.getClass().getResourceAsStream(
				"test.bpmn");
		InputStream inputStreamPng = this.getClass().getResourceAsStream(
				"test.png");
		Deployment deployment = processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
				.createDeployment()// 创建一个部署对象
				.name("test")// 添加部署的名称
				.addInputStream("test.bpmn", inputStreamBpmn)//
				.addInputStream("test.png", inputStreamPng)//
				.deploy();// 完成部署
		System.out.println("部署ID：" + deployment.getId());//
		System.out.println("部署名称：" + deployment.getName());//
	}

	/** 启动流程实例 */
	@Test
	public void startProcessInstance() {
		// 流程定义的key
		String processDefinitionKey = "test";
		ProcessInstance pi = processEngine.getRuntimeService()// 与正在执行的流程实例和执行对象相关的Service
				.startProcessInstanceByKey(processDefinitionKey);// 使用流程定义的key启动流程实例，key对应helloworld.bpmn文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
		System.out.println("流程实例ID:" + pi.getId());// 流程实例ID 101
		System.out.println("流程定义ID:" + pi.getProcessDefinitionId());// 流程定义ID
																	// helloworld:1:4
	}

	/** 查询当前人的个人任务 */
	@Test
	public void findMyPersonalTask() {
		//String assignee = "审核";// 审核 供应商1 供应商2 采购方
		//String assignee = "供应商1";//审核 供应商1 供应商2 采购方
		String assignee = "经理";//审核 供应商1 供应商2 采购方
		List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.createTaskQuery()// 创建任务查询对象
				/** 查询条件（where部分） */
				.taskAssignee(assignee)// 指定个人任务查询，指定办理人
				// .taskCandidateUser(candidateUser)//组任务的办理人查询
				// .processDefinitionId(processDefinitionId)//使用流程定义ID查询
				// .processInstanceId(processInstanceId)//使用流程实例ID查询
				// .executionId(executionId)//使用执行对象ID查询
				/** 排序 */
				.orderByTaskCreateTime().asc()// 使用创建时间的升序排列
				/** 返回结果集 */
				// .singleResult()//返回惟一结果集
				// .count()//返回结果集的数量
				// .listPage(firstResult, maxResults);//分页查询
				.list();// 返回列表
		if (list != null && list.size() > 0) {
			for (Task task : list) {
				System.out.println("任务ID:" + task.getId());
				System.out.println("任务名称:" + task.getName());
				System.out.println("任务的创建时间:" + task.getCreateTime());
				System.out.println("任务的办理人:" + task.getAssignee());
				System.out.println("流程实例ID：" + task.getProcessInstanceId());
				System.out.println("执行对象ID:" + task.getExecutionId());
				System.out.println("流程定义ID:" + task.getProcessDefinitionId());
				System.out
						.println("########################################################");
			}
		}
		System.out.println("CompeteProcessTest.findMyPersonalTask()");
	}

	/** 完成我的任务 审核 */
	@Test
	public void completeMyPersonalTask() {
		// 任务ID
		String taskId = "204";
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("approve", "1");// 0: 不通过，1： 通过
		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId);
		System.out.println("完成任务：任务ID：" + taskId);
	}
	

	/** 完成我的任务 报价 */
	@Test
	public void completeMyPersonalTaskShenhe() {
		// 任务ID
		String taskId = "409";
		Map<String, Object> ve = new HashMap<String, Object>();
		ve.put("pirce2", "200");
		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
				.complete(taskId, ve);
		System.out.println("完成任务：任务ID：" + taskId);
	}

	/** 完成我的任务 竞价确认 */
	@Test
	public void okCompleteMyPersonalTask() {
		
		// 任务ID
		String taskId = "604";
		Map<String, Object> variables=new HashMap<String, Object>();
		variables.put("isok", "yes");//是否同意： 
		
		processEngine.getTaskService()// 与正在执行的任务管理相关的Service
		.complete(taskId, variables);
		System.out.println("完成任务：任务ID：" + taskId);
	}
	
	/**
	 * 查看历史变量
	 */
	@Test
	public void findHisVariables(){
		//执行的流程id
		String processInstanceId="101";
		//查看供应商报价
		List<HistoricVariableInstance> list = processEngine.getHistoryService().createHistoricVariableInstanceQuery()//
				.processInstanceId(processInstanceId)//
				.orderByVariableName().asc()//
				.list();
		if(list!=null){
			System.out.println("流程实例ID		参数名		参数值		");
			for (HistoricVariableInstance his : list) {
				System.out.print(""+his.getProcessInstanceId());
				System.out.print("			"+his.getVariableName());
				System.out.print("		"+his.getValue());
				System.out.println();
			}
		}
	}
	
	/**
	 * 获取跟踪的流程图
	 * 
	 * @return
	 * @throws Exception
	 */
	@Test
	public void viewProcessPic() throws Exception {
		String executionId="201";
		
		// 不使用spring请使用下面的两行代码 解决乱码问题
		ProcessEngineImpl defaultProcessEngine = (ProcessEngineImpl) ProcessEngines.getDefaultProcessEngine();
		Context.setProcessEngineConfiguration(defaultProcessEngine.getProcessEngineConfiguration());
		
		ProcessInstance processInstance = defaultProcessEngine//
				.getRuntimeService()//
				.createProcessInstanceQuery()//
				.processInstanceId(executionId)//
				.singleResult();
		
	    BpmnModel bpmnModel = defaultProcessEngine//
	    		.getRepositoryService()//
	    		.getBpmnModel(processInstance.getProcessDefinitionId());
		
	    List<String> activeActivityIds = processEngine.getRuntimeService()//
	    		.getActiveActivityIds(executionId);
	    

	    // 使用spring注入引擎请使用下面的这行代码
	    //Context.setProcessEngineConfiguration(processEngine.getProcessEngineConfiguration());

	    InputStream imageStream = ProcessDiagramGenerator.generateDiagram(bpmnModel, "png",activeActivityIds);
	    
		// 将图片生成到D盘的目录下
		File file = new File("D:/" + "compete.png");
		// 将输入流的图片写到D盘下
		FileUtils.copyInputStreamToFile(imageStream, file);
		System.out.println("CompeteProcessTest.findProcessPic()");
	}
	
	
	/** 查询流程定义 */
	@Test
	public void findProcessDefinition() {
		List<ProcessDefinition> list = processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
				.createProcessDefinitionQuery()// 创建一个流程定义的查询
				/** 指定查询条件,where条件 */
				// .deploymentId(deploymentId)//使用部署对象ID查询
				// .processDefinitionId(processDefinitionId)//使用流程定义ID查询
				// .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
				// .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

				/** 排序 */
				.orderByProcessDefinitionVersion().asc()// 按照版本的升序排列
				// .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

				/** 返回的结果集 */
				.list();// 返回一个集合列表，封装流程定义
		// .singleResult();//返回惟一结果集
		// .count();//返回结果集数量
		// .listPage(firstResult, maxResults);//分页查询
		if (list != null && list.size() > 0) {
			for (ProcessDefinition pd : list) {
				System.out.println("流程定义ID:" + pd.getId());// 流程定义的key+版本+随机生成数
				System.out.println("流程定义的名称:" + pd.getName());// 对应helloworld.bpmn文件中的name属性值
				System.out.println("流程定义的key:" + pd.getKey());// 对应helloworld.bpmn文件中的id属性值
				System.out.println("流程定义的版本:" + pd.getVersion());// 当流程定义的key值相同的相同下，版本升级，默认1
				System.out.println("资源名称bpmn文件:" + pd.getResourceName());
				System.out.println("资源名称png文件:" + pd.getDiagramResourceName());
				System.out.println("部署对象ID：" + pd.getDeploymentId());
				System.out
						.println("#########################################################");
			}
		}
	}

	/** 查询流程状态（判断流程正在执行，还是结束） */
	@Test
	public void isProcessEnd() {
		String processInstanceId = "204";
		ProcessInstance pi = processEngine.getRuntimeService()// 表示正在执行的流程实例和执行对象
				.createProcessInstanceQuery()// 创建流程实例查询
				.processInstanceId(processInstanceId)// 使用流程实例ID查询
				.singleResult();
		if (pi == null) {
			System.out.println("流程已经结束");
		} else {
			System.out.println("流程没有结束");
		}
	}

	/** 查询执行的所有的流程 */
	@Test
	public void findAllProcess() {
		List<ProcessInstance> list = processEngine.getRuntimeService()// 表示正在执行的流程实例和执行对象
				.createProcessInstanceQuery()// 创建流程实例查询
				.list();

		if (list != null && list.size() > 0) {
			for (ProcessInstance pi : list) {
				System.out.println("ID:" + pi.getId());
				System.out.println("ProcessInstanceId:"
						+ pi.getProcessInstanceId());
				System.out.println("ProcessInstanceId:"
						+ pi.getProcessDefinitionId());
				System.out
						.println("#########################################################");
			}
		}
	}

	/**
	 * 查看流程图
	 * 
	 * @throws IOException
	 */
	@Test
	public void viewPic() throws IOException {
		/** 将生成图片放到文件夹下 */
		String deploymentId = "3501";
		// 获取图片资源名称
		List<String> list = processEngine.getRepositoryService()//
				.getDeploymentResourceNames(deploymentId);
		// 定义图片资源的名称
		String resourceName = "";
		if (list != null && list.size() > 0) {
			for (String name : list) {
				if (name.indexOf(".png") >= 0) {
					resourceName = name;
				}
			}
		}

		// 获取图片的输入流
		InputStream in = processEngine.getRepositoryService()//
				.getResourceAsStream(deploymentId, resourceName);

		// 将图片生成到D盘的目录下
		File file = new File("D:/" + resourceName);
		// 将输入流的图片写到D盘下
		FileUtils.copyInputStreamToFile(in, file);
	}

	/** 获取流程变量 */
	@Test
	public void getVariables() {
		/** 与任务（正在执行） */
		TaskService taskService = processEngine.getTaskService();
		// 任务ID
		String taskId = "2708";
		/** 一：获取流程变量，使用基本数据类型 */
		// Integer days = (Integer) taskService.getVariable(taskId, "请假天数");
		// Date date = (Date) taskService.getVariable(taskId, "请假日期");
		// String resean = (String) taskService.getVariable(taskId, "请假原因");
		// System.out.println("请假天数："+days);
		// System.out.println("请假日期："+date);
		// System.out.println("请假原因："+resean);
		/** 二：获取流程变量，使用javabean类型 */
		String price = (String) taskService.getVariable(taskId, "price");
		System.out.println("报价：" + price);
	}
}
