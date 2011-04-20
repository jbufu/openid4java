/**
 * Copyright 2009 Google Inc.
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
 *
 */
package org.openid4java.server;

import com.google.inject.Inject;

import org.openid4java.discovery.yadis.YadisResolver;

/**
 * Factory object that, given a Yadis resolver, makes {@link RealmVerifier}s.
 */
public class RealmVerifierFactory {

  private final YadisResolver _yadisResolver;

  @Inject
  public RealmVerifierFactory(YadisResolver yadisResolver) {
    _yadisResolver = yadisResolver;
  }

  public RealmVerifier getRealmVerifierForConsumer() {
    return new RealmVerifier(false, _yadisResolver);
  }

  public RealmVerifier getRealmVerifierForServer() {
    return new RealmVerifier(true, _yadisResolver);
  }
}
