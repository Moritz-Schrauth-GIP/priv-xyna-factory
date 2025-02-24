/*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 * Copyright 2022 GIP SmartMercial GmbH, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 */
package com.gip.xyna.xact.trigger;



import org.apache.log4j.Logger;

import com.gip.xyna.CentralFactoryLogging;
import com.gip.xyna.utils.exceptions.XynaException;
import com.gip.xyna.utils.snmp.agent.RequestHandler;
import com.gip.xyna.utils.snmp.agent.utils.OidSingleDispatcher;
import com.gip.xyna.xdev.xfractmod.xmdm.ConnectionFilter;
import com.gip.xyna.xdev.xfractmod.xmdm.XynaObject;
import com.gip.xyna.xprc.XynaOrder;



public class FactoryManagementSNMPFilter extends ConnectionFilter<SNMPTriggerConnection> {

  private static final long serialVersionUID = 1L;

  private static Logger logger = CentralFactoryLogging.getLogger(FactoryManagementSNMPFilter.class);

  private static OidSingleDispatcher oidSingleDispatcher;
  private static RequestHandler requestHandler;
  static {
    oidSingleDispatcher = new OidSingleDispatcher();
    oidSingleDispatcher.add(new XynaPropertyHandler()); //TODO sch�ner w�re, wenn die handler beim trigger registriert werden. der hat dann die �bersicht, was es so alles gibt
    requestHandler = new DefaultRequestHandler(oidSingleDispatcher);
  }


  /**
   * analyzes TriggerConnection and creates XynaOrder if it accepts the connection. if this filter does not return a
   * XynaOrder, Xyna Processing will call generateXynaOrder() of the next Filter registered for the Trigger
   * 
   * @param tc
   * @return XynaOrder which will be started by Xyna Processing. null if this Filter doesn't accept the connection
   * @throws XynaException caused by errors reading data from triggerconnection or having an internal error. results in
   *           onError() being called by Xyna Processing.
   * @throws InterruptedException if onError() should not be called. (e.g. if for a http trigger connection this filter
   *           decides, it wants to return a 500 servererror, and not call any workflow)
   */
  public XynaOrder generateXynaOrder(SNMPTriggerConnection tc) throws XynaException, InterruptedException {
    // wirft interruptedexception, falls oid bearbeitet wurde (filter greift), ansonsten passiert einfach nichts
    // (n�chster filter...)
    tc.handleEvent(requestHandler);    
    return null;
  }


  public void onResponse(XynaObject response, SNMPTriggerConnection tc) {
    //nicht zu implementieren, da keine xynaorder gestartet wird
  }


  public void onError(XynaException[] e, SNMPTriggerConnection tc) {
    if (e != null && e.length > 0) {
      try {
        tc.sendError(RequestHandler.GENERAL_ERROR, e);
      } catch (XynaException e1) {
        logger.error("could not send Error", e1);
      }
    }
  }


  @Override
  public String getClassDescription() {
    return "Filter for SNMP requests. Handles requests for all XynaProperties.";
  }


}
