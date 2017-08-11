// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Changes
// -------
// 170217: Changed from URL to Uri.
// 161115: Changed return type from Strings to actual URL objects, and filtering out malformed URLs.
// 161114: Removed implementation of interface PlaylistParser to make method getUrls static.

package org.npr.android.util;


import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PlsParser {
    private final static String TAG = PlsParser.class.getName();

    public static List<Uri> getUris(File file) throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader(file), 1024);
        LinkedList<Uri> uris = new LinkedList<>();
        while (true) {
            try {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                String url = parseLine(line);
                if (url != null && !url.equals("")) {
                    uris.add(Uri.parse(url));                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uris;
    }

    private static String parseLine(String line) {
        if (line == null) {
            return null;
        }
        String trimmed = line.trim();
        if (trimmed.contains("http")) {
            return trimmed.substring(trimmed.indexOf("http"));
        }
        return "";
    }
}