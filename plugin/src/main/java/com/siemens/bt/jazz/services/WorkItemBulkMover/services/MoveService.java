package com.siemens.bt.jazz.services.WorkItemBulkMover.services;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.*;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.helpers.WorkItemHelpers;
import com.siemens.bt.jazz.services.base.rest.RestRequest;
import org.apache.commons.logging.Log;
import org.apache.http.auth.AuthenticationException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.google.gson.reflect.TypeToken;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeDefinition;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.MovePreparationResult;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.WorkItemMover;
import com.siemens.bt.jazz.services.base.rest.AbstractRestService;
import com.siemens.bt.jazz.services.base.utils.RequestReader;
import org.eclipse.core.runtime.NullProgressMonitor;

public class MoveService extends AbstractRestService {
    IWorkItemServer workItemServer;
    IProgressMonitor monitor;
    Gson gson;
    Type workItemIdCollectionType;
    Type attributesCollectionType;
    Type resultsType;

    public MoveService(Log log, HttpServletRequest request, HttpServletResponse response, RestRequest restRequest, TeamRawService parentService) {
        super(log, request, response, restRequest, parentService);
        this.workItemServer = parentService.getService(IWorkItemServer.class);
        this.monitor = new NullProgressMonitor();
        this.gson = new Gson();
        this.workItemIdCollectionType = new TypeToken<Collection<Integer>>(){}.getType();
        this.attributesCollectionType = new TypeToken<Collection<AttributeDefinition>>(){}.getType();
        this.resultsType = new TypeToken<Collection<AttributeDefinition>>() {}.getType();
    }
	
	public void execute() throws IOException, URISyntaxException, AuthenticationException {
        JsonObject responseJson = new JsonObject();
        WorkItemMover mover = new WorkItemMover(parentService);
        boolean isMoved = false;
        Collection<AttributeDefinition> moveResults = null;

        // read request data
        JsonObject workItemData = RequestReader.readAsJson(request);
        JsonPrimitive targetPA = workItemData.getAsJsonPrimitive("targetProjectArea");
        JsonArray workItemJson = workItemData.getAsJsonArray("workItems");
        JsonArray attributesJson = workItemData.getAsJsonArray("attributeDefinitions");

        // map cient data to model
        Collection<Integer> clientWorkItemList = gson.fromJson(workItemJson, workItemIdCollectionType);
        Collection<AttributeDefinition> clientMappingDefinitions = gson.fromJson(attributesJson, attributesCollectionType);

        try {
            // fetch full work item information
			List<IWorkItem> items = WorkItemHelpers.fetchWorkItems(clientWorkItemList, workItemServer, monitor);

			// prepare movement and track fields to be changed
			MovePreparationResult preparationResult = mover.PrepareMove(items, targetPA.getAsString(), clientMappingDefinitions);

			// store attribute based ovservations to be able to return this information to the end user
			moveResults = preparationResult.getAttributeDefinitions().getAttributeDefinitionCollection();

			// try to move the work items...
			IStatus status = mover.MoveAll(preparationResult.getWorkItems());
			isMoved = status.isOK();
		} catch (Exception e) {
            // Inform the user the the items could not be moved
            responseJson.addProperty("error", e.getMessage());
		}

        // prepare data to be returend
        responseJson.addProperty("successful", isMoved);
        responseJson.add("mapping", gson.toJsonTree(moveResults, resultsType));
        response.getWriter().write(gson.toJson(responseJson));
    }
}