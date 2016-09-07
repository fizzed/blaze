/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.blaze.ssh.impl;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.ssh.SshConnect;
import com.fizzed.blaze.ssh.SshProvider;
import com.fizzed.blaze.util.MutableUri;
import java.util.Arrays;
import java.util.List;

public class JschProvider implements SshProvider {

    public static final List<String> SCHEMES = Arrays.asList("ssh");
    
    @Override
    public List<String> schemes() {
        return SCHEMES;
    }

    @Override
    public SshConnect connect(Context context, MutableUri uri) {
        return new JschConnect(context, uri);
    }
    
}
