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
package org.jgrasstools.hortonmachine.modules.networktools.epanet.core;

@SuppressWarnings("nls")
public enum Components {
    EN_NODECOUNT(0, "Nodes"), //
    EN_TANKCOUNT(1, "Reservoirs and tank nodes"), //
    EN_LINKCOUNT(2, "Links"), //
    EN_PATCOUNT(3, "Time patterns"), //
    EN_CURVECOUNT(4, "Curves"), //
    EN_CONTROLCOUNT(5, "Simple controls");

    private int code;
    private String type;
    Components( int code, String type ) {
        this.code = code;
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
