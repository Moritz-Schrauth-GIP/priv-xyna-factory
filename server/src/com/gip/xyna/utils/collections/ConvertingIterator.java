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
package com.gip.xyna.utils.collections;

import java.util.Iterator;


/**
 * Dieser Iterator-Wrapper f�hrt eine Datenkonvertierung durch. 
 */
public abstract class ConvertingIterator<F,T> implements Iterator<T> {

  private Iterator<F> iter;

  public ConvertingIterator(Iterator<F> iter) {
    this.iter = iter;
  }
  
  public boolean hasNext() {
    return iter.hasNext();
  }

  public T next() {
    return convert(iter.next());
  }

  public void remove() {
    iter.remove();
  }
  
  /**
   * Konvertiert Element des zugrundeliegenden Iterators
   * @param next
   * @return
   */
  protected abstract T convert(F next);
  
}
