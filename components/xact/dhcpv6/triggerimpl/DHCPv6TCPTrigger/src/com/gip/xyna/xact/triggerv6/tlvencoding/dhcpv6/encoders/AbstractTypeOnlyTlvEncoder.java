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
package com.gip.xyna.xact.triggerv6.tlvencoding.dhcpv6.encoders;

import java.io.IOException;
import java.io.OutputStream;

import com.gip.xyna.xact.triggerv6.tlvencoding.dhcpv6.Node;
import com.gip.xyna.xact.triggerv6.tlvencoding.dhcpv6.TypeOnlyNode;




/**
 * Abstract type only TLV encoder.
 */
public abstract class AbstractTypeOnlyTlvEncoder extends AbstractTlvEncoder {

    public AbstractTypeOnlyTlvEncoder(final int typeEncoding) {
        super(typeEncoding);
    }

    @Override
    protected final void writeNode(final Node node, final OutputStream target) throws IOException {
        if (!(node instanceof TypeOnlyNode)) {
            throw new IllegalArgumentException("Provided node is not a type only node: <" + node + ">.");
        }
        writeTypeOnlyNode((TypeOnlyNode) node, target);
    }

    protected abstract void writeTypeOnlyNode(final TypeOnlyNode node, final OutputStream target) throws IOException;
}
