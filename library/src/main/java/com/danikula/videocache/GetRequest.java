package com.danikula.videocache;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.danikula.videocache.Preconditions.checkNotNull;

/**
 * Model for Http GET request.
 *
 * @author Alexey Danilov (danikula@gmail.com).
 */
class GetRequest {

    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-");
    private static final Pattern URL_PATTERN = Pattern.compile("GET /(.*) HTTP");
    private static final Pattern RANGE_HEADER_ENDOFFSET = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-(\\d*)");

    public final String uri;
    public final long rangeOffset;
    public final boolean partial;
    public final long rangeEndOffset;

    public GetRequest(String request) {
        checkNotNull(request);
        Log.i("wangshuo","request = "+request);
        long offset = findRangeOffset(request);
        this.rangeOffset = Math.max(0, offset);
        this.partial = offset >= 0;
        this.uri = findUri(request);
        this.rangeEndOffset = findRangeEndOffset(request);
    }

    public static GetRequest read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder stringRequest = new StringBuilder();
        String line;
        while (!TextUtils.isEmpty(line = reader.readLine())) { // until new line (headers ending)
            stringRequest.append(line).append('\n');
        }
        return new GetRequest(stringRequest.toString());
    }

    private long findRangeOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            if(TextUtils.isEmpty(rangeValue)){
                return 0;
            }
            return Long.parseLong(rangeValue);
        }
        return -1;
    }

    private long findRangeEndOffset(String request) {
        Matcher matcher = RANGE_HEADER_ENDOFFSET.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(2);
            if(TextUtils.isEmpty(rangeValue)){
              return -1;
            }
            return Long.parseLong(rangeValue);
        }
        return -1;
    }

    private String findUri(String request) {
        Matcher matcher = URL_PATTERN.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid request `" + request + "`: url not found!");
    }

    @Override
    public String toString() {
        return "GetRequest{" +
                "rangeOffset=" + rangeOffset +
                ", partial=" + partial +
                ", uri='" + uri + '\'' +
                '}';
    }
}
