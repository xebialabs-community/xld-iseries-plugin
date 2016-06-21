package ext.deployit.community.plugin.iseries.step;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.CommandCall;

import com.xebialabs.deployit.plugin.api.flow.*;
import com.xebialabs.deployit.plugin.api.udm.artifact.Artifact;
import com.xebialabs.deployit.plugin.generic.freemarker.ArtifactUploader;
import com.xebialabs.deployit.plugin.generic.freemarker.CiAwareObjectWrapper;
import com.xebialabs.deployit.plugin.generic.freemarker.ConfigurationHolder;
import com.xebialabs.overthere.RuntimeIOException;

import ext.deployit.community.plugin.iseries.ci.Server;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import static com.google.common.collect.Maps.newHashMap;

public class ISeriesScriptExecutionStep implements Step, PreviewStep {

    private final String script;
    private final int order;
    private final String description;
    private final Server server;
    private final Map<String, Object> freeMarkerContext;

    private transient ExecutionContext ctx;

    public ISeriesScriptExecutionStep(final int order, final String script, final Server server, final Map<String, Object> freeMarkerContext, final String description) {
        this.order = order;
        this.description = description;
        this.script = script;
        this.server = server;
        this.freeMarkerContext = freeMarkerContext;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public StepExitCode execute(final ExecutionContext ctx) throws Exception {
        this.ctx = ctx;
        return doExecute();
    }

    @Override
    public Preview getPreview() {
        String contents = evaluateTemplate(script, freeMarkerContext, true);
        return Preview.withContents(contents);
    }

    protected StepExitCode doExecute() throws Exception {

        getCtx().logOutput(String.format("Trying to create a connection to %s using username %s", server.getAddress(), server.getUsername()));

        final AS400 as400 = new AS400(server.getAddress(), server.getUsername(), server.getPassword());

        String script = evaluateTemplate(this.script, freeMarkerContext, false);

        final String[] lines = script.split(System.getProperty("line.separator"));
        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;

            getCtx().logOutput("Executing: " + line);
            CommandCall cc = new CommandCall(as400);
            boolean run = cc.run(line);
            if (run) {
                getCtx().logError("successfully Run: " + line);
                for (AS400Message message : cc.getMessageList()) {
                    getCtx().logOutput(message.getText());
                }
            } else {
                getCtx().logError("Error Executing: " + line);
                for (AS400Message message : cc.getMessageList()) {
                    getCtx().logError(message.getText());
                }
                return StepExitCode.FAIL;
            }
        }

        return StepExitCode.SUCCESS;
    }

    protected ExecutionContext getCtx() {
        return ctx;
    }

    public String evaluateTemplate(String templatePath, Map<String, Object> vars, boolean maskPasswords) {
        Configuration cfg = ConfigurationHolder.getConfiguration();
        try {
            Template template = cfg.getTemplate(templatePath);
            StringWriter sw = new StringWriter();

            Map<String, Object> varsWithStatics = newHashMap(vars);
            varsWithStatics.put("step", this);
            //The step never uploads content on the remote machine.
            template.createProcessingEnvironment(varsWithStatics, sw, new CiAwareObjectWrapper(
                    new ArtifactUploader() {

                        @Override
                        public String upload(final Artifact artifact) {
                            return "NOT-UPLOADED----" + artifact.getName();
                        }
                    }, maskPasswords)
            ).process();
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }


}
