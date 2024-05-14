package cx.by.tusik.intellijPlugin.backgroundImagePro

import java.io.File
import java.net.HttpURLConnection
import java.util.*
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

/**
 * Author: Allan de Queiroz
 * Date:   07/05/17
 */
internal class ImagesHandler {
    fun downloadImage(imageUrl: String, targetPath: String): String? {
        try {
            val initialUrl = URL(imageUrl)
            val connection = initialUrl.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connect()

            var urlToDownload: URL = initialUrl
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                val redirectedUrl = connection.getHeaderField("Location")
                urlToDownload = URL(redirectedUrl)
            }

            val bytes = urlToDownload.readBytes()
            val fileName = urlToDownload.path.split("/").last()
            val timestamp = Instant.now().toEpochMilli()
            val realPath = Paths.get(targetPath, fileName)
            Files.write(realPath, bytes)
            println("Downloaded image to: $realPath")
            return realPath.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    fun clearCache(cacheFolder: String){
        val dir = Paths.get(cacheFolder)
        if (!Files.exists(dir)) {
            return
        }
        val list = dir.toFile().listFiles() ?: return
        for (f in list) {
            if (f.isDirectory) {
                clearCache(f.absolutePath)
            } else {
                if(f.extension == "jpg" || f.extension == "png" || f.extension == "jpeg"){
                    f.delete()
                }

            }
        }
    }
    /**
     * @param folder folder to search for images
     * @return random image or null
     */
    fun getRandomImage(folder: String): String? {
        if (folder.isEmpty()) {
            return null
        }
        val images: MutableList<String> = ArrayList()
        collectImages(images, folder)
        val count = images.size
        if (count == 0) {
            return null
        }
        val randomGenerator = Random()
        val index = randomGenerator.nextInt(images.size)
        return images[index]
    }

    fun getImageFromNet(url: String, targetDir: String): String? {
        val dir = Paths.get(targetDir)
        if (!Files.exists(dir)) {
            Files.createDirectories(dir)
        }

        return downloadImage(url, targetDir.toString())
    }

    private fun collectImages(images: MutableList<String>, folder: String) {
        val root = File(folder)
        if (!root.exists()) {
            return
        }
        val list = root.listFiles() ?: return
        for (f in list) {
            if (f.isDirectory) {
                collectImages(images, f.absolutePath)
            } else {
                if (!isImage(f)) {
                    continue
                }
                images.add(f.absolutePath)
            }
        }
    }

    private fun isImage(file: File): Boolean {
        val s = Files
            .probeContentType(file.toPath()) ?: ""
        val parts = s.split("/".toRegex()).toTypedArray()
        return parts.isNotEmpty() && "image" == parts[0]
    }

}