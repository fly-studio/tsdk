package org.fly.tsdk.sdk.utils;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.fly.core.text.json.Jsonable;
import org.fly.tsdk.exceptions.SignatureNotFoundException;
import org.fly.tsdk.io.ApkParser;
import org.fly.tsdk.io.Logger;
import org.fly.tsdk.io.StorageHelper;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

public class ChannelHelper {
    private static final String TAG = "ChannelHelper";
    private static final String KEY = "sub_channel";
    private static final String DEFAULT_CHARSET = "UTF-8";
    // Our Channel Block ID
    public static final int APK_CHANNEL_BLOCK_ID = 0x71777777;

    public static String getSubChannel(Context context) {
        String apkPath = StorageHelper.getApkPath(context);

        if (null == apkPath)
            return null;


        try (final ApkParser apkParser = new ApkParser(new File(apkPath))) {
            apkParser.open();
            //Comment first
            String comment = byteBufferToString(apkParser.getComment());

            if (expectSubChannel(comment)) {
                return readSubChannelJson(comment);
            }

            // sign v2 block second
            Map<Integer, ByteBuffer> signBlockMap = apkParser.findSigningBlockIdValues();

            if (signBlockMap != null && signBlockMap.get(APK_CHANNEL_BLOCK_ID) != null)
            {
                String signBlock = byteBufferToString(signBlockMap.get(APK_CHANNEL_BLOCK_ID));

                if (expectSubChannel(signBlock)) {
                    return readSubChannelJson(signBlock);
                }
            }

            // file in META-INF/sub_channel.json
            String metaFileContent = byteToString(apkParser.getFileContent("META-INF/" + KEY + ".json"));
            if (expectSubChannel(metaFileContent)) {
                return readSubChannelJson(metaFileContent);
            }

        } catch (IOException | SignatureNotFoundException e) {
            Logger.e(TAG, "Cannot read channel from apk file.", e);
            return null;
        }

        return null;

    }

    private static String readSubChannelJson(String json){
        if (StringUtils.isEmpty(json))
            return null;

        Map<String, String> jsonMap = Jsonable.Builder.jsonToMap(json);

        return jsonMap.get(KEY);
    }

    private static boolean expectSubChannel(String result) {
        return !StringUtils.isEmpty(result) && result.startsWith("{") && result.endsWith("}") && result.contains(KEY);
    }

    private static String byteToString(final byte[] bytes)
    {
        if (bytes == null) {
            return null;
        }

        try {
            return new String(bytes, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            Logger.e(TAG, e.getMessage(), e);
        }

        return null;
    }

    private static String byteBufferToString(final ByteBuffer byteBuffer) {
        byte[] bytes = byteBufferToBytes(byteBuffer);
        return byteToString(bytes);
    }

    /**
     * get data from byteBuffer
     *
     * @param byteBuffer buffer
     * @return useful data
     */
    private static byte[] byteBufferToBytes(final ByteBuffer byteBuffer) {

        final byte[] array = byteBuffer.array();
        final int arrayOffset = byteBuffer.arrayOffset();
        return Arrays.copyOfRange(array, arrayOffset + byteBuffer.position(),
                arrayOffset + byteBuffer.limit());
    }
}
