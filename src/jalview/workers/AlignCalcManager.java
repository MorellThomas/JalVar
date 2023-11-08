/*
 * Jalview - A Sequence Alignment Editor and Viewer ($$Version-Rel$$)
 * Copyright (C) $$Year-Rel$$ The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.workers;

import jalview.api.AlignCalcManagerI;
import jalview.api.AlignCalcWorkerI;
import jalview.datamodel.AlignmentAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlignCalcManager implements AlignCalcManagerI
{
  /*
   * list of registered workers
   */
  private volatile List<AlignCalcWorkerI> restartable;

  /*
   * types of worker _not_ to run (for example, because they have
   * previously thrown errors)
   */
  private volatile List<Class<? extends AlignCalcWorkerI>> blackList;

  /*
   * global record of calculations in progress
   */
  private volatile List<AlignCalcWorkerI> inProgress;

  /*
   * record of calculations pending or in progress in the current context
   */
  private volatile Map<Class<? extends AlignCalcWorkerI>, List<AlignCalcWorkerI>> updating;

  /*
   * workers that have run to completion so are candidates for visual-only 
   * update of their results
   */
  private HashSet<AlignCalcWorkerI> canUpdate;

  /**
   * Constructor
   */
  public AlignCalcManager()
  {
    restartable = Collections
            .synchronizedList(new ArrayList<AlignCalcWorkerI>());
    blackList = Collections.synchronizedList(
            new ArrayList<Class<? extends AlignCalcWorkerI>>());
    inProgress = Collections
            .synchronizedList(new ArrayList<AlignCalcWorkerI>());
    updating = Collections.synchronizedMap(
            new Hashtable<Class<? extends AlignCalcWorkerI>, List<AlignCalcWorkerI>>());
    canUpdate = new HashSet<AlignCalcWorkerI>();
  }

  @Override
  public void notifyStart(AlignCalcWorkerI worker)
  {
    synchronized (updating)
    {
      List<AlignCalcWorkerI> upd = updating.get(worker.getClass());
      if (upd == null)
      {
        updating.put(worker.getClass(), upd = Collections
                .synchronizedList(new ArrayList<AlignCalcWorkerI>()));
      }
      synchronized (upd)
      {
        upd.add(worker);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.api.AlignCalcManagerI#isPending(jalview.api.AlignCalcWorkerI)
   */
  @Override
  public boolean isPending(AlignCalcWorkerI workingClass)
  {
    List<AlignCalcWorkerI> upd;
    synchronized (updating)
    {
      upd = updating.get(workingClass.getClass());
      if (upd == null)
      {
        return false;
      }
      synchronized (upd)
      {
        if (upd.size() > 1)
        {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public boolean notifyWorking(AlignCalcWorkerI worker)
  {
    synchronized (inProgress)
    {
      if (inProgress.contains(worker))
      {
        return false; // worker is already working, so ask caller to wait around
      }
      else
      {
        inProgress.add(worker);
      }
    }
    return true;
  }

  @Override
  public void workerComplete(AlignCalcWorkerI worker)
  {
    synchronized (inProgress)
    {
      // System.err.println("Worker " + worker + " marked as complete.");
      inProgress.remove(worker);
      List<AlignCalcWorkerI> upd = updating.get(worker.getClass());
      if (upd != null)
      {
        synchronized (upd)
        {
          upd.remove(worker);
        }
        canUpdate.add(worker);
      }
    }
  }

  @Override
  public void disableWorker(AlignCalcWorkerI worker)
  {
    synchronized (blackList)
    {
      blackList.add(worker.getClass());
    }
  }

  @Override
  public boolean isDisabled(AlignCalcWorkerI worker)
  {
    synchronized (blackList)
    {
      return blackList.contains(worker.getClass());
    }
  }

  @Override
  public void startWorker(AlignCalcWorkerI worker)
  {
    if (!isDisabled(worker))
    {
      Thread tw = new Thread(worker);
      tw.setName(worker.getClass().toString());
      tw.start();
    }
  }

  @Override
  public boolean isWorking(AlignCalcWorkerI worker)
  {
    synchronized (inProgress)
    {// System.err.println("isWorking : worker "+(worker!=null ?
     // worker.getClass():"null")+ " "+hashCode());
      return worker != null && inProgress.contains(worker);
    }
  }

  @Override
  public boolean isWorking()
  {
    boolean working=false;
    synchronized (inProgress)
    {
      // System.err.println("isWorking "+hashCode());
      working |= inProgress.size() > 0;
    }
    synchronized (updating)
    {
      Collection<List<AlignCalcWorkerI>> workersLists = updating.values();
      synchronized (workersLists)
      {
        for (List<AlignCalcWorkerI> workers : workersLists)
        {
          if (workers!=null)
          {
            synchronized (workers) {
              working |= workers.size() > 0;
            }
          }
        }
      }
    }
    return working;
  }

  @Override
  public void registerWorker(AlignCalcWorkerI worker)
  {
    synchronized (restartable)
    {
      if (!restartable.contains(worker))
      {
        restartable.add(worker);
      }
      startWorker(worker);
    }
  }

  @Override
  public void restartWorkers()
  {
    synchronized (restartable)
    {
      for (AlignCalcWorkerI worker : restartable)
      {
        startWorker(worker);
      }
    }
  }

  @Override
  public boolean workingInvolvedWith(
          AlignmentAnnotation alignmentAnnotation)
  {
    synchronized (inProgress)
    {
      for (AlignCalcWorkerI worker : inProgress)
      {
        if (worker.involves(alignmentAnnotation))
        {
          return true;
        }
      }
    }
    synchronized (updating)
    {
      for (List<AlignCalcWorkerI> workers : updating.values())
      {
        for (AlignCalcWorkerI worker : workers)
        {
          if (worker.involves(alignmentAnnotation))
          {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public void updateAnnotationFor(
          Class<? extends AlignCalcWorkerI> workerClass)
  {

    AlignCalcWorkerI[] workers;
    synchronized (canUpdate)
    {
      workers = canUpdate.toArray(new AlignCalcWorkerI[0]);
    }
    for (AlignCalcWorkerI worker : workers)
    {
      if (workerClass.equals(worker.getClass()))
      {
        worker.updateAnnotation();
      }
    }
  }

  @Override
  public List<AlignCalcWorkerI> getRegisteredWorkersOfClass(
          Class<? extends AlignCalcWorkerI> workerClass)
  {
    List<AlignCalcWorkerI> workingClass = new ArrayList<AlignCalcWorkerI>();
    synchronized (canUpdate)
    {
      for (AlignCalcWorkerI worker : canUpdate)
      {
        if (workerClass.equals(worker.getClass()))
        {
          workingClass.add(worker);
        }
      }
    }
    return (workingClass.size() == 0) ? null : workingClass;
  }

  @Override
  public void enableWorker(AlignCalcWorkerI worker)
  {
    synchronized (blackList)
    {
      blackList.remove(worker.getClass());
    }
  }

  @Override
  public void removeRegisteredWorkersOfClass(
          Class<? extends AlignCalcWorkerI> typeToRemove)
  {
    List<AlignCalcWorkerI> removable = new ArrayList<AlignCalcWorkerI>();
    Set<AlignCalcWorkerI> toremovannot = new HashSet<AlignCalcWorkerI>();
    synchronized (restartable)
    {
      for (AlignCalcWorkerI worker : restartable)
      {
        if (typeToRemove.equals(worker.getClass()))
        {
          removable.add(worker);
          toremovannot.add(worker);
        }
      }
      restartable.removeAll(removable);
    }
    synchronized (canUpdate)
    {
      for (AlignCalcWorkerI worker : canUpdate)
      {
        if (typeToRemove.equals(worker.getClass()))
        {
          removable.add(worker);
          toremovannot.add(worker);
        }
      }
      canUpdate.removeAll(removable);
    }
    // TODO: finish testing this extension

    /*
     * synchronized (inProgress) { // need to kill or mark as dead any running
     * threads... (inProgress.get(typeToRemove)); }
     * 
     * if (workers == null) { return; } for (AlignCalcWorkerI worker : workers)
     * {
     * 
     * if (isPending(worker)) { worker.abortAndDestroy(); startWorker(worker); }
     * else { System.err.println("Pending exists for " + workerClass); } }
     */
  }

  /**
   * Deletes the worker that update the given annotation, provided it is marked
   * as deletable.
   */
  @Override
  public void removeWorkerForAnnotation(AlignmentAnnotation ann)
  {
    /*
     * first just find those to remove (to avoid
     * ConcurrentModificationException)
     */
    List<AlignCalcWorkerI> toRemove = new ArrayList<AlignCalcWorkerI>();
    for (AlignCalcWorkerI worker : restartable)
    {
      if (worker.involves(ann))
      {
        if (worker.isDeletable())
        {
          toRemove.add(worker);
        }
      }
    }

    /*
     * remove all references to deleted workers so any references 
     * they hold to annotation data can be garbage collected 
     */
    for (AlignCalcWorkerI worker : toRemove)
    {
      restartable.remove(worker);
      blackList.remove(worker.getClass());
      inProgress.remove(worker);
      canUpdate.remove(worker);
      synchronized (updating)
      {
        List<AlignCalcWorkerI> upd = updating.get(worker.getClass());
        if (upd != null)
        {
          upd.remove(worker);
        }
      }
    }
  }
}
