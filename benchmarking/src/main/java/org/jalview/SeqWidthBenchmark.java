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

import org.jalview.HiddenColumnsBenchmark.HiddenColsAndStartState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;

/*
 * A class to benchmark hidden columns performance
 */
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class SeqWidthBenchmark {

	/*
	 * State with multiple hidden columns and a start position set
	 */
	@State(Scope.Thread)
	public static class AlignmentState
	{
		@Param({"100", "1000", "10000", "100000"})
		public int numSeqs;
		
		Random rand = new Random();
		
		AlignmentI al;
		
		@Setup
		public void setup()
		{
			rand.setSeed(1234);
			
			SequenceI[] seqs = new Sequence[numSeqs];
		    for (int i = 0; i < numSeqs; i++)
		    {
		      int count = rand.nextInt(10000); 
		      StringBuilder aas = new StringBuilder();
		      for (int j=0; j<count; j++)
		      {
		    	 aas.append("a");
		      }

		      seqs[i] = new Sequence("Sequence" + i, aas.toString());
		    }
			al = new Alignment(seqs);
		}
	}
	
	
	@Benchmark
	@BenchmarkMode({Mode.Throughput})
	public int benchSeqGetWidth(AlignmentState tstate)
	{
		return tstate.al.getWidth();
	}
	
}
