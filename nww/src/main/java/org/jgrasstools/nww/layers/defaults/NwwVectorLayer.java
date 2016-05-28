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
package org.jgrasstools.nww.layers.defaults;

import org.jgrasstools.nww.gui.style.SimpleStyle;

/**
 * Common interface for layers.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public interface NwwVectorLayer extends NwwLayer {
    public enum GEOMTYPE {
        POINT, LINE, POLYGON
    };

    public GEOMTYPE getType();

    public void setStyle(SimpleStyle style);
    
    public SimpleStyle getStyle();

}