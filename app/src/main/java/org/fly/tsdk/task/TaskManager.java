package org.fly.tsdk.task;

import org.fly.tsdk.io.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

final public class TaskManager {
    private static final String TAG = "TaskManager";

    protected final LinkedList<Task> runningTasks = new LinkedList<>();
    protected final Map<String, Task> completedTasks = new HashMap<>();

    public TaskManager() {
        running();
    }

    public void execute(Task task)
    {
        addTask(task);
    }

    public void syncExecute(Task task)
    {
        addTask(task);
    }

    private void addTask(Task task)
    {
        task.setManager(this);
        synchronized (runningTasks) {
            runningTasks.add(task);
            runningTasks.notify(); // 唤起线程
        }
    }

    public void submit(Task task) {
        synchronized (completedTasks) {
            completedTasks.put(task.getName(), task);
        }

        synchronized (runningTasks) {
            runningTasks.notify(); // thread wake up
        }
    }

    private Map<String, Object> getDependencies(Task task)
    {
        Map<String, Object> results = new HashMap<>();

        if (!task.hasDependency()){
            return results;
        }

        synchronized (completedTasks)
        {
            for (String name : task.getDependentNames()
                 ) {
                Task dependentTask = completedTasks.get(name);

                if (dependentTask != null)
                    results.put(dependentTask.getName(), dependentTask.getResult());
                else
                    return null;
            }
        }

        return results;
    }

    public void running()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted())
                {
                    Task runningTask = null;

                    try {
                        synchronized (runningTasks)
                        {
                            while (runningTasks.isEmpty())
                                runningTasks.wait(); // 线程休眠

                            for (Task task: runningTasks)
                            {
                                Map<String, Object> dependencies = getDependencies(task);
                                if (dependencies != null)
                                {
                                    task.setDependentResults(dependencies);
                                    runningTask = task;
                                    break;
                                }
                            }

                            if (runningTask == null)
                            {
                                runningTasks.wait(); // 没有可用的, 休眠
                            } else {
                                runningTasks.remove(runningTask);
                            }
                        }

                        if (runningTask != null)
                        {
                            runningTask.execute();
                        }

                    } catch (InterruptedException e)
                    {
                        Logger.e(TAG, e.getMessage(), e);
                        if (runningTask != null)
                            runningTask.triggerError(e);

                        break;// all break

                    } catch (Throwable e) {
                        if (runningTask != null)
                            runningTask.triggerError(e);

                    } finally {
                        runningTask = null;
                    }
                }
            }
        }).start();
    }
}
