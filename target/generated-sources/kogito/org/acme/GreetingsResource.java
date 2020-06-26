package org.acme;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.kie.api.runtime.process.WorkItemNotFoundException;
import org.kie.kogito.Application;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceExecutionException;
import org.kie.kogito.process.WorkItem;
import org.kie.kogito.process.workitem.Policy;
import org.kie.kogito.process.impl.Sig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acme.GreetingsModel;
import org.acme.GreetingsModelOutput;

@Path("/greetings")
@javax.enterprise.context.ApplicationScoped()
public class GreetingsResource {

    @javax.inject.Inject()
    @javax.inject.Named("greetings")
    Process<GreetingsModel> process;

    @javax.inject.Inject()
    Application application;

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GreetingsModelOutput createResource_greetings(@Context HttpHeaders httpHeaders, @QueryParam("businessKey") String businessKey, GreetingsModelInput resource) {
        if (resource == null) {
            resource = new GreetingsModelInput();
        }
        final GreetingsModelInput value = resource;
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<GreetingsModel> pi = process.createInstance(businessKey, mapInput(value, new GreetingsModel()));
            String startFromNode = httpHeaders.getHeaderString("X-KOGITO-StartFromNode");
            if (startFromNode != null) {
                pi.startFrom(startFromNode);
            } else {
                pi.start();
            }
            return getModel(pi);
        });
    }

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    public List<GreetingsModelOutput> getResources_greetings() {
        return process.instances().values().stream().map(pi -> mapOutput(new GreetingsModelOutput(), pi.variables())).collect(Collectors.toList());
    }

    @GET()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GreetingsModelOutput getResource_greetings(@PathParam("id") String id) {
        return process.instances().findById(id).map(pi -> mapOutput(new GreetingsModelOutput(), pi.variables())).orElse(null);
    }

    @DELETE()
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GreetingsModelOutput deleteResource_greetings(@PathParam("id") final String id) {
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<GreetingsModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                pi.abort();
                return getModel(pi);
            }
        });
    }

    @POST()
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GreetingsModelOutput updateModel_greetings(@PathParam("id") String id, GreetingsModel resource) {
        return org.kie.kogito.services.uow.UnitOfWorkExecutor.executeInUnitOfWork(application.unitOfWorkManager(), () -> {
            ProcessInstance<GreetingsModel> pi = process.instances().findById(id).orElse(null);
            if (pi == null) {
                return null;
            } else {
                pi.updateVariables(resource);
                return mapOutput(new GreetingsModelOutput(), pi.variables());
            }
        });
    }

    @GET()
    @Path("/{id}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getTasks_greetings(@PathParam("id") String id, @QueryParam("user") final String user, @QueryParam("group") final List<String> groups) {
        return process.instances().findById(id).map(pi -> pi.workItems(policies(user, groups))).map(l -> l.stream().collect(Collectors.toMap(WorkItem::getId, WorkItem::getName))).orElse(null);
    }

    protected GreetingsModelOutput getModel(ProcessInstance<GreetingsModel> pi) {
        if (pi.status() == ProcessInstance.STATE_ERROR && pi.error().isPresent()) {
            throw new ProcessInstanceExecutionException(pi.id(), pi.error().get().failedNodeId(), pi.error().get().errorMessage());
        }
        return mapOutput(new GreetingsModelOutput(), pi.variables());
    }

    protected Policy[] policies(String user, List<String> groups) {
        if (user == null) {
            return new Policy[0];
        }
        org.kie.kogito.auth.IdentityProvider identity = null;
        if (user != null) {
            identity = new org.kie.kogito.services.identity.StaticIdentityProvider(user, groups);
        }
        return new Policy[] { SecurityPolicy.of(identity) };
    }

    protected GreetingsModel mapInput(GreetingsModelInput input, GreetingsModel resource) {
        resource.fromMap(input.toMap());
        return resource;
    }

    protected GreetingsModelOutput mapOutput(GreetingsModelOutput output, GreetingsModel resource) {
        output.fromMap(resource.getId(), resource.toMap());
        return output;
    }
}
