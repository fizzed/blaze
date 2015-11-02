/*
 * Copyright 2015 Fizzed, Inc.
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
package com.fizzed.blaze.internal;

import com.fizzed.blaze.core.Dependency;
import com.fizzed.blaze.core.DependencyResolveException;
import com.fizzed.blaze.core.DependencyResolver;
import com.fizzed.blaze.Context;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Joe Lauer
 */
public class NoopDependencyResolver implements DependencyResolver {
    private static final Logger log = LoggerFactory.getLogger(NoopDependencyResolver.class);
    
    @Override
    public List<File> resolve(Context context, List<Dependency> resolvedDependencies, List<Dependency> dependencies) throws DependencyResolveException, ParseException, IOException {
        log.debug("Noop resolving in effect (doing nothing)");
        return null;
    }
    
}
