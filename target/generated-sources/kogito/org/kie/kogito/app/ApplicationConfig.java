package org.kie.kogito.app;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kie.kogito.decision.DecisionConfig;
import org.kie.kogito.process.ProcessConfig;
import org.kie.kogito.rules.RuleConfig;

@javax.inject.Singleton()
public class ApplicationConfig implements org.kie.kogito.Config {

    protected ProcessConfig processConfig;

    protected RuleConfig ruleConfig;

    protected DecisionConfig decisionConfig;

    private org.kie.kogito.process.WorkItemHandlerConfig defaultWorkItemHandlerConfig = new org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig();

    private org.kie.kogito.uow.UnitOfWorkManager defaultUnitOfWorkManager = new org.kie.kogito.services.uow.DefaultUnitOfWorkManager(new org.kie.kogito.services.uow.CollectingUnitOfWorkFactory());

    private org.kie.kogito.jobs.JobsService defaultJobsService = null;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.process.WorkItemHandlerConfig> workItemHandlerConfig;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.uow.UnitOfWorkManager> unitOfWorkManager;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.jobs.JobsService> jobsService;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfigs;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.api.event.process.ProcessEventListener> processEventListeners;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.kogito.rules.RuleEventListenerConfig> ruleEventListenerConfigs;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.api.event.rule.AgendaEventListener> agendaEventListeners;

    @javax.inject.Inject()
    javax.enterprise.inject.Instance<org.kie.api.event.rule.RuleRuntimeEventListener> ruleRuntimeEventListeners;

    @Override
    public ProcessConfig process() {
        return processConfig;
    }

    @Override
    public RuleConfig rule() {
        return ruleConfig;
    }

    @Override
    public DecisionConfig decision() {
        return decisionConfig;
    }

    private static <C, L> List<L> merge(Collection<C> configs, Function<C, Collection<L>> configToListeners, Collection<L> listeners) {
        return Stream.concat(configs.stream().flatMap(c -> configToListeners.apply(c).stream()), listeners.stream()).collect(Collectors.toList());
    }

    protected org.kie.kogito.process.WorkItemHandlerConfig extract_workItemHandlerConfig() {
        if (workItemHandlerConfig.isUnsatisfied() == false)
            return workItemHandlerConfig.get();
        else
            return defaultWorkItemHandlerConfig;
    }

    protected org.kie.kogito.uow.UnitOfWorkManager extract_unitOfWorkManager() {
        if (unitOfWorkManager.isUnsatisfied() == false)
            return unitOfWorkManager.get();
        else
            return defaultUnitOfWorkManager;
    }

    protected org.kie.kogito.jobs.JobsService extract_jobsService() {
        if (jobsService.isUnsatisfied() == false)
            return jobsService.get();
        else
            return defaultJobsService;
    }

    private org.kie.kogito.process.ProcessEventListenerConfig extract_processEventListenerConfig() {
        return this.merge_processEventListenerConfig(java.util.stream.StreamSupport.stream(processEventListenerConfigs.spliterator(), false).collect(java.util.stream.Collectors.toList()), java.util.stream.StreamSupport.stream(processEventListeners.spliterator(), false).collect(java.util.stream.Collectors.toList()));
    }

    private org.kie.kogito.process.ProcessEventListenerConfig merge_processEventListenerConfig(java.util.Collection<org.kie.kogito.process.ProcessEventListenerConfig> processEventListenerConfigs, java.util.Collection<org.kie.api.event.process.ProcessEventListener> processEventListeners) {
        return new org.kie.kogito.process.impl.CachedProcessEventListenerConfig(merge(processEventListenerConfigs, org.kie.kogito.process.ProcessEventListenerConfig::listeners, processEventListeners));
    }

    private org.kie.kogito.rules.RuleEventListenerConfig extract_ruleEventListenerConfig() {
        return this.merge_ruleEventListenerConfig(java.util.stream.StreamSupport.stream(ruleEventListenerConfigs.spliterator(), false).collect(java.util.stream.Collectors.toList()), java.util.stream.StreamSupport.stream(agendaEventListeners.spliterator(), false).collect(java.util.stream.Collectors.toList()), java.util.stream.StreamSupport.stream(ruleRuntimeEventListeners.spliterator(), false).collect(java.util.stream.Collectors.toList()));
    }

    private org.kie.kogito.rules.RuleEventListenerConfig merge_ruleEventListenerConfig(java.util.Collection<org.kie.kogito.rules.RuleEventListenerConfig> ruleEventListenerConfigs, java.util.Collection<org.kie.api.event.rule.AgendaEventListener> agendaEventListeners, java.util.Collection<org.kie.api.event.rule.RuleRuntimeEventListener> ruleRuntimeEventListeners) {
        return new org.drools.core.config.CachedRuleEventListenerConfig(merge(ruleEventListenerConfigs, org.kie.kogito.rules.RuleEventListenerConfig::agendaListeners, agendaEventListeners), merge(ruleEventListenerConfigs, org.kie.kogito.rules.RuleEventListenerConfig::ruleRuntimeListeners, ruleRuntimeEventListeners));
    }

    public org.kie.kogito.Addons addons() {
        return new org.kie.kogito.Addons(java.util.Arrays.asList());
    }

    @javax.annotation.PostConstruct()
    public void init() {
        processConfig = new org.kie.kogito.process.impl.StaticProcessConfig(extract_workItemHandlerConfig(), extract_processEventListenerConfig(), extract_unitOfWorkManager(), extract_jobsService());
        ruleConfig = new org.drools.core.config.StaticRuleConfig(extract_ruleEventListenerConfig());
        decisionConfig = null;
    }
}
