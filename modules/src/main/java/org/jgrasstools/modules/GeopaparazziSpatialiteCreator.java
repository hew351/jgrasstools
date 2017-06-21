/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com
 *
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of JGrasstools (http://www.jgrasstools.org)
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * JGrasstools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.modules;

import static org.jgrasstools.gears.i18n.GearsMessages.OMSHYDRO_AUTHORCONTACTS;
import static org.jgrasstools.gears.i18n.GearsMessages.OMSHYDRO_AUTHORNAMES;
import static org.jgrasstools.gears.i18n.GearsMessages.OMSHYDRO_DRAFT;
import static org.jgrasstools.gears.i18n.GearsMessages.OMSHYDRO_LICENSE;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.IsEqualsToImpl;
import org.geotools.filter.LiteralExpressionImpl;
import org.geotools.styling.Style;
import org.jgrasstools.dbs.compat.ASpatialDb;
import org.jgrasstools.dbs.spatialite.ESpatialiteGeometryType;
import org.jgrasstools.dbs.spatialite.QueryResult;
import org.jgrasstools.dbs.spatialite.jgt.JGTImportExportUtils;
import org.jgrasstools.gears.io.geopaparazzi.styles.GeopaparazziDatabaseProperties;
import org.jgrasstools.gears.io.geopaparazzi.styles.ISpatialiteTableAndFieldsNames;
import org.jgrasstools.gears.io.vectorreader.OmsVectorReader;
import org.jgrasstools.gears.libs.exceptions.ModelsIOException;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.spatialite.GTSpatialiteThreadsafeDb;
import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.SldUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gears.utils.geometry.EGeometryType;
import org.jgrasstools.gears.utils.style.FeatureTypeStyleWrapper;
import org.jgrasstools.gears.utils.style.LineSymbolizerWrapper;
import org.jgrasstools.gears.utils.style.PointSymbolizerWrapper;
import org.jgrasstools.gears.utils.style.PolygonSymbolizerWrapper;
import org.jgrasstools.gears.utils.style.RuleWrapper;
import org.jgrasstools.gears.utils.style.StyleWrapper;
import org.jgrasstools.gears.utils.style.SymbolizerWrapper;
import org.jgrasstools.gears.utils.style.TextSymbolizerWrapper;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import oms3.annotations.Author;
import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Keywords;
import oms3.annotations.Label;
import oms3.annotations.License;
import oms3.annotations.Name;
import oms3.annotations.Status;
import oms3.annotations.UI;

@Description(GeopaparazziSpatialiteCreator.DESCRIPTION)
@Author(name = OMSHYDRO_AUTHORNAMES, contact = OMSHYDRO_AUTHORCONTACTS)
@Keywords(GeopaparazziSpatialiteCreator.OmsGeopaparazziSpatialiteCreator_TAGS)
@Label(JGTConstants.MOBILE)
@Name(GeopaparazziSpatialiteCreator.OmsGeopaparazziSpatialiteCreator_NAME)
@Status(OMSHYDRO_DRAFT)
@License(OMSHYDRO_LICENSE)
public class GeopaparazziSpatialiteCreator extends JGTModel {

    @Description(THE_GEOPAPARAZZI_DATABASE_FILE)
    @UI(JGTConstants.FILEIN_UI_HINT)
    @In
    public String inGeopaparazzi = null;

    @Description(OmsGeopaparazziSpatialiteCreator_encoding)
    @In
    public String pEncoding = "UTF-8";

    @Description(OmsGeopaparazziSpatialiteCreator_sizefactor)
    @In
    public int pSizeFactor = 3;

    @Description(OmsGeopaparazziSpatialiteCreator_lineswidthfactor)
    @In
    public int pLinesWidthFactor = 6;

    @Description(OmsGeopaparazziSpatialiteCreator_inShapefilesFolder)
    @UI(JGTConstants.FOLDERIN_UI_HINT)
    @In
    public String inShapefilesFolder = null;

    // VARS DOCS START
    public static final String THE_GEOPAPARAZZI_DATABASE_FILE = "The existing or new spatialite database file.";
    public static final String DESCRIPTION = "Creates a spatialite database for geopaparazzi from a set of shapefiles or adds to an existing one.";
    public static final String OmsGeopaparazziSpatialiteCreator_LABEL = JGTConstants.VECTORPROCESSING;
    public static final String OmsGeopaparazziSpatialiteCreator_encoding = "The encoding to use for the import.";
    public static final String OmsGeopaparazziSpatialiteCreator_sizefactor = "A multiplication factor between SLD and geopap sizes (appilies to border widths and sizes).";
    public static final String OmsGeopaparazziSpatialiteCreator_lineswidthfactor = "A multiplication factor between SLD and geopap line widths (appilies to line widths).";
    public static final String OmsGeopaparazziSpatialiteCreator_TAGS = "geopaparazzi, vector";
    public static final String OmsGeopaparazziSpatialiteCreator_NAME = "geopaparazzispatialitecreator";
    public static final String OmsGeopaparazziSpatialiteCreator_inShapefilesFolder = "The folder of shapefiles to import.";
    // VARS DOCS END

    @Execute
    public void process() throws Exception {
        checkNull(inGeopaparazzi, inShapefilesFolder);

        if (pEncoding == null || pEncoding.trim().length() == 0) {
            pEncoding = "UTF-8";
        }

        if (pSizeFactor < 1) {
            pSizeFactor = 3;
        }
        if (pLinesWidthFactor < 1) {
            pLinesWidthFactor = 6;
        }

        File shpFolder = new File(inShapefilesFolder);
        File[] shpfiles = shpFolder.listFiles(new FilenameFilter(){
            @Override
            public boolean accept( File dir, String name ) {
                return name.endsWith(".shp");
            }
        });

        if (shpfiles.length == 0) {
            throw new ModelsIOException("The supplied folder doesn't contain any shapefile.", this);
        }

        try (ASpatialDb db = new GTSpatialiteThreadsafeDb()) {
            if (!db.open(inGeopaparazzi)) {
                db.initSpatialMetadata(null);
            }

            if (!db.hasTable(GeopaparazziDatabaseProperties.PROPERTIESTABLE)) {
                GeopaparazziDatabaseProperties.createPropertiesTable(db);
            } else {
                QueryResult qres1 = db.getTableRecordsMapFromRawSql("select * from dataproperties", 10);
                pm.message("Dataproperties already existing: ");
                for( Object[] objs : qres1.data ) {
                    pm.message(Arrays.toString(objs));
                }
                pm.message("----------------------------------");
            }

            pm.beginTask("Importing shapefiles...", shpfiles.length);
            for( File shpFile : shpfiles ) {
                String name = FileUtilities.getNameWithoutExtention(shpFile);

                if (db.hasTable(name)) {
                    pm.errorMessage("Table already existing: " + name);
                    continue;
                }

                SimpleFeatureCollection fc = OmsVectorReader.readVector(shpFile.getAbsolutePath());
                SimpleFeatureType schema = fc.getSchema();
                CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();

                String epsgStr = CrsUtilities.getCodeFromCrs(crs);
                String sirdStr = epsgStr.substring(5);

                int srid = Integer.parseInt(sirdStr);

                EGeometryType geomType = EGeometryType.forGeometryDescriptor(schema.getGeometryDescriptor());
                ESpatialiteGeometryType spatialiteGeometryType = geomType.toSpatialiteGeometryType();

                JGTImportExportUtils.importShapefileThroughVirtualTable(db, name, shpFile.getAbsolutePath(), pEncoding, srid,
                        spatialiteGeometryType);

                Style style = SldUtilities.getStyleFromFile(shpFile);
                if (style != null) {
                    String uniqueName = "/#" + name + "#geometry";

                    StyleWrapper styleWrapper = new StyleWrapper(style);
                    List<FeatureTypeStyleWrapper> featureTypeStylesWrapperList = styleWrapper.getFeatureTypeStylesWrapperList();
                    if (featureTypeStylesWrapperList.size() > 0) {
                        List<RuleWrapper> rulesWrapperList = new ArrayList<>();
                        for( FeatureTypeStyleWrapper ftsWrapper : featureTypeStylesWrapperList ) {
                            List<RuleWrapper> rulesWrappers = ftsWrapper.getRulesWrapperList();
                            rulesWrapperList.addAll(rulesWrappers);
                        }

                        if (rulesWrapperList.size() == 1) {
                            RuleWrapper ruleWrapper = rulesWrapperList.get(0);
                            SymbolizerWrapper geometrySymbolizersWrapper = ruleWrapper.getGeometrySymbolizersWrapper();
                            if (geometrySymbolizersWrapper != null) {
                                org.jgrasstools.gears.io.geopaparazzi.styles.Style gpStyle = createBaseStyle(db, uniqueName,
                                        rulesWrapperList);

                                populateStyleObject(gpStyle, geometrySymbolizersWrapper);

                                GeopaparazziDatabaseProperties.updateStyle(db, gpStyle);
                            }
                        } else if (rulesWrapperList.size() > 1) {
                            org.jgrasstools.gears.io.geopaparazzi.styles.Style gpStyle = createBaseStyle(db, uniqueName,
                                    rulesWrapperList);
                            gpStyle.themeMap = new HashMap<>();
                            for( RuleWrapper ruleWrapper : rulesWrapperList ) {
                                SymbolizerWrapper geometrySymbolizersWrapper = ruleWrapper.getGeometrySymbolizersWrapper();

                                org.jgrasstools.gears.io.geopaparazzi.styles.Style themeStyle = createBaseStyle(null, uniqueName,
                                        rulesWrapperList);
                                populateStyleObject(themeStyle, geometrySymbolizersWrapper);

                                Filter filter = ruleWrapper.getRule().getFilter();
                                if (filter instanceof IsEqualsToImpl) {
                                    IsEqualsToImpl equalsFilter = (IsEqualsToImpl) filter;
                                    Expression expression1 = equalsFilter.getExpression1();
                                    Expression expression2 = equalsFilter.getExpression2();

                                    setFilter(gpStyle, themeStyle, expression1);
                                    setFilter(gpStyle, themeStyle, expression2);
                                }
                            }

                            GeopaparazziDatabaseProperties.updateStyle(db, gpStyle);
                        } else {
                            pm.errorMessage("Unable to export SLD for: " + shpFile);
                            continue;
                        }

                    }

                }
                pm.worked(1);

            }
            pm.done();

            QueryResult qres = db.getTableRecordsMapFromRawSql("select * from dataproperties", 100);
            pm.message("Dataproperties inserted: ");
            int theme = qres.names.indexOf(ISpatialiteTableAndFieldsNames.THEME);
            for( Object[] objs : qres.data ) {
                String themeString = objs[theme].toString().replaceAll("\\s+", " ");
                if (themeString.length() > 20) {
                    objs[theme] = themeString.substring(0, 15) + "...";
                }
                pm.message(Arrays.toString(objs));
            }

        }

    }

    private void setFilter( org.jgrasstools.gears.io.geopaparazzi.styles.Style mainStyle,
            org.jgrasstools.gears.io.geopaparazzi.styles.Style themeStyle, Expression expression ) {
        if (expression instanceof AttributeExpressionImpl) {
            AttributeExpressionImpl attr = (AttributeExpressionImpl) expression;
            mainStyle.themeField = attr.getPropertyName();
        } else if (expression instanceof LiteralExpressionImpl) {
            LiteralExpressionImpl attr = (LiteralExpressionImpl) expression;
            mainStyle.themeMap.put(attr.getValue().toString(), themeStyle);
        }
    }

    private void populateStyleObject( org.jgrasstools.gears.io.geopaparazzi.styles.Style gpStyle,
            SymbolizerWrapper geometrySymbolizersWrapper ) {
        if (geometrySymbolizersWrapper instanceof PointSymbolizerWrapper) {
            PointSymbolizerWrapper psw = (PointSymbolizerWrapper) geometrySymbolizersWrapper;

            gpStyle.shape = psw.getMarkName();
            gpStyle.size = pSizeFactor * getFloat(psw.getSize(), gpStyle.size);

            gpStyle.width = pSizeFactor * getFloat(psw.getStrokeWidth(), gpStyle.width);
            gpStyle.strokealpha = getFloat(psw.getStrokeOpacity(), gpStyle.strokealpha);
            gpStyle.strokecolor = getString(psw.getStrokeColor(), null);

            gpStyle.fillalpha = getFloat(psw.getFillOpacity(), gpStyle.fillalpha);
            gpStyle.fillcolor = getString(psw.getFillColor(), null);
        } else if (geometrySymbolizersWrapper instanceof PolygonSymbolizerWrapper) {
            PolygonSymbolizerWrapper psw = (PolygonSymbolizerWrapper) geometrySymbolizersWrapper;

            gpStyle.width = pSizeFactor * getFloat(psw.getStrokeWidth(), gpStyle.width);
            gpStyle.strokealpha = getFloat(psw.getStrokeOpacity(), gpStyle.strokealpha);
            gpStyle.strokecolor = getString(psw.getStrokeColor(), null);
            gpStyle.fillalpha = getFloat(psw.getFillOpacity(), gpStyle.fillalpha);
            gpStyle.fillcolor = getString(psw.getFillColor(), null);
        } else if (geometrySymbolizersWrapper instanceof LineSymbolizerWrapper) {
            LineSymbolizerWrapper lsw = (LineSymbolizerWrapper) geometrySymbolizersWrapper;

            gpStyle.width = pLinesWidthFactor * getFloat(lsw.getStrokeWidth(), gpStyle.width);
            gpStyle.strokealpha = getFloat(lsw.getStrokeOpacity(), gpStyle.strokealpha);
            gpStyle.strokecolor = getString(lsw.getStrokeColor(), null);
        }
    }

    private org.jgrasstools.gears.io.geopaparazzi.styles.Style createBaseStyle( ASpatialDb db, String uniqueName,
            List<RuleWrapper> rulesWrapperListForTextSymbolizer ) throws Exception {
        String fieldLabel = "";
        TextSymbolizerWrapper textSymbolizersWrapper = null;
        if (rulesWrapperListForTextSymbolizer != null) {
            // use first available textsymbolizer
            for( RuleWrapper ruleWrapper : rulesWrapperListForTextSymbolizer ) {
                textSymbolizersWrapper = ruleWrapper.getTextSymbolizersWrapper();
                if (textSymbolizersWrapper != null) {
                    fieldLabel = textSymbolizersWrapper.getLabelName();
                    break;
                }
            }
        }

        org.jgrasstools.gears.io.geopaparazzi.styles.Style gpStyle = GeopaparazziDatabaseProperties
                .createDefaultPropertiesForTable(db, uniqueName, fieldLabel);
        if (fieldLabel != null && fieldLabel.trim().length() > 0 && textSymbolizersWrapper != null) {
            String fontSize = textSymbolizersWrapper.getFontSize();
            try {
                double fontSizeDouble = Double.parseDouble(fontSize);
                gpStyle.labelsize = (float) fontSizeDouble * pSizeFactor;
            } catch (Exception e) {
                // ignore size
            }
        }
        return gpStyle;
    }

    private float getFloat( String value, float defaultValue ) {
        float num = defaultValue;
        try {
            num = Float.parseFloat(value);
        } catch (Exception e) {
            // ignore and get default
        }
        return num;
    }

    private String getString( String value, String defaultValue ) {
        if (value == null || value.trim().length() == 0)
            value = defaultValue;
        return value;
    }

    public static void main( String[] args ) throws Exception {
        GeopaparazziSpatialiteCreator c = new GeopaparazziSpatialiteCreator();
        c.inGeopaparazzi = "/home/hydrologis/data/naturalearth_italy_thematic.sqlite";
        c.pEncoding = null;
        c.inShapefilesFolder = "/home/hydrologis/data/naturalearth_italy_thematic";
        c.process();
    }
}
