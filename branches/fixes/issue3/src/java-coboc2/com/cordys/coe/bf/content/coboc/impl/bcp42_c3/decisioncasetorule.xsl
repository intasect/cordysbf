<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  	 <xsl:variable name="orgdn" select="/tuple/old/ENTITY/ORGANIZATION" />
  	 <xsl:variable name="userdn" select="/tuple/old/ENTITY/OWNER" />
  	 <xsl:variable name="dc-name" select="/tuple/old/ENTITY/OBJECT/DecisionCase/Name" />
  	 <xsl:variable name="rule-prefix" select="/tuple/old/ENTITY/OBJECT/DecisionCase/RulePrefix" />
  	 <xsl:variable name="template-key" select="/tuple/old/ENTITY/OBJECT/DecisionCase/Source" />
  	 <xsl:variable name="template-name">
		 <xsl:call-template name="lastIndexOf">
			<xsl:with-param name="string" select="$template-key"/>
			<xsl:with-param name="char" select="&apos;/&apos;"/>
		 </xsl:call-template>
	 </xsl:variable>
  
  <xsl:template match="/">
	  <xsl:apply-templates select="tuple/old/ENTITY/OBJECT/DecisionCase"/>	  
  </xsl:template>
  
  <xsl:template match="DecisionCase">
	<rulegroup bf-version="bcp42_c3">
		<rulegroup-content>
	        <rulegroup>
	            <rulegroupid/>
	            <orgcontext><xsl:value-of select="$orgdn" /></orgcontext>
	            <rulegroupname><xsl:value-of select="$dc-name" /></rulegroupname>
	            <rulegroupowner><xsl:value-of select="$userdn" /></rulegroupowner>
	            <rulegroupdescription><xsl:value-of select="Description" /></rulegroupdescription>
	            <rulegrouppriority>5</rulegrouppriority>
	        </rulegroup>
		</rulegroup-content>
	    <rules>
		    <xsl:apply-templates select="RuleGroup/Rule"/>	  
	    </rules>
	</rulegroup>
  </xsl:template>
  
  <xsl:template match="Rule">
  	<xsl:variable name="rule-name">
  		<xsl:value-of select="$rule-prefix" />-<xsl:value-of select="RuleNumber" />
  	</xsl:variable>
 	<xsl:variable name="rule-id">
  		<xsl:value-of select="dc-name" />/<xsl:value-of select="$rule-name" />
  	</xsl:variable>
  
   <rule>
       <ruleid><xsl:value-of select="$rule-id" /></ruleid>
       <version>1.0</version>
       <rulegroupid><xsl:value-of select="$dc-name" /></rulegroupid>
       <rulename><xsl:value-of select="$rule-name" /></rulename>
       <ruleowner><xsl:value-of select="$userdn" /></ruleowner>
       <orgcontext><xsl:value-of select="$orgdn" /></orgcontext>
       <ruledescription>Converted from decision case <xsl:value-of select="$dc-name" /></ruledescription>
       <rulepriority>5</rulepriority>
       <expirydate/>
       <enabled>true</enabled>
       <debug>false</debug>
       <toggletype>false</toggletype>
       <ruletype>constraint</ruletype>
       <link/>
       <ruletemplateid/>
       <mutex_rules/>
       <overrides_rules/>
       <br_triggers>
           <trigger>
               <template_id><xsl:value-of select="$template-key"/></template_id>
               <template_name><xsl:value-of select="$template-name" /></template_name>
               <on_insert>true</on_insert>
               <on_update>true</on_update>
               <on_delete>true</on_delete>
           </trigger>
       </br_triggers>
       <ismultiple>true</ismultiple>
       <openswith>decisioncaseeditor</openswith>       
       <ruledefinition>
           <rule>
               <usexpath>false</usexpath>
               <metainfo/>
		        <if>
		          <condition>
		            <xsl:for-each select="Condition">            
		                <xsl:if test="@Expression">
			    	   	   <xsl:value-of select="@Expression"/>
				      	   <xsl:if test="following-sibling::Condition/@Expression"> and </xsl:if>
				       </xsl:if>
		            </xsl:for-each>
		          </condition>
		          <conditionModel>
		              <xsl:for-each select="Condition">            
		                <xsl:if test="@Expression">
			    	   	   <expression>
			    	   	   		<xsl:attribute name="xpath">
		                          <xsl:value-of select="@Reference"/>
		                        </xsl:attribute>
			    	   	   		<xsl:value-of select="@Expression"/>
			    	   	   </expression>			      	   
				       </xsl:if>
		            </xsl:for-each>
		          </conditionModel>  
		          <then>
		            <action>
		              <xsl:for-each select="Action">
		                <xsl:choose>
		                  <xsl:when test="ActionDetails/ActionType = &apos;insert&apos;">
		                    <insert>
		                      <template>
		                        <xsl:attribute name="id">
		                          <xsl:value-of select="ActionDetails/TemplateID"/>
		                        </xsl:attribute>
		                        <xsl:attribute name="path">
		                          <xsl:value-of select="ActionDetails/TemplatePath"/>
		                        </xsl:attribute>
		                        <xsl:call-template name="lastIndexOf">
		                          <xsl:with-param name="string" select="ActionDetails/TemplatePath"/>
		                          <xsl:with-param name="char" select="&apos;/&apos;"/>
		                        </xsl:call-template>
		                      </template>
		                      <assignment>
		                        <xsl:call-template name="parseExpression">
		                          <xsl:with-param name="string" select="ActionDetails/Assignments"/>
		                        </xsl:call-template>
		                      </assignment>
		                    </insert>
		                  </xsl:when>
		                  <xsl:when test="ActionDetails/ActionType = &apos;update&apos;">
		                    <update>
		                      <template>
		                        <xsl:attribute name="id">
		                          <xsl:value-of select="ActionDetails/TemplateID"/>
		                        </xsl:attribute>
		                        <xsl:attribute name="path">
		                          <xsl:value-of select="ActionDetails/TemplatePath"/>
		                        </xsl:attribute>
		                        <xsl:call-template name="lastIndexOf">
		                          <xsl:with-param name="string" select="ActionDetails/TemplatePath"/>
		                          <xsl:with-param name="char" select="&apos;/&apos;"/>
		                        </xsl:call-template>
		                      </template>
		                      <assignment>
		                        <xsl:call-template name="parseExpression">
		                          <xsl:with-param name="string" select="ActionDetails/Assignments"/>
		                        </xsl:call-template>
		                      </assignment>
		                    </update>
		                  </xsl:when>
		                  <xsl:when test="ActionDetails/ActionType = &apos;delete&apos;">
		                    <delete>
		                      <template>
		                        <xsl:attribute name="path">
		                          <xsl:value-of select="ActionDetails/TemplatePath"/>
		                        </xsl:attribute>
		                        <xsl:attribute name="id">
		                          <xsl:value-of select="ActionDetails/TemplateID"/>
		                        </xsl:attribute>
		                        <xsl:call-template name="lastIndexOf">
		                          <xsl:with-param name="string" select="ActionDetails/TemplatePath"/>
		                          <xsl:with-param name="char" select="&apos;/&apos;"/>
		                        </xsl:call-template>
		                      </template>
		                    </delete>
		                  </xsl:when>
		                  <xsl:when test="ActionDetails/ActionType = &apos;assignment&apos;">
		                    <assignment>
		                      <xsl:attribute name="name">
		                        <xsl:value-of select="concat(ActionDetails/ActionType,&apos;-&apos;,ActionDetails/ActionName)"/>
		                      </xsl:attribute>
		                      <xsl:attribute name="description">
		                        <xsl:value-of select="ActionDetails/ActionObjectName"/>
		                      </xsl:attribute>                     
		                      <xsl:choose>
					  <xsl:when test="ActionDetails/AssignmentExpression/expression">
					      <xsl:copy-of select="ActionDetails/AssignmentExpression/expression"/> 
					  </xsl:when>
					  <xsl:otherwise>
						  <xsl:call-template name="parseExpression">
							  <xsl:with-param name="string" select="ActionDetails/AssignmentExpression"/>
						  </xsl:call-template>
					  </xsl:otherwise>	      
				      </xsl:choose>	      
		                    </assignment>
		                  </xsl:when>
		                  <xsl:when test="ActionDetails/ActionType = &apos;abort&apos;">
		                    <abort>
		                      <xsl:attribute name="name">
		                        <xsl:value-of select="concat(ActionDetails/ActionType,&apos;-&apos;,ActionDetails/ActionName)"/>
		                      </xsl:attribute>
		                      <assignment>
		                        <xsl:choose>
		                          <xsl:when test="ActionDetails/AbortMessage/expression">
		                            <xsl:copy-of select="ActionDetails/AbortMessage/expression"/>
		                          </xsl:when>
		                          <xsl:otherwise>
		                            <xsl:call-template name="parseExpression">
		                              <xsl:with-param name="string" select="concat(&apos;bre.abort.msg=&apos;,ActionDetails/AbortMessage)"/>
		                            </xsl:call-template>
		                          </xsl:otherwise>
		                        </xsl:choose>
		                      </assignment>
		                    </abort>
		                  </xsl:when>
		                  <xsl:when test="ActionDetails/ActionType = &apos;flowengine&apos;">
		                    <flow>
		                      <xsl:attribute name="name">
		                        <xsl:value-of select="concat(ActionDetails/ActionType,&apos;-&apos;,ActionDetails/ActionName)"/>
		                      </xsl:attribute>
		                      <method>
		                        <xsl:value-of select="concat(&apos;FlowEngine&apos;,string(&apos;&apos;))"/>
		                      </method>
		                      <parameters>
		                        <processname>
		                          <xsl:value-of select="ActionDetails/ProcessName"/>
		                        </processname>
		                        <modelSpace>
		                          <xsl:value-of select="ActionDetails/ProcessModelSpace"/>
		                        </modelSpace>
		                        <message>
		                          <xsl:value-of select="ActionDetails/ProcessInputMessage"/>
		                        </message>
		                      </parameters>
		                    </flow>
		                  </xsl:when>
		                  <xsl:when test="ActionDetails/ActionType = &apos;soaprpc&apos;">
		                    <soaprpc>
		                      <xsl:attribute name="name">
		                        <xsl:value-of select="concat(ActionDetails/ActionType,&apos;-&apos;,ActionDetails/ActionName)"/>
		                      </xsl:attribute>
		                      <namespace>
		                        <xsl:value-of select="ActionDetails/MethodNameSpace"/>
		                      </namespace>
		                      <method>
		                        <xsl:value-of select="ActionDetails/MethodName"/>
		                      </method>
		                      <parameters>
		                        <xsl:value-of select="ActionDetails/SoapRequest"/>
		                      </parameters>
		                    </soaprpc>
		                  </xsl:when>
		                  <xsl:when test="ActionDetails/ActionType = &apos;inbox&apos;">
				      <inbox>
					<parameters>
					  <DeliverMessage xmlns="http://schemas.cordys.com/1.0/notification">
					    <SOURCE>RuleEngine</SOURCE>
					    <MESSAGE_DATA>
					      <Application>
						<url>
						  <xsl:value-of select="ActionDetails/UrlToLoad"/>
						</url>
						<caption>
						  <xsl:value-of select="ActionDetails/Description"/>
						</caption>
						<description>
						  <xsl:value-of select="ActionDetails/Description"/>
						</description>
						<data><message_data>
						  <xsl:value-of select="ActionDetails/Message"/>
						</message_data></data>
					      </Application>
					    </MESSAGE_DATA>
					    <MESSAGE_TYPE>INFO</MESSAGE_TYPE>
					    <TARGET>CPCINBOX</TARGET>
					    <SUBJECT>
					      <xsl:value-of select="ActionDetails/Subject"/>
					    </SUBJECT>
					    <RECIEVERS>
		            <xsl:call-template name="createParticipants">
		              <xsl:with-param name="string" select="ActionDetails/User"/>
		            </xsl:call-template>
		            <xsl:call-template name="createParticipants">
		              <xsl:with-param name="string" select="ActionDetails/Role"/>
		            </xsl:call-template>
		          </RECIEVERS>
					    <SENDER><xsl:value-of select="$userdn" /></SENDER>
					  </DeliverMessage>
					</parameters>
				      </inbox>
		                  </xsl:when>
		                </xsl:choose>
		              </xsl:for-each>
		            </action>
		          </then>
		        </if>
		     </rule>
		 </ruledefinition>
      </rule>    
  </xsl:template>
  <xsl:template name="createParticipants">
    <xsl:param name="string" />
    <xsl:choose>
      <xsl:when test="substring-before($string,&apos;;&apos;)">
        <PARTICIPANT_DN>
          <xsl:value-of select="substring-before($string,&apos;;&apos;)"/>
        </PARTICIPANT_DN>
        <xsl:call-template name="createParticipants">
          <xsl:with-param name="string" select="substring-after($string, &apos;;&apos;)"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="lastIndexOf">
    <xsl:param name="string"/>
    <xsl:param name="char"/>
    <xsl:choose>
      <xsl:when test="contains($string, $char)">
        <xsl:call-template name="lastIndexOf">
          <xsl:with-param name="string" select="substring-after($string, $char)"/>
          <xsl:with-param name="char" select="$char"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="createExpression">
    <xsl:param name="string"/>    
    <expression><xsl:value-of select="$string" /></expression>
  </xsl:template> 
  <xsl:template name="parseExpression">
    <xsl:param name="string"/>
    <xsl:choose>
      <xsl:when test="substring-before($string,&apos;;&apos;) !=&apos;&apos;">
        <xsl:call-template name="createExpression">
          <xsl:with-param name="string" select="substring-before($string, &apos;;&apos;)"/>
        </xsl:call-template>
        <xsl:call-template name="parseExpression">
          <xsl:with-param name="string" select="substring-after($string, &apos;;&apos;)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="createExpression">
          <xsl:with-param name="string" select="$string"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
