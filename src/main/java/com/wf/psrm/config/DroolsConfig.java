package com.wf.psrm.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {
	private static final String RULES_FED_LEVEL_DRL = "rules/fed-level-rules.drl";
	private static final String RULES_ON_HOLD_RULES_DRL = "rules/oh-hold-rule.drl";
	private static final String RULES_DEBIT_AMOUNT_DRL = "rules/debit-amount-rules.drl";
	private static final String RULES_OTHER_DRL = "rules/other-ruels.drl";
	private static final KieServices kieServices = KieServices.Factory.get();

	@Bean
	public KieContainer kieContainer() {
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_FED_LEVEL_DRL));
		kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_ON_HOLD_RULES_DRL));
		kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_DEBIT_AMOUNT_DRL));
		kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_OTHER_DRL));
		KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
		kb.buildAll();
		KieModule kieModule = kb.getKieModule();
		KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
		return kieContainer;
	}
}
