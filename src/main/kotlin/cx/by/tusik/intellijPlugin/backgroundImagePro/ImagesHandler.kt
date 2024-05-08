package cx.by.tusik.intellijPlugin.backgroundImagePro

import java.io.File
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
            val url = URL(imageUrl)
            val bytes = url.readBytes()
            val timestamp = Instant.now().toEpochMilli()
            val realPath = Paths.get(targetPath, "$timestamp.jpg")
            Files.write(realPath, bytes)
            // 打印下载的图片路径
            println("Downloaded image to: $realPath")
            return realPath.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
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