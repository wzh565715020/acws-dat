package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 主要做了一个去重的操作，当时同一个任务的时候，会覆盖
 */
@SuppressWarnings("unchecked")
public class TaskPriorityBlockingQueue {

    private final ReentrantLock lock;

    private transient Comparator<TaskPo> comparator;

    private int capacity;

    private volatile int size;
    private TaskPo[] queue;
    private ConcurrentHashSet<String> ID_SET = new ConcurrentHashSet<String>();

    public TaskPriorityBlockingQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        this.lock = new ReentrantLock();
        this.comparator = new Comparator<TaskPo>() {
            @Override
            public int compare(TaskPo left, TaskPo right) {
                if (left.getTaskId().equals(right.getTaskId())) {
                    return 0;
                }
                int compare = left.getTriggerTime().compareTo(right.getTriggerTime());
                if (compare != 0) {
                    return compare;
                }
                if (compare != 0) {
                    return compare;
                }
                compare = left.getCreateDate().compareTo(right.getCreateDate());
                if (compare != 0) {
                    return compare;
                }
                return -1;
            }
        };
        this.queue = new TaskPo[this.capacity];
    }

    public int size() {
        return size;
    }

    public boolean offer(TaskPo e) {
        if (e == null)
            throw new NullPointerException();
        if (size >= capacity) {
            // 满了，添加失败
            return false;
        }
        final ReentrantLock lock = this.lock;
        lock.lock();
        int n = size;
        try {
            if (ID_SET.contains(e.getId())) {
                // 如果已经存在了，替换
            	return true;
            }else{
                siftUpUsingComparator(n, e, queue, comparator);
                size = n + 1;
                ID_SET.add(e.getId());
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    public TaskPo poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size - 1;
            if (n < 0)
                return null;
            else {
                TaskPo[] array = queue;
                TaskPo result = array[0];
                TaskPo x = array[n];
                array[n] = null;
                siftDownUsingComparator(0, x, array, n, comparator);
                size = n;
                ID_SET.remove(result.getId());
                return result;
            }
        } finally {
            lock.unlock();
        }
    }

	private <E> void siftDownUsingComparator(int k, E x, Object[] array,
                                             int n,
                                             Comparator<? super E> cmp) {
        if (n > 0) {
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && cmp.compare((E) c, (E) array[right]) > 0)
                    c = array[child = right];
                int code = cmp.compare(x, (E) c);
                if (code <= 0)
                    break;
                array[k] = c;
                k = child;
            }
            array[k] = x;
        }
    }

    private <E> void siftUpUsingComparator(int k, E x, Object[] array,
                                           Comparator<? super E> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            int compCode = cmp.compare(x, (E)e);
            if (compCode >= 0)
                break;
            array[k] = e;
            k = parent;
        }
        array[k] = x;
    }

    private boolean replace(TaskPo o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i != -1) {
                this.queue[i] = o;
                return true;
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    private int indexOf(TaskPo o) {
        if (o != null) {
            TaskPo[] array = queue;
            int n = size;
            for (int i = 0; i < n; i++)
                if (o.getTaskId().equals(array[i].getTaskId()))
                    return i;
        }
        return -1;
    }

    public String toString() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = size;
            if (n == 0)
                return "[]";
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < n; ++i) {
                Object e = queue[i];
                sb.append(e == this ? "(this Collection)" : e);
                if (i != n - 1)
                    sb.append(',').append(' ');
            }
            return sb.append(']').toString();
        } finally {
            lock.unlock();
        }
    }

}
