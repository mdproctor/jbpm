/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.casemgmt.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbpm.casemgmt.api.dynamic.TaskSpecification;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.api.model.instance.CaseRoleInstance;
import org.jbpm.casemgmt.api.model.instance.CommentInstance;
import org.jbpm.casemgmt.api.model.instance.CommentSortBy;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.internal.query.QueryContext;

/**
 * Provides case management operations.
 */
public interface CaseService {

    /**
     * Starts a new case for given definition with empty case file.
     * <br/>
     * Case id is generated based on case id prefix (defined on case definition) and generated value.<br>
     * <code>CASE-XXXXXXXXX</code> where <code>XXXXXXX</code> is generated value for the prefix<br/>
     * Examples:
     * <ul> 
     *  <li>CASE-0000000123</li>
     *  <li>HR-0000000321</li>
     *  <li>LOAN-0000000099</li>
     * </ul>
     * @param deploymentId deployment id of project that case definition belongs to
     * @param caseDefinitionId id of case definition
     * @return returns unique case id in the format PREFIX-GENERATED_ID as described above
     */
    String startCase(String deploymentId, String caseDefinitionId);
    
    /**
     * Starts a new case for given definition with given case file.
     * <br/>
     * Case id is generated based on case id prefix (defined on case definition) and generated value.<br>
     * <code>CASE-XXXXXXXXX</code> where <code>XXXXXXX</code> is generated value for the prefix<br/>
     * Examples:
     * <ul> 
     *  <li>CASE-0000000123</li>
     *  <li>HR-0000000321</li>
     *  <li>LOAN-0000000099</li>
     * </ul>
     * @param deploymentId deployment id of project that case definition belongs to
     * @param caseDefinitionId id of case definition
     * @param caseFile initial case file to be used for this case
     * @return returns unique case id in the format PREFIX-GENERATED_ID as described above
     */
    String startCase(String deploymentId, String caseDefinitionId, CaseFileInstance caseFile);
    
    /**
     * Returns Case file for give case id
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method 
     * @return returns current snapshot of CaseFileInstance
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    CaseFileInstance getCaseFileInstance(String caseId) throws CaseNotFoundException;
    
    /**
     * Returns case instance (only if it's active) identified by given case id - does not load case file, roles, milestones nor stages.
     * Use {@link #getCaseInstance(String, boolean, boolean, boolean, boolean)} for more advanced fetch options.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @return returns current snapshot of CaseInstance
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    CaseInstance getCaseInstance(String caseId) throws CaseNotFoundException;
    
    /**
     * Returns case instance (only if it's active) identified by given case id with options on what should be fetched
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param withData determines if case file should be loaded
     * @param withRoles determines if role assignments should be loaded
     * @param withMilestones determines if milestones should be loaded
     * @param withStages determines with stages should be loaded
     * @return returns current snapshot of CaseInstance
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    CaseInstance getCaseInstance(String caseId, boolean withData, boolean withRoles, boolean withMilestones, boolean withStages) throws CaseNotFoundException;
    
    /**
     * Cancels case with given case id (including all attached process instances if any). 
     * Does not affect case file so in case it can still be used to reopen the case by starting new instances. 
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void cancelCase(String caseId) throws CaseNotFoundException;
    
    /**
     * Permanently destroys case identified by given case id. It performs the same operation as abortCase 
     * and destroys the case file and other attached information.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void destroyCase(String caseId) throws CaseNotFoundException;
    
    /*
     * dynamic case operations section
     */
    
    /**
     * Adds new user task to specified case. Should be used when user task should be added to the main process instance of the case. 
     * If there are more process instances for given case and user task should be added to specific process instance 
     * {@link #addDynamicTask(Long, TaskSpecification)} should be used
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param taskSpecification complete specification that defines the type of a task to be added
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDynamicTask(String caseId, TaskSpecification taskSpecification) throws CaseNotFoundException;
    
    /**
     * Adds new user task to specified process instance. 
     * @param processInstanceId unique process instance id
     * @param taskSpecification complete specification that defines the type of a task to be added
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDynamicTask(Long processInstanceId, TaskSpecification taskSpecification) throws ProcessInstanceNotFoundException;
    
    /**
     * Adds new user task to specified case and stage. Should be used when user task should be added to the main process instance of the case. 
     * If there are more process instances for given case and user task should be added to specific process instance 
     * {@link #addDynamicTaskToStage(Long, String, TaskSpecification)} should be used
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param stageId id of the stage there the task should be added
     * @param taskSpecification complete specification that defines the type of a task to be added
     * @throws CaseNotFoundException thrown in case case was not found with given id
     * @throws StageNotFoundException thrown in case stage does not exist
     */
    void addDynamicTaskToStage(String caseId, String stageId, TaskSpecification taskSpecification) throws CaseNotFoundException, StageNotFoundException;
    
    /**
     * Adds new user task to specified case and stage on given process instance. 
     * @param processInstanceId unique process instance id
     * @param stageId id of the stage there the task should be added
     * @param taskSpecification complete specification that defines the type of a task to be added
     * @throws CaseNotFoundException thrown in case case was not found with given id
     * @throws StageNotFoundException thrown in case stage does not exist
     */
    void addDynamicTaskToStage(Long processInstanceId, String stageId, TaskSpecification taskSpecification) throws CaseNotFoundException, StageNotFoundException;
    
    /**
     * Adds new subprocess (identified by process id) to given process instance. Should be used when subprocess should be added to the main process instance of the case. 
     * If there are more process instances for given case and subprocess should be added to specific process instance 
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param processId identifier of the process to be added
     * @param parameters optional parameters for the subprocess
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDynamicSubprocess(String caseId, String processId, Map<String, Object> parameters) throws CaseNotFoundException;
    
    /**
     * Adds new subprocess (identified by process id) to case.
     * @param processInstanceId unique process instance id
     * @param processId identifier of the process to be added
     * @param parameters optional parameters for the subprocess
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDynamicSubprocess(Long processInstanceId, String processId, Map<String, Object> parameters) throws CaseNotFoundException;
    
    /**
     * Adds new subprocess (identified by process id) to given process instance. Should be used when subprocess should be added to the 
     * main process instance of the case. If there are more process instances for given case and subprocess should be added to specific process instance 
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method {@link #addDynamicSubprocess(Long, String, Map)}
     * method should be used instead.
     * @param stageId id of the stage there the task should be added
     * @param processId identifier of the process to be added
     * @param parameters optional parameters for the subprocess
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDynamicSubprocessToStage(String caseId, String stageId, String processId, Map<String, Object> parameters) throws CaseNotFoundException;
    
    /**
     * Adds new subprocess (identified by process id) to case.
     * @param processInstanceId unique process instance id
     * @param stageId id of the stage there the task should be added
     * @param processId identifier of the process to be added
     * @param parameters optional parameters for the subprocess
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDynamicSubprocessToStage(Long processInstanceId, String stageId, String processId, Map<String, Object> parameters) throws CaseNotFoundException;
    
    /**
     * Triggers given by fragmentName adhoc element (such as task, milestone) within given case.  Should be used when fragment should be triggered 
     * on the main process instance of the case. If there are more process instances for given case and fragment should be triggered on specific process
     *  instance {@link #triggerAdHocFragment(Long, String, Object)} method should be used instead 
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param fragmentName name of the element that can be triggered
     * @param data optional data to be given when triggering the node
     */
    void triggerAdHocFragment(String caseId, String fragmentName, Object data) throws CaseNotFoundException;
    
    /**
     * Triggers given by fragmentName adhoc element (such as task, milestone) within given process instance
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param fragmentName name of the element that can be triggered
     * @param data optional data to be given when triggering the node
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void triggerAdHocFragment(Long processInstanceId, String fragmentName, Object data) throws CaseNotFoundException;
    
    /*
     * Case file section
     */
    
    /**
     * Adds given named value into case file of given case.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param name unique name for given value to be put into case file
     * @param value actual value to be added to case file
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDataToCaseFile(String caseId, String name, Object value) throws CaseNotFoundException;
    
    /**
     * Adds complete map to case file of given case. Replaces any existing value that is registered under same name.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param data key value representing data to be added to case file
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addDataToCaseFile(String caseId, Map<String,Object> data) throws CaseNotFoundException;
    
    /**
     * Removes given variable (stored under name) from case file of given case.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param name variable name that should be removed from case file
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void removeDataFromCaseFile(String caseId, String name) throws CaseNotFoundException;
    
    /**
     * Removes given variables (stored under variableNames) from case file of given case.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param variableNames list of variables to be removed from the case file
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void removeDataFromCaseFile(String caseId, List<String> variableNames) throws CaseNotFoundException;
    
    /*
     * Case role section
     */
    
    /**
     * Assigns given entity (user or group) to case role for a given case. Case roles can be used for user task assignments.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param role name of the role entity should be assigned to
     * @param entity user or group to be assigned to given role
     * @throws CaseNotFoundException thrown in case case was not found with given id
     * @throws IllegalArgumentException thrown in case there no role found with given name or cardinality was exceeded 
     */
    void assignToCaseRole(String caseId, String role, OrganizationalEntity entity) throws CaseNotFoundException;
    
    /**
     * Removes given entity (user or group) from the case role for a given case.
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param role name of the role entity should be assigned to
     * @param entity entity user or group to be assigned to given role
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void removeFromCaseRole(String caseId, String role, OrganizationalEntity entity) throws CaseNotFoundException;
    
    /**
     * Returns role assignments for given case
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method 
     * @return returns collection of all currently defined role assignments of the given case
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    Collection<CaseRoleInstance> getCaseRoleAssignments(String caseId) throws CaseNotFoundException;
    
    /*
     * Case comments section
     */
    
    /**
     * Returns all case comments sorted by date
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method 
     * @return returns all comments added to given case
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    Collection<CommentInstance> getCaseComments(String caseId, QueryContext queryContext) throws CaseNotFoundException;
    
    /**
     * Returns all case comments sorted with given sortBy
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param sortBy defines how to sort comments
     * @return sorted comments
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    Collection<CommentInstance> getCaseComments(String caseId, CommentSortBy sortBy, QueryContext queryContext) throws CaseNotFoundException;
    
    /**
     * Adds new comment to the case
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param author author of the comment
     * @param comment actual comment (text)
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void addCaseComment(String caseId, String author, String comment) throws CaseNotFoundException;
    
    /**
     * Updated given comment with entire text provided
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param commentId unique id of the comment
     * @param author author of the comment
     * @param text updated text of the comment
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void updateCaseComment(String caseId, String commentId, String author, String text) throws CaseNotFoundException;
    
    /**
     * Removes given comment from the case comments list
     * @param caseId unique case id in the format PREFIX-GENERATED_ID as described on startCase method
     * @param commentId unique id of the comment
     * @throws CaseNotFoundException thrown in case case was not found with given id
     */
    void removeCaseComment(String caseId, String commentId) throws CaseNotFoundException;
    
    /*
     * Case model instance creation section
     */
    
    /**
     * Builds and returns new CaseFileInstance with given data. Not yet associated with any case
     * @param deploymentId deployment that case belongs to
     * @param caseDefinition id of the case definition to be able to properly setup case file
     * @param data initial data for case file
     * @return returns new instance (not associated with case) of CaseFileInstance populated with given data
     */
    CaseFileInstance newCaseFileInstance(String deploymentId, String caseDefinition, Map<String, Object> data);
    
    /**
     * Builds and returns new CaseFileInstance with given data and roles assignments. Not yet associated with any case
     * @param deploymentId deployment that case belongs to
     * @param caseDefinition id of the case definition to be able to properly setup case file
     * @param data initial data for case file
     * @param rolesAssignment initial role assignment
     * @return returns new instance (not associated with case) of CaseFileInstance populated with given data
     */
    CaseFileInstance newCaseFileInstance(String deploymentId, String caseDefinition, Map<String, Object> data, Map<String, OrganizationalEntity> rolesAssignment);
    
    /**
     * Returns new TaskSpecification describing user task so it can be created as dynamic task. All string
     * based attributes support variable expressions (#{variable-name})
     * @param taskName - mandatory name of the task
     * @param description - optional description of the task
     * @param actorIds - optional list (comma separated) of actors to be assigned
     * @param groupIds - optional list (comma separated) of groups to be assigned 
     * @param parameters - optional parameters (task inputs)
     * @return
     */
    TaskSpecification newHumanTaskSpec(String taskName, String description, String actorIds, String groupIds, Map<String, Object> parameters);
    
    /**
     * Returns new TaskSpecification describing generic (work item based) task so it can be added as dynamic task.
     * @param nodeType - type of a node (same as used for registering work item handler)
     * @param nodeName - name of the node to be assigned on task
     * @param parameters - optional parameters (task inputs)
     * @return
     */
    TaskSpecification newTaskSpec(String nodeType, String nodeName, Map<String, Object> parameters);
}
