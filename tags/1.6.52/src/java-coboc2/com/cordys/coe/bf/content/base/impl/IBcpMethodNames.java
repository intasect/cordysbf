/**
 * Copyright 2006 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Build Framework. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cordys.coe.bf.content.base.impl;

import javax.xml.namespace.QName;

/**
 * Contains QNames for all methods as well as SOAP request template ID's. 
 *
 * @author mpoyhone
 */
public interface IBcpMethodNames
{
    
    String LDAP_NAMESPACE = "http://schemas.cordys.com/1.0/ldap";   
    QName LDAP_METHOD_UPDATE = new QName(LDAP_NAMESPACE, "Update");
    
    String LDAP_TEMPLATEID_GETMETHODSETS = "LDAP-GetMethodSets";
    String LDAP_TEMPLATEID_GETCHILDREN = "LDAP-GetChildren";
    
    String COBOC_NAMESPACE = "http://schemas.cordys.com/1.0/coboc";
    String COBOC_NAMESPACE_42 = "http://schemas.cordys.com/4.2/coboc";
    String COBOC_NOTIFICATION_NAMESPACE = "http://schemas.cordys.com/1.0/notification";
    String COBOC_SCHEDULER_NAMESPACE = "http://schemas.cordys.com/scheduler/1.0";
    QName COBOC_METHOD_ADDRULE= new QName(COBOC_NAMESPACE, "AddRule");
    QName COBOC_METHOD_UPDATERULE = new QName(COBOC_NAMESPACE, "UpdateRule");
    QName COBOC_METHOD_DELETERULE = new QName(COBOC_NAMESPACE, "DeleteRule");
    QName COBOC_METHOD_CREATERULEGROUP = new QName(COBOC_NAMESPACE, "CreateRuleGroup");
    QName COBOC_METHOD_UPDATERULEGROUP = new QName(COBOC_NAMESPACE, "UpdateRuleGroup");
    QName COBOC_METHOD_DELETERULEGROUP = new QName(COBOC_NAMESPACE, "DeleteRuleGroup");
    QName COBOC_METHOD_CREATESCHEDULETEMPLATE = new QName(COBOC_NAMESPACE, "CreateScheduleTemplate");
    QName COBOC_METHOD_UPDATESCHEDULETEMPLATE = new QName(COBOC_NAMESPACE, "UpdateScheduleTemplate");
    QName COBOC_METHOD_DELETESCHEDULETEMPLATE = new QName(COBOC_NAMESPACE, "DeleteScheduleTemplate");
    QName COBOC_METHOD_CREATEMESSAGEMODEL = new QName(COBOC_NOTIFICATION_NAMESPACE, "CreateMessageModel");
    QName COBOC_METHOD_UPDATEMESSAGEMODEL = new QName(COBOC_NOTIFICATION_NAMESPACE, "UpdateMessageModel");
    QName COBOC_METHOD_DELETEMESSAGEMODEL = new QName(COBOC_NOTIFICATION_NAMESPACE, "DeleteMessageModel");
    QName COBOC_METHOD_GETCOLLECTION  = new QName(COBOC_NAMESPACE_42, "GetCollection");
    QName COBOC_METHOD_GETXMLOBJECT = new QName(COBOC_NAMESPACE_42, "GetXMLObject");
    QName COBOC_METHOD_UPDATEXMLOBJECT = new QName(COBOC_NAMESPACE, "UpdateXMLObject");
    
    String XMLSTORE_NAMESPACE = "http://schemas.cordys.com/1.0/xmlstore";  
    QName XMLSTORE_METHOD_GETCOLLECTION = new QName(XMLSTORE_NAMESPACE, "GetCollection");
    QName XMLSTORE_METHOD_GETXMLOBJECT = new QName(XMLSTORE_NAMESPACE, "GetXMLObject");
    QName XMLSTORE_METHOD_UPDATEXMLOBJECT = new QName(XMLSTORE_NAMESPACE, "UpdateXMLObject");
    
    String COBOC_TEMPLATEID_GETROOTFOLDERCONTENTS = "COBOC-GetRootFolderContents";
    String COBOC_TEMPLATEID_GETSUBFOLDERCONTENTS = "COBOC-GetSubFolderContents";
    String COBOC_TEMPLATEID_GETRULEGROUPSBYOWNER = "COBOC-GetRuleGroupsByOwner";
    String COBOC_TEMPLATEID_GETRULESBYOWNER = "COBOC-GetRulesByOwner";
    String COBOC_TEMPLATEID_GETALLSCHEDULETEMPLATES = "COBOC-GetAllScheduleTemplates";
    String COBOC_TEMPLATEID_GETALLMESSAGEMODELS = "COBOC-GetAllMessageModels";
    String COBOC_TEMPLATEID_GETMESSAGEMODELDETAILS = "COBOC-GetMessageModelDetails";
    String COBOC_TEMPLATEID_GETTEMPLATESPECIAL_ATTRIBUTES = "COBOC-GetTemplateSpecialAttributes";
    String COBOC_TEMPLATEID_CREATETEMPLATESPECIAL_ATTRIBUTE = "COBOC-CreateTemplateSpecialAttributes";
    String COBOC_TEMPLATEID_DELETETEMPLATESPECIAL_ATTRIBUTE = "COBOC-DeleteTemplateSpecialAttributes";
    
    // C3 Methods
    String COBOC_TEMPLATEID_GETMESSAGETEMPLATES = "COBOC-GetMessageTemplates";
    String COBOC_TEMPLATEID_GETMESSAGETEMPLATEDETAILS = "COBOC-GetMessageTemplateDetails";
    String COBOC_TEMPLATEID_GETINBOXMODELS = "COBOC-GetInboxModels";
    String COBOC_TEMPLATEID_GETEMAILMODELS = "COBOC-GetEmailModels";
    QName COBOC_METHOD_UPDATEMESSAGETEMPLATE = new QName(COBOC_NOTIFICATION_NAMESPACE, "UpdateMessageTemplate");
    QName COBOC_METHOD_UPDATEEMAILMODEL = new QName(COBOC_NOTIFICATION_NAMESPACE, "UpdateEmailModel");
    String COBOC_TEMPLATEID_GETWSAPPSCLASSREGISTRY = "COBOC-GetWsAppServerClassRegistry"; // Actually XMLStore GetCollection method.
}
