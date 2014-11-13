/*
 * Copyright 2014 Fizzed Inc.
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
package co.fizzed.blaze.action;

import co.fizzed.blaze.core.Context;
import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author joelauer
 */
public class CopyFilesAction extends Action<Void> {
    
    private File source;
    private File target;
    //private Boolean recursive;
    private FileFilter filter;
    
    public CopyFilesAction(Context context) {
        super(context, "cp");
        //this.recursive = Boolean.FALSE;
    }

    public File getSource() {
        return source;
    }

    public CopyFilesAction source(File source) {
        this.source = source;
        return this;
    }

    public File getTarget() {
        return target;
    }

    public CopyFilesAction target(File target) {
        this.target = target;
        return this;
    }
    
    /**
    public Boolean getRecursive() {
        return recursive;
    }
    
    public CopyFilesAction recursive(Boolean recursive) {
        this.recursive = recursive;
        return this;
    }
    */
    
    public FileFilter getFilter() {
        return filter;
    }
    
    public CopyFilesAction filter(FileFilter filter) {
        this.filter = filter;
        return this;
    }
    
    @Override
    protected Result<Void> execute() throws Exception {
        File resolvedSource = this.context.resolveWithBaseDir(source);
        File resolvedTarget = this.context.resolveWithBaseDir(target);
        
        if (resolvedSource.isDirectory()) {
            if (resolvedTarget.isFile()) {
                throw new IllegalArgumentException("Source and target dirs are both dirs");
            }
            FileUtils.copyDirectory(resolvedSource, resolvedTarget, filter);
        } else {
            FileUtils.copyFileToDirectory(resolvedSource, resolvedTarget);
        }
        return new Result(Void.TYPE);
    }
    
}
