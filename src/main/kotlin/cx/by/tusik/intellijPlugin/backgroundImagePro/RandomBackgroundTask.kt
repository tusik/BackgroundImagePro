package cx.by.tusik.intellijPlugin.backgroundImagePro

import cx.by.tusik.intellijPlugin.backgroundImagePro.ui.Settings
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.wm.impl.IdeBackgroundUtil
import java.awt.EventQueue
import java.io.File

class RandomBackgroundTask : Runnable {

    private val imagesHandler: ImagesHandler = ImagesHandler()
    fun clearCache(){
        val prop = PropertiesComponent.getInstance()
        val cacheFolder = prop.getValue(Settings.CACHE_FOLDER, "")
        val file = File(cacheFolder)
        if (!file.exists()) {
            NotificationCenter.notice("Cache folder not set")
            return
        }
        // 删除缓存文件夹下的所有图片
        imagesHandler.clearCache(cacheFolder)


    }
    override fun run() {
        val prop = PropertiesComponent.getInstance()

        val lastClearCacheTime = prop.getValue(Settings.CACHE_CLEAR_DATE, "0").toLong()
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClearCacheTime > 24 * 60 * 60 * 1000) {
            clearCache()
            prop.setValue(Settings.CACHE_CLEAR_DATE, currentTime.toString())
        }

        val folder = prop.getValue(Settings.FOLDER, "")
        val imageSource = prop.getValue(Settings.IMAGE_SOURCE, "internet")
        if (folder.isEmpty()) {
            NotificationCenter.notice("Image folder not set")
            return
        }
        var image: String? = null
        image = if (imageSource == "internet") {
            imagesHandler.getImageFromNet(
                prop.getValue(Settings.IMAGE_URL, ""),
                prop.getValue(Settings.CACHE_FOLDER, "")
            )
        } else {
            val file = File(folder)
            if (!file.exists()) {
                NotificationCenter.notice("Image folder not set")
                return
            }
            imagesHandler.getRandomImage(folder)
        }
        if (image == null) {
            NotificationCenter.notice("No image found")
            return
        }
        if (image.contains(",")) {
            NotificationCenter.notice("Intellij wont load images with ',' character\n$image")
        }

        val o = prop.getInt(Settings.OPACITY, 15)
        val opacity = if (o < 0 || o > 100) {
            NotificationCenter.notice("opacity must be between [0,100],Your value is not within this range,The value is set to a default value of 15")
            15
        } else o

        //默认透明度设为25
        prop.setValue(IdeBackgroundUtil.FRAME_PROP, "$image,$opacity")
        prop.setValue(IdeBackgroundUtil.EDITOR_PROP, "$image,$opacity")
        // 添加相关事件
        EventQueue.invokeLater {
            IdeBackgroundUtil.repaintAllWindows()
        }
    }

    companion object {
        val instance: RandomBackgroundTask by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { RandomBackgroundTask() }
    }
}
