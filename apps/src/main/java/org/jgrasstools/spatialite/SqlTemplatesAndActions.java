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
package org.jgrasstools.spatialite;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jgrasstools.dbs.spatialite.QueryResult;
import org.jgrasstools.dbs.spatialite.objects.ColumnLevel;
import org.jgrasstools.dbs.spatialite.objects.TableLevel;
import org.jgrasstools.gears.spatialite.SpatialiteImportUtils;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.jgrasstools.gui.console.LogConsoleController;
import org.jgrasstools.gui.utils.GuiBridgeHandler;
import org.jgrasstools.gui.utils.GuiUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple queries templates.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("serial")
public class SqlTemplatesAndActions {
    private static final Logger logger = LoggerFactory.getLogger(SqlTemplatesAndActions.class);

    public static Action getSelectOnColumnAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Select on column"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query;
                if (column.geomColumn != null) {
                    query = "SELECT AsBinary(" + column.columnName + ") FROM " + column.parent.tableName;
                } else {
                    query = "SELECT " + column.columnName + " FROM " + column.parent.tableName;
                }
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getUpdateOnColumnAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Update on column"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "UPDATE " + column.parent.tableName + " SET " + column.columnName + " = XXX";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getAddGeometryAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        String title = "Add geometry column";
        return new AbstractAction(title){
            @Override
            public void actionPerformed( ActionEvent e ) {

                String[] labels = {"Table name", "Column name", "SRID", "Geometry type", "Dimension"};
                String[] values = {column.parent.tableName, column.columnName, "4326", "POINT", "XY"};
                String[] result = GuiUtilities.showMultiInputDialog(spatialiteViewer, title, labels, values, null);

                String query = "SELECT AddGeometryColumn('" + result[0] + "', '" + result[1] + "',  " + result[2] + ", '"
                        + result[3] + "', '" + result[4] + "')";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getRecoverGeometryAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        String title = "Recover geometry column";
        return new AbstractAction(title){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String[] labels = {"Table name", "Column name", "SRID", "Geometry type", "Dimension"};
                String[] values = {column.parent.tableName, column.columnName, "4326", "POINT", "XY"};
                String[] result = GuiUtilities.showMultiInputDialog(spatialiteViewer, title, labels, values, null);

                String query = "SELECT RecoverGeometryColumn('" + result[0] + "', '" + result[1] + "',  " + result[2] + ", '"
                        + result[3] + "', '" + result[4] + "')";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getDiscardGeometryColumnAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Discard geometry column"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "SELECT DiscardGeometryColumn('" + column.parent.tableName + "', '"
                        + column.geomColumn.f_geometry_column + "');";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getCreateSpatialIndexAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Create spatial index"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "SELECT CreateSpatialIndex('" + column.parent.tableName + "','" + column.columnName + "');";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getCheckSpatialIndexAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Check spatial index"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "SELECT CheckSpatialIndex('" + column.parent.tableName + "','" + column.columnName + "');";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getRecoverSpatialIndexAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Recover spatial index"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "SELECT RecoverSpatialIndex('" + column.parent.tableName + "','" + column.columnName + "');";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getDisableSpatialIndexAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Disable spatial index"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "SELECT DisableSpatialIndex('" + column.parent.tableName + "','" + column.columnName + "');";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getShowSpatialMetadataAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Show spatial metadata"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "SELECT * FROM geom_cols_ref_sys WHERE Lower(f_table_name) = Lower('" + column.parent.tableName
                        + "') AND Lower(f_geometry_column) = Lower('" + column.columnName + "')";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getCombinedSelectAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Create combined select statement"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String[] tableColsFromFK = column.tableColsFromFK();
                String refTable = tableColsFromFK[0];
                String refColumn = tableColsFromFK[1];
                String tableName = column.parent.tableName;
                String query = "SELECT t1.*, t2.* FROM " + tableName + " t1, " + refTable + " t2" + "\nWHERE t1."
                        + column.columnName + "=t2." + refColumn;
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getQuickViewOtherTableAction( ColumnLevel column, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Quick view other table"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    String[] tableColsFromFK = column.tableColsFromFK();
                    String refTable = tableColsFromFK[0];
                    QueryResult queryResult = spatialiteViewer.currentConnectedDatabase.getTableRecordsMapIn(refTable, null, true,
                            20, -1);
                    spatialiteViewer.loadDataViewer(queryResult);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Action getRefreshDatabaseAction( GuiBridgeHandler guiBridge, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Refresh"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                final LogConsoleController logConsole = new LogConsoleController(spatialiteViewer.pm);
                JFrame window = guiBridge.showWindow(logConsole.asJComponent(), "Console Log");

                new Thread(() -> {
                    logConsole.beginProcess("Refresh database");
                    try {
                        spatialiteViewer.refreshDatabaseTree();
                    } catch (Exception ex) {
                        spatialiteViewer.currentConnectedDatabase = null;
                        logger.error("Error refreshing database...", ex);
                    } finally {
                        logConsole.finishProcess();
                        logConsole.stopLogging();
                        logConsole.setVisible(false);
                        window.dispose();
                    }
                }).start();
            }
        };
    }

    public static Action getCopyDatabasePathAction( SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Copy path"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String databasePath = spatialiteViewer.currentConnectedDatabase.getDatabasePath();
                GuiUtilities.copyToClipboard(databasePath);
            }
        };
    }

    public static Action getCreateTableFromShapefileSchemaAction( GuiBridgeHandler guiBridge,
            SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Create table from shapefile"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                File[] openFiles = guiBridge.showOpenFileDialog("Open shapefile", GuiUtilities.getLastFile());
                if (openFiles != null && openFiles.length > 0) {
                    try {
                        GuiUtilities.setLastPath(openFiles[0].getAbsolutePath());
                    } catch (Exception e1) {
                        logger.error("ERROR", e1);
                    }
                } else {
                    return;
                }
                try {
                    SpatialiteImportUtils.createTableFromShp(spatialiteViewer.currentConnectedDatabase, openFiles[0]);
                    spatialiteViewer.refreshDatabaseTree();
                } catch (Exception e1) {
                    logger.error("ERROR", e1);
                }

            }
        };
    }

    public static Action getSelectAction( TableLevel table, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Select statement"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    String query = SpatialiteGuiUtils.getSelectQuery(spatialiteViewer.currentConnectedDatabase, table, false,
                            true);
                    spatialiteViewer.addTextToQueryEditor(query);
                } catch (Exception e1) {
                    logger.error("Error", e1);
                }
            }
        };
    }

    public static Action getDropAction( TableLevel table, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Drop table statement"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    List<ColumnLevel> columnsList = table.columnsList;
                    for( ColumnLevel columnLevel : columnsList ) {
                        if (columnLevel.geomColumn != null) {
                            String query = "SELECT DiscardGeometryColumn('" + table.tableName + "', '"
                                    + columnLevel.geomColumn.f_geometry_column + "');";
                            spatialiteViewer.addTextToQueryEditor(query);
                            query = "SELECT DisableSpatialIndex('" + table.tableName + "', '"
                                    + columnLevel.geomColumn.f_geometry_column + "');";
                            spatialiteViewer.addTextToQueryEditor(query);
                            query = "DROP TABLE idx_" + table.tableName + "_" + columnLevel.geomColumn.f_geometry_column + ";";
                            spatialiteViewer.addTextToQueryEditor(query);
                        }
                    }
                    String query = "drop table " + table.tableName + ";";
                    spatialiteViewer.addTextToQueryEditor(query);
                } catch (Exception ex) {
                    logger.error("Error", ex);
                }
            }
        };
    }

    public static Action getCountRowsAction( TableLevel table, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Count table records"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    String tableName = table.tableName;
                    long count = spatialiteViewer.currentConnectedDatabase.getCount(tableName);
                    JOptionPane.showMessageDialog(spatialiteViewer, "Count: " + count);
                } catch (Exception ex) {
                    logger.error("Error", ex);
                }
            }
        };
    }

    public static Action getImportShapefileDataAction( GuiBridgeHandler guiBridge, TableLevel table,
            SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Import data from shapefile"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                File[] openFiles = guiBridge.showOpenFileDialog("Open shapefile", GuiUtilities.getLastFile());
                if (openFiles != null && openFiles.length > 0) {
                    try {
                        GuiUtilities.setLastPath(openFiles[0].getAbsolutePath());
                    } catch (Exception e1) {
                        logger.error("ERROR", e1);
                    }
                } else {
                    return;
                }

                final LogConsoleController logConsole = new LogConsoleController(spatialiteViewer.pm);
                JFrame window = guiBridge.showWindow(logConsole.asJComponent(), "Console Log");

                new Thread(() -> {
                    boolean hasErrors = false;
                    logConsole.beginProcess("Importing data...");
                    try {
                        hasErrors = !SpatialiteImportUtils.importShapefile(spatialiteViewer.currentConnectedDatabase,
                                openFiles[0], spatialiteViewer.currentSelectedTable.tableName, -1, spatialiteViewer.pm);
                    } catch (Exception ex) {
                        logger.error("Error importing data from shapefile", ex);
                    } finally {
                        logConsole.finishProcess();
                        logConsole.stopLogging();
                        if (!hasErrors) {
                            logConsole.setVisible(false);
                            window.dispose();
                        }
                    }
                }).start();
            }
        };
    }

    public static Action getReprojectTableAction( TableLevel table, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Reproject table"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    ColumnLevel geometryColumn = table.getFirstGeometryColumn();
                    if (geometryColumn == null) {
                        GuiUtilities.showInfoMessage(spatialiteViewer, null, "Only spatial tables can be reprojected.");
                    }

                    String[] labels = {"New table name", "New SRID"};
                    String[] values = {table.tableName + "4326", "4326"};
                    String[] result = GuiUtilities.showMultiInputDialog(spatialiteViewer, "Reprojection parameters", labels,
                            values, null);

                    String query = SpatialiteGuiUtils.getSelectQuery(spatialiteViewer.currentConnectedDatabase, table, false,
                            false);
                    String tableName = table.tableName;
                    String letter = tableName.substring(0, 1);
                    String columnName = letter + "." + geometryColumn.columnName;

                    query = query.replaceFirst(columnName, "TRANSFORM(" + columnName + ", " + result[1] + ")");
                    query = "create table " + result[0] + " as " + query + ";\n";
                    query += "SELECT RecoverGeometryColumn('" + result[0] + "', '" + geometryColumn.columnName + "'," + result[1]
                            + ",'" + geometryColumn.columnType + "'," + geometryColumn.geomColumn.coord_dimension + ");";

                    spatialiteViewer.addTextToQueryEditor(query);
                } catch (Exception ex) {
                    logger.error("Error", ex);
                }
            }
        };
    }

    public static Action getQuickViewTableAction( TableLevel table, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Quick View Table"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                try {
                    String query = SpatialiteGuiUtils.getSelectQuery(spatialiteViewer.currentConnectedDatabase, table, false,
                            true);
                    spatialiteViewer.viewSpatialQueryResult(query, spatialiteViewer.pm);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Action getUpdateLayerStats( GuiBridgeHandler guiBridge, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Update Layer Statistics"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                String query = "SELECT UpdateLayerStatistics();";
                spatialiteViewer.addTextToQueryEditor(query);
            }
        };
    }

    public static Action getImportSqlFileAction( GuiBridgeHandler guiBridge, SpatialiteViewer spatialiteViewer ) {
        return new AbstractAction("Import sql file"){
            @Override
            public void actionPerformed( ActionEvent e ) {
                File[] openFiles = guiBridge.showOpenFileDialog("Open sql file", GuiUtilities.getLastFile());
                if (openFiles != null && openFiles.length > 0) {
                    try {
                        GuiUtilities.setLastPath(openFiles[0].getAbsolutePath());
                    } catch (Exception e1) {
                        logger.error("ERROR", e1);
                    }
                } else {
                    return;
                }

                final LogConsoleController logConsole = new LogConsoleController(spatialiteViewer.pm);
                JFrame window = guiBridge.showWindow(logConsole.asJComponent(), "Console Log");

                new Thread(() -> {
                    boolean hasErrors = false;
                    logConsole.beginProcess("Running sql from file...");
                    try {
                        File sqlFile = openFiles[0];
                        String readFile = FileUtilities.readFile(sqlFile);
                        spatialiteViewer.runQuery(readFile, spatialiteViewer.pm);
                        spatialiteViewer.refreshDatabaseTree();
                    } catch (Exception ex) {
                        logger.error("Error importing sql from file", ex);
                    } finally {
                        logConsole.finishProcess();
                        logConsole.stopLogging();
                        if (!hasErrors) {
                            logConsole.setVisible(false);
                            window.dispose();
                        }
                    }
                }).start();

            }
        };
    }
}
