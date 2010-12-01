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
package com.cordys.coe.bf.utils;

import com.cordys.coe.bf.BFContext;
import com.cordys.coe.bf.content.types.EContentType;
import com.cordys.coe.bf.content.types.coboc.CobocContentBase;
import com.cordys.coe.bf.content.types.coboc.CobocContentHandle;

import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;

import org.apache.commons.beanutils.BeanUtils;

/**
 * CoBOC content utilities.
 *
 * @author mpoyhone
 */
public class CobocUtils
{
    /**
     * Returns the the string representation of the given object based
     * on JavaBean properties.
     *
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("unchecked")
    public static String beanToString(Object oBean, String sType,
                                      boolean bPretty)
    {
        StringBuffer sbRes = new StringBuffer();
        boolean bFirst = true;

        sbRes.append(sType);
        sbRes.append("[");

        if (bPretty)
        {
            sbRes.append("\n");
        }

        Map<String, Object> mProperties = null;

        try
        {
            mProperties = (Map<String, Object>) BeanUtils.describe(oBean);
        }
        catch (Exception ignored)
        {
        }

        if (mProperties != null)
        {
            for (String sName : mProperties.keySet())
            {
                String sValue;

                try
                {
                    sValue = BeanUtils.getProperty(oBean, sName);
                }
                catch (Exception ignored)
                {
                    continue;
                }

                if (!bFirst && !bPretty)
                {
                    sbRes.append(", ");
                }

                if (bPretty)
                {
                    sbRes.append("\t");
                }

                sbRes.append(sName).append("=").append(sValue);
                bFirst = false;

                if (bPretty)
                {
                    sbRes.append("\n");
                }
            }
        }

        sbRes.append("]");

        if (bPretty)
        {
            sbRes.append("\n");
        }

        return sbRes.toString();
    }

    /**
     * DOCUMENTME
     *
     * @param bcContext DOCUMENTME
     * @param ctType DOCUMENTME
     * @param bReadObjectIds DOCUMENTME
     * @param lNodeList DOCUMENTME
     */
    public static void convertCobocHandleNodes(BFContext bcContext,
                                               EContentType ctType,
                                               boolean bReadObjectIds,
                                               List<OMNode> lNodeList)
    {
        if (bcContext == null)
        {
            return;
        }

        if (lNodeList == null)
        {
            return;
        }

        for (OMNode onNode : lNodeList)
        {
            String sNodeValue = null;

            if (onNode.getType() == OMNode.ELEMENT_NODE)
            {
                sNodeValue = ((OMElement) onNode).getText();
            }

            if (sNodeValue == null)
            {
                continue;
            }

            CobocContentHandle cchHandle = new CobocContentHandle(ctType);

            if (bReadObjectIds)
            {
                cchHandle.setObjectId(sNodeValue);
            }
            else
            {
                cchHandle.setKey(sNodeValue);
            }

            updateCobocHandleId(bcContext, cchHandle);

            if (bReadObjectIds)
            {
                sNodeValue = cchHandle.getKey();
            }
            else
            {
                sNodeValue = cchHandle.getObjectId();
            }

            if (sNodeValue != null)
            {
                if (onNode.getType() == OMNode.ELEMENT_NODE)
                {
                    ((OMElement) onNode).setText(sNodeValue);
                }
            }
        }
    }

    /**
     * DOCUMENTME
     *
     * @param bcContext DOCUMENTME
     * @param cchHandle DOCUMENTME
     */
    public static void updateCobocHandle(BFContext bcContext,
                                         CobocContentHandle cchHandle)
    {
        if (bcContext == null)
        {
            return;
        }

        CobocContentBase ccbObject = (CobocContentBase) bcContext.findContent(cchHandle);

        if (ccbObject != null)
        {
            String sObjectId = ccbObject.getObjectId();
            String sKey = ccbObject.getKey();

            if (sObjectId != null)
            {
                cchHandle.setObjectId(sObjectId);
            }

            if (sKey != null)
            {
                cchHandle.setKey(ccbObject.getKey());
            }
        }
    }

    /**
     * DOCUMENTME
     *
     * @param bcContext DOCUMENTME
     * @param cchHandle DOCUMENTME
     */
    public static void updateCobocHandleId(BFContext bcContext,
                                           CobocContentHandle cchHandle)
    {
        if (bcContext == null)
        {
            return;
        }

        CobocContentBase ccbObject = (CobocContentBase) bcContext.findContent(cchHandle);

        if (ccbObject != null)
        {
            cchHandle.setObjectId(ccbObject.getObjectId());
        }
    }

    /**
     * DOCUMENTME
     *
     * @param bcContext DOCUMENTME
     * @param cchaHandles DOCUMENTME
     */
    public static void updateCobocHandleIds(BFContext bcContext,
                                            CobocContentHandle[] cchaHandles)
    {
        if (bcContext == null)
        {
            return;
        }

        if (cchaHandles == null)
        {
            return;
        }

        for (CobocContentHandle cchHandle : cchaHandles)
        {
            updateCobocHandleId(bcContext, cchHandle);
        }
    }
}
