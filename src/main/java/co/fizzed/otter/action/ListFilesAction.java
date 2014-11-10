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
package co.fizzed.otter.action;

import co.fizzed.otter.core.Context;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author joelauer
 */
public class ListFilesAction extends Action<List<File>> {
    
    private File path;
    private Boolean recursive;
    private FileFilter filter;
    
    public ListFilesAction(Context context) {
        super(context);
        this.recursive = Boolean.FALSE;
    }

    public File getPath() {
        return path;
    }
    
    public ListFilesAction setPath(File path) {
        this.path = path;
        return this;
    }
    
    public ListFilesAction setPath(String path) {
        this.path = new File(path);
        return this;
    }
    
    public Boolean getRecursive() {
        return recursive;
    }
    
    public ListFilesAction recursive(Boolean recursive) {
        this.recursive = recursive;
        return this;
    }
    
    public FileFilter getFilter() {
        return filter;
    }
    
    public ListFilesAction filter(FileFilter filter) {
        this.filter = filter;
        return this;
    }
    
    @Override
    public Result<List<File>> call() throws Exception {
        List<File> results = new ArrayList<File>();
        doListFiles(this.path, results, this.recursive);
        return new Result(results);
    }
    
    private void doListFiles(File f, List<File> results, boolean recursive) {
        if (filter == null || filter.accept(f)) {
            results.add(f);
        }
        
        if (f.isDirectory()) {
            File[] childFiles = f.listFiles();
            for (File childFile : childFiles) {
                if (filter == null || filter.accept(f)) {
                    results.add(f);
                }
                if (recursive) {
                    doListFiles(childFile, results, recursive);
                }
            }
        }
    }
    
}
