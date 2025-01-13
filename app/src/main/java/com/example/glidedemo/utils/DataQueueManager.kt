package com.example.glidedemo.utils


import com.example.glidedemo.bean.CleanUpDetailData
import com.example.glidedemo.bean.ScanMediaData
import com.example.glidedemo.bean.SimilarImagesData
import com.example.glidedemo.extensions.getFilenameFromPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object DataQueueManager {
    //默认相似度
    private var SIMILARITY = 90
    private val cleanupFunctionUtils = CleanupFunctionUtils()

    //未分组list
    private val notSimilarList: MutableList<CleanUpDetailData> = mutableListOf()

    //首页展示的五张图片
    private val itemShowList: ArrayList<ScanMediaData> = arrayListOf()

    //所有
    private val itemAllList: ArrayList<SimilarImagesData> = arrayListOf()

    //所有
    private val allCleanUpDetailDataList: ArrayList<CleanUpDetailData> = arrayListOf()

    //已分组map,查找分组
    private val mediumGroups = ConcurrentHashMap<String, CopyOnWriteArrayList<CleanUpDetailData>>()

    //扫描状态
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Loading(0))
    val scanState = _scanState.asStateFlow()

    /**
     * 大小
     */
    private var similarDataSize = 0L

    /**
     * 数量
     */
    private var similarSizeCount = 0

    /**
     * 临时保存数据
     * 操作完成批量检查数据
     */
    suspend fun addSimilarSource(data: CleanUpDetailData?) = withContext(Dispatchers.IO) {
        if (data == null) {
            checkSimilarBatch(allCleanUpDetailDataList)
            _scanState.emit(ScanState.Complete(similarSizeCount, similarDataSize, itemShowList))
            return@withContext
        }

        val state = scanState.value
        if (state !is ScanState.Loading) return@withContext

        // 更新当前扫描进度

        val currentCount = state.count + 1
        _scanState.emit(ScanState.Loading(currentCount))
        val hash = data.extra!!["hash"] as String
        itemAllList.add(
            SimilarImagesData(
                data.id,
                data.path.getFilenameFromPath(),
                data.path,
                data.stamp,
                0L,
                hash,
                data.size,
                null
            )
        )
    }


    private suspend fun checkSimilarBatch(dataList: List<CleanUpDetailData>) {
        if (dataList.isEmpty()) {
            return
        }
        val newGroups = mutableListOf<SimilarImagesData>()

        // 对每个数据进行分组匹配
        for (data in dataList) {
            val matchedGroup = mediumGroups.values.find { group ->
                cleanupFunctionUtils.checkSimilar(data, group[0], SIMILARITY)
            }

            if (matchedGroup != null) {
                // 加入匹配的分组
                updateGroupData(matchedGroup, data)
            } else {
                // 检查未分组列表
                val matchedNotSimilar = notSimilarList.find {
                    cleanupFunctionUtils.checkSimilar(data, it, SIMILARITY)
                }

                if (matchedNotSimilar != null) {
                    // 创建新分组
                    createNewGroup(matchedNotSimilar, data)
                } else {
                    // 添加到未分组列表
                    notSimilarList.add(data)
                }
            }
        }
        // 一次性保存所有更新的分组
        similarImagesDB.insertAll(newGroups)

        _scanState.emit(ScanState.Complete(similarSizeCount, similarDataSize, itemShowList))
    }


    // 更新分组数据
    private fun updateGroupData(
        group: CopyOnWriteArrayList<CleanUpDetailData>, data: CleanUpDetailData
    ) {
        group.add(data)
        similarSizeCount++
        similarDataSize += data.size

        // 添加到展示列表
        if (itemShowList.size < 5) {
            val item = ScanMediaData(
                data.name, data.path, data.size, data.stamp, MediaTypeEnum.TYPE_IMAGES
            )
            itemShowList.add(item)
        }
    }

    // 创建新分组
    private fun createNewGroup(data1: CleanUpDetailData, data2: CleanUpDetailData) {
        val key = createNewKey()
        val newGroup = CopyOnWriteArrayList<CleanUpDetailData>().apply {
            add(data1)
            add(data2)
        }
        mediumGroups[key] = newGroup

        similarSizeCount += 2
        similarDataSize += data1.size + data2.size

        // 更新展示列表
        val item1 = ScanMediaData(
            data1.name, data1.path, data1.size, data1.stamp, MediaTypeEnum.TYPE_IMAGES
        )
        val item2 = ScanMediaData(
            data2.name, data2.path, data2.size, data2.stamp, MediaTypeEnum.TYPE_IMAGES
        )
        if (itemShowList.size < 4) {
            itemShowList.add(item1)
            itemShowList.add(item2)
        } else if (itemShowList.size < 5) {
            itemShowList.add(item1)
        }
    }


    private var i = 0

    private fun createNewKey(): String {
        i++
        return "Key_$i"
    }

    fun clear() {
        notSimilarList.clear()
        itemAllList.clear()
        mediumGroups.clear()
        itemShowList.clear()
        _scanState.value = ScanState.Loading(0)
        clearCount()
    }

    fun clearCount() {
        similarSizeCount = 0
        similarDataSize = 0L
    }

    /**
     * 修改大小和数量
     */
    suspend fun updateSimilarData(
        typeStr: String, data: CleanUpDetailData?, similarPhotoDataList: ArrayList<ScanMediaData>
    ) {
        var newSize = 0L
        var newCount = 0
        val type = when (typeStr) {
            CleanTypeEnum.TYPE_SCREENSHOT.name -> CleanTypeEnum.TYPE_SCREENSHOT
            CleanTypeEnum.TYPE_SIMILAR.name -> CleanTypeEnum.TYPE_SIMILAR
            else -> return
        }
        if (data != null) {
            newSize = similarDataSize - data.size
            similarSizeCount -= 1
            newCount = similarSizeCount
        } else {
            newSize = similarDataSize
            newCount = similarSizeCount
        }
        _scanState.emit(ScanState.Delete(type, newCount, newSize, similarPhotoDataList))

    }

    fun setSimilarDataSize(size: Long, count: Int) {
        similarDataSize = size
        similarSizeCount = count
    }

}


abstract class ScanState {
    /**
     * 扫描中 返回当前扫描的数量
     */

    class Loading(val count: Int) : ScanState()

    /**
     * 扫描完成 返回相似图片数量 和总大小
     */
    class Complete(val similarCount: Int, val size: Long, val list: ArrayList<ScanMediaData>) :
        ScanState()

    /**
     * 删除数据
     * type:类型
     * count:总数量
     * size :总数量
     * list:展示的缩略图
     */
    class Delete(
        val type: CleanTypeEnum, val count: Int, val size: Long, val list: ArrayList<ScanMediaData>
    ) : ScanState()
}
