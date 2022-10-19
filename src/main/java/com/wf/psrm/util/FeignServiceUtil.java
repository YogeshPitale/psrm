package com.wf.psrm.util;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.wf.psrm.domain.TransactionDetails;

@FeignClient(value = "wfRules", url = "http://localhost:8093")
public interface FeignServiceUtil {

	@PostMapping("/v1/getTransactionStatus")
	ResponseEntity<Object> getTransactionStatus(@RequestBody TransactionDetails cRules);
}
