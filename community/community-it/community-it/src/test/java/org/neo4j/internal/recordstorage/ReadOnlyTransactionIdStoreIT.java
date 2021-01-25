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
package org.neo4j.internal.recordstorage;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.tracing.DefaultPageCacheTracer;
import org.neo4j.test.extension.DbmsExtension;
import org.neo4j.test.extension.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@DbmsExtension
class ReadOnlyTransactionIdStoreIT
{
    @Inject
    private PageCache pageCache;
    @Inject
    private DatabaseLayout databaseLayout;
    @Inject
    private FileSystemAbstraction fs;

    @Test
    void testPageCacheAccessOnTransactionIdStoreConstruction() throws IOException
    {
        var pageCacheTracer = new DefaultPageCacheTracer();
        var cursorTracer = pageCacheTracer.createPageCursorTracer( "testPageCacheAccessOnTransactionIdStoreConstruction" );
        new ReadOnlyTransactionIdStore( fs, pageCache, databaseLayout, cursorTracer );

        assertThat( cursorTracer.pins() ).isEqualTo( 4 );
        assertThat( cursorTracer.unpins() ).isEqualTo( 4 );
        assertThat( cursorTracer.hits() ).isEqualTo( 4 );
    }
}
