/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jgrasstools.gears.modules.v.smoothing;

import jaitools.jts.LineSmoother;

import java.util.ArrayList;
import java.util.List;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Out;
import oms3.annotations.Status;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;
import org.jgrasstools.gears.libs.monitor.LogProgressMonitor;
import org.jgrasstools.gears.utils.features.FeatureGeometrySubstitutor;
import org.jgrasstools.gears.utils.features.FeatureUtilities;
import org.jgrasstools.gears.utils.geometry.GeometryUtilities;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

@Description("The line smoother from the jaitools project.")
@Author(name = "Andrea Antonello", contact = "www.hydrologis.com")
@Keywords("Smoothing, Vector")
@Status(Status.DRAFT)
@Label(JGTConstants.VECTORPROCESSING)
@License("http://www.gnu.org/licenses/gpl-3.0.html")
public class LineSmootherJaitools extends JGTModel {

    @Description("The features to be smoothed.")
    @In
    public SimpleFeatureCollection inFC;

    @Description("The point features that define intersections.")
    @In
    public SimpleFeatureCollection pointFeatures;

    @Description("A value between 0 and 1 (inclusive) specifying the tightness of fit of the smoothed boundary (0 is loose).")
    @In
    public double pAlpha = 0;

    @Description("The progress monitor.")
    @In
    public IJGTProgressMonitor pm = new LogProgressMonitor();

    @Description("The smoothed features.")
    @Out
    public SimpleFeatureCollection outFC;

    private GeometryFactory gF = GeometryUtilities.gf();

    @Execute
    public void process() throws Exception {
        if (!concatOr(outFC == null, doReset)) {
            return;
        }
        outFC = FeatureCollections.newCollection();

        int id = 0;
        pm.message("Collecting geometries...");
        List<SimpleFeature> linesList = FeatureUtilities.featureCollectionToList(inFC);
        int size = inFC.size();
        FeatureGeometrySubstitutor fGS = new FeatureGeometrySubstitutor(inFC.getSchema());
        pm.beginTask("Smoothing features...", size);
        LineSmoother smoother = new LineSmoother(gF);
        for( SimpleFeature line : linesList ) {
            Geometry geometry = (Geometry) line.getDefaultGeometry();
            int numGeometries = geometry.getNumGeometries();
            List<LineString> smoothedList = new ArrayList<LineString>();
            for( int i = 0; i < numGeometries; i++ ) {
                Geometry geometryN = geometry.getGeometryN(i);
                if (geometryN instanceof LineString) {
                    LineString lineString = (LineString) geometryN;
                    LineString smoothed = smoother.smooth(lineString, pAlpha);
                    smoothedList.add(smoothed);
                }
            }
            if (smoothedList.size() != 0) {
                LineString[] lsArray = (LineString[]) smoothedList.toArray(new LineString[smoothedList.size()]);
                MultiLineString multiLineString = gF.createMultiLineString(lsArray);
                SimpleFeature newFeature = fGS.substituteGeometry(line, multiLineString, id);
                outFC.add(newFeature);
                id++;
            }
            pm.worked(1);
        }
        pm.done();
    }

}