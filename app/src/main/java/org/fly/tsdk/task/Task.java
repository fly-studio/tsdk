package org.fly.tsdk.task;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final public class Task {
    private String name;
    private String[] dependentNames;
    private TaskExecutor executor;
    private TaskManager taskManager;
    private Map<String, Object> dependentResults = null;
    private Object result = null;
    private boolean withCallback = true;

    private Task() {

    }

    private void setName(String name) {
        this.name = name;
    }

    private void setDependentNames(String[] dependentNames)
    {
        this.dependentNames = dependentNames;
    }

    private void setExecutor(TaskExecutor executor) {
        this.executor = executor;
    }

    private void withoutCallback() {
        this.withCallback = false;
    }

    public Task setManager(TaskManager taskManager)
    {
        this.taskManager = taskManager;
        return this;
    }

    private void setResult(Object result) {
        this.result = result;
        executor = null;
    }

    public Object getResult() {
        return result;
    }

    public String getName() {
        return name;
    }

    public String[] getDependentNames() {
        return dependentNames;
    }

    public boolean hasDependency() {
        return dependentNames.length > 0;
    }

    public Map<String, Object> getDependentResults() {
        return dependentResults;
    }

    public void setDependentResults(Map<String, Object> dependentResults) {
        this.dependentResults = dependentResults;
    }

    public void triggerError(Throwable e) {
        if (null != executor)
            executor.onError(e);
        executor = null;
    }

    public void execute()
    {
        executor.call(getDependentResults(), new TaskCallback() {
            @Override
            public void callback(Object result) {

                if (taskManager != null && withCallback)
                {
                    setResult(result);
                    taskManager.submit(Task.this);
                }
            }
        });
    }

    public static class Builder {

        private Set<String> dependencies = new HashSet<>();
        private Task task = new Task();

        public Builder(TaskExecutor executor)
        {
            task.setExecutor(executor);
        }

        public Builder name(String name)
        {
            task.setName(name.toUpperCase());
            return this;
        }

        public Builder dependOn(String dependency)
        {
            dependencies.add(dependency.toUpperCase());
            return this;
        }

        public Builder executor(TaskExecutor executor)
        {
            task.setExecutor(executor);
            return this;
        }

        public Builder withoutCallback()
        {
            task.withoutCallback();
            return this;
        }

        public Task build()
        {
            task.setDependentNames(dependencies.toArray(new String[0]));
            return task;
        }
    }

}
