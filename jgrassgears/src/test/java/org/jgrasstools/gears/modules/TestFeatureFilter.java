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
package org.jgrasstools.gears.modules;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.jgrasstools.gears.modules.v.vectorfilter.VectorFilter;
import org.jgrasstools.gears.utils.HMTestCase;
import org.jgrasstools.gears.utils.HMTestMaps;
import org.opengis.feature.simple.SimpleFeature;
/**
 * Test for the reprojection modules.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class TestFeatureFilter extends HMTestCase {

    @SuppressWarnings("nls")
    public void testFeatureFilter() throws Exception {

        SimpleFeatureCollection testFC = HMTestMaps.testFC;

        VectorFilter filter = new VectorFilter();
        filter.inFeatures = testFC;
        filter.pCql = "cat > 2";
        filter.process();
        SimpleFeatureCollection outFC = filter.outFeatures;

        FeatureIterator<SimpleFeature> featureIterator = outFC.features();
        SimpleFeature feature = featureIterator.next();
        assertNotNull(feature);

        Integer attribute = (Integer) feature.getAttribute("cat");
        assertEquals(3, attribute.intValue());
        featureIterator.close();

    }
}
