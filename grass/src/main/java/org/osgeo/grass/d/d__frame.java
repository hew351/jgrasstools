package org.osgeo.grass.d;

import org.jgrasstools.grass.utils.ModuleSupporter;

import oms3.annotations.Author;
import oms3.annotations.Documentation;
import oms3.annotations.Label;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.UI;
import oms3.annotations.Keywords;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Out;
import oms3.annotations.Status;

@Description("Manages display frames on the user's graphics monitor.")
@Author(name = "Grass Developers Community", contact = "http://grass.osgeo.org")
@Keywords("display")
@Name("d__frame")
@Status(Status.CERTIFIED)
@License("General Public License Version >=2)")
public class d__frame {

	@Description("Frame to be created/selected (optional)")
	@In
	public String $$framePARAMETER;

	@Description("Where to place the frame, values in percent (implies -c) (optional)")
	@In
	public String $$atPARAMETER;

	@Description("Create a new frame")
	@In
	public boolean $$cFLAG = false;

	@Description("Select a frame")
	@In
	public boolean $$sFLAG = false;

	@Description("Remove all frames and erase the screen")
	@In
	public boolean $$eFLAG = false;

	@Description("Print name of current frame")
	@In
	public boolean $$pFLAG = false;

	@Description("Print names of all frames")
	@In
	public boolean $$aFLAG = false;

	@Description("List map names displayed in GRASS monitor")
	@In
	public boolean $$lFLAG = false;

	@Description("Debugging output")
	@In
	public boolean $$DFLAG = false;

	@Description("Verbose module output")
	@In
	public boolean $$verboseFLAG = false;

	@Description("Quiet module output")
	@In
	public boolean $$quietFLAG = false;


	@Execute
	public void process() throws Exception {
		ModuleSupporter.processModule(this);
	}

}
