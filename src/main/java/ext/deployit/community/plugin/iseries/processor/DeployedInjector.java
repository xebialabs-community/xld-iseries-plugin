package ext.deployit.community.plugin.iseries.processor;

import java.util.Collections;

import com.xebialabs.deployit.plugin.api.deployment.planning.PrePlanProcessor;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.DeltaSpecification;
import com.xebialabs.deployit.plugin.api.flow.Step;
import com.xebialabs.deployit.plugin.api.udm.Deployed;

import ext.deployit.community.plugin.iseries.ci.ExecutedScript;

public class DeployedInjector {

    @PrePlanProcessor
    public java.util.List<Step> inject(DeltaSpecification deltaSpec) {
        for (Delta delta : deltaSpec.getDeltas()) {
            injectDeployed(delta.getPrevious(), deltaSpec);
            injectDeployed(delta.getDeployed(), deltaSpec);
        }
        // not adding any steps to the plan
        return Collections.emptyList();
    }

    private void injectDeployed(Deployed<?, ?> deployed, DeltaSpecification deltaSpec) {
        if (deployed instanceof ExecutedScript) {
            ExecutedScript<?> executedScript = (ExecutedScript<?>) deployed;
            executedScript.setPlanOperation(deltaSpec.getOperation());
            executedScript.setDeployedApplication(deltaSpec.getDeployedApplication());
        }
    }

}
