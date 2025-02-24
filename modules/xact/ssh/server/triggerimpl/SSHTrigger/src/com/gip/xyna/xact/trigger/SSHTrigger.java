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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;

import com.gip.xyna.CentralFactoryLogging;
import com.gip.xyna.xact.exceptions.XACT_TriggerCouldNotBeStartedException;
import com.gip.xyna.xact.exceptions.XACT_TriggerCouldNotBeStoppedException;
import com.gip.xyna.xdev.xfractmod.xmdm.EventListener;

import xact.ssh.server.XynaSSHServer;

public class SSHTrigger extends EventListener<SSHTriggerConnection, SSHStartParameter> implements Factory<Command> {

  private static Logger logger = CentralFactoryLogging.getLogger(SSHTrigger.class);

  private XynaSSHServer sshd;
  
  private BlockingQueue<SSHTriggerConnection> requests;
  
  private SSHStartParameter startParameter;

  
  public SSHTrigger() {
  }

  public void start(SSHStartParameter sp) throws XACT_TriggerCouldNotBeStartedException {
    this.startParameter = sp;
    
    requests = new LinkedBlockingQueue<SSHTriggerConnection>();
    
    sshd = new XynaSSHServer();
    try {
      sshd.init( sp );
    } catch (Exception e) { //XACT_InterfaceNoIPv6ConfiguredException, XACT_NetworkInterfaceNotFoundException, XACT_InterfaceNoIPConfiguredException
      throw new XACT_TriggerCouldNotBeStartedException(e) {
        private static final long serialVersionUID = 1L;
      };
    }
    
    sshd.setShellFactory(this);
    
    try {
      sshd.start();
    } catch (Exception e) {
      logger.error("Problems starting SSH Server: " + e);
      throw new XACT_TriggerCouldNotBeStartedException(e) {
        private static final long serialVersionUID = 1L;
      };
    }
  }

  public Command create() {
    return new ShellCommand(sshd, requests, startParameter);
  }
  
 

  public SSHTriggerConnection receive() {
    SSHTriggerConnection sshCon = null;
    while (true) {
      try {
        sshCon = requests.poll(1000,TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        //dann halt k�rzer warten
      }
      if( sshCon != null ) {
        if( sshCon.getRequestType() == null ) {
          return null; //ung�ltiger Eintrag nach Aufruf stop()
        }
        return sshCon;
      }
    }
  }

  /**
   * called by Xyna Processing to stop the Trigger.
   * should make sure, that start() may be called again directly afterwards. connection instances
   * returned by the method receive() should not be expected to work after stop() has been called.
   */
  public void stop() throws XACT_TriggerCouldNotBeStoppedException {
    try {
      logger.info("SSHTrigger stop");
      sshd.stop(true);
      requests.clear();
      
      //laufendes Receive darf keinen g�ltigen Eintrag finden, daher Dummy-Eintrag zum Wecken
      requests.offer( new SSHTriggerConnection(null, null) ); 
      
    } catch (IOException e) {
      throw new RuntimeException("Problems stopping SSH Server: " + e);
    }
  }
  
  /**
   * Called by Xyna Processing if there are not enough system capacities to process the request.
   */
  protected void onProcessingRejected(String cause, SSHTriggerConnection con) {
    con.sendLineQuietly(con.getCustomization().getErrorPrefix() + "processing rejected: "+cause);
    con.close();
  }

  /**
   * called when a triggerconnection generated by this trigger was not accepted by any filter
   * registered to this trigger
   * @param con corresponding triggerconnection
   */
  public void onNoFilterFound(SSHTriggerConnection con) {
    con.sendLineQuietly(con.getCustomization().getErrorPrefix() + "no filter");
    con.close();
  }

  /**
   * @return description of this trigger
   */
  public String getClassDescription() {
    return "SSH Trigger. Receives SSH Requests.";
  }


}
