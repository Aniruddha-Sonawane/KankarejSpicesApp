package com.kankarej.kankarejspices.util

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun getOptimizedUrl(originalUrl: String, width: Int = 600): String {
    if (originalUrl.contains("drive.google.com")) {
        // Strip https:// to match wsrv.nl requirement
        val cleanUrl = originalUrl.replace("https://", "")
        val encodedUrl = URLEncoder.encode(cleanUrl, StandardCharsets.UTF_8.toString())
        
        // Return the proxy URL asking for WebP format at specific width
        return "https://wsrv.nl/?url=$encodedUrl&w=$width&output=webp&q=80"
    }
    return originalUrl
}