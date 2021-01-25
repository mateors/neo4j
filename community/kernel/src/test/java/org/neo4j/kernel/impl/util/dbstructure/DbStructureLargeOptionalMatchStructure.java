/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.kernel.impl.util.dbstructure;

import org.neo4j.internal.helpers.collection.Visitable;
import org.neo4j.kernel.api.schema.index.TestIndexDescriptorFactory;

//
// GENERATED FILE. DO NOT EDIT.
//
// This has been generated by:
//
//   org.neo4j.kernel.impl.util.dbstructure.DbStructureTool
//   org.neo4j.kernel.impl.util.dbstructure.DbStructureLargeOptionalMatchStructure [<output source root>] <db-dir>
//
// (using org.neo4j.kernel.impl.util.dbstructure.InvocationTracer)
//

public enum DbStructureLargeOptionalMatchStructure
implements Visitable<DbStructureVisitor>
{
    INSTANCE;

    @Override
    public void accept( DbStructureVisitor visitor )
    {
        visitor.visitLabel( 2, "Label20" );
        visitor.visitLabel( 3, "Label4" );
        visitor.visitLabel( 4, "Label22" );
        visitor.visitLabel( 5, "Label6" );
        visitor.visitLabel( 7, "Label1" );
        visitor.visitLabel( 8, "Label12" );
        visitor.visitLabel( 9, "Label14" );
        visitor.visitLabel( 10, "Label26" );
        visitor.visitLabel( 11, "Label10" );
        visitor.visitLabel( 12, "Label24" );
        visitor.visitLabel( 13, "Label8" );
        visitor.visitLabel( 14, "Label18" );
        visitor.visitLabel( 15, "Label19" );
        visitor.visitLabel( 16, "Label3" );
        visitor.visitLabel( 17, "Label16" );
        visitor.visitLabel( 18, "Label15" );
        visitor.visitLabel( 19, "Label21" );
        visitor.visitLabel( 20, "Label5" );
        visitor.visitLabel( 22, "Label2" );
        visitor.visitLabel( 23, "Label11" );
        visitor.visitLabel( 24, "Label13" );
        visitor.visitLabel( 25, "Label17" );
        visitor.visitLabel( 26, "Label25" );
        visitor.visitLabel( 27, "Label9" );
        visitor.visitLabel( 28, "Label23" );
        visitor.visitLabel( 29, "Label7" );
        visitor.visitLabel( 31, "Label27" );
        visitor.visitPropertyKey( 0, "id" );
        visitor.visitPropertyKey( 27, "deleted" );
        visitor.visitRelationshipType( 1, "REL1" );
        visitor.visitRelationshipType( 2, "REL4" );
        visitor.visitRelationshipType( 4, "REL2" );
        visitor.visitRelationshipType( 5, "REL5" );
        visitor.visitRelationshipType( 6, "REL8" );
        visitor.visitRelationshipType( 9, "REL6" );
        visitor.visitIndex( TestIndexDescriptorFactory.forLabel( 22, 0 ), ":Label2(id)", 0.3641877706337751d, 304838L );
        visitor.visitAllNodesCount( 2668827L );
        visitor.visitNodeCount( 2, "Label20", 3L );
        visitor.visitNodeCount( 3, "Label4", 0L );
        visitor.visitNodeCount( 4, "Label22", 0L );
        visitor.visitNodeCount( 5, "Label6", 0L );
        visitor.visitNodeCount( 7, "Label1", 111110L );
        visitor.visitNodeCount( 8, "Label12", 111112L );
        visitor.visitNodeCount( 9, "Label14", 99917L );
        visitor.visitNodeCount( 10, "Label26", 3L );
        visitor.visitNodeCount( 11, "Label10", 111150L );
        visitor.visitNodeCount( 12, "Label24", 0L );
        visitor.visitNodeCount( 13, "Label8", 0L );
        visitor.visitNodeCount( 14, "Label18", 111112L );
        visitor.visitNodeCount( 15, "Label19", 3L );
        visitor.visitNodeCount( 16, "Label3", 0L );
        visitor.visitNodeCount( 17, "Label16", 0L );
        visitor.visitNodeCount( 18, "Label15", 0L );
        visitor.visitNodeCount( 19, "Label21", 0L );
        visitor.visitNodeCount( 20, "Label5", 0L );
        visitor.visitNodeCount( 22, "Label2", 310059L );
        visitor.visitNodeCount( 23, "Label11", 179015L );
        visitor.visitNodeCount( 24, "Label13", 99917L );
        visitor.visitNodeCount( 25, "Label17", 179021L );
        visitor.visitNodeCount( 26, "Label25", 3L );
        visitor.visitNodeCount( 27, "Label9", 111150L );
        visitor.visitNodeCount( 28, "Label23", 0L );
        visitor.visitNodeCount( 29, "Label7", 0L );
        visitor.visitNodeCount( 31, "Label27", 1567352L );
        visitor.visitRelCount( -1, -1, -1, "MATCH ()-[]->() RETURN count(*)", 4944492L );

   }
}

/* END OF GENERATED CONTENT */
