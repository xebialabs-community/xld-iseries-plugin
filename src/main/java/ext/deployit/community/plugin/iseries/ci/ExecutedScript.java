package ext.deployit.community.plugin.iseries.ci;

import java.util.Collections;
import java.util.Map;
import com.google.common.base.Strings;

import com.xebialabs.deployit.plugin.api.deployment.planning.*;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Operation;
import com.xebialabs.deployit.plugin.api.udm.*;
import com.xebialabs.deployit.plugin.api.udm.base.BaseDeployed;

import ext.deployit.community.plugin.iseries.step.ISeriesScriptExecutionStep;

import static com.google.common.collect.Maps.newHashMap;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "A script executed on an iSeries system.")
public class ExecutedScript<D extends Deployable> extends BaseDeployed<D, Server> {

    @Property(required = true, hidden = true, description = "Classpath to the script that executed on the iSeries container for the create operation.")
    private String createScript;

    @Property(required = false, hidden = true, description = "Classpath to the script that executed on the iSeries container for the modify operation.")
    private String modifyScript;

    @Property(required = false, hidden = true, description = "Classpath to the script that executed on the iSeries container for the destroy operation.")
    private String destroyScript;

    @Property(required = false, hidden = true, description = "Classpath to the script that executed on the iSeries container for the noop operation.")
    private String noopScript;

    @Property(hidden = true, defaultValue = "50", description = "The order of the step in the step list for the create operation.")
    private int createOrder;

    @Property(hidden = true, defaultValue = "40", description = "The order of the step in the step list for the destroy operation.")
    private int destroyOrder;

    @Property(hidden = true, defaultValue = "50", description = "The order of the step in the step list for the modify operation.")
    private int modifyOrder;

    @Property(hidden = true, defaultValue = "50", description = "The order of the step in the step list for the noop operation.")
    private int noopOrder;

    @Property(hidden = true, defaultValue = "Create")
    private String createVerb;

    @Property(hidden = true, defaultValue = "Modify")
    private String modifyVerb;

    @Property(hidden = true, defaultValue = "Destroy")
    private String destroyVerb;

    @Property(hidden = true, defaultValue = "Modify")
    private String noopVerb;

    //Injected by the DeployedInjector during pre-planning phase
    //----------------------------------------------------------
    private DeployedApplication deployedApplication;
    private Operation planOperation;
    //----------------------------------------------------------


    @Create
    public void executeCreate(DeploymentPlanningContext ctx, Delta delta) {
        addStep(ctx, getCreateOrder(), getCreateScript(), getCreateVerb(), null);
    }

    private boolean addStep(final DeploymentPlanningContext ctx, final int order, final String script, final String verb, Deployed<?, ?> previousDeployed) {
        if (Strings.nullToEmpty(script).trim().isEmpty()) {
            return false;
        }

        Map<String, Object> freeMarkerContext = Collections.singletonMap("deployed", (Object) this);
        if (previousDeployed != null) {
            freeMarkerContext = newHashMap(freeMarkerContext);
            freeMarkerContext.put("previousDeployed", previousDeployed);
        }
        ctx.addStep(new ISeriesScriptExecutionStep(order, script, getContainer(), freeMarkerContext, getDescription(verb)));
        return true;
    }

    @Modify
    public void executeModify(DeploymentPlanningContext ctx, Delta delta) {
        boolean modifyStepAdded = addStep(ctx, getModifyOrder(), getModifyScript(), getModifyVerb(), delta.getPrevious());
        if (!modifyStepAdded) {
            executeDestroy(ctx, delta);
            executeCreate(ctx, delta);
        }
    }

    @SuppressWarnings("unchecked")
    @Destroy
    public void executeDestroy(DeploymentPlanningContext ctx, Delta delta) {
        ExecutedScript<D> which = (ExecutedScript<D>) delta.getPrevious();
        which.addStep(ctx, which.getDestroyOrder(), which.getDestroyScript(), which.getDestroyVerb(), null);
    }

    @Noop
    public void executeNoop(DeploymentPlanningContext ctx, Delta delta) {
        addStep(ctx, getNoopOrder(), getNoopScript(), getNoopVerb(), null);
    }


    public String getCreateScript() {
        return createScript;
    }

    public void setCreateScript(final String createScript) {
        this.createScript = createScript;
    }

    public String getModifyScript() {
        return modifyScript;
    }

    public void setModifyScript(final String modifyScript) {
        this.modifyScript = modifyScript;
    }

    public String getDestroyScript() {
        return destroyScript;
    }

    public void setDestroyScript(final String destroyScript) {
        this.destroyScript = destroyScript;
    }

    public String getNoopScript() {
        return noopScript;
    }

    public void setNoopScript(final String noopScript) {
        this.noopScript = noopScript;
    }

    public int getCreateOrder() {
        return createOrder;
    }

    public void setCreateOrder(final int createOrder) {
        this.createOrder = createOrder;
    }

    public int getDestroyOrder() {
        return destroyOrder;
    }

    public void setDestroyOrder(final int destroyOrder) {
        this.destroyOrder = destroyOrder;
    }

    public int getModifyOrder() {
        return modifyOrder;
    }

    public void setModifyOrder(final int modifyOrder) {
        this.modifyOrder = modifyOrder;
    }

    public int getNoopOrder() {
        return noopOrder;
    }

    public void setNoopOrder(final int noopOrder) {
        this.noopOrder = noopOrder;
    }

    public String getCreateVerb() {
        return createVerb;
    }

    public void setCreateVerb(final String createVerb) {
        this.createVerb = createVerb;
    }

    public String getModifyVerb() {
        return modifyVerb;
    }

    public void setModifyVerb(final String modifyVerb) {
        this.modifyVerb = modifyVerb;
    }

    public String getDestroyVerb() {
        return destroyVerb;
    }

    public void setDestroyVerb(final String destroyVerb) {
        this.destroyVerb = destroyVerb;
    }

    public String getNoopVerb() {
        return noopVerb;
    }

    public void setNoopVerb(final String noopVerb) {
        this.noopVerb = noopVerb;
    }

    public String getDescription(String verb) {
        return String.format("%s %s on %s", verb, getDeployable().getName(), getContainer().getName());
    }

    public DeployedApplication getDeployedApplication() {
        return deployedApplication;
    }

    public void setDeployedApplication(final DeployedApplication deployedApplication) {
        this.deployedApplication = deployedApplication;
    }

    public Operation getPlanOperation() {
        return planOperation;
    }

    public void setPlanOperation(final Operation planOperation) {
        this.planOperation = planOperation;
    }
}
