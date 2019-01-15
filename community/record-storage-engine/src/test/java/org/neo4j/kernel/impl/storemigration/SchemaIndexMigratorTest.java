/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.kernel.impl.storemigration;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import org.neo4j.common.ProgressReporter;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.kernel.api.index.IndexDirectoryStructure;
import org.neo4j.kernel.api.index.IndexProvider;
import org.neo4j.kernel.api.index.IndexProviderDescriptor;
import org.neo4j.kernel.impl.store.format.standard.StandardV3_4;
import org.neo4j.kernel.impl.store.format.standard.StandardV4_0;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.neo4j.graphdb.factory.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

class SchemaIndexMigratorTest
{
    private final FileSystemAbstraction fs = mock( FileSystemAbstraction.class );
    private final ProgressReporter progressReporter = mock( ProgressReporter.class );
    private final IndexProvider indexProvider = mock( IndexProvider.class );
    private final DatabaseLayout databaseLayout = DatabaseLayout.of( new File( "store" ), DEFAULT_DATABASE_NAME );
    private final DatabaseLayout migrationLayout = DatabaseLayout.of( new File( "migrationDir" ), DEFAULT_DATABASE_NAME );

    private final SchemaIndexMigrator migrator = new SchemaIndexMigrator( fs, indexProvider );

    @Test
    void schemaAndLabelIndexesRemovedAfterSuccessfulMigration() throws IOException
    {
        IndexDirectoryStructure directoryStructure = mock( IndexDirectoryStructure.class );
        File indexProviderRootDirectory = databaseLayout.file( "just-some-directory" );
        when( directoryStructure.rootDirectory() ).thenReturn( indexProviderRootDirectory );
        when( indexProvider.directoryStructure() ).thenReturn( directoryStructure );
        when( indexProvider.getProviderDescriptor() )
                .thenReturn( new IndexProviderDescriptor( "key", "version" ) );

        migrator.migrate( databaseLayout, migrationLayout, progressReporter, StandardV3_4.STORE_VERSION,
                StandardV4_0.STORE_VERSION );

        migrator.moveMigratedFiles( migrationLayout, databaseLayout, StandardV3_4.STORE_VERSION, StandardV4_0.STORE_VERSION );

        verify( fs ).deleteRecursively( indexProviderRootDirectory );
    }
}
