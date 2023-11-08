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

package org.jalview;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Param;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenColumns;

/*
 * A class to benchmark hidden columns performance
 */
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class HiddenColsIteratorsBenchmark {
	/*
	 * State with multiple hidden columns and a start position set
	 */
	@State(Scope.Thread)
	public static class HiddenColsAndStartState
	{
		@Param({"300", "10000", "100000"})
		public int maxcols;
		
		@Param({"1", "50", "90"})
		public int startpcnt; // position as percentage of maxcols
		
		@Param({"1","15","100"})
		public int hide;
		
		HiddenColumns h = new HiddenColumns();
		Random rand = new Random();
		
		public int hiddenColumn;
		public int visibleColumn;
	
		@Setup
		public void setup()
		{
			rand.setSeed(1234);
			int lastcol = 0;
	    	while (lastcol < maxcols)
	    	{
	    		int count = rand.nextInt(100); 
	    		lastcol += count;
	    		h.hideColumns(lastcol, lastcol+hide);
	    		lastcol+=hide;
	    	}
	    	
	    	// make sure column at start is hidden
	    	hiddenColumn = (int)(maxcols * startpcnt/100.0);
	    	h.hideColumns(hiddenColumn, hiddenColumn);
	    	
	    	// and column <hide> after start is visible
	    	ColumnSelection sel = new ColumnSelection();
	    	h.revealHiddenColumns(hiddenColumn+hide, sel);
	    	visibleColumn = hiddenColumn+hide;
	    	
	    	System.out.println("Maxcols: " + maxcols + " HiddenCol: " + hiddenColumn + " Hide: " + hide);
	    	System.out.println("Number of hidden columns: " + h.getSize());
		}
	}
	
	/* Convention: functions in alphabetical order */
	
	@Benchmark
	@BenchmarkMode({Mode.Throughput})
	public int benchStartIterator(HiddenColsAndStartState tstate)
	{
		int res = 0;
		int startx = tstate.visibleColumn;
		Iterator<Integer> it = tstate.h.getStartRegionIterator(startx,
				startx+60);
        while (it.hasNext())
        {
          res = it.next() - startx;
          Blackhole.consumeCPU(5);
        }
        return res;
	}
	
	@Benchmark
	@BenchmarkMode({Mode.Throughput})
	public int benchBoundedIterator(HiddenColsAndStartState tstate)
	{
		int startx = tstate.visibleColumn;
		int blockStart = startx;
		int blockEnd;
		int screenY = 0;
		Iterator<int[]> it = tstate.h.getBoundedIterator(startx,
				startx+60);
        while (it.hasNext())
        {
        	int[] region = it.next();
          
        	blockEnd = Math.min(region[0] - 1, blockStart + 60 - screenY);
  	      
  	      	screenY += blockEnd - blockStart + 1;
  	      	blockStart = region[1] + 1;
        	
        	Blackhole.consumeCPU(5);
        }
        return blockStart;
	}
}
