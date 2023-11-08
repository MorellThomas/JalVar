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
// run an alignment on the current alignFrame
import jalview.ws.jws2.*;
import jalview.datamodel.*;

// First get the JABA service discoverer and see if there are any services.
jalview.ws.jws2.Jws2Discoverer disc = jalview.ws.jws2.Jws2Discoverer.getDiscoverer();
while (disc.isRunning()) {
Thread.sleep(50);
}
if (!disc.isRunning() && !disc.hasServices())
{
  // discoverer is not running, and has no services - so run it again.
  disc.run();
}

for (jalview.ws.jws2.Jws2Discoverer.Jws2Instance service: disc.getServices()) {

if (service.serviceType.indexOf("uscle")>-1) {
  // now - go through the services if any, and find a Muscle service
  def msaf;
  try {
	msaf = currentAlFrame;
  } catch (q) {
        // currentAlFrame is not defined - so we were run as an interactive script from the Groovy console
	// in that case, just pick the first alignmentFrame in the stack.
	msaf = Jalview.getAlignFrames()[0]
  };
  // Finally start Jalview's JabaWS MSA Client with the alignment from msaf
  new MsaWSClient(service, msaf.getTitle(), msaf.gatherSequencesForAlignment(), false,
                true, msaf.getViewport().getAlignment()
                .getDataset(), msaf);
  break;
}
}
