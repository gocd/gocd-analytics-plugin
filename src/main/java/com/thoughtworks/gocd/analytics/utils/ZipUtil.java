/*
 * Copyright 2020 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.gocd.analytics.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static final int READ_BUFFER_SIZE = 2156;
    private static final FileTime EPOCH = FileTime.from(Instant.EPOCH);

    private ZipUtil() {
    }

    static void zipContents(String dir, ZipOutputStream zos, String relativeTo) throws IOException {
        File sourceDir = new File(dir);
        Path from = Paths.get(relativeTo);

        byte[] readBuffer = new byte[READ_BUFFER_SIZE];
        int bytesIn = 0;

        for (String entry : Objects.requireNonNull(sourceDir.list())) {
            File f = new File(sourceDir, entry);

            if (f.isDirectory()) {
                zipContents(f.getPath(), zos, relativeTo);
                continue;
            }

            try (FileInputStream fis = new FileInputStream(f)) {
                String relPath = from.relativize(Paths.get(f.getPath())).toString();
                zos.putNextEntry(new ZipEntry(relPath).
                        setCreationTime(EPOCH).
                        setLastModifiedTime(EPOCH).
                        setLastAccessTime(EPOCH));

                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
            }
        }
    }

    /**
     * Recursively zips a path
     *
     * @param dir the source path to zip
     * @param os  the destination {@link OutputStream}
     * @throws IOException on error
     */
    public static void zip_R(String dir, OutputStream os) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(os)) {
            zipContents(dir, zos, dir);
        }
    }
}
