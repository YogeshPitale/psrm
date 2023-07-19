package com.wf.psrm;

import com.wf.psrm.util.MVELParser;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentSystemRiskMonitorApplication {

	public static void main(String[] args) {
		//SpringApplication.run(PaymentSystemRiskMonitorApplication.class, args);
		String condition_ip="{\"condition\" : \"x.person.age>40 && x.person.name=='Johny'\", \"options\":[{\"value\":true,\"path\":1},{\"value\":false,\"path\":2}]}";
		String message="{\"person\":{\"age\":47,\"name\":\"Johny\"}}";
		JSONObject input_condition = new JSONObject(condition_ip);
		String expression = input_condition.getString("condition");
		System.out.println("Script:"+MVELParser.executeScript(expression,message));
	}

}
